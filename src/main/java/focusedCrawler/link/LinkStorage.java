/*
############################################################################
##
## Copyright (C) 2006-2009 University of Utah. All rights reserved.
##
## This file is part of DeepPeep.
##
## This file may be used under the terms of the GNU General Public
## License version 2.0 as published by the Free Software Foundation
## and appearing in the file LICENSE.GPL included in the packaging of
## this file.  Please review the following to ensure GNU General Public
## Licensing requirements will be met:
## http://www.opensource.org/licenses/gpl-license.php
##
## If you are unsure which license is appropriate for your use (for
## instance, you are interested in developing a commercial derivative
## of DeepPeep), please contact us at deeppeep@sci.utah.edu.
##
## This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
## WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
##
############################################################################
*/
package focusedCrawler.link;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.crawler.async.RobotsTxtHandler;
import focusedCrawler.crawler.async.SitemapXmlHandler;
import focusedCrawler.link.backlink.BacklinkSurfer;
import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierFactory;
import focusedCrawler.link.classifier.LinkClassifierFactoryException;
import focusedCrawler.link.classifier.LinkClassifierFactoryImpl;
import focusedCrawler.link.classifier.LinkClassifierHub;
import focusedCrawler.link.classifier.builder.LinkClassifierBuilder;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
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
    private final int learnLimit;

    private final FrontierManager frontierManager;
    private final BipartiteGraphManager graphManager;
    private final OnlineLearning onlineLearning;

    private AtomicBoolean onlineLearningIsRunning = new AtomicBoolean(false);
    private AtomicInteger numberOfPages = new AtomicInteger(0);
    private AtomicInteger numberOfBacklink = new AtomicInteger(0);

    public LinkStorage(LinkStorageConfig config,
                       BipartiteGraphManager manager,
                       FrontierManager frontierManager)
                       throws IOException {
        this(config, manager, frontierManager, null);
    }
    
    public LinkStorage(LinkStorageConfig config,
                       BipartiteGraphManager manager,
                       FrontierManager frontierManager,
                       OnlineLearning onlineLearning) throws IOException {
        this.frontierManager = frontierManager;
        this.graphManager = manager;
        this.getBacklinks = config.getBacklinks();
        this.getOutlinks = config.getOutlinks();
        this.onlineLearning = onlineLearning;
        this.learnLimit = config.getLearningLimit();
    }

    public void close(){
        logger.info("Shutting down GraphManager...");
        graphManager.getRepository().close();
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
        
        int numberOfPages = this.numberOfPages.incrementAndGet();
        
        try {
            
            if (getBacklinks && page.isAuth()) {
                logger.info(">>>>>GETTING BACKLINKS:" + page.getURL().toString());
                graphManager.insertBacklinks(page);
                numberOfBacklink.incrementAndGet();
                logger.info("TOTAL BACKLINKS:" + numberOfBacklink.get());
            }
            
            if (onlineLearning != null && numberOfPages % learnLimit == 0) {
                if(onlineLearningIsRunning.compareAndSet(false, true)) {
                    // onlineLearningIsRunning is true
                    logger.info("RUNNING ONLINE LEARNING...");
                    onlineLearning.execute();
                    frontierManager.clearFrontier();
                    onlineLearningIsRunning.set(false);
                }
            }
            
            if (getBacklinks) {
                if (page.isHub()) {
                    graphManager.insertOutlinks(page);
                }
            } else {
                if (getOutlinks) {
                    graphManager.insertOutlinks(page);
                }
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
            return frontierManager.nextURL();
        } catch (FrontierPersistentException e) {
            throw new StorageException(e.getMessage(), e);
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
                                            throws LinkClassifierFactoryException,
                                                   FrontierPersistentException,
                                                   IOException {

        
        Path stoplistPath = Paths.get(configPath, "/stoplist.txt");
        StopList stoplist;
        if(Files.exists(stoplistPath)) {
            stoplist = new StopListFile(stoplistPath.toFile().getCanonicalPath());
        } else {
            stoplist = StopListFile.DEFAULT;
        }
        
        LinkClassifierFactory linkClassifierFactory = new LinkClassifierFactoryImpl(stoplist, modelPath);
        LinkClassifier linkClassifier = linkClassifierFactory.createLinkClassifier(config.getTypeOfClassifier());

        FrontierManager frontierManager = FrontierManagerFactory.create(config, configPath, dataPath, seedFile, metricsManager);

        BipartiteGraphRepository graphRep = new BipartiteGraphRepository(dataPath);

        BipartiteGraphManager manager = createBipartiteGraphManager(config, linkClassifier, frontierManager, graphRep);

        LinkStorage linkStorage;
        if (config.isUseOnlineLearning()) {
            LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(stoplist);
            LinkClassifierBuilder cb = new LinkClassifierBuilder(dataPath, graphRep, stoplist, wrapper, frontierManager.getFrontier());
            OnlineLearning onlineLearning = new OnlineLearning(frontierManager.getFrontier(), manager, cb, config.getOnlineMethod(), dataPath);
            logger.info("ONLINE LEARNING:" + config.getOnlineMethod());
            linkStorage = new LinkStorage(config, manager, frontierManager, onlineLearning);
        } else {
            linkStorage = new LinkStorage(config, manager, frontierManager);
        }

        return linkStorage;
    }

    private static BipartiteGraphManager createBipartiteGraphManager(LinkStorageConfig config,
            LinkClassifier linkClassifier, FrontierManager frontierManager,
            BipartiteGraphRepository graphRepository) {

        if (config.getBacklinks()) {
            return new BipartiteGraphManager(frontierManager, graphRepository, linkClassifier,
                    config.getMaxPagesPerDomain(), new BacklinkSurfer(config.getBackSurferConfig()),
                    new LinkClassifierHub());
        } else {
            return new BipartiteGraphManager(frontierManager, graphRepository, linkClassifier,
                    config.getMaxPagesPerDomain(), null, null);
        }
    }
    
}

