package achecrawler.target.repository;

import achecrawler.target.model.Page;
import achecrawler.util.CloseableIterator;

public interface TargetRepository {

    public boolean insert(Page target);

    public void close();

    public CloseableIterator<Page> pagesIterator();

}
