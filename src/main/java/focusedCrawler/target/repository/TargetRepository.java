package focusedCrawler.target.repository;

import focusedCrawler.target.model.Page;
import focusedCrawler.util.CloseableIterator;

public interface TargetRepository {

    public boolean insert(Page target);

    public void close();

    public CloseableIterator<Page> pagesIterator();

}
