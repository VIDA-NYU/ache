package focusedCrawler.util.persistence;

import java.util.Iterator;

/**
 * Represents an iterator of a {@link PersistentHashtable}. Requesting a operator may open
 * underlying resources that should be properly closed.
 * 
 * Ideally, they should be used inside a try-with-resources block, so they are automatically closed:
 * 
 * <pre>
 * {@code
 *  try(TupleIterator{@literal <T>} it = hashtable.iterator()) {
 *      while(it.hasNext()) {
 *          Tuple{@literal <T>} t = it.next();
 *          //...
 *      }
 *  }
 * </pre>
 * 
 * @author aeciosantos
 *
 * @param <T>
 */
public interface TupleIterator<T> extends AutoCloseable, Iterator<Tuple<T>> {

}
