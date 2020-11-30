package achecrawler.minhash;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

import achecrawler.util.persistence.rocksdb.BytesBytesHashtable;

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

    public Iterator<Integer> query(int[] minhashes) {
        RoaringBitmap candidates = new RoaringBitmap();
        for (int band = 0; band < nBands; band++) {
            int[] hashes = computeHash(minhashes, band, nRows);
            RoaringBitmap idsBitmap = bandsStorage.getValues(band, hashes);
            if (idsBitmap != null) {
                candidates.or(idsBitmap);
            }
        }
        return candidates.iterator();
    }

    public boolean isDuplicate(int[] minhashes) {
        for (int band = 0; band < nBands; band++) {
            int[] hashes = computeHash(minhashes, band, nRows);
            if (bandsStorage.contains(band, hashes)) {
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

        public boolean contains(int band, int[] hashes);

        public RoaringBitmap getValues(int band, int[] hashes);

    }

    protected static class InMemoryStorage implements LSHStorage {

        private final Map<String, RoaringBitmap> maps = new HashMap<>();

        public InMemoryStorage(int nBands) {}

        @Override
        public void insertToBand(int band, int[] hashes, int id) {
            String hexkey = toHex(band, hashes);
            RoaringBitmap idsBitmap = maps.get(hexkey);
            if (idsBitmap == null) {
                idsBitmap = RoaringBitmap.bitmapOf(id);
                maps.put(hexkey, idsBitmap);
            } else {
                idsBitmap.add(id);
            }
        }

        @Override
        public RoaringBitmap getValues(int band, int[] hashes) {
            RoaringBitmap idsBitmap = maps.get(toHex(band, hashes));
            return idsBitmap == null ? null : idsBitmap;
        }

        private static String toHex(int band, int[] hashes) {
            StringBuffer sb = new StringBuffer();
            sb.append(Integer.toHexString(band));
            for (int i = 0; i < hashes.length; i++) {
                sb.append(Integer.toHexString(hashes[i]));
            }
            return sb.toString();
        }

        @Override
        public boolean contains(int band, int[] hashes) {
            RoaringBitmap idsBitmap = maps.get(toHex(band, hashes));
            return (idsBitmap != null && !idsBitmap.isEmpty()) ? true : false;
        }
    }

    protected static class DBStorage extends BytesBytesHashtable implements LSHStorage {

        public DBStorage(String path) {
            super(path);
        }

        @Override
        public void insertToBand(int band, int[] hashes, int id) {
            byte[] hashtableKey = createKey(band, hashes);
            byte[] idsBitmapBytes = super.get(hashtableKey);
            if (idsBitmapBytes == null) {
                RoaringBitmap idsBitmap = RoaringBitmap.bitmapOf(id);
                idsBitmapBytes = serializeBitmap(idsBitmap);
            } else {
                RoaringBitmap idsBitmap = unserializeBitmap(idsBitmapBytes);
                idsBitmap.add(id);
                idsBitmapBytes = serializeBitmap(idsBitmap);
            }
            super.put(hashtableKey, idsBitmapBytes);
        }

        @Override
        public RoaringBitmap getValues(int band, int[] hashes) {
            byte[] hashtableKey = createKey(band, hashes);
            byte[] bitmapBytes = super.get(hashtableKey);
            if (bitmapBytes == null) {
                return null;
            } else {
                return unserializeBitmap(bitmapBytes);
            }
        }

        @Override
        public boolean contains(int band, int[] hashes) {
            byte[] hashtableKey = createKey(band, hashes);
            byte[] bitmapBytes = super.get(hashtableKey);
            return bitmapBytes != null;
        }

        private byte[] createKey(int band, int[] hashes) {
            ByteBuffer byteBuffer = ByteBuffer.allocate((hashes.length + 1) * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(band);
            intBuffer.put(hashes);
            return byteBuffer.array();
        }

        private RoaringBitmap unserializeBitmap(byte[] bitmapBytes) {
            ByteBuffer bb = ByteBuffer.wrap(bitmapBytes);
            return new ImmutableRoaringBitmap(bb).toRoaringBitmap();
        }

        private byte[] serializeBitmap(RoaringBitmap bitmap) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (DataOutputStream dos = new DataOutputStream(bos)) {
                bitmap.serialize(dos);
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize roaring bitmap.");
            }
            return bos.toByteArray();
        }
    }

}
