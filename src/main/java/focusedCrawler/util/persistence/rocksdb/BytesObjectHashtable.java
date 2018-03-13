package focusedCrawler.util.persistence.rocksdb;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class BytesObjectHashtable<T> extends BytesBytesHashtable {

    private static final ThreadLocal<Kryo> KRYOS = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            return kryo;
        };
    };

    private Class<T> contentClass;

    public BytesObjectHashtable(String path, Class<T> contentClass) {
        super(path);
        this.contentClass = contentClass;
    }

    public void put(byte[] key, T value) {
        byte[] valueBytes = serializeObject(value);
        super.put(key, valueBytes);
    }

    private byte[] serializeObject(T value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        KRYOS.get().writeObject(output, value);
        output.flush();
        return baos.toByteArray();
    }

    public T getObject(byte[] key) {
        byte[] value = super.get(key);
        if (value == null) {
            return null;
        }
        return unserializeObject(value);
    }

    private T unserializeObject(byte[] value) {
        Input input = new Input(value);
        return KRYOS.get().readObject(input, contentClass);
    }

}
