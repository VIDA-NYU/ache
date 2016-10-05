package focusedCrawler.util.persistence.rocksdb;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import focusedCrawler.util.persistence.HashtableDb;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.persistence.TupleIterator;

public class RocksDBHashtable<T> implements HashtableDb<T>, Closeable {
    
    private static final ThreadLocal<Kryo> KRYOS = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            return kryo;
        };
    };
    
    private Class<T> contentClass;
    private Options options;
    private RocksDB db;
    
    static {
        RocksDB.loadLibrary();
    }
    
    public RocksDBHashtable(String path, Class<T> contentClass) {
        this.contentClass = contentClass;
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        try {
          this.db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to open database at "+path, e);
        }
    }

    @Override
    public void put(List<Tuple<T>> tuples) {
        if(tuples == null || tuples.size() == 0){
            return;
        }
        for(Tuple<T> tuple : tuples) {
            put(tuple.getKey(), tuple.getValue());
        }
    }

    @Override
    public void put(String key, T value) {
        try {
            byte[] valueBytes = serializeObject(value);
            db.put(key.getBytes(), valueBytes);
        } catch (RocksDBException e) {
            throw new RuntimeException("failed to write value to database: "+key);
        }
    }

    private byte[] serializeObject(T value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        KRYOS.get().writeObject(output, value);
        output.flush();
        return baos.toByteArray();
    }
    
    private T unserializeObject(byte[] value) {
        Input input = new Input(value);
        return KRYOS.get().readObject(input, contentClass);
    }
    

    @Override
    public List<Tuple<T>> listElements() {
        List<Tuple<T>> items = new ArrayList<>();;
        try(RocksDBIterator it = new RocksDBIterator()) {
            while(it.hasNext()) {
                items.add(it.next());
            }
            return items;
        }
    }

    @Override
    public T get(String key) {
        byte[] bytes = key.getBytes();
        try {
            byte[] value = db.get(bytes);
            if(value == null) {
                return null;
            }
            return unserializeObject(value);
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to get value from database for key: "+key, e);
        }
    }

    @Override
    public void close() {
        synchronized(db) {
            if (db != null) {
                db.close();
                db = null;
                options.close();
                options = null;
            }
        }
    }

    @Override
    public TupleIterator<T> iterator() {
        return new RocksDBIterator();
    }
    
    public class RocksDBIterator implements TupleIterator<T>, Closeable {

        final private RocksIterator cursor;
        private boolean hasNext;
        private boolean isOpen;
        private byte[] value;
        private byte[] key;

        public RocksDBIterator() {
            this.cursor = db.newIterator();
            this.cursor.seekToFirst();
            this.isOpen = true;
            readNextTuple(true);
        }

        private void readNextTuple(boolean firstEntry) {
            if(!firstEntry) {
                cursor.next();
            }
            if(cursor.isValid()) {
                this.hasNext = true;
                this.key = cursor.key();
                this.value = cursor.value();
            } else {
                this.close();
            }
        }

        @Override
        public void close() {
            if(this.isOpen) {
                cursor.close();
                this.isOpen = false;
                this.hasNext = false;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Tuple<T> next() {
            if (!hasNext) {
                return null;
            }
            T value = unserializeObject(this.value);
            Tuple<T> tuple = new Tuple<T>(new String(key), value);
            readNextTuple(false);
            return tuple;
        }
        
        public void remove() {
            throw new UnsupportedOperationException("remove() not yet supported by "+getClass().getName());
        }

    }

}
