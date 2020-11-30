package achecrawler.util;

import java.util.Iterator;

/**
 * Represents an iterator that should be closed. Requesting a iterator might open underlying
 * resources that should be properly closed. Ideally, CloseableIterators should be used inside a
 * try-with-resources block, so they are always automatically closed:
 * 
 * <pre>
 * {@code
 *  try(CloseableIterator<T> it = iterator()) {
 *      while(it.hasNext()) {
 *          T t = it.next();
 *          //...
 *      }
 *  }
 * </pre>
 * 
 * @author aeciosantos
 *
 * @param <T> the type of the iterable value
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
}
