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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import focusedCrawler.link.frontier.FrontierPersistentException;
import focusedCrawler.link.frontier.FrontierTargetRepository;
import focusedCrawler.link.frontier.FrontierTargetRepositoryBaseline;
import focusedCrawler.util.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.dashboard.LinkMonitor;
import focusedCrawler.util.parser.SimpleWrapper;
import focusedCrawler.util.persistence.PersistentHashtable;
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

//Data structure for dashboard//
	private LinkMonitor monitor;
	private int refreshFreq;//export the urls in the frontier after inserting refreshFreq of pages.
	private List<String> outLinks = new ArrayList<String>();
///////////////////////////////

    public LinkStorage(LinkStorageConfig config, BipartiteGraphManager manager,
                       FrontierManager frontierManager, LinkMonitor linkMonitor)
                       throws IOException {
        this.frontierManager = frontierManager;
        this.graphManager = manager;
        this.monitor = linkMonitor;
        this.getBacklinks = config.getBacklinks();
        this.getOutlinks = config.getOutlinks();
        this.refreshFreq = config.getFrontierRefreshFrequency();
    }

    public void setOnlineLearning(OnlineLearning onlineLearning, int learnLimit) {
        this.onlineLearning = onlineLearning;
        this.learnLimit = learnLimit;
    }

    public List<String> getFrontierPages() throws Exception {
        // List<String> pages = new ArrayList<String>();
        // Tuple[] tuples =
        // frontierManager.getFrontierPersistent().getFrontierPages();
        // for (Tuple tuple: tuples)
        // pages.add(tuple.getKey());
        // return pages;
        return frontierManager.getFrontierPersistent().getFrontierPages();
    }

  /**
   * This method inserts links from a given page into the frontier
   * @param obj Object - page containing links
   * @return Object
   */
  public synchronized Object insert(Object obj) throws StorageException {
    long initialTime = System.currentTimeMillis();

    Page page = (Page)obj;
    numberOfPages++;
		try
		{
			if((numberOfPages % refreshFreq) == 0)
			{
				List<String> list = getFrontierPages();
				monitor.exportFrontierPages(list);
				monitor.exportOutLinks(outLinks);
				outLinks.clear();
			}
		}
		catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
    try {
    	if(getBacklinks && page.isAuth()){
    		logger.info(">>>>>GETTING BACKLINKS:" + page.getURL().toString());
    		graphManager.insertBacklinks(page);
    		numberOfBacklink++;
    		logger.info("TOTAL BACKLINKS:" + numberOfBacklink);
    	}
    	if(onlineLearning != null && numberOfPages % learnLimit == 0){
    		logger.info("RUNNING ONLINE LEARNING...");
    		onlineLearning.execute();
    		frontierManager.clearFrontier();
    	}
    	if(getBacklinks){
    		if(page.isHub()){
    			graphManager.insertOutlinks(page);
    		}
    	}else{
    		if(getOutlinks){
    			String sOutLinks = graphManager.insertOutlinks(page);	
					outLinks.add(sOutLinks);
    		}
    	}
    }
    catch (LinkClassifierException ex) {
    	logger.info("A LinkClassifierException occurred.", ex);
    	throw new StorageException(ex.getMessage(), ex);
    }
    catch (FrontierPersistentException ex) {
    	logger.info("A FrontierPersistentException occurred.", ex);
    	throw new StorageException(ex.getMessage(), ex);
    }
    catch (IOException ex) {
        logger.info("An IOException occurred.", ex);
    	throw new StorageException(ex.getMessage(), ex);
    }
    catch (Exception ex) {
    	logger.info("An Exception occurred.", ex);
    	throw new StorageException(ex.getMessage(), ex);
    }
    
    long finalTime = System.currentTimeMillis();
    totalTime = totalTime + (finalTime - initialTime);
    double average = totalTime/numberOfPages;
    
    logger.info("\n> TOTAL PAGES:" + numberOfPages +
                "\n> TOTAL TIME:" + (finalTime - initialTime) +
                "\n> AVERAGE:" + average);
    return null;
  }

  /**
   * This method sends a link to crawler
   * @param obj Object
   * @return Object
   */
  public synchronized Object select(Object obj) throws StorageException {
    Object next = null;
    try {
      next = frontierManager.nextURL();
    }
    catch (FrontierPersistentException ex) {
      ex.printStackTrace();
      throw new StorageException(ex.getMessage());
    }
    return next;
  }

    public static void main(String[] args) throws FrontierPersistentException {

        try {
            String configPath = args[0];
            String seedFile = args[1];
            String dataPath = args[2];

            String linkConfigFile = configPath + "/link_storage/link_storage.cfg";
            ParameterFile config = new ParameterFile(linkConfigFile);

            Storage linkStorage = createLinkStorage(configPath, seedFile, dataPath, config);

            StorageBinder binder = new StorageBinder(config);
            binder.bind(linkStorage);
        } catch (Exception e) {
            logger.error("Problem while starting LinkStorage.", e);
        }
    }
    
    public static Storage createLinkStorage(String configPath, String seedFile,
                                            String dataPath, ParameterFile params)
                                            throws LinkClassifierFactoryException,
                                                   MalformedURLException,
                                                   FrontierPersistentException,
                                                   IOException {

        String stoplistFile = configPath + "/stoplist.txt";
        
        LinkStorageConfig config = new LinkStorageConfig(params);
        
        LinkClassifierFactory factory = new LinkClassifierFactoryImpl(stoplistFile);
        LinkClassifier linkClassifier = factory.createLinkClassifier(config.getTypeOfClassifier());

        FrontierTargetRepositoryBaseline frontier = createFrontier(seedFile, config, dataPath);

        logger.info("FRONTIER: " + frontier.getClass());

        FrontierManager frontierManager = new FrontierManager(frontier, config.getMaxSizeLinkQueue(), config.getMaxSizeLinkQueue());

        BipartiteGraphRep graphRep = new BipartiteGraphRep(dataPath, config.getBiparitieGraphRepConfig());

        BipartiteGraphManager manager = createBipartiteGraphManager(config, linkClassifier, frontierManager, graphRep);

        LinkMonitor monitor = new LinkMonitor(dataPath + "/" + "data_monitor/frontierpages.csv", dataPath + "/" + "data_monitor/outlinks.csv");

        LinkStorage linkStorage = new LinkStorage(config, manager, frontierManager, monitor);

        if (config.isUseOnlineLearning()) {
            StopList stoplist = new StopListArquivo(stoplistFile);
            WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);
            ClassifierBuilder cb = new ClassifierBuilder(graphRep, stoplist, wrapper, frontier);
            
            logger.info("ONLINE LEARNING:" + config.getOnlineMethod());
            OnlineLearning onlineLearning = new OnlineLearning(frontier, manager, cb, config.getOnlineMethod(), dataPath + "/" + config.getTargetStorageDirectory());
            linkStorage.setOnlineLearning(onlineLearning, config.getLearningLimit());
        }

        return linkStorage;
    }

    private static FrontierTargetRepositoryBaseline createFrontier(String seedFile,
                                                                   LinkStorageConfig config,
                                                                   String dataPath) {
        
        PersistentHashtable persistentHash = new PersistentHashtable(dataPath + "/" + config.getLinkDirectory(), config.getMaxCacheUrlsSize());
        
        FrontierTargetRepositoryBaseline frontier = null;
        if (config.isUseScope()) {
            String[] urls = ParameterFile.getSeeds(seedFile);
            HashMap<String, Integer> scope = extractDomains(urls);
            if (config.getTypeOfClassifier().contains("Baseline")) {
                frontier = new FrontierTargetRepositoryBaseline(persistentHash, scope);
            } else {
                frontier = new FrontierTargetRepository(persistentHash, scope);
            }
        } else {
            if (config.getTypeOfClassifier().contains("Baseline")) {
                frontier = new FrontierTargetRepositoryBaseline(persistentHash, 10000);
            } else {
                frontier = new FrontierTargetRepository(persistentHash, 10000);
            }
        }
        return frontier;
    }

    private static HashMap<String, Integer> extractDomains(String[] urls) {
        HashMap<String, Integer> scope = new HashMap<String, Integer>();
        for (int i = 0; i < urls.length; i++) {
            try {
                URL url = new URL(urls[i]);
                String host = url.getHost();
                logger.info(url.toString());
                scope.put(host, new Integer(1));
            } catch (MalformedURLException e) {
                logger.warn("Invalid URL in seeds file. Ignoring URL: "+urls[i]);
            }
        }
        return scope;
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
            
            SimpleWrapper simpleWrapper = new SimpleWrapper(patternIni, patternEnd);
            SimpleWrapper simpleWrapperTitle = new SimpleWrapper(patternIniTitle, patternEndTitle);
            BacklinkSurfer surfer = new BacklinkSurfer(simpleWrapper, simpleWrapperTitle);
            
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

