package achecrawler.util.persistence.rocksdb;

public class StringIntHashtable extends AbstractRocksDbHashtable {

    public final int absentValue = -1;

    public StringIntHashtable(String path) {
        super(path);
    }

    protected StringIntHashtable() {}

    public int get(String key) {
        byte[] bytes = getBytes(stringToBytes(key));
        if (bytes == null) {
            return absentValue;
        } else {
            return bytesToInt(bytes);
        }
    }

    public void put(String key, int value) {
        putBytes(stringToBytes(key), intToBytes(value));
    }

}
