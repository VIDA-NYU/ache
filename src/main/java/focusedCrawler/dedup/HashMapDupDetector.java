package focusedCrawler.dedup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;

import focusedCrawler.util.Sampler;

public class HashMapDupDetector implements DupDetector {

    private final int maxSamplesPerHash = 100;
    private Map<String, Sampler<String>> urlsByHash = new HashMap<>();

    @Override
    public boolean detectAndIndex(String url, String content) {
        return detectAndIndex(url, content.getBytes());
    }

    @Override
    public boolean detectAndIndex(String url, byte[] contentBytes) {
        String fingerprint = DigestUtils.md5Hex(contentBytes);
        Sampler<String> list = urlsByHash.get(fingerprint);
        if (list == null) {
            list = new Sampler<>(maxSamplesPerHash);
            urlsByHash.put(fingerprint, list);
        }
        list.sample(url);

        return list.getSamples().size() > 1;
    }

    @Override
    public DupData getDuplicationSample() {
        List<List<String>> dupClusters = new ArrayList<>();
        List<String> uniqueSample = new ArrayList<>();
        for (Entry<String, Sampler<String>> entry : urlsByHash.entrySet()) {
            Sampler<String> sample = entry.getValue();
            if (sample.reservoirSize() > 1) {
                List<String> duplicates = new ArrayList<>();
                for (String url : sample.getSamples()) {
                    duplicates.add(url);
                }
                dupClusters.add(duplicates);
            } else {
                uniqueSample.add(sample.getSamples().get(0));
            }
        }
        return new DupData(dupClusters, uniqueSample);
    }

    @Override
    public void close() {
        // No-op
    }

}
