package focusedCrawler.util.persistence.rocksdb;

public class IntStringHashtable extends BytesBytesHashtable {

    public IntStringHashtable(String path) {
        super(path);
    }

    protected IntStringHashtable() {}

    public String get(int key) {
        byte[] bytes = super.get(intToBytes(key));
        if (bytes == null) {
            return null;
        } else {
            return bytesToString(bytes);
        }
    }

    public void put(int key, String value) {
        super.put(intToBytes(key), stringToBytes(value));
    }

}

