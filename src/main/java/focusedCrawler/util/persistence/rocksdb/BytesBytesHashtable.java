package focusedCrawler.util.persistence.rocksdb;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.google.common.io.BaseEncoding;

import focusedCrawler.util.CloseableIterator;
import focusedCrawler.util.KV;

public class BytesBytesHashtable implements Closeable, Iterable<KV<byte[], byte[]>> {

    private Options options;
    private RocksDB db;

    static {
        RocksDB.loadLibrary();
    }

    protected BytesBytesHashtable() {}

    public BytesBytesHashtable(String path) {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        try {
            this.db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to open database at " + path, e);
        }
    }

    public void put(byte[] key, byte[] value) {
        try {
            db.put(key, value);
        } catch (RocksDBException e) {
            throw new RuntimeException(
                "Failed to write key to database: " + BaseEncoding.base16().encode(key));
        }
    }

    public byte[] get(byte[] key) {
        try {
            return db.get(key);
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to get value from database for key: " + key, e);
        }
    }

    @Override
    public synchronized void close() {
        if (db != null) {
            db.close();
            db = null;
            options.close();
            options = null;
        }
    }

    @Override
    public CloseableIterator<KV<byte[], byte[]>> iterator() {
        return new RocksDBIterator(this.db);
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

}
