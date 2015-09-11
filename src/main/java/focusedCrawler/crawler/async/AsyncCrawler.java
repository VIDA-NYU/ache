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
import focusedCrawler.util.storage.StorageFactoryException;

public class AsyncCrawler {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncCrawler.class);

    private boolean stop = false;

    private Storage targetStorage;
    private LinkStorage linkStorage;

    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage) {
        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
    }

    public void run() {
        UserAgent userAgent = new UserAgent("ACHE", "", "");
        HttpDownloader downloader = new HttpDownloader(userAgent);
        try {
            while (!stop) {
                try {
                    LinkRelevance link = ((LinkRelevance) linkStorage.select(null));
                    
                    FetchedResultHandler resultHandler = new FetchedResultHandler(targetStorage);
                    downloader.dipatchDownload(link, resultHandler);
                    
                } catch (DataNotFoundException e) {
                    if(downloader.stillWorking()) {
                        System.out.println("Waiting for links from pages being downloaded...");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {}
                        continue;
                    } else {
                        stop = true;
                        System.out.println("LinkStorage ran out of links...");
                        break;
                    }
                } catch (StorageException e) {
                    logger.warn("Problem dispatching link.", e);
                } 
            }
            downloader.await();
        } finally {
            System.out.println("Shutting down crawler...");
            downloader.close();
            System.out.println("Done.");
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
            
        } catch (StorageFactoryException e) {
            logger.error("Problem while creating Storage", e);
        } catch (Exception e) {
            logger.error("Problem while starting crawler.", e);
        }
    }


}
