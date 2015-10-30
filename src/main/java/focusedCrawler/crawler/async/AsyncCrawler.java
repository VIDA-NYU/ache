package focusedCrawler.crawler.async;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.fetcher.http.UserAgent;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;

public class AsyncCrawler {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private boolean stop = false;

    private Storage targetStorage;
    private LinkStorage linkStorage;
    private HttpDownloader downloader;
    private UserAgent userAgent;

    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage) {
        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
        this.userAgent = new UserAgent("ACHE", "", "https://github.com/ViDA-NYU/ache");
        this.downloader = new HttpDownloader(userAgent);
    }

    public void run() {
        try {
            while (!this.stop) {
                
                LinkRelevance link = null;
                try {
                    link = (LinkRelevance) linkStorage.select(null);
                }
                catch (DataNotFoundException e) {
                    //
                    // There are no more links available in the frontier right now
                    //
                    if(downloader.stillWorking()) {
                        // If there are links still being downloaded, new links 
                        // may be found in these pages, so try we should wait some
                        // time until more links are available and again once more
                        try {
                            logger.info("Waiting for links from pages being downloaded...");
                            Thread.sleep(100);
                        } catch (InterruptedException ie) { }
                        continue;
                    } else {
                        // Already waited for link storage process eventual unprocessed links
                        // and there are no more links being downloaded, so stop crawler
                        logger.info("LinkStorage ran out of links, stopping crawler.");
                        this.stop = true;
                        break;
                    }
                } catch (StorageException e) {
                    logger.warn("Problem dispatching link.", e);
                }
                
                if(link != null) {
                    FetchedResultHandler resultHandler = new FetchedResultHandler(targetStorage);
                    downloader.dipatchDownload(link, resultHandler);
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
        this.stop = true;
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
