package focusedCrawler.crawler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import focusedCrawler.crawler.async.RobotsTxtHandler.RobotsData;
import focusedCrawler.crawler.async.SitemapXmlHandler.SitemapData;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.StorageException;

public class AsyncCrawler {
	
    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private final TargetStorage targetStorage;
    private final LinkStorage linkStorage;
    private final Downloader downloader;
    
    private volatile boolean shouldStop = false;
    private Object running = new Object();
    private boolean isShutdown = false;

    public AsyncCrawler(TargetStorage targetStorage, LinkStorage linkStorage, Downloader downloader) {
        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
        this.downloader = downloader;
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
                        downloader.dispatchDownload(link);
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
            downloader.close();
            linkStorage.close();
            targetStorage.close();
            isShutdown = true;
            logger.info("Shutdown finished.");
        }
    }
}
