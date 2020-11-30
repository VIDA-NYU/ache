package achecrawler.target.repository;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.target.model.Page;
import achecrawler.util.CloseableIterator;
import achecrawler.util.IteratorBase;
import achecrawler.util.KV;
import achecrawler.util.persistence.rocksdb.StringObjectHashtable;

/**
 * A target repository that stores pages in a (RocksDB) key-value store.
 * 
 * @author aeciosantos
 *
 */
public class RocksDBTargetRepository implements TargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(RocksDBTargetRepository.class);

    private final StringObjectHashtable<Page> db;

    public RocksDBTargetRepository(Path directory) {
        this(directory.toString());
    }

    public RocksDBTargetRepository(String directory) {
        this.db = new StringObjectHashtable<>(directory, Page.class);
    }

    public boolean insert(Page target) {
        try {
            synchronized (this) {
                db.put(target.getRequestedUrl(), target);
                if (target.getRedirectedURL() != null) {
                    db.put(target.getRedirectedURL().toString(), target);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to store object in repository.", e);
            return false;
        }
    }

    public Page get(String url) {
        return db.get(url);
    }

    public void close() {
        db.close();
    }

    @Override
    public CloseableIterator<Page> pagesIterator() {
        return new RepositoryIterator(db.iterator());
    }

    protected class RepositoryIterator
            extends IteratorBase<KV<String, Page>>
            implements CloseableIterator<Page> {

        public RepositoryIterator(CloseableIterator<KV<String, Page>> it) {
            super(it);
        }

        @Override
        public Page next() {
            KV<String, Page> next = it.next();
            return next.getValue();
        }

    }

}
