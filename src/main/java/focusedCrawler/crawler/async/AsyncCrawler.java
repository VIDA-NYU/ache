package focusedCrawler.crawler.async;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.fetcher.http.UserAgent;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.target.TargetStorage;
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
//        int i = 0;
        try {
            while (!stop) {
                try {
                    
//                    LinkRelevance link = getNextLink();
                    LinkRelevance link = getLinkFromFrontier();
//                    System.out.println("Dispatching link "+i++);
                    
                    FetchedResultHandler resultHandler = new FetchedResultHandler(targetStorage);
                    downloader.dipatchDownload(link, resultHandler);
                    
                } catch (Exception e) {
                    logger.warn("Problem dispatching link.", e);
                }
            }
            downloader.await();
        } finally {
            System.out.println("Shutting down crawler....");
            downloader.close();
            System.out.println("Done.");
        }
    }
    
    public void stop() {
        this.stop = true;
    }

    private LinkRelevance getLinkFromFrontier() throws StorageException, Exception {
        LinkRelevance link = ((LinkRelevance) linkStorage.select(null));
        if(link == null) {
            Thread.sleep(1000);
        }
        n++;
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//        }
        return link;
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


    static int n = 0;
    static String[] urls = readAlexaSeeds();

    private static String[] readAlexaSeeds() {
        String file = "/home/aeciosantos/workspace/alexa-seeds.shuf.txt";
        List<String> list = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            return (String[]) list.toArray(new String[list.size()]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // FIXME Temporary code for tests, should get links from LinkStorage
    private LinkRelevance getNextLink() {
        int j = n % urls.length;
        n++;
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e) {
//        }
        if (n > 10000) {
            this.stop = true;
        }
        try {
            return new LinkRelevance(new URL(urls[j]), 1.0d);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create link relevance.", e);
        }
    }

}
