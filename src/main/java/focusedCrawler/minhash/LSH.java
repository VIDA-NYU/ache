package focusedCrawler.minhash;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;

import focusedCrawler.util.persistence.rocksdb.BytesObjectHashtable;

/**
 * Implementation of locality-sensitive hashing algorithm for finding near-duplicate content.
 * 
 * @author aeciosantos
 *
 */
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
        this(nHashes, computeNumberOfBandsForThreshold(nHashes, jaccardThreshold),
            new DBStorage(dataPath));
    }

    public LSH(int nHashes, int nBands, String dataPath) {
        this(nHashes, nBands, new DBStorage(dataPath));
    }

    public LSH(int nHashes, int nBands, LSHStorage bandsStorage) {
        if ((nHashes % nBands) != 0) {
            throw new IllegalArgumentException(
                "Bands must divide nHashes (" + nHashes + ") evenly");
        }
        this.nBands = nBands;
        this.nRows = nHashes / nBands;
        this.bandsStorage = bandsStorage;
    }

    /**
     * Finds the number of bands that need to be used for a given similarity threshold.
     * 
     * @param nHashes
     * @param jaccardThreshold
     * @return
     */
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

    public void insert(int id, int[] minhashes) {
        for (int band = 0; band < nBands; band++) {
            int[] hashes = computeHash(minhashes, band, nRows);
            bandsStorage.insertToBand(band, hashes, id);
        }
    }

    public Set<Integer> query(int[] minhashes) {
        Set<Integer> candidates = new HashSet<Integer>();
        for (int band = 0; band < nBands; band++) {
            int[] hashes = computeHash(minhashes, band, nRows);
            Collection<Integer> values = bandsStorage.getValues(band, hashes);
            if (values != null && !values.isEmpty()) {
                candidates.addAll(values);
            }
        }
        return candidates;
    }

    public boolean isDuplicate(int[] minhashes) {
        for (int band = 0; band < nBands; band++) {
            int[] hashes = computeHash(minhashes, band, nRows);
            Collection<Integer> values = bandsStorage.getValues(band, hashes);
            if (values != null && !values.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static int[] computeHash(int[] minhashes, int band, int nRows) {
        int[] hashes = new int[nRows];
        for (int r = 0; r < nRows; r++) {
            hashes[r] = minhashes[band * nRows + r];
        }
        return hashes;
    }

    protected interface LSHStorage {

        public void insertToBand(int band, int[] hashes, int id);

        public Collection<Integer> getValues(int band, int[] hashes);

    }

    protected static class InMemoryStorage implements LSHStorage {

        private final ArrayListMultimap<String, Integer>[] maps;

        @SuppressWarnings("unchecked")
        public InMemoryStorage(int nBands) {
            maps = new ArrayListMultimap[nBands];
            for (int i = 0; i < nBands; i++) {
                maps[i] = ArrayListMultimap.create();
            }
        }

        @Override
        public void insertToBand(int band, int[] hashes, int id) {
            maps[band].put(toHex(hashes), id);
        }

        @Override
        public Collection<Integer> getValues(int band, int[] hashes) {
            return maps[band].get(toHex(hashes));
        }

        private static String toHex(int[] hashes) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < hashes.length; i++) {
                sb.append(Integer.toHexString(hashes[i]));
            }
            return sb.toString();
        }
    }

    @SuppressWarnings("rawtypes")
    protected static class DBStorage extends BytesObjectHashtable<TreeSet> implements LSHStorage {

        public DBStorage(String path) {
            super(path, TreeSet.class);
        }

        @Override
        public void insertToBand(int band, int[] hashes, int id) {
            byte[] hashtableKey = createKey(band, hashes);
            @SuppressWarnings("unchecked")
            TreeSet<Integer> idsSet = super.getObject(hashtableKey);
            if (idsSet == null) {
                idsSet = new TreeSet<>();
            }
            idsSet.add(id);
            super.put(hashtableKey, idsSet);
        }

        @Override
        public Collection<Integer> getValues(int band, int[] hashes) {
            byte[] hashtableKey = createKey(band, hashes);
            @SuppressWarnings("unchecked")
            TreeSet<Integer> bytes = super.getObject(hashtableKey);
            if (bytes == null) {
                return null;
            } else {
                return bytes;
            }
        }

        private byte[] createKey(int band, int[] hashes) {
            ByteBuffer byteBuffer = ByteBuffer.allocate((hashes.length + 1) * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(band);
            intBuffer.put(hashes);
            return byteBuffer.array();
        }

    }

}
