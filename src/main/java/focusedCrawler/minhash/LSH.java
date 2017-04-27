package focusedCrawler.minhash;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;

import focusedCrawler.util.persistence.rocksdb.RocksDBHashtable;

public class LSH {
    
    private final int nBands;
    private final int nRows;
    private final LSHStorage bandsStorage;
    
    public LSH(int nHashes, double jaccardThreshold) {
        this(nHashes, computeNumberOfBandsForThreshold(nHashes, jaccardThreshold));
    }
    
    public LSH(int nHashes, int nBands) {
        this(nHashes, nBands, new InMemoryStorage(nBands));
    }
    
    public LSH(int nHashes, double jaccardThreshold, String dataPath) {
        this(nHashes, computeNumberOfBandsForThreshold(nHashes, jaccardThreshold), new DBStorage(dataPath));
    }
    
    public LSH(int nHashes, int nBands, String dataPath) {
        this(nHashes, nBands, new DBStorage(dataPath));
    }
    
    public LSH(int nHashes, int nBands, LSHStorage bandsStorage) {
        if ((nHashes % nBands) != 0) {
            throw new IllegalArgumentException("Bands must divide nHashes (" + nHashes + ") evenly");
        }
        this.nBands = nBands;
        this.nRows = nHashes / nBands;
        this.bandsStorage = bandsStorage;
    }

    public double targetThreshold(int nHashes, int nBands) {
        return Math.pow(1.0 / nHashes, (1.0 / (nHashes / nBands)));
    }


    private static int computeNumberOfBandsForThreshold(int nHashes, double jaccardThreshold) {
        int bands = nHashes;
        while (bands > 1) {
            if ((nHashes % bands) == 0) {
                double threshold = Math.pow((double) 1.0 / bands, (double) bands / nHashes);
                if (threshold > jaccardThreshold) {
                    break;
                }
            }
            bands--;
        }
        return bands;
    }

    public void insert(String key, int[] hashes) {
        for (int b = 0; b < nBands; b++) {
            StringBuffer sb = new StringBuffer();
            for (int r = 0; r < nRows; r++) {
                sb.append(Integer.toHexString(hashes[b * nRows + r]));
            }
            String hh = sb.toString();
            bandsStorage.insertToBand(b, hh, key);
        }
    }

    public Set<String> query(int[] hashes) {
        Set<String> candidates = new HashSet<String>();
        for (int b = 0; b < nBands; b++) {
            StringBuffer sb = new StringBuffer();
            for (int r = 0; r < nRows; r++) {
                sb.append(Integer.toHexString(hashes[b * nRows + r]));
            }
            String hh = sb.toString();
            Collection<String> values = bandsStorage.getValues(b, hh);
            if (values != null) {
                candidates.addAll(values);
            }
        }
        return candidates;
    }

    interface LSHStorage {
        public void insertToBand(int b, String hh, String key);

        public Collection<String> getValues(int b, String hh);
    }
    
    static class InMemoryStorage implements LSHStorage {

        private final ArrayListMultimap<String, String>[] maps;

        @SuppressWarnings("unchecked")
        public InMemoryStorage(int nBands) {
            maps = new ArrayListMultimap[nBands];
            for (int i = 0; i < nBands; i++) {
                maps[i] = ArrayListMultimap.create();
            }
        }

        public void insertToBand(int band, String hexHash, String key) {
            maps[band].put(hexHash, key);
        }

        public Collection<String> getValues(int band, String hexHash) {
            return maps[band].get(hexHash);
        }
    }

    static class DBStorage implements LSHStorage {

        private final RocksDBHashtable<TreeSet<String>> maps;

        @SuppressWarnings({"unchecked", "rawtypes"})
        public DBStorage(String path) {
            maps = new RocksDBHashtable(path, TreeSet.class);
        }

        public void insertToBand(int band, String hexHash, String key) {
            String hashtableKey = band + hexHash;
            TreeSet<String> keysSet = maps.get(hashtableKey);
            if (keysSet == null) {
                keysSet = new TreeSet<>();
            }
            keysSet.add(key);
            maps.put(hashtableKey, keysSet);
        }

        public Collection<String> getValues(int band, String hexHash) {
            String hashtableKey = band + hexHash;
            return maps.get(hashtableKey);
        }

    }

}
