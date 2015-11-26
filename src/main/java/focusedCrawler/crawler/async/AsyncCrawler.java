package focusedCrawler.crawler.async;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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

    private final LinkStorage linkStorage;
    private final UserAgent userAgent;
    private final HttpDownloader downloader;
    private final FetchedResultHandler resultHandler;
    private final int minimumAccessInterval;
    private final Cache<String, Long> domainAccessCache;
    
    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage) {
        this(targetStorage, linkStorage, 5000);
    }
    
    public AsyncCrawler(Storage targetStorage, LinkStorage linkStorage, int minimumAccessInterval) {
        this.linkStorage = linkStorage;
        this.userAgent = new UserAgent("ACHE", "", "https://github.com/ViDA-NYU/ache");
        this.downloader = new HttpDownloader(userAgent);
        this.resultHandler = new FetchedResultHandler(targetStorage);
        this.minimumAccessInterval = minimumAccessInterval;
        if(this.minimumAccessInterval > 0) {
            // this cache maintain the access times of the domains and remove
            // them automatically after the specified minimumAccessInterval
            this.domainAccessCache = CacheBuilder.newBuilder()
                    .maximumSize(100000)
                    .expireAfterWrite(this.minimumAccessInterval, TimeUnit.MILLISECONDS)
                    .build();
        } else {
            this.domainAccessCache = null;
        }        
    }

    public void run() {
        try {
            while (!this.stop) {
                try {
                    LinkRelevance link = getNextLink();
                    if(link != null) {
                        downloader.dipatchDownload(link, resultHandler);
                    }
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
                            Thread.sleep(1000);
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
                } catch (InterruptedException e) {
                    logger.warn("Interrupted during resttime.", e);
                }
            }
            downloader.await();
        } finally {
            logger.info("Shutting down crawler...");
            downloader.close();
            logger.info("Done.");
        }
    }

    private LinkRelevance getNextLink() throws InterruptedException, StorageException, DataNotFoundException {
        
        LinkRelevance link = (LinkRelevance) linkStorage.select(null);

        if(domainAccessCache != null) {
            
            String domainName = link.getTopLevelDomainName();
            Long lastAccessTime = domainAccessCache.getIfPresent(domainName);
           
            if(lastAccessTime != null && lastAccessTime + minimumAccessInterval > System.currentTimeMillis()) {
                logger.info("Link can't be downloaded right now. " +
                            "Sleeping {}ms before dispatching download.",
                             minimumAccessInterval);
                Thread.sleep(minimumAccessInterval);
            }
            
            // record time this domain was last accessed 
            domainAccessCache.put(domainName, System.currentTimeMillis());
        }
        
        return link;
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
