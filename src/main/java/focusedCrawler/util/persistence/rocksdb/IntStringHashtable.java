package focusedCrawler.util.persistence.rocksdb;

public class IntStringHashtable extends AbstractRocksDbHashtable {

    public IntStringHashtable(String path) {
        this(path, false);
    }

    public IntStringHashtable(String path, boolean readOnly) {
        super(path, readOnly);
    }

    protected IntStringHashtable() {}

    public String get(int key) {
        byte[] bytes = getBytes(intToBytes(key));
        if (bytes == null) {
            return null;
        } else {
            return bytesToString(bytes);
        }
    }

    public void put(int key, String value) {
        putBytes(intToBytes(key), stringToBytes(value));
    }

}
