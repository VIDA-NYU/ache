package focusedCrawler.crawler.async;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import focusedCrawler.config.Configuration;
import focusedCrawler.crawler.async.HttpDownloader.Callback;
import focusedCrawler.crawler.cookies.Cookie;
import focusedCrawler.crawler.cookies.CookieUtils;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.MetricsManager;

public class AsyncCrawler extends AbstractExecutionThreadService {

    private static final int RUN_OUT_OF_LINKS_DEFAULT = -1;
    private static final int MAX_RUN_OUT_OF_LINKS_TIME_MS = 5000;
    private static final int RUN_OUT_OF_LINKS_WAIT_TIME = 1000;

    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private final TargetStorage targetStorage;
    private final LinkStorage linkStorage;
    private final HttpDownloader downloader;
    private final Map<LinkRelevance.Type, HttpDownloader.Callback> handlers = new HashMap<>();
    private MetricsManager metricsManager;
    private Configuration config;
    private long runOutOfLinksTime = RUN_OUT_OF_LINKS_DEFAULT;

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
                    this.runOutOfLinksTime = RUN_OUT_OF_LINKS_DEFAULT;
                    Callback handler = handlers.get(link.getType());
                    if (handler == null) {
                        logger.error("No registered handler for link type: " + link.getType());
                        continue;
                    }
                    downloader.dipatchDownload(link, handler);
                }
            } catch (DataNotFoundException e) {
                // There are no more links available in the frontier right now. We need to check
                // whether it is a temporary state to decide if the crawler should stop running.

                boolean hasPendingLinks = downloader.hasPendingDownloads() || !e.ranOutOfLinks();
                if (hasPendingLinks) {
                    // If there are still pending downloads, new links may be found in these pages,
                    // so we should wait some time until more links are available and try again.
                    waitMilliseconds(RUN_OUT_OF_LINKS_WAIT_TIME);
                    continue;
                }

                // Even when the frontier runs out of links and there are no pending downloads,
                // there may be still some pages being processed, in which case the crawler may
                // find some new links. Therefore, we still keep trying to select from the frontier
                // for a fixed amount of time (MAX_RUN_OUT_OF_LINKS_TIME_MS) to avoid race conditions.
                if (!hasPendingLinks && this.runOutOfLinksTime == RUN_OUT_OF_LINKS_DEFAULT) {
                    this.runOutOfLinksTime = System.currentTimeMillis();
                }

                // The crawler should stop only after having ran out of links for a few seconds
                // This time is necessary to
                long timeSinceRunOutOfLinks = System.currentTimeMillis() - this.runOutOfLinksTime;
                if (this.runOutOfLinksTime != RUN_OUT_OF_LINKS_DEFAULT &&
                        timeSinceRunOutOfLinks > MAX_RUN_OUT_OF_LINKS_TIME_MS) {
                    // There are no more pending downloads, no more links available in the frontier,
                    // and we already waited some time new links. Now we can stop the crawler.
                    logger.info("LinkStorage ran out of links for {} ms, stopping crawler.",
                            timeSinceRunOutOfLinks);
                    stopAsync();
                    break;
                } else {
                    logger.info("LinkStorage ran out of links for {} ms...",
                            timeSinceRunOutOfLinks);
                }

                logger.info("Waiting for links from pages being processed...");
                waitMilliseconds(RUN_OUT_OF_LINKS_WAIT_TIME);

            } catch (Exception e) {
                logger.error("An unexpected error happened.", e);
            }
        }
    }

    private void waitMilliseconds(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
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
