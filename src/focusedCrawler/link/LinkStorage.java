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

import focusedCrawler.util.parser.SimpleWrapper;
import focusedCrawler.link.classifier.LinkClassifierHub;
import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierException;
import focusedCrawler.link.classifier.LinkClassifierFactory;
import focusedCrawler.link.classifier.LinkClassifierFactoryImpl;
import focusedCrawler.link.classifier.LinkClassifierFactoryException;
import focusedCrawler.link.classifier.builder.BacklinkSurfer;
import focusedCrawler.link.classifier.builder.ClassifierBuilder;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.FrontierPersistentException;
import focusedCrawler.link.frontier.FrontierTargetRepository;
import focusedCrawler.link.frontier.FrontierTargetRepositoryBaseline;
import focusedCrawler.util.PriorityQueueLink;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageBinderException;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.distribution.StorageBinder;
import focusedCrawler.util.Page;
import focusedCrawler.util.dashboard.LinkMonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

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
	private List<String> outLinks;
///////////////////////////////

  public LinkStorage(BipartiteGraphManager manager, FrontierManager frontierManager, boolean getBacklinks, boolean getOutlinks, LinkMonitor mnt, int freq) throws IOException {
    this.frontierManager = frontierManager;
    this.graphManager = manager;
    this.getBacklinks = getBacklinks;
    this.getOutlinks = getOutlinks;
		this.monitor = mnt;
		this.refreshFreq = freq;
		this.outLinks = new ArrayList<String>();
  }

  public void setOnlineLearning(OnlineLearning onlineLearning, int learnLimit){
	  this.onlineLearning = onlineLearning;
	  this.learnLimit = learnLimit;
  }
  

	public List<String> getFrontierPages() throws Exception
	{
		//List<String> pages = new ArrayList<String>();
		//Tuple[] tuples = frontierManager.getFrontierPersistent().getFrontierPages();
		//for (Tuple tuple: tuples)
		//	pages.add(tuple.getKey());
		//return pages;
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
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    try {
    	if(getBacklinks && page.isAuth()){
    		System.out.println(">>>>>GETTING BACKLINKS:" + page.getURL().toString());
    		graphManager.insertBacklinks(page);
    		numberOfBacklink++;
    	    System.out.println("TOTAL BACKLINKS:" + numberOfBacklink);
    	}
    	if(onlineLearning != null && numberOfPages % learnLimit == 0){
    		System.out.println("RUNNING ONLINE LEARNING...");
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
    }catch (LinkClassifierException ex) {
    	ex.printStackTrace();
    	throw new StorageException(ex.getMessage());
    }catch (FrontierPersistentException ex1) {
    	ex1.printStackTrace();
    	throw new StorageException(ex1.getMessage());
    }catch (IOException ex) {
    	ex.printStackTrace();
    	throw new StorageException(ex.getMessage());
    }catch (Exception ex) {
    	ex.printStackTrace();
    	throw new StorageException(ex.getMessage());
    }
    
    long finalTime = System.currentTimeMillis();
    totalTime = totalTime + (finalTime - initialTime);
    double average = totalTime/numberOfPages;
    System.out.println("TOTAL PAGES:" + numberOfPages);
    System.out.println("TOTAL TIME:" + (finalTime - initialTime));
    System.out.println("AVERAGE:" + average);
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

     try{
       String configPath = args[0];
       String seedFile = args[1];
       String dataPath = args[2];
       String linkConfigFile = configPath + "/link_storage/link_storage.cfg";
       String stoplistFile = configPath + "/stoplist.txt";
       ParameterFile config = new ParameterFile(linkConfigFile);
       LinkClassifierFactory factory = new LinkClassifierFactoryImpl(stoplistFile);
       LinkClassifier linkClassifier = factory.createLinkClassifier(config.getParam("TYPE_OF_CLASSIFIER"));
       PriorityQueueLink queue = new PriorityQueueLink(config.getParamInt("MAX_SIZE_LINK_QUEUE"));
       PersistentHashtable persistentHash = new PersistentHashtable(dataPath + "/" + config.getParam("LINK_DIRECTORY"),config.getParamInt("MAX_CACHE_URLS_SIZE"));
       FrontierTargetRepositoryBaseline frontier = null;
       boolean getOutlinks = config.getParamBoolean("GRAB_LINKS"); 
       boolean useScope = config.getParamBoolean("USE_SCOPE");
       System.out.println("USE_SCOPE:" + useScope);
       if(useScope){
           HashMap<String,Integer> scope = new HashMap<String,Integer>();
                    //ParameterFile seedConfig = new ParameterFile(seedFile);
           //String[] urls = seedConfig.getParam("SEEDS", " ");
           String[] urls = ParameterFile.getSeeds(seedFile);
           for (int i = 0; i < urls.length; i++) {
        	   java.net.URL url = new java.net.URL(urls[i]);
        	   String host = url.getHost();
//        	   host = host + url.getPath();
//        	   if(host.lastIndexOf(".") != -1){
//        		   String serverTemp = host.substring(0,host.lastIndexOf("."));
//       				int index = serverTemp.lastIndexOf(".");
//       				if(index != -1){
//       					host = host.substring(index+1);
//       				}
//        	   }
        	   System.out.println(url);
        	   scope.put(host, new Integer(1));
           }   
           if(config.getParam("TYPE_OF_CLASSIFIER").contains("Baseline")){
        	   frontier = new FrontierTargetRepositoryBaseline(persistentHash,scope);
           }else{
        	   frontier = new FrontierTargetRepository(persistentHash,scope);   
           }
       }else{
           if(config.getParam("TYPE_OF_CLASSIFIER").contains("Baseline")){
        	   frontier = new FrontierTargetRepositoryBaseline(persistentHash,10000);
           }else{
        	   frontier = new FrontierTargetRepository(persistentHash,10000);   
           }
       }
       
       System.out.println("FRONTIER: " + frontier.getClass());
       
       FrontierManager frontierManager = new FrontierManager(
    		   queue,frontier,config.getParamInt("MAX_SIZE_LINK_QUEUE"));

       Storage linkStorage = null;
       
       System.out.println(">> LOADING GRAPH...");
       PersistentHashtable url2id = new PersistentHashtable(dataPath + "/" + config.getParam("URL_ID_DIRECTORY"),100000);
       PersistentHashtable authID = new PersistentHashtable(dataPath + "/" + config.getParam("AUTH_ID_DIRECTORY"),100000);
       PersistentHashtable authGraph = new PersistentHashtable(dataPath + "/" + config.getParam("AUTH_GRAPH_DIRECTORY"),100000);
       PersistentHashtable hubID = new PersistentHashtable(dataPath + "/" + config.getParam("HUB_ID_DIRECTORY"),100000);
       PersistentHashtable hubGraph = new PersistentHashtable(dataPath + "/" + config.getParam("HUB_GRAPH_DIRECTORY"),100000);
       BipartiteGraphRep graphRep = new BipartiteGraphRep(authGraph,url2id,authID,hubID,hubGraph);
       System.out.println(">> DONE GRAPH.");
//       //to avoid hitting backlink site
//       PersistentHashtable url2id_initial = new PersistentHashtable(config.getParam("URL_ID_DIRECTORY")+"_initial",100000);
//       PersistentHashtable authID_initial = new PersistentHashtable(config.getParam("AUTH_ID_DIRECTORY")+"_initial",100000);
//       PersistentHashtable authGraph_initial = new PersistentHashtable(config.getParam("AUTH_GRAPH_DIRECTORY")+"_initial",100000);
//       PersistentHashtable hubID_initial = new PersistentHashtable(config.getParam("HUB_ID_DIRECTORY")+"_initial",100000);
//       PersistentHashtable hubGraph_initial = new PersistentHashtable(config.getParam("HUB_GRAPH_DIRECTORY")+"_initial",100000);
//       BipartiteGraphRep graphRep_initial = new BipartiteGraphRep(authGraph_initial,url2id_initial,authID_initial,hubID_initial,hubGraph_initial);

       BipartiteGraphManager manager = null;
       boolean getBacklinks = config.getParamBoolean("SAVE_BACKLINKS");
       if(getBacklinks){
           ParameterFile backSurferCFG = new ParameterFile(config.getParam("BACKLINK_CONFIG"));
           SimpleWrapper simpleWrapper = new SimpleWrapper(backSurferCFG.getParam("PATTERN_INI"),
        		   backSurferCFG.getParam("PATTERN_END"));
           SimpleWrapper simpleWrapperTitle = new SimpleWrapper(backSurferCFG.getParam("PATTERN_INI_TITLE"),
        		   backSurferCFG.getParam("PATTERN_END_TITLE"));
           BacklinkSurfer surfer = new BacklinkSurfer(simpleWrapper,simpleWrapperTitle);
           LinkClassifier bClassifier = new LinkClassifierHub();
           manager = new BipartiteGraphManager(frontierManager,graphRep,linkClassifier,bClassifier);
           manager.setBacklinkSurfer(surfer);
//         manager.setSecondRep(graphRep_initial);    	   
       }else{
           manager = new BipartiteGraphManager(frontierManager,graphRep,linkClassifier,null);
       }
	   LinkMonitor mnt = new LinkMonitor(dataPath + "/" + "data_monitor/frontierpages.csv", dataPath + "/" + "data_monitor/outlinks.csv");
       int freq = config.getParamInt("FRONTIER_REFRESH_FREQUENCY");
       int maxPages = config.getParamInt("MAX_PAGES_PER_DOMAIN");
       manager.setMaxPages(maxPages);      
       linkStorage = new LinkStorage(manager,frontierManager,getBacklinks,getOutlinks, mnt, freq);//hard coding interval
       boolean useOnlineLearning = config.getParamBoolean("ONLINE_LEARNING");
       if(useOnlineLearning){
    	  // StopList stoplist = new StopListArquivo(config.getParam("STOPLIST_FILES"));
    	   StopList stoplist = new StopListArquivo(stoplistFile);
    	   WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);
    	   ClassifierBuilder cb = new ClassifierBuilder(graphRep,stoplist,wrapper,frontier);
    	   System.out.println("ONLINE LEARNING:" + config.getParam("ONLINE_METHOD"));
    	   OnlineLearning onlineLearning = new OnlineLearning(frontier, manager, cb, config.getParam("ONLINE_METHOD"), dataPath + "/" + config.getParam("TARGET_STORAGE_DIRECTORY"));
    	   ((LinkStorage)linkStorage).setOnlineLearning(onlineLearning,config.getParamInt("LEARNING_LIMIT"));
       }
       StorageBinder binder = new StorageBinder(config);
       binder.bind(linkStorage);
     }
     catch (java.io.IOException ex) {
       ex.printStackTrace();
     }
     catch(LinkClassifierFactoryException ex){
       ex.printStackTrace();
     }
     catch (StorageBinderException ex1) {
       ex1.printStackTrace();
     } catch (CacheException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}

