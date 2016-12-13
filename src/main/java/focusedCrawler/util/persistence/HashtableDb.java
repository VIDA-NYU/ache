package focusedCrawler.util.persistence;

import java.util.List;

public interface HashtableDb<T> {

    public void put(List<Tuple<T>> tuples) throws Exception;

    public void put(String key, T value) throws Exception;

    public List<Tuple<T>> listElements() throws Exception;

    public T get(String key) throws Exception;

    public TupleIterator<T> iterator() throws Exception;
    
    public void close();

}
