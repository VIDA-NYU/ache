package focusedCrawler.link;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.BaseRobotRules;
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

    private final boolean insertSiteMaps;
    private final boolean disallowSitesInRobotsTxt;

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
        this.disallowSitesInRobotsTxt = config.getDisallowSitesInRobotsFile();
        this.insertSiteMaps = config.getDownloadSitemapXml();
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
        if (disallowSitesInRobotsTxt) {
            this.insertRobotRules(robotsData.link, robotsData.robotRules);
        }
        if (insertSiteMaps) {
            for (String sitemap : robotsData.robotRules.getSitemaps()) {
                try {
                    frontierManager.insert(LinkRelevance.createSitemap(sitemap, 299));
                } catch (Exception e) {
                    logger.error("Failed to insert sitemap from robot: " + sitemap);
                }
            }
        }
    }
    
    public void insert(SitemapXmlHandler.SitemapData sitemapData) {
        for (String link : sitemapData.links) {
            try {
                frontierManager.insert(LinkRelevance.createForward(link, 1.0d));
            } catch (Exception e) {
                logger.error("Failed to insert link into the frontier: "+link);
            }
        }
        logger.info("Added {} URLs from sitemap.", sitemapData.links.size());
        
        for (String sitemap : sitemapData.sitemaps) {
            try {
                frontierManager.insert(LinkRelevance.createSitemap(sitemap, 299));
            } catch (Exception e) {
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
            return frontierManager.nextURL(true);
        } catch (FrontierPersistentException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    public static void runServer(String configPath, String seedFilePath,
                                 String dataOutputPath, String modelPath,
                                 LinkStorageConfig config)
                                 throws FrontierPersistentException {
        try {
            MetricsManager metricsManager = new MetricsManager(dataOutputPath);
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

    /**
     * Inserts the robot rules object into the HashMap
     * 
     * @param link
     * @param robotRules
     * @throws NullPointerException
     *             when either of the argument is null
     */
    public void insertRobotRules(LinkRelevance link, BaseRobotRules robotRules) {
        if (link == null || robotRules == null) {
            throw new NullPointerException("Link argument or robot rules argument cannot be null");
        }
        frontierManager.getFrontier().insertRobotRules(link, robotRules);
    }

    public void addSeeds(List<String> seeds) {
        frontierManager.addSeeds(seeds);
    }

}
