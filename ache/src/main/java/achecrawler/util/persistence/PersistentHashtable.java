package achecrawler.util.persistence;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import achecrawler.link.frontier.Visitor;
import achecrawler.util.CloseableIterator;
import achecrawler.util.IteratorBase;
import achecrawler.util.KV;
import achecrawler.util.persistence.rocksdb.StringObjectHashtable;

public class PersistentHashtable<T> {

    public enum DB {
        ROCKSDB
    }

    private static Logger logger = LoggerFactory.getLogger(PersistentHashtable.class);

    private StringObjectHashtable<T> persistentTable;

    private int tempMaxSize = 1000;
    private List<KV<String, T>> tempList = new ArrayList<>(tempMaxSize);

    private Cache<String, T> cache;

    public PersistentHashtable(String path, int cacheSize, Class<T> contentClass) {
        this(path, cacheSize, contentClass, DB.ROCKSDB);
    }

    public PersistentHashtable(String path, int cacheSize, Class<T> contentClass, DB backend) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
        if (backend == DB.ROCKSDB) {
            this.persistentTable = new StringObjectHashtable<>(file.getPath(), contentClass);
        } else {
            throw new UnsupportedOperationException(
                    "No database backend available for: " + backend);
        }
    }

    /**
     * DEPRECATED: may cause OutOfMemoryError on large crawls.
     * 
     * @return
     * @throws Exception
     */
    @Deprecated
    public List<Tuple<T>> getTable() {
        try (TupleIterator<T> it = this.iterator()) {
            List<Tuple<T>> items = new ArrayList<>();
            while (it.hasNext()) {
                items.add(it.next());
            }
            return items;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get hashtable values.", e);
        }
    }

    /**
     * DEPRECATED: may cause OutOfMemoryError on large crawls.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public Tuple<T>[] getTableAsArray() {
        List<Tuple<T>> table = getTable();
        return table.toArray((Tuple<T>[]) Array.newInstance(Tuple.class, table.size()));
    }

    public synchronized T get(String key) {
        try {
            key = URLEncoder.encode(key, "UTF-8");

            T obj = cache.getIfPresent(key);
            if (obj == null) {
                obj = persistentTable.get(key);
            }
            return obj;
        } catch (Exception e) {
            logger.error("Failed to get key from hashtable.", e);
            return null;
        }
    }

    public synchronized boolean put(String key, T value) {
        try {
            key = URLEncoder.encode(key, "UTF-8");
            cache.put(key, value);
            tempList.add(new KV<String, T>(key, value));
            if (tempList.size() == tempMaxSize) {
                commit();
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to store item in persitent hashtable.", e);
            return false;
        }
    }

    public synchronized void commit() {
        if (tempList.isEmpty())
            return;
        if (!tempList.isEmpty()) {
            for (KV<String, T> tuple : tempList) {
                persistentTable.put(tuple.getKey(), tuple.getValue());
            }
            tempList.clear();
        }
    }

    public synchronized void close() {
        this.commit();
        persistentTable.close();
    }

    @Deprecated
    public synchronized List<Tuple<T>> orderedSet(final Comparator<T> valueComparator) {
        try {
            List<Tuple<T>> elements = getTable();
            Collections.sort(elements, new Comparator<Tuple<T>>() {
                @Override
                public int compare(Tuple<T> o1, Tuple<T> o2) {
                    return valueComparator.compare(o1.getValue(), o2.getValue());
                }
            });
            return elements;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list elements from hashtable.", e);
        }
    }

    public TupleIterator<T> iterator() {
        try {
            this.commit();
            return new TupleIteratorImpl(persistentTable.iterator());
        } catch (Exception e) {
            throw new RuntimeException("Failed to open hashtable iterator.", e);
        }
    }

    public void visitTuples(Visitor<Tuple<T>> visitor) {
        try (TupleIterator<T> it = iterator()) {
            while (it.hasNext()) {
                visitor.visit(it.next());
            }
        } catch (Exception e) {
            logger.error("Failed to close iterator.", e);
        }
    }

    private class TupleIteratorImpl
            extends IteratorBase<KV<String, T>>
            implements TupleIterator<T> {

        public TupleIteratorImpl(CloseableIterator<KV<String, T>> it) {
            super(it);
        }

        @Override
        public Tuple<T> next() {
            KV<String, T> next = it.next();
            if (next == null)
                return null;
            else
                return new Tuple<>(next.getKey(), next.getValue());
        }

    }
}
