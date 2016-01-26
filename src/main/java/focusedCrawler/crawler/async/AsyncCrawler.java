package focusedCrawler.crawler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.fetcher.http.UserAgent;
import focusedCrawler.link.DownloadScheduler;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;

public class AsyncCrawler {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private static final int DEFAULT_MAX_LINKS_SCHEDULER = 10000;
    private static final int MINIMUM_ACCESS_INTERVAL = 5000;
    
    private final LinkStorage linkStorage;
    private final UserAgent userAgent;
    private final HttpDownloader downloader;
    private final FetchedResultHandler resultHandler;
    private final DownloadScheduler downloadScheduler;
    
    private boolean shouldStop = false;
    
    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage) {
        this(targetStorage,
             linkStorage,
             MINIMUM_ACCESS_INTERVAL,
             DEFAULT_MAX_LINKS_SCHEDULER);
    }
    
    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage,
                        int minimumAccessInterval, int maxLinksInScheduler) {
        this.linkStorage = linkStorage;
        this.userAgent = new UserAgent("ACHE", "", "https://github.com/ViDA-NYU/ache");
        this.downloader = new HttpDownloader(userAgent);
        this.resultHandler = new FetchedResultHandler(targetStorage);
        this.downloadScheduler = new DownloadScheduler(minimumAccessInterval, maxLinksInScheduler);
    }
    
    private class DownloadDispatcher extends Thread {
        public DownloadDispatcher() {
            setName("download-dispatcher");
        }
        @Override
        public void run() {
            while(!shouldStop) {
                LinkRelevance linkRelevance = downloadScheduler.nextLink();
                if(linkRelevance != null) {
                    downloader.dipatchDownload(linkRelevance, resultHandler);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.error("LinkDispatcher was interrupted.", e);
                    }
                }
            }
        }
    }

    public void run() {
        try {
            DownloadDispatcher linkDispatcher = new DownloadDispatcher();
            linkDispatcher.start();
            while(!this.shouldStop) {
                try {
                    LinkRelevance link = (LinkRelevance) linkStorage.select(null);
                    if(link != null) {
                        downloadScheduler.addLink(link);
                    }
                }
                catch (DataNotFoundException e) {
                    // There are no more links available in the frontier right now
                    if(downloader.hasPendingDownloads() || downloadScheduler.hasPendingLinks()) {
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
            downloader.await();
        } finally {
            logger.info("Shutting down crawler...");
            downloader.close();
            logger.info("Done.");
        }
    }

    public void stop() {
        this.shouldStop = true;
    }

}
