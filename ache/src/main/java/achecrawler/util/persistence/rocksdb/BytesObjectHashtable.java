package achecrawler.util.persistence.rocksdb;

import achecrawler.util.CloseableIterator;
import achecrawler.util.IteratorBase;
import achecrawler.util.KV;
import achecrawler.util.Kryos;

public class BytesObjectHashtable<T>
        extends AbstractRocksDbHashtable
        implements Iterable<KV<byte[], T>> {

    private Kryos<T> kryos;

    public BytesObjectHashtable(String path, Class<T> contentClass) {
        super(path);
        this.kryos = new Kryos<>(contentClass);
    }

    public void put(byte[] key, T value) {
        byte[] valueBytes = kryos.serializeObject(value);
        putBytes(key, valueBytes);
    }

    public T get(byte[] key) {
        byte[] value = getBytes(key);
        if (value == null) {
            return null;
        }
        return kryos.unserializeObject(value);
    }

    @Override
    public CloseableIterator<KV<byte[], T>> iterator() {
        return new BytesObjectIterator(new RocksDBIterator(super.db));
    }

    private class BytesObjectIterator
            extends IteratorBase<KV<byte[], byte[]>>
            implements CloseableIterator<KV<byte[], T>> {

        public BytesObjectIterator(CloseableIterator<KV<byte[], byte[]>> it) {
            super(it);
        }

        @Override
        public KV<byte[], T> next() {
            KV<byte[], byte[]> next = it.next();
            if (next == null)
                return null;
            T value = kryos.unserializeObject(next.getValue());
            return new KV<byte[], T>(next.getKey(), value);
        }
    }

}
