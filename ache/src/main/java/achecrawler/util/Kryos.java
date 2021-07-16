package achecrawler.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;

/**
 * A thread-safe wrapper for serialization using Kryos.
 * 
 * @author aeciosantos
 *
 * @param <T> The type of the object which will be serialized/unserialized.
 */
public class Kryos<T> {

    private static final ThreadLocal<Kryo> KRYOS = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            registerDeserializers(kryo);
            return kryo;
        };
    };

    private static void registerDeserializers(Kryo kryo) {
        // register deserializers for java collections that are not available by default
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
        kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
        kryo.register(Collections.singletonList("").getClass(),
                new CollectionsSingletonListSerializer());
        kryo.register(Collections.singleton("").getClass(),
                new CollectionsSingletonSetSerializer());
        kryo.register(Collections.singletonMap("", "").getClass(),
                new CollectionsSingletonMapSerializer());
    }

    private Class<T> contentClass;

    public Kryos(Class<T> contentClass) {
        this.contentClass = contentClass;
    }

    public byte[] serializeObject(T value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        KRYOS.get().writeObject(output, value);
        output.flush();
        return baos.toByteArray();
    }

    public T unserializeObject(byte[] value) {
        if (value == null)
            return null;
        Input input = new Input(value);
        return KRYOS.get().readObject(input, contentClass);
    }

}
