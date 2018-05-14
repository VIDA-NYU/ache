package focusedCrawler.minhash;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.dedup.DupDetector;
import focusedCrawler.tokenizers.ShingleTokenizer;
import focusedCrawler.tokenizers.Tokenizers;
import focusedCrawler.util.KV;
import focusedCrawler.util.LogFile;
import focusedCrawler.util.Sampler;
import focusedCrawler.util.UnionFind;
import focusedCrawler.util.persistence.rocksdb.IntStringHashtable;
import focusedCrawler.util.persistence.rocksdb.StringIntHashtable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Builds a "index" of near-duplicate pages using locality-sensitive hashing.
 *
 * @author aeciosantos
 */
public class DuplicatePageIndexer implements DupDetector {

    public static Logger logger = LoggerFactory.getLogger(DuplicatePageIndexer.class);

    private static final double DEFAULT_JACCARD_THRESHOLD = 0.95;
    private static final int DEFAULT_NUM_HASHES = 256;
    private static final int DEFAULT_NUM_SHINGLES = 9;

    private ShingleTokenizer shinglesTokenizer = Tokenizers.shingles(DEFAULT_NUM_SHINGLES);
    private MinHasher hasher = new MinHasher(DEFAULT_NUM_HASHES);
    private LSH lsh;
    private StringIntHashtable urlToId;
    private IntStringHashtable idToUrl;
    private LogFile neardupLog;

    private int numSamples = 1000;
    private Sampler<String> unique = new Sampler<>(numSamples);
    private Sampler<KV<Integer, int[]>> duplicates = new Sampler<>(numSamples);

    public DuplicatePageIndexer() {
        this(DEFAULT_JACCARD_THRESHOLD);
    }

    public DuplicatePageIndexer(double jaccardThreshold) {
        this.lsh = new LSH(DEFAULT_NUM_HASHES, jaccardThreshold);
        this.urlToId = new StringIntHashtable() {

            Map<String, Integer> map = new HashMap<>();

            public int get(String key) {
                Integer value = map.get(key);
                return value == null ? absentValue : value;
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
        this(dataPath, DEFAULT_JACCARD_THRESHOLD);
    }

    public DuplicatePageIndexer(String dataPath, double jaccardThreshold) {
        this(Paths.get(dataPath), jaccardThreshold);
    }

    public DuplicatePageIndexer(Path dataPath, double jaccardThreshold) {
        Path basePath = dataPath.resolve("near_duplicates");
        this.neardupLog = new LogFile(dataPath.resolve("data_monitor/nearduplicates.csv"));
        this.lsh = new LSH(DEFAULT_NUM_HASHES, jaccardThreshold,
                basePath.resolve("lsh").toString());
        this.urlToId = new StringIntHashtable(basePath.resolve("keys_db").toString());
        this.idToUrl = new IntStringHashtable(basePath.resolve("ids_db").toString());
    }

    public int insert(String key, String text) {
        int[] signatures = computeMinHashSignatures(text);
        int keyId = getId(key);
        lsh.insert(keyId, signatures);
        return keyId;
    }

    @Override
    public boolean detectAndIndex(String url, byte[] contentBytes) {
        return detectAndIndex(url, new String(contentBytes));
    }

    @Override
    public boolean detectAndIndex(String url, String text) {
        int[] signatures = computeMinHashSignatures(text);
        boolean isDuplicate = lsh.isDuplicate(signatures);
        if (neardupLog != null && isDuplicate) {
            neardupLog.printf("%s\n", url);
        }
        int id = insert(url, text);
        if (isDuplicate) {
            duplicates.sample(new KV<>(id, signatures));
        } else {
            unique.sample(url);
        }
        return isDuplicate;
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

    public boolean isNearDuplicate(String url, String text) {
        int[] signatures = computeMinHashSignatures(text);
        boolean isDuplicate = lsh.isDuplicate(signatures);
        return isDuplicate;
    }

    public Set<String> findNearDuplicates(String text) {
        int[] signatures = computeMinHashSignatures(text);
        Iterator<Integer> duplicateIds = lsh.query(signatures);
        Set<String> keys = new HashSet<>();
        while (duplicateIds.hasNext()) {
            int id = duplicateIds.next();
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

    @Override
    public DupData getDuplicationSample() {
        UnionFind unionFind = new UnionFind(1);
        for (KV<Integer, int[]> dup : duplicates.getSamples()) {
            int id = dup.getKey();
            Iterator<Integer> duplicateIds = lsh.query(dup.getValue());
            if (duplicateIds != null && duplicateIds.hasNext()) {
                while (duplicateIds.hasNext()) {
                    int dupId = duplicateIds.next();
                    unionFind.union(id, dupId);
                }
            }
        }
        Int2ObjectMap<IntList> sets = unionFind.listSets();
        List<List<String>> dupClusters = new ArrayList<>();
        List<String> uniqueSample = new ArrayList<>();
        for (Int2ObjectMap.Entry<IntList> cluster : sets.int2ObjectEntrySet()) {
            List<String> urlDupCluster = new ArrayList<>();
            IntList list = cluster.getValue();
            if (list.size() > 1) {
                for (int i = 0; i < list.size(); i++) {
                    int id = list.getInt(i);
                    urlDupCluster.add(getUrlBy(id));
                }
                dupClusters.add(urlDupCluster);
            } else {
                String url = getUrlBy(list.get(0));
                uniqueSample.add(url);
            }
        }
        return new DupData(dupClusters, uniqueSample);
    }

}
