package achecrawler.util.persistence;

import achecrawler.util.CloseableIterator;

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
public interface TupleIterator<T> extends CloseableIterator<Tuple<T>> {

}
