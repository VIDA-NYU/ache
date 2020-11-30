package achecrawler.util.persistence.rocksdb;

import achecrawler.util.CloseableIterator;
import achecrawler.util.KV;

public class BytesBytesHashtable
        extends AbstractRocksDbHashtable
        implements Iterable<KV<byte[], byte[]>> {

    protected BytesBytesHashtable() {}

    public BytesBytesHashtable(String path) {
        super(path);
    }

    public void put(byte[] key, byte[] value) {
        putBytes(key, value);
    }

    public byte[] get(byte[] key) {
        return getBytes(key);
    }

    @Override
    public CloseableIterator<KV<byte[], byte[]>> iterator() {
        return new RocksDBIterator(this.db);
    }

}
