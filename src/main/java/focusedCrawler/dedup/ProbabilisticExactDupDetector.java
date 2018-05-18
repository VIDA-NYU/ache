package focusedCrawler.dedup;

import org.apache.commons.codec.digest.DigestUtils;

import com.github.jparkie.pdd.ProbabilisticDeDuplicator;
import com.github.jparkie.pdd.impl.RLBSBFDeDuplicator;
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Multimap;
//import com.google.common.io.BaseEncoding;

public class ProbabilisticExactDupDetector implements DupDetector {

    private static final long NUM_BITS = 128 * 8L * 1024L * 1024L;
    private final ProbabilisticDeDuplicator deduper = RLBSBFDeDuplicator.create(NUM_BITS, 0.001D);
    
//    private Multimap<String, String> hashToUrl = ArrayListMultimap.create();

    @Override
    public boolean detectAndIndex(String url, String content) {
        return detectAndIndex(url, content.getBytes());
    }
    
    @Override
    public boolean detectAndIndex(String url, byte[] contentBytes) {
        byte[] fingerprint  = DigestUtils.md5(contentBytes);
        boolean isDistinct = deduper.classifyDistinct(fingerprint); // classifies and updates seen fingerprints
//        if(!isDistinct) {
//            String md5hex = BaseEncoding.base16().lowerCase().encode(bytes);
//            hashToUrl.put(md5hex, url);
//        }
        return !isDistinct;
    }

    @Override
    public DupData getDuplicationSample() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // No-op
    }

}
