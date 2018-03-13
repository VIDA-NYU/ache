package focusedCrawler.util.persistence.rocksdb;

public class StringIntHashtable extends BytesBytesHashtable {

    public final int absentValue = -1;

    public StringIntHashtable(String path) {
        super(path);
    }

    protected StringIntHashtable() {}

    public int get(String key) {
        byte[] bytes = super.get(stringToBytes(key));
        if (bytes == null) {
            return absentValue;
        } else {
            return bytesToInt(bytes);
        }
    }

    public void put(String key, int value) {
        super.put(stringToBytes(key), intToBytes(value));
    }

}

