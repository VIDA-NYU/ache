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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierException;
import focusedCrawler.link.classifier.LinkClassifierFactory;
import focusedCrawler.link.classifier.LinkClassifierFactoryException;
import focusedCrawler.link.classifier.LinkClassifierFactoryImpl;
import focusedCrawler.link.classifier.LinkClassifierHub;
import focusedCrawler.link.classifier.builder.BacklinkSurfer;
import focusedCrawler.link.classifier.builder.ClassifierBuilder;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.FrontierManagerFactory;
import focusedCrawler.link.frontier.FrontierPersistentException;
import focusedCrawler.util.Page;
import focusedCrawler.util.parser.SimpleWrapper;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.distribution.StorageBinder;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;

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

public class LinkStorage extends StorageDefault{

  public static final Logger logger = LoggerFactory.getLogger(LinkStorage.class);
	

  private FrontierManager frontierManager;

  private BipartiteGraphManager graphManager;

  private OnlineLearning onlineLearning;

  private int numberOfPages = 0;

  private int numberOfBacklink = 0;

  private long totalTime = 0;
  
  private boolean getBacklinks = false;
  
  private boolean getOutlinks = false;
  
  private int learnLimit = 10;

    public LinkStorage(LinkStorageConfig config,
                       BipartiteGraphManager manager,
                       FrontierManager frontierManager)
                       throws IOException {
        this.frontierManager = frontierManager;
        this.graphManager = manager;
        this.getBacklinks = config.getBacklinks();
        this.getOutlinks = config.getOutlinks();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                close();
            }
        });
    }
    
    public void close(){
        logger.info("Shutting down GraphManager...");
        graphManager.getRepository().close();
        logger.info("Shutting down FrontierManager...");
        this.frontierManager.close();
        logger.info("done.");
    }

    public void setOnlineLearning(OnlineLearning onlineLearning, int learnLimit) {
        this.onlineLearning = onlineLearning;
        this.learnLimit = learnLimit;
    }

    /**
     * This method inserts links from a given page into the frontier
     * 
     * @param obj
     *            Object - page containing links
     * @return Object
     */
    public synchronized Object insert(Object obj) throws StorageException {
        long initialTime = System.currentTimeMillis();

        Page page = (Page) obj;
        numberOfPages++;
        
        try {
            
            if (getBacklinks && page.isAuth()) {
                logger.info(">>>>>GETTING BACKLINKS:" + page.getURL().toString());
                graphManager.insertBacklinks(page);
                numberOfBacklink++;
                logger.info("TOTAL BACKLINKS:" + numberOfBacklink);
            }
            
            if (onlineLearning != null && numberOfPages % learnLimit == 0) {
                logger.info("RUNNING ONLINE LEARNING...");
                onlineLearning.execute();
                frontierManager.clearFrontier();
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
            
        } catch (LinkClassifierException ex) {
            logger.info("A LinkClassifierException occurred.", ex);
            throw new StorageException(ex.getMessage(), ex);
        } catch (FrontierPersistentException ex) {
            logger.info("A FrontierPersistentException occurred.", ex);
            throw new StorageException(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.info("An IOException occurred.", ex);
            throw new StorageException(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.info("An Exception occurred.", ex);
            throw new StorageException(ex.getMessage(), ex);
        }

        long finalTime = System.currentTimeMillis();
        totalTime = totalTime + (finalTime - initialTime);
        double average = totalTime / numberOfPages;

        logger.info("\n> TOTAL PAGES:" + numberOfPages + "\n> TOTAL TIME:" + (finalTime - initialTime) + "\n> AVERAGE:"
                + average);
        return null;
    }

    /**
     * This method sends a link to crawler
     */
    public synchronized Object select(Object obj) throws StorageException {
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
            Storage linkStorage = createLinkStorage(configPath, seedFilePath,
                                                    dataOutputPath, modelPath,
                                                    config);

            StorageBinder binder = new StorageBinder(config.getStorageServerConfig());
            binder.bind(linkStorage);
        } catch (Exception e) {
            logger.error("Problem while starting LinkStorage.", e);
        }
    }
    
    public static Storage createLinkStorage(String configPath, String seedFile, 
                                            String dataPath, String modelPath,
                                            LinkStorageConfig config)
                                            throws LinkClassifierFactoryException,
                                                   FrontierPersistentException,
                                                   IOException {

        String stoplistFile = configPath + "/stoplist.txt";
        
        LinkClassifierFactory linkClassifierFactory = new LinkClassifierFactoryImpl(stoplistFile, modelPath);
        LinkClassifier linkClassifier = linkClassifierFactory.createLinkClassifier(config.getTypeOfClassifier());

        FrontierManager frontierManager = FrontierManagerFactory.create(config, configPath, dataPath, seedFile, stoplistFile);

        BipartiteGraphRep graphRep = new BipartiteGraphRep(dataPath, config.getBiparitieGraphRepConfig());

        BipartiteGraphManager manager = createBipartiteGraphManager(config, linkClassifier, frontierManager, graphRep);

        LinkStorage linkStorage = new LinkStorage(config, manager, frontierManager);

        if (config.isUseOnlineLearning()) {
            StopList stoplist = new StopListArquivo(stoplistFile);
            WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);
            ClassifierBuilder cb = new ClassifierBuilder(graphRep, stoplist, wrapper, frontierManager.getFrontier());
            
            logger.info("ONLINE LEARNING:" + config.getOnlineMethod());
            OnlineLearning onlineLearning = new OnlineLearning(frontierManager.getFrontier(), manager, cb, config.getOnlineMethod(), dataPath + "/" + config.getTargetStorageDirectory());
            linkStorage.setOnlineLearning(onlineLearning, config.getLearningLimit());
        }

        return linkStorage;
    }

    private static BipartiteGraphManager createBipartiteGraphManager(LinkStorageConfig config,
                LinkClassifier linkClassifier, FrontierManager frontierManager,
                BipartiteGraphRep graphRep) {
        
        BipartiteGraphManager manager = null;
        if(config.getBacklinks()) {
            
            String patternIni = config.getBackSurferConfig().getPatternIni();
            String patternEnd = config.getBackSurferConfig().getPatternEnd();
            
            String patternIniTitle = config.getBackSurferConfig().getPatternIniTitle();
            String patternEndTitle = config.getBackSurferConfig().getPatternEndTitle();
            
            String mozAccessID = config.getBackSurferConfig().getMozAccessId();
            String mozKey = config.getBackSurferConfig().getMozKey();
            
            SimpleWrapper simpleWrapper = new SimpleWrapper(patternIni, patternEnd);
            SimpleWrapper simpleWrapperTitle = new SimpleWrapper(patternIniTitle, patternEndTitle);
            BacklinkSurfer surfer = new BacklinkSurfer(simpleWrapper, simpleWrapperTitle);
            surfer.setAccessID(mozAccessID);
            surfer.setPassKey(mozKey);
            
            LinkClassifier bClassifier = new LinkClassifierHub();
            manager = new BipartiteGraphManager(frontierManager, graphRep, linkClassifier, bClassifier);
            manager.setBacklinkSurfer(surfer);
            
        } else{
            manager = new BipartiteGraphManager(frontierManager,graphRep,linkClassifier,null);
        }
        
        manager.setMaxPages(config.getMaxPagesPerDomain());
        
        return manager;
    }
    
}

