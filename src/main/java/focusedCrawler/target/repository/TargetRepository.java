package focusedCrawler.target.repository;

import focusedCrawler.target.model.Page;

public interface TargetRepository {

    public boolean insert(Page target);

    public void close();

}
