package focusedCrawler.minhash;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.LogFile;
import focusedCrawler.util.Tokenizers;
import focusedCrawler.util.Tokenizers.ShingleTokenizer;
import focusedCrawler.util.persistence.rocksdb.IntStringHashtable;
import focusedCrawler.util.persistence.rocksdb.StringIntHashtable;

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
    private StringIntHashtable urlToId;
    private IntStringHashtable idToUrl;
    private LogFile neardupLog;

    public DuplicatePageIndexer() {
        this.lsh = new LSH(numHashes, jaccardThreshold);
        this.urlToId = new StringIntHashtable() {

            Map<String, Integer> map = new HashMap<>();

            public int get(String key) {
                Integer v = map.get(key);
                return v == null ? absentValue : v;
            }

            public void put(String key, int value) {
                map.put(key, value);
            }

        };
        this.idToUrl = new IntStringHashtable() {

            Map<Integer, String> map = new HashMap<>();

            @Override
            public String get(int key) {
                return map.get(key);
            }

            @Override
            public void put(int key, String value) {
                map.put(key, value);
            }
        };
    }

    public DuplicatePageIndexer(String dataPath) {
        this(dataPath, 0.9d);
    }

    public DuplicatePageIndexer(String dataPath, double jaccardThreshold) {
        Path base = Paths.get(dataPath);
        this.jaccardThreshold = jaccardThreshold;
        this.lsh = new LSH(numHashes, jaccardThreshold,
            base.resolve("near_duplicates").toString());
        this.neardupLog = new LogFile(base.resolve("data_monitor/nearduplicates.csv"));
        this.urlToId = new StringIntHashtable(base.resolve("keys_db").toString());
        this.idToUrl = new IntStringHashtable(base.resolve("ids_db").toString());
    }

    public void insert(String key, String text) {
        int[] signatures = computeMinHashSignatures(text);
        int keyId = getId(key);
        lsh.insert(keyId, signatures);
    }

    private synchronized int getId(String key) {
        int id = urlToId.get(key);
        if (id == urlToId.absentValue) {
            int maxId = urlToId.get("MAX");
            if (maxId == urlToId.absentValue) {
                id = 0;
            } else {
                id = maxId + 1;
            }
            urlToId.put("MAX", id);
            urlToId.put(key, id);
            idToUrl.put(id, key);
        }
        return id;
    }

    public boolean isNearDuplicate(String text) {
        int[] signatures = computeMinHashSignatures(text);
        return lsh.isDuplicate(signatures);
    }

    public Set<String> findNearDuplicates(String text) {
        int[] signatures = computeMinHashSignatures(text);
        Set<Integer> duplicateIds = lsh.query(signatures);
        Set<String> keys = new HashSet<String>();
        for (int id : duplicateIds) {
            keys.add(getUrlBy(id));
        }
        return keys;
    }

    private String getUrlBy(int id) {
        return idToUrl.get(id);
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

    public boolean detectAndIndex(String text, String url) {
        Set<String> duplicates = findNearDuplicates(text);
        boolean isNearDuplicate = !duplicates.isEmpty();
        insert(url, text);
        if (isNearDuplicate) {
            neardupLog.printf("%s", url);
            for (String duplicate : duplicates) {
                neardupLog.printf(" %s", duplicate);
            }
            neardupLog.printf("\n");
        }
        return isNearDuplicate;
    }

}
