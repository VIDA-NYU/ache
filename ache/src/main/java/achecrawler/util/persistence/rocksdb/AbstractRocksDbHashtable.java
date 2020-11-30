package achecrawler.util.persistence.rocksdb;

import com.google.common.base.Preconditions;
import java.io.Closeable;
import java.io.File;
import java.io.UnsupportedEncodingException;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.google.common.io.BaseEncoding;

public abstract class AbstractRocksDbHashtable implements Closeable {

    protected Options options;
    protected RocksDB db;

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
            String message = String.format(
                    "Failed to open/create RocksDB database at %s. Error code: %s",
                    path, e.getStatus().getCodeString());
            throw new RuntimeException(message, e);
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

}
