package achecrawler.dedup;

import com.github.jparkie.pdd.ProbabilisticDeDuplicator;
import com.github.jparkie.pdd.impl.RLBSBFDeDuplicator;
import com.google.common.io.BaseEncoding;
import achecrawler.util.KV;
import achecrawler.util.Sampler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

public class ProbabilisticExactDupDetector implements DupDetector {

    private static final long NUM_BITS = 128 * 8L * 1024L * 1024L;
    private final ProbabilisticDeDuplicator deduper = RLBSBFDeDuplicator.create(NUM_BITS, 0.001D);

    private int numSamples = 1000;
    private Sampler<KV<String, String>> unique = new Sampler<>(numSamples);
    private Sampler<KV<String, String>> duplicates = new Sampler<>(numSamples);

    @Override
    public boolean detectAndIndex(String url, String content) {
        return detectAndIndex(url, content.getBytes());
    }

    @Override
    public boolean detectAndIndex(String url, byte[] contentBytes) {
        byte[] fingerprint = DigestUtils.md5(contentBytes);

        boolean isDistinct = deduper
                .classifyDistinct(fingerprint); // this classifies and updates seen fingerprints

        if (isDistinct) {
            unique.sample(new KV<>(url, toHex(fingerprint)));
        } else {
            duplicates.sample(new KV<>(url, toHex(fingerprint)));
        }
        return !isDistinct;
    }

    @Override
    public DupData getDuplicationSample() {

        // Generate duplicate cluster by groups all duplicate URLs by its fingerprint
        List<List<String>> dupClusters = new ArrayList<>();
        Map<String, List<String>> duplicatesByHash = new HashMap<>();
        for (KV<String, String> kv : duplicates.getSamples()) {
            String fingerprint = kv.getValue();
            String url = kv.getKey();
            List<String> urls = duplicatesByHash.get(fingerprint);
            if (urls == null) {
                urls = new ArrayList<>();
                dupClusters.add(urls);
            }
            urls.add(url);
            duplicatesByHash.put(fingerprint, urls);
        }

        // Create list of unique pages (excludes any page that has a duplicate)
        List<String> uniques = new ArrayList<>();
        for (KV<String, String> kv : this.unique.getSamples()) {
            String fingerprint = kv.getValue();
            String url = kv.getKey();
            if (!duplicatesByHash.containsKey(fingerprint)) {
                uniques.add(url);
            }
        }

        return new DupData(dupClusters, uniques);
    }

    private String toHex(byte[] fingerprint) {
        return BaseEncoding.base16().encode(fingerprint);
    }

    @Override
    public void close() {
        // No-op
    }

}
