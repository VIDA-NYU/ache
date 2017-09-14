package focusedCrawler.crawler.async;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import focusedCrawler.config.Configuration;
import focusedCrawler.crawler.async.HttpDownloader.Callback;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageConfig;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.StorageFactoryException;
import focusedCrawler.util.storage.distribution.StorageCreator;

public class AsyncCrawler extends AbstractExecutionThreadService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private final Storage targetStorage;
    private final Storage linkStorage;
    private final HttpDownloader downloader;
    private final Map<LinkRelevance.Type, HttpDownloader.Callback> handlers = new HashMap<>();

    private MetricsManager metricsManager;

    public AsyncCrawler(Storage targetStorage, Storage linkStorage,
            AsyncCrawlerConfig crawlerConfig, String dataPath, MetricsManager metricsManager) {

        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
        this.metricsManager = metricsManager;
        this.downloader =
                new HttpDownloader(crawlerConfig.getDownloaderConfig(), dataPath, metricsManager);

        this.handlers.put(LinkRelevance.Type.FORWARD, new FetchedResultHandler(targetStorage));
        this.handlers.put(LinkRelevance.Type.SITEMAP, new SitemapXmlHandler(linkStorage));
        this.handlers.put(LinkRelevance.Type.ROBOTS, new RobotsTxtHandler(linkStorage,
                crawlerConfig.getDownloaderConfig().getUserAgentName()));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stopAsync();
                awaitTerminated();
            }
        });
    }

    @Override
    protected void run() {
        while (isRunning()) {
            try {
                LinkRelevance link = (LinkRelevance) linkStorage.select(null);
                if (link != null) {
                    Callback handler = handlers.get(link.getType());
                    if (handler == null) {
                        logger.error("No registered handler for link type: " + link.getType());
                        continue;
                    }
                    downloader.dipatchDownload(link, handler);
                }
            } catch (DataNotFoundException e) {
                // There are no more links available in the frontier right now
                if (downloader.hasPendingDownloads() || !e.ranOutOfLinks()) {
                    // If there are still pending downloads, new links
                    // may be found in these pages, so we should wait some
                    // time until more links are available and try again
                    try {
                        logger.info("Waiting for links from pages being downloaded...");
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    }
                    continue;
                }
                // There are no more pending downloads and there are no
                // more links available in the frontier, so stop crawler
                logger.info("LinkStorage ran out of links, stopping crawler.");
                stopAsync();
                break;
            } catch (StorageException e) {
                logger.error("Problem when selecting link from LinkStorage.", e);
            } catch (Exception e) {
                logger.error("An unexpected error happened.", e);
            }
        }
    }

    @Override
    public void shutDown() {
        logger.info("Starting crawler shutdown...");
        downloader.await();
        downloader.close();
        if (linkStorage instanceof LinkStorage) {
            ((LinkStorage) linkStorage).close();
        }
        if (targetStorage instanceof TargetStorage) {
            ((TargetStorage) targetStorage).close();
        }
        if (metricsManager != null) {
            metricsManager.close();
        }
        logger.info("Shutdown finished.");
    }

    public static AsyncCrawler create(String configPath, String dataPath, String seedPath,
            String modelPath, String esIndexName, String esTypeName) throws Exception {

        Configuration config = new Configuration(configPath);

        MetricsManager metricsManager = new MetricsManager(false, dataPath);
        Storage linkStorage = LinkStorage.createLinkStorage(configPath, seedPath, dataPath,
                modelPath, config.getLinkStorageConfig(), metricsManager);

        Storage targetStorage = TargetStorage.createTargetStorage(configPath, modelPath, dataPath,
                esIndexName, esTypeName, config.getTargetStorageConfig(), linkStorage);

        return new AsyncCrawler(targetStorage, linkStorage, config.getCrawlerConfig(), dataPath,
                metricsManager);
    }

    public static void run(Configuration config, String dataPath)
            throws IOException, NumberFormatException {
        logger.info("Starting CrawlerManager...");
        try {
            StorageConfig linkStorageServerConfig =
                    config.getLinkStorageConfig().getStorageServerConfig();
            Storage linkStorage = new StorageCreator(linkStorageServerConfig).produce();

            StorageConfig targetServerConfig =
                    config.getTargetStorageConfig().getStorageServerConfig();
            Storage targetStorage = new StorageCreator(targetServerConfig).produce();

            AsyncCrawlerConfig crawlerConfig = config.getCrawlerConfig();
            AsyncCrawler crawler = new AsyncCrawler(targetStorage, linkStorage, crawlerConfig,
                    dataPath, new MetricsManager(dataPath));
            crawler.startAsync();
            crawler.awaitTerminated();
        } catch (StorageFactoryException ex) {
            logger.error("An error occurred while starting CrawlerManager. ", ex);
        }
    }

    public MetricsManager getMetricsManager() {
        return metricsManager;
    }

    public void addSeeds(List<String> seeds) {
        if (linkStorage instanceof LinkStorage) {
            ((LinkStorage) linkStorage).addSeeds(seeds);
        }
    }

}
