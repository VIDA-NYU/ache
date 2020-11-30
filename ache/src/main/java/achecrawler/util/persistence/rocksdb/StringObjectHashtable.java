package achecrawler.util.persistence.rocksdb;

import achecrawler.util.CloseableIterator;
import achecrawler.util.IteratorBase;
import achecrawler.util.KV;
import achecrawler.util.Kryos;

public class StringObjectHashtable<T>
        extends AbstractRocksDbHashtable
        implements Iterable<KV<String, T>> {

    private Kryos<T> kryos;

    public StringObjectHashtable(String path, Class<T> contentClass) {
        super(path);
        this.kryos = new Kryos<>(contentClass);
    }

    public void put(String key, T value) {
        byte[] valueBytes = kryos.serializeObject(value);
        byte[] keyBytes = key.getBytes();
        putBytes(keyBytes, valueBytes);
    }

    public T get(String key) {
        byte[] bytes = key.getBytes();
        byte[] valueBytes = getBytes(bytes);
        return kryos.unserializeObject(valueBytes);
    }

    @Override
    public CloseableIterator<KV<String, T>> iterator() {
        return new StringObjectIterator(new RocksDBIterator(super.db));
    }

    protected class StringObjectIterator
            extends IteratorBase<KV<byte[], byte[]>>
            implements CloseableIterator<KV<String, T>> {

        public StringObjectIterator(CloseableIterator<KV<byte[], byte[]>> it) {
            super(it);
        }

        @Override
        public KV<String, T> next() {
            KV<byte[], byte[]> next = it.next();
            if (next == null) {
                return null;
            } else {
                T value = kryos.unserializeObject(next.getValue());
                return new KV<>(new String(next.getKey()), value);
            }
        }

    }

}
