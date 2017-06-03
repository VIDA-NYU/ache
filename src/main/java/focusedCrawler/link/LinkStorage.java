package focusedCrawler.link;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.crawler.async.RobotsTxtHandler;
import focusedCrawler.crawler.async.SitemapXmlHandler;
import focusedCrawler.link.classifier.LinkClassifierFactory;
import focusedCrawler.link.classifier.builder.LinkClassifierBuilder;
import focusedCrawler.link.classifier.online.BipartiteOnlineLearning;
import focusedCrawler.link.classifier.online.ForwardOnlineLearning;
import focusedCrawler.link.classifier.online.OnlineLearning;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.FrontierManagerFactory;
import focusedCrawler.link.frontier.FrontierPersistentException;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.distribution.StorageBinder;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListFile;

/**
 *
 * <p>Description: This class receives links to be inserted
 * in frontier, sends links to crawler and starts the link storage server.</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class LinkStorage extends StorageDefault {

    public static final Logger logger = LoggerFactory.getLogger(LinkStorage.class);

    private final boolean getBacklinks;
    private final boolean getOutlinks;

    private final FrontierManager frontierManager;
    private final OnlineLearning onlineLearning;
    private final Set<String> blackList = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());

    public LinkStorage(LinkStorageConfig config,
                       FrontierManager frontierManager) throws IOException {
        this(config, frontierManager, null);
    }
    
    public LinkStorage(LinkStorageConfig config,
                       FrontierManager frontierManager,
                       OnlineLearning onlineLearning) throws IOException {
        this.frontierManager = frontierManager;
        this.onlineLearning = onlineLearning;
        this.getBacklinks = config.getBacklinks();
        this.getOutlinks = config.getOutlinks();
    }

    public void close(){
        
        logger.info("Shutting down FrontierManager...");
        this.frontierManager.close();
        logger.info("done.");
    }

    /**
     * This method inserts links from a given page into the frontier
     * 
     * @param obj
     *            Object - page containing links
     * @return Object
     */
    public Object insert(Object obj) throws StorageException {
        if(obj instanceof Page) {
            return insert((Page) obj);
        } 
        else if(obj instanceof RobotsTxtHandler.RobotsData) {
            insert((RobotsTxtHandler.RobotsData) obj);
        }
        else if(obj instanceof SitemapXmlHandler.SitemapData) {
            insert((SitemapXmlHandler.SitemapData) obj);
        }
        return null;
    }
    
    public void insert(RobotsTxtHandler.RobotsData robotsData) {
        for (String sitemap : robotsData.sitemapUrls) {
            try {
                frontierManager.insert(new LinkRelevance(sitemap, 299, LinkRelevance.Type.SITEMAP));
            } catch (MalformedURLException | FrontierPersistentException e) {
                logger.error("Failed to insert sitemap from robot: "+sitemap);
            }
        }
    }
    
    public void insert(SitemapXmlHandler.SitemapData sitemapData) {
        for (String link : sitemapData.links) {
            try {
                frontierManager.insert(new LinkRelevance(link, 1.0d, LinkRelevance.Type.FORWARD));
            } catch (MalformedURLException | FrontierPersistentException e) {
                logger.error("Failed to insert link into the frontier: "+link);
            }
        }
        logger.info("Added {} URLs from sitemap.", sitemapData.links.size());
        
        for (String sitemap : sitemapData.sitemaps) {
            try {
                frontierManager.insert(new LinkRelevance(new URL(sitemap), 299, LinkRelevance.Type.SITEMAP));
            } catch (MalformedURLException | FrontierPersistentException e) {
                logger.error("Failed to insert sitemap into the frontier: "+sitemap);
            }
        }
        logger.info("Added {} child sitemaps.", sitemapData.sitemaps.size());
    }
    
    
    public Object insert(Page page) throws StorageException {
        try {
            if (getBacklinks && page.isAuth()) {
                frontierManager.insertBacklinks(page);
            }

            if (getBacklinks) {
                if (page.isHub()) {
                    frontierManager.insertOutlinks(page);
                }
            } else {
                if (getOutlinks) {
                    frontierManager.insertOutlinks(page);
                }
            }

            if (onlineLearning != null) {
                onlineLearning.pushFeedback(page);
            }
        } catch (Exception ex) {
            logger.info("Failed to insert page into LinkStorage.", ex);
            throw new StorageException(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * This method sends a link to crawler
     * @throws DataNotFoundException 
     */
    public synchronized Object select(Object obj) throws StorageException, DataNotFoundException {
        try {
            LinkRelevance link = frontierManager.nextURL();
            if(!blackList.contains(link.getTopLevelDomainName())){
                return link;
            }else {
                logger.info("Dead Domain ignored: "+link.getTopLevelDomainName());
                return select(null);
            }
        } catch (FrontierPersistentException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    public synchronized void addToBlackList(String url){
        addToBlackList(url,1d);
    }

    public synchronized void addToBlackList(String url, double relevance){
        try {
            addToBlackList(new LinkRelevance(url, relevance));
        }catch (MalformedURLException mue){
            logger.info("MalformedURLException - "+url);
        }
    }

    public synchronized void addToBlackList(LinkRelevance link){
        blackList.add(link.getTopLevelDomainName());
    }
    public synchronized void removeFromBlackList(String url){
        try {
            blackList.remove(new LinkRelevance(url, 1d).getTopLevelDomainName());
        }catch (MalformedURLException mue){
            logger.info("MalformedURLException - "+url);
        }
    }

    public static void runServer(String configPath, String seedFilePath,
                                 String dataOutputPath, String modelPath,
                                 LinkStorageConfig config)
                                 throws FrontierPersistentException {
        try {
            MetricsManager metricsManager = new MetricsManager();
            Storage linkStorage = createLinkStorage(configPath, seedFilePath,
                                                    dataOutputPath, modelPath,
                                                    config, metricsManager);

            StorageBinder binder = new StorageBinder(config.getStorageServerConfig());
            binder.bind(linkStorage);
        } catch (Exception e) {
            logger.error("Problem while starting LinkStorage.", e);
        }
    }
    
    public static Storage createLinkStorage(String configPath, String seedFile, 
                                            String dataPath, String modelPath,
                                            LinkStorageConfig config,
                                            MetricsManager metricsManager)
                                            throws FrontierPersistentException,
                                                   IOException {
        
        Path stoplistPath = Paths.get(configPath, "/stoplist.txt");
        StopList stoplist;
        if(Files.exists(stoplistPath)) {
            stoplist = new StopListFile(stoplistPath.toFile().getCanonicalPath());
        } else {
            stoplist = StopListFile.DEFAULT;
        }
        
        LinkClassifierFactory.setDefaultStoplist(stoplist);

        FrontierManager frontierManager = FrontierManagerFactory.create(config, configPath, dataPath, modelPath, seedFile, metricsManager);

        OnlineLearning onlineLearning = null;
        if (config.isUseOnlineLearning()) {
            onlineLearning = createOnlineLearning(dataPath, config, stoplist, frontierManager);
        }
        
        return new LinkStorage(config, frontierManager, onlineLearning);
    }

    private static OnlineLearning createOnlineLearning(String dataPath, LinkStorageConfig config,
                                                       StopList stoplist,
                                                       FrontierManager frontierManager) {

        LinkClassifierBuilder cb = new LinkClassifierBuilder(dataPath, stoplist, frontierManager);
        String onlineLearningType = config.getOnlineMethod();
        logger.info("Online Learning method:" + onlineLearningType);
        switch (onlineLearningType) {
            case "FORWARD_CLASSIFIER_BINARY":
                return new ForwardOnlineLearning(config.getLearningLimit(), frontierManager, cb,
                                                 ForwardOnlineLearning.Type.BINARY, dataPath);
            case "FORWARD_CLASSIFIER_LEVELS":
                return new ForwardOnlineLearning(config.getLearningLimit(), frontierManager, cb,
                                                 ForwardOnlineLearning.Type.LEVELS, dataPath);
            case "LINK_CLASSIFIERS":
                return new BipartiteOnlineLearning(config.getLearningLimit(), frontierManager, cb,
                                                   dataPath);
            default:
                throw new IllegalArgumentException("Unknown online learning method: " + onlineLearningType);
        }
    }

}
