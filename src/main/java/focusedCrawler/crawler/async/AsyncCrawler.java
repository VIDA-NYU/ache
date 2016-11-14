package focusedCrawler.crawler.async;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import focusedCrawler.crawler.async.HttpDownloader.Callback;
import focusedCrawler.crawler.async.RobotsTxtHandler.RobotsData;
import focusedCrawler.crawler.async.SitemapXmlHandler.SitemapData;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.StorageException;

public class AsyncCrawler {
	
    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private final TargetStorage targetStorage;
    private final LinkStorage linkStorage;
    private final HttpDownloader downloader;
    private final Map<LinkRelevance.Type, HttpDownloader.Callback> handlers = new HashMap<>();
    
    private volatile boolean shouldStop = false;
    private Object running = new Object();
    private boolean isShutdown = false;

    private EventBus eventBus;

    
    public AsyncCrawler(TargetStorage targetStorage, LinkStorage linkStorage,
            AsyncCrawlerConfig crawlerConfig, String dataPath,
            MetricsManager metricsManager) {
        
        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
        this.downloader = new HttpDownloader(crawlerConfig.getDownloaderConfig(), dataPath, metricsManager);
        
        this.eventBus = new EventBus();
        this.eventBus.register(this);
        
        this.handlers.put(LinkRelevance.Type.FORWARD, new FetchedResultHandler(eventBus));
        this.handlers.put(LinkRelevance.Type.SITEMAP, new SitemapXmlHandler(eventBus));
        this.handlers.put(LinkRelevance.Type.ROBOTS,  new RobotsTxtHandler(eventBus, crawlerConfig.getDownloaderConfig().getUserAgentName()));
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
        });
    }
    
    @Subscribe
    public void sendToStorage(SitemapData sitemapData) {
        linkStorage.insert(sitemapData);
    }
    
    @Subscribe
    public void sendToStorage(RobotsData robotsData) {
        linkStorage.insert(robotsData);
    }
    
    @Subscribe
    public void sendToStorage(Page page) throws StorageException {
        targetStorage.insert(page);
    }
    
    public void run() {
        synchronized (running) {
            while(!this.shouldStop) {
                try {
                    LinkRelevance link = (LinkRelevance) linkStorage.select(null);
                    if(link != null) {
                        Callback handler = handlers.get(link.getType());
                        if(handler == null) {
                            logger.error("No registered handler for link type: "+link.getType());
                            continue;
                        }
                        downloader.dipatchDownload(link, handler);
                    }
                }
                catch (DataNotFoundException e) {
                    // There are no more links available in the frontier right now
                    if(downloader.hasPendingDownloads() || !e.ranOutOfLinks()) {
                        // If there are still pending downloads, new links 
                        // may be found in these pages, so we should wait some
                        // time until more links are available and try again
                        try {
                            logger.info("Waiting for links from pages being downloaded...");
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) { }
                        continue;
                    }
                    // There are no more pending downloads and there are no
                    // more links available in the frontier, so stop crawler
                    logger.info("LinkStorage ran out of links, stopping crawler.");
                    this.shouldStop = true;
                    break;
                } catch (StorageException e) {
                    logger.error("Problem when selecting link from LinkStorage.", e);
                } catch (Exception e) {
                    logger.error("An unexpected error happened.", e);
                }
            }
        }
    }

    public void shutdown() {
        shouldStop = true;
        synchronized(running) {
            if(isShutdown) {
               return; 
            }
            logger.info("Starting crawler shuttdown...");
            downloader.await();
            downloader.close();
            if(linkStorage instanceof LinkStorage) {
                ((LinkStorage)linkStorage).close();
            }
            if(targetStorage instanceof TargetStorage) {
                ((TargetStorage)targetStorage).close();
            }
            isShutdown = true;
            logger.info("Shutdown finished.");
        }
    }
}
