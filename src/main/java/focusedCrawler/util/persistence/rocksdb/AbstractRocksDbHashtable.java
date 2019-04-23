package focusedCrawler.util.persistence.rocksdb;

import com.google.common.base.Preconditions;
import focusedCrawler.util.CloseableIterator;
import focusedCrawler.util.KV;
import java.io.Closeable;
import java.io.File;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.google.common.io.BaseEncoding;
import org.rocksdb.RocksIterator;

public abstract class AbstractRocksDbHashtable implements Closeable {

    protected Options options;
    protected RocksDB db;
    private List<RocksDBIterator> iterators = new ArrayList<>();

    static {
        RocksDB.loadLibrary();
    }

    protected AbstractRocksDbHashtable() {}

    public AbstractRocksDbHashtable(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        try {
            this.db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to open database at " + path, e);
        }
    }

    protected void putBytes(byte[] keyBytes, byte[] valueBytes) {
        try {
            db.put(keyBytes, valueBytes);
        } catch (RocksDBException e) {
            String hexKey = BaseEncoding.base16().encode(keyBytes);
            throw new RuntimeException("Failed to write key to database: " + hexKey);
        }
    }

    protected byte[] getBytes(byte[] keyBytes) {
        Preconditions.checkNotNull(this.db, "Make sure the database is open.");
        byte[] valueBytes;
        try {
            valueBytes = db.get(keyBytes);
        } catch (RocksDBException e) {
            String hexKey = BaseEncoding.base16().encode(keyBytes);
            throw new RuntimeException("Failed to get value from database for key: " + hexKey, e);
        }
        return valueBytes;
    }

    @Override
    public synchronized void close() {
        if (db != null) {
            for (Iterator<RocksDBIterator> listIt = this.iterators.iterator(); listIt.hasNext(); ) {
                RocksDBIterator dbIt = listIt.next();
                listIt.remove();
                dbIt.close();
            }
            db.close();
            db = null;
            options.close();
            options = null;
        }
    }

    /*
     * Converts an int to a byte array using big-endian order.
     */
    static byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) (value)};
    }

    /*
     * Converts a byte array to an int using big-endian order.
     */
    static int bytesToInt(byte[] bytes) {
        return (bytes[0]) << 24 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8 |
                (bytes[3] & 0xFF);
    }

    static byte[] stringToBytes(String value) {
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not supported", e);
        }
    }

    static String bytesToString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not supported", e);
        }
    }

    protected RocksDBIterator openIterator() {
        RocksDBIterator it = new RocksDBIterator(this.db);
        this.iterators.add(it);
        return it;
    }

    public class RocksDBIterator implements CloseableIterator<KV<byte[], byte[]>> {

        private final RocksIterator cursor;
        private boolean hasNext;
        private boolean isOpen;
        private byte[] value;
        private byte[] key;
        private RocksDB db;

        private RocksDBIterator(RocksDB db) {
            this.db = db;
            this.cursor = db.newIterator();
            this.cursor.seekToFirst();
            this.isOpen = true;
            readNextKV(true);
        }

        private void readNextKV(boolean firstEntry) {
            if (!firstEntry) {
                cursor.next();
            }
            if (cursor.isValid()) {
                this.hasNext = true;
                this.key = cursor.key();
                this.value = cursor.value();
            } else {
                this.close();
            }
        }

        @Override
        public void close() {
            if (this.isOpen) {
                iterators.remove(this);
                cursor.close();
                this.isOpen = false;
                this.hasNext = false;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public KV<byte[], byte[]> next() {
            if (!hasNext) {
                return null;
            }
            KV<byte[], byte[]> kv = new KV<>(this.key, this.value);
            readNextKV(false);
            return kv;
        }

        public void remove() {
            try {
                db.delete(key);
            } catch (RocksDBException e) {
                throw new RuntimeException("Failed to remove entry from RocksDb");
            }
        }

    }

}
