package achecrawler.util.persistence.rocksdb;

public class IntStringHashtable extends AbstractRocksDbHashtable {

    public IntStringHashtable(String path) {
        super(path);
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
