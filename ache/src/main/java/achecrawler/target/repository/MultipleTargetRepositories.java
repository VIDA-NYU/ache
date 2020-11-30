package achecrawler.target.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.target.model.Page;
import achecrawler.util.CloseableIterator;

public class MultipleTargetRepositories implements TargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(MultipleTargetRepositories.class);

    private final List<TargetRepository> repositories;

    public MultipleTargetRepositories(List<TargetRepository> repositories) {
        if (repositories == null || repositories.size() == 0) {
            throw new IllegalArgumentException("Repositories can not be null or empty.");
        }
        this.repositories = repositories;
    }

    @Override
    public boolean insert(Page target) {
        boolean insertedAll = true;
        for (TargetRepository repository : repositories) {
            try {
                if (!repository.insert(target)) {
                    insertedAll = false;
                }
            } catch (Exception e) {
                logger.error("Failed to insert page to target repository: "
                        + repository.getClass().getCanonicalName(), e);
                insertedAll = false;
            }
        }
        return insertedAll;
    }

    @Override
    public void close() {
        for (TargetRepository repository : repositories) {
            try {
                repository.close();
            } catch (Exception e) {
                logger.error("Failed to close target repository: "
                        + repository.getClass().getCanonicalName(), e);
            }
        }
    }

    @Override
    public CloseableIterator<Page> pagesIterator() {
        throw new UnsupportedOperationException("Iterator not supportted for multiple repositories");
    }

}
