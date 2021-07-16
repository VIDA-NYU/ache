package achecrawler.link;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.SimpleRobotRules;
import achecrawler.crawler.async.RobotsTxtHandler;
import achecrawler.crawler.async.SitemapXmlHandler;
import achecrawler.link.classifier.LinkClassifierFactory;
import achecrawler.link.classifier.builder.LinkClassifierBuilder;
import achecrawler.link.classifier.online.BipartiteOnlineLearning;
import achecrawler.link.classifier.online.ForwardOnlineLearning;
import achecrawler.link.classifier.online.OnlineLearning;
import achecrawler.link.frontier.FrontierManager;
import achecrawler.link.frontier.FrontierManagerFactory;
import achecrawler.link.frontier.FrontierPersistentException;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.DataNotFoundException;
import achecrawler.util.MetricsManager;
import achecrawler.util.StorageException;
import achecrawler.util.string.StopList;
import achecrawler.util.string.StopListFile;

public class LinkStorage {

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
                logger.error("Failed to insert link into the frontier: "+link, e);
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
                onlineLearning.notifyPageCrawled(page);
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

    public static LinkStorage create(String configPath, String seedFile, String dataPath,
            String modelPath, LinkStorageConfig config, MetricsManager metricsManager)
            throws FrontierPersistentException, IOException {
        
        Path stoplistPath = Paths.get(configPath, "/stoplist.txt");
        StopList stoplist;
        if(Files.exists(stoplistPath)) {
            stoplist = new StopListFile(stoplistPath.toFile().getCanonicalPath());
        } else {
            stoplist = StopListFile.DEFAULT;
        }
        
        LinkClassifierFactory.setDefaultStoplist(stoplist);

        FrontierManager frontierManager = FrontierManagerFactory.create(config, configPath,
                dataPath, modelPath, seedFile, metricsManager);

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
                return new ForwardOnlineLearning(config.getLearningLimit(),
                        config.isOnlineLearningAsync(), frontierManager, cb,
                        ForwardOnlineLearning.Type.BINARY, dataPath);
            case "FORWARD_CLASSIFIER_LEVELS":
                return new ForwardOnlineLearning(config.getLearningLimit(),
                        config.isOnlineLearningAsync(), frontierManager, cb,
                        ForwardOnlineLearning.Type.LEVELS, dataPath);
            case "LINK_CLASSIFIERS":
                return new BipartiteOnlineLearning(config.getLearningLimit(),
                        config.isOnlineLearningAsync(), frontierManager, cb,
                        dataPath);
            default:
                throw new IllegalArgumentException(
                        "Unknown online learning method: " + onlineLearningType);
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
    public void insertRobotRules(LinkRelevance link, SimpleRobotRules robotRules) {
        if (link == null || robotRules == null) {
            throw new NullPointerException("Link argument or robot rules argument cannot be null");
        }
        frontierManager.getFrontier().insertRobotRules(link, robotRules);
    }

    public void addSeeds(List<String> seeds) {
        frontierManager.addSeeds(seeds);
    }

}
