package focusedCrawler.minhash;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.Tokenizers;
import focusedCrawler.util.Tokenizers.ShingleTokenizer;

/**
 * Builds a "index" of near-duplicate pages using locality-sensitive hashing.
 * 
 * @author aeciosantos
 *
 */
public class DuplicatePageIndexer {
    
    public static Logger logger = LoggerFactory.getLogger(DuplicatePageIndexer.class);
    
    private int numHashes = 256;
    private double jaccardThreshold = 0.9d;
    private int numberOfShingles = 9;
    
    private ShingleTokenizer shinglesTokenizer = Tokenizers.shingles(numberOfShingles);
    private MinHasher hasher = new MinHasher(numHashes);
    private LSH lsh;
    
    public DuplicatePageIndexer() {
        this.lsh = new LSH(numHashes, jaccardThreshold);
    }
    
    public DuplicatePageIndexer(String dataPath) {
        this.lsh = new LSH(numHashes, jaccardThreshold, dataPath);
    }
    
    public void insert(String id, String text) throws Exception {
        int[] signatures = computeMinHashSignatures(text);
        lsh.insert(id, signatures);
    }

    public boolean isNearDuplicate(String text) {
        Set<String> dupes = findNearDuplicates(text);
        return !dupes.isEmpty();
    }
    
    public Set<String> findNearDuplicates(String text) {
        int[] signatures = computeMinHashSignatures(text);
        Set<String> dupCandidates = lsh.query(signatures);
        return dupCandidates;
    }
    
    private int[] computeMinHashSignatures(String cleanText) {
        try {
            Set<Integer> hashedShingles = shinglesTokenizer.hashedTokenSet(cleanText);
            return hasher.minHashSignature(hashedShingles);
        } catch (Exception e) {
            logger.warn("Failed to parse clean text into shingles.", e);
            return new int[0];
        }
    }
    
    public static void main(String[] args) throws Exception {
        DuplicatePageIndexer dpi = new DuplicatePageIndexer();
        String content = new String(Files.readAllBytes(Paths.get("/tmp/ache.html")));
        String content2 = new String(Files.readAllBytes(Paths.get("/tmp/ache-dup.html")));
        dpi.insert("1", content);
        System.out.println(dpi.isNearDuplicate(content));
        System.out.println(dpi.isNearDuplicate(content2));
    }

}
