package focusedCrawler.crawler.async;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.fetcher.http.UserAgent;
import focusedCrawler.link.DownloadScheduler;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;

public class AsyncCrawler {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private int maxLinksInScheduler = 10000;
    private boolean shouldStop = false;

    private final LinkStorage linkStorage;
    private final UserAgent userAgent;
    private final HttpDownloader downloader;
    private final FetchedResultHandler resultHandler;
    private final DownloadScheduler downloadScheduler;
    
    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage) {
        this(targetStorage, linkStorage, 10000);
    }
    
    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage, int minimumAccessInterval) {
        this.linkStorage = linkStorage;
        this.userAgent = new UserAgent("ACHE", "", "https://github.com/ViDA-NYU/ache");
        this.downloader = new HttpDownloader(userAgent);
        this.resultHandler = new FetchedResultHandler(targetStorage);
        this.downloadScheduler = new DownloadScheduler(minimumAccessInterval);
    }
    
    class DownloadDispatcher extends Thread {
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
                        Thread.sleep(10);
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
            while (!this.shouldStop) {
                try {
                    if(downloadScheduler.numberOfLinks() > maxLinksInScheduler) {
                        Thread.sleep(100);
                        continue;
                    }
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
                            Thread.sleep(maxLinksInScheduler);
                        } catch (InterruptedException ie) { }
                        continue;
                    }
                    // There are no more pending downloads and there are no
                    // more links available in the frontier, so stop crawler
                    logger.info("LinkStorage ran out of links, stopping crawler.");
                    this.shouldStop = true;
                    break;
                } catch (StorageException e) {
                    logger.error("Problem dispatching link.", e);
                } catch (InterruptedException e) {
                    logger.error("Interrupted during rest time.", e);
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

    public static void main(String[] args) throws IOException {
        
        String dataOutputPath = "/tmp/output";
        String configPath = "./config/sample_config";
        String seedPath   = "./config/sample.seeds";
        String modelPath  = "./config/sample_model";
        
        ParameterFile linkStorageConfig = new ParameterFile(configPath + "/link_storage/link_storage.cfg");

        try {
            Storage linkStorage = LinkStorage.createLinkStorage(configPath, seedPath,
                                                                dataOutputPath, linkStorageConfig);
            
            // start target storage
            String targetConfFile = configPath + "/target_storage/target_storage.cfg";
            ParameterFile targetStorageConfig = new ParameterFile(targetConfFile);
            
            String indexName = null;
            Storage targetStorage = TargetStorage.createTargetStorage(configPath, modelPath,
                                                                      dataOutputPath,
                                                                      indexName,
                                                                      targetStorageConfig,
                                                                      linkStorage);
            
            // start crawl manager
            AsyncCrawler crawler = new AsyncCrawler(targetStorage, (LinkStorage) linkStorage);
            crawler.run();
            
        } catch (Exception e) {
            logger.error("Problem while starting crawler.", e);
        }
    }


}
