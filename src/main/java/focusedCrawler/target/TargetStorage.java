package focusedCrawler.target;

import focusedCrawler.dedup.DupDetector;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.LinkStorage;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifierException;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.classifier.TargetRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.repository.TargetRepository;
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.StorageException;

public class TargetStorage {

    public static final Logger logger = LoggerFactory.getLogger(TargetStorage.class);

    private TargetRepository targetRepository;
    private LinkStorage linkStorage;
    private TargetClassifier targetClassifier;
    private TargetStorageConfig config;
    private LangDetection langDetector = new LangDetection();
    private TargetStorageMonitor monitor;
    private DupDetector dupDetector;

    public TargetStorage(TargetClassifier targetClassifier, TargetRepository targetRepository,
            LinkStorage linkStorage, TargetStorageMonitor monitor,
            TargetStorageConfig config, DupDetector dupDetector) {

        this.targetClassifier = targetClassifier;
        this.targetRepository = targetRepository;
        this.linkStorage = linkStorage;
        this.config = config;
        this.monitor = monitor;
        this.dupDetector = dupDetector;
    }

    /**
     * Inserts a page into the repository.
     */
    public Object insert(Page page) {

        // non-html pages saved directly
        if (!page.isHtml()) {
            page.setTargetRelevance(TargetRelevance.IRRELEVANT);
            targetRepository.insert(page);
            monitor.countPage(page, false, 0.0d);
            return null;
        }

        if (config.isEnglishLanguageDetectionEnabled()) {
            // Only accept English language
            if (this.langDetector.isEnglish(page) == false) {
                logger.info("Ignoring non-English page: " + page.getURL().toString());
                return null;
            }
        }

        if (config.isNearDuplicateDetectionEnabled()) {
            String text = page.getParsedData().getCleanText();
            String key = page.getRequestedUrl();
            boolean isNearDuplicate = dupDetector.detectAndIndex(key, text);
            page.setNearDuplicate(isNearDuplicate);
            if (config.ignoreNearDuplicates() && isNearDuplicate) {
                return null;
            }
        }

        try {
            TargetRelevance relevance;
            if (targetClassifier != null) {
                relevance = targetClassifier.classify(page);
            } else {
                relevance = TargetRelevance.RELEVANT;
            }

            page.setTargetRelevance(relevance);

            if (relevance.isRelevant() || config.isSaveNegativePages()) {
                targetRepository.insert(page);
            }

            if (relevance.isRelevant()) {
                if (config.isBipartite()) {
                    // set the page is as authority if using backlinks
                    page.setAuth(true);
                }
                linkStorage.insert(page);
            } else {
                if (!config.isHardFocus()) {
                    if (config.isBipartite()) {
                        if (page.isHub()) {
                            linkStorage.insert(page);
                        }
                    } else {
                        linkStorage.insert(page);
                    }
                }
            }

            monitor.countPage(page, relevance.isRelevant(), relevance.getRelevance());

            if (monitor.getTotalOfPages() > config.getVisitedPageLimit()) {
                logger.info("Visited page limit exceeded. Exiting crawler. pagelimit="
                        + config.getVisitedPageLimit());
                System.exit(0);
            }
        } catch (TargetClassifierException | StorageException e) {
            logger.error("Unexpected error while inserting page.", e);
        }
        return null;
    }

    public static TargetStorage create(String configPath, String modelPath, String dataPath,
            String esIndexName, String esTypeName, TargetStorageConfig config,
            LinkStorage linkStorage, MetricsManager metricsManager,
            DupDetector dupDetector) throws IOException {

        // if one wants to use a classifier
        TargetClassifier targetClassifier = null;
        if (modelPath != null && !modelPath.isEmpty()) {
            targetClassifier = TargetClassifierFactory.create(modelPath);
        }

        TargetRepository targetRepository =
                TargetRepositoryFactory.create(dataPath, esIndexName, esTypeName, config);

        TargetStorageMonitor monitor = null;
        if (metricsManager != null) {
            monitor = new TargetStorageMonitor(dataPath, metricsManager);
        } else {
            monitor = new TargetStorageMonitor(dataPath);
        }

        return new TargetStorage(targetClassifier, targetRepository, linkStorage,
                monitor, config, dupDetector);
    }

    public void close() {
        targetRepository.close();
        monitor.close();
    }

}
