package achecrawler.crawler.async;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import achecrawler.config.Configuration;
import achecrawler.crawler.async.HttpDownloader.Callback;
import achecrawler.crawler.cookies.Cookie;
import achecrawler.crawler.cookies.CookieUtils;
import achecrawler.link.LinkStorage;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.TargetStorage;
import achecrawler.util.DataNotFoundException;
import achecrawler.util.MetricsManager;

public class AsyncCrawler extends AbstractExecutionThreadService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private final TargetStorage targetStorage;
    private final LinkStorage linkStorage;
    private final HttpDownloader downloader;
    private final Map<LinkRelevance.Type, HttpDownloader.Callback> handlers = new HashMap<>();
    private MetricsManager metricsManager;
    private Configuration config;

    public AsyncCrawler(String crawlerId, TargetStorage targetStorage, LinkStorage linkStorage,
                        Configuration config, String dataPath, MetricsManager metricsManager) {

        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
        this.config = config;
        this.metricsManager = metricsManager;

        HttpDownloaderConfig downloaderConfig = config.getCrawlerConfig().getDownloaderConfig();
        this.downloader = new HttpDownloader(downloaderConfig, dataPath, metricsManager);

        this.handlers.put(LinkRelevance.Type.FORWARD, new FetchedResultHandler(crawlerId, targetStorage));
        this.handlers.put(LinkRelevance.Type.SITEMAP, new SitemapXmlHandler(linkStorage));
        this.handlers.put(LinkRelevance.Type.ROBOTS, new RobotsTxtHandler(linkStorage,
                downloaderConfig.getUserAgentName()));

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
        linkStorage.close();
        targetStorage.close();
        if (metricsManager != null) {
            metricsManager.close();
        }
        logger.info("Shutdown finished.");
    }

    public static AsyncCrawler create(String crawlerId, String configPath, String dataPath, String seedPath,
            String modelPath, String esIndexName, String esTypeName) throws Exception {

        Configuration config = new Configuration(configPath);

        MetricsManager metricsManager = new MetricsManager(false, dataPath);

        LinkStorage linkStorage = LinkStorage.create(configPath, seedPath, dataPath,
                modelPath, config.getLinkStorageConfig(), metricsManager);

        TargetStorage targetStorage = TargetStorage.create(configPath, modelPath, dataPath,
                esIndexName, esTypeName, config.getTargetStorageConfig(), linkStorage,
                metricsManager);

        return new AsyncCrawler(crawlerId, targetStorage, linkStorage, config, dataPath, metricsManager);
    }

    public MetricsManager getMetricsManager() {
        return metricsManager;
    }

    public void addSeeds(List<String> seeds) {
        linkStorage.addSeeds(seeds);
    }

    public Configuration getConfig() {
        return config;
    }
    
    /**
     * Add cookies to the right fetcher.
     * @param cookies
     */
    public void addCookies(HashMap<String, List<Cookie>> cookies) {
        if (cookies == null) {
            throw new NullPointerException("Cookies argument is null");
        }
        CookieUtils.addCookies(cookies, downloader.getFetcher());
    }

}
