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

    private final StringIntHashtable urlToId;
    private final IntStringHashtable idToUrl;
    private final LSH lsh;
    private final LogFile neardupLog;
    private final MinHasher hasher;
    private final ShingleTokenizer tokenizer;
    private final Sampler<String> unique;
    private final Sampler<KV<Integer, int[]>> duplicates;

    public DuplicatePageIndexer(Builder builder) {
        boolean readOnly = builder.readOnly;
        double jaccardThreshold = builder.minJaccardSimilarity;
        int numOfHashFunctions = builder.numberOfHashes;

        if (builder.dataPath != null) {
            Path basePath = builder.dataPath.resolve("near_duplicates");
            Path logPath = builder.dataPath.resolve("data_monitor/nearduplicates.csv");
            String lshPath = basePath.resolve("lsh").toString();
            String keysDb = basePath.resolve("keys_db").toString();
            String idsDb = basePath.resolve("ids_db").toString();

            this.urlToId = new StringIntHashtable(keysDb, readOnly);
            this.idToUrl = new IntStringHashtable(idsDb, readOnly);
            this.lsh = new LSH(numOfHashFunctions, jaccardThreshold, lshPath, readOnly);
            this.neardupLog = new LogFile(logPath);
        } else {
            this.urlToId = createInMemoryStringIntMap();
            this.idToUrl = createInMemoryIntStringMap();
            this.lsh = new LSH(numOfHashFunctions, jaccardThreshold);
            this.neardupLog = null;
        }

        this.hasher = new MinHasher(builder.numberOfHashes);
        this.tokenizer = Tokenizers.shingles(builder.numberOfShingles);

        this.unique = new Sampler<>(builder.numberOfSamples);
        this.duplicates = new Sampler<>(builder.numberOfSamples);
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
            Set<Integer> hashedShingles = tokenizer.hashedTokenSet(cleanText);
            return hasher.minHashSignature(hashedShingles);
        } catch (Exception e) {
            logger.warn("Failed to parse clean text into shingles.", e);
            return new int[0];
        }
    }

    @Override
    public DupData getDuplicationSample() {
        UnionFind unionFind = new UnionFind(1);
        List<KV<Integer, int[]>> samples = duplicates.getSamples();
        for (KV<Integer, int[]> dup : samples) {
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

    @Override
    public void close() {
        if (this.neardupLog != null) {
            this.neardupLog.close();
        }
        if (this.lsh != null) {
            this.lsh.close();
        }
        if (this.urlToId != null) {
            this.urlToId.close();
        }
        if (this.idToUrl != null) {
            this.idToUrl.close();
        }
    }

    private IntStringHashtable createInMemoryIntStringMap() {
        return new IntStringHashtable() {

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

    private StringIntHashtable createInMemoryStringIntMap() {
        return new StringIntHashtable() {

            Map<String, Integer> map = new HashMap<>();

            public int get(String key) {
                Integer value = map.get(key);
                return value == null ? absentValue : value;
            }

            public void put(String key, int value) {
                map.put(key, value);
            }

        };
    }

    public static class Builder {

        private int numberOfSamples = 1000;
        private double minJaccardSimilarity = 0.95;
        private int numberOfShingles = 9;
        private int numberOfHashes = 256;
        private Path dataPath = null;
        private boolean readOnly = false;

        public Builder setMinJaccardSimilarity(double similarity) {
            this.minJaccardSimilarity = similarity;
            return this;
        }

        public Builder setDataPath(String dataPath) {
            return setDataPath(Paths.get(dataPath));
        }

        public Builder setDataPath(Path dataPath) {
            this.dataPath = dataPath;
            return this;
        }

        public Builder setNumberOfSamples(int numberOfSamples) {
            this.numberOfSamples = numberOfSamples;
            return this;
        }

        public Builder setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Builder setNumberOfShingles(int numberOfShingles) {
            this.numberOfShingles = numberOfShingles;
            return this;
        }

        public Builder setNumberOfHashes(int hashes) {
            this.numberOfHashes = hashes;
            return this;
        }

        public DuplicatePageIndexer build() {
            return new DuplicatePageIndexer(this);
        }
    }

}
