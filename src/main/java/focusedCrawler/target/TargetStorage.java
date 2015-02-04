package focusedCrawler.target;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URLEncoder;
import java.util.List; 
import java.util.ArrayList; 
import java.lang.String;

import weka.classifiers.Classifier;
import weka.core.Instances;

import focusedCrawler.util.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageBinderException;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.StorageFactoryException;
import focusedCrawler.util.storage.distribution.StorageBinder;
import focusedCrawler.util.storage.distribution.StorageCreator;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.dashboard.TargetMonitor;

/**
 * This class runs a socket server responsible to store pages coming from the crawler client.
 * @author lbarbosa
 *
 */
public class TargetStorage  extends StorageDefault{

    private TargetClassifier targetClassifier;

    private String fileLocation;
    
    private TargetRepository targetRepository;

    private int totalOfPages;
    
    private int totalOnTopicPages;

    private int limitOfPages;

    private float relevanceThreshold;
    
    private Storage linkStorage;
      
    private StringBuffer urls = new StringBuffer();

//Data structure for dashboard ////////////
    private int crawledPageRefreshFreq;
    private int relevantPageRefreshFreq;
    private int harvestinfoRefreshFreq;    
    private boolean refreshSync;
    private int refreshFreq;//if refresh_sync is true, this variable will be used as refresh frequency for all information
    private List<String> crawledUrls; 
    private List<String> relevantUrls;
    private List<String> nonRelevantUrls;
    private List<String> harvestRates;
    private TargetMonitor monitor;
////////////////////////////////////////////

    private boolean hardFocus;
    
    private boolean getBacklinks = false;

    private LangDetection langDetect;
    
    public TargetStorage(TargetClassifier targetClassifier, String fileLocation, TargetRepository targetRepository, 
                           Storage linkStorage, int limitOfPages, boolean hardFocus, boolean getBacklinks, 
                           int crawledFreq, int relevantFreq,  int harvestinfoFreq, int syncFreq, boolean isRefreshSync,
                       	   float relevanceThreshold, TargetMonitor mnt) {
        this.targetClassifier = targetClassifier;
        this.fileLocation = fileLocation;
        this.targetRepository = targetRepository;
        this.linkStorage = linkStorage;
        this.relevanceThreshold = relevanceThreshold;
        this.limitOfPages = limitOfPages;
        this.crawledUrls = new ArrayList<String>();
        this.relevantUrls = new ArrayList<String>();
        this.nonRelevantUrls = new ArrayList<String>();
        this.harvestRates = new ArrayList<String>();
        this.hardFocus = hardFocus;
        this.getBacklinks = getBacklinks;
        this.crawledPageRefreshFreq = crawledFreq;
        this.relevantPageRefreshFreq = relevantFreq;
        this.harvestinfoRefreshFreq = harvestinfoFreq;
        this.refreshFreq = syncFreq;
        this.refreshSync = isRefreshSync;
        this.totalOfPages = 0;
        this.totalOnTopicPages = 0;
        this.langDetect = new LangDetection();
        this.langDetect.init("libs/profiles/");//This is hard coded, should be fixed
        this.monitor = mnt;
    }

    public TargetStorage(String fileLocation, TargetRepository targetRepository, Storage linkStorage, 
            int limitOfPages, boolean hardFocus, boolean getBacklinks, TargetMonitor mnt) {
        this(null, fileLocation, targetRepository, linkStorage, limitOfPages, hardFocus, getBacklinks, 
          100, 100, 100, 100, true, 0.9f, mnt);
    }

    /**
     * Inserts a page into the repository. 
     */
    public synchronized Object insert(Object obj) throws StorageException {
        Page page = (Page)obj;
		//Only accept English
    	if (this.langDetect.detect_page(page) == false){
        	System.out.println(">>>> non-English page: " + page.getIdentifier());
      		return null;
    	}

        urls.append(fileLocation + File.separator + page.getURL().getHost() + File.separator + URLEncoder.encode(page.getIdentifier()));
        urls.append("\n");
        crawledUrls.add(page.getIdentifier() + "\t" + String.valueOf(System.currentTimeMillis() / 1000L));
        totalOfPages++;

        try {
          if(targetClassifier != null){
              double prob = targetClassifier.distributionForInstance(page)[0];
              page.setRelevance(prob);
              System.out.println(">>>PROCESSING: " + page.getIdentifier() + " PROB:" + prob);
              if(prob > this.relevanceThreshold){
                  targetRepository.insert(page);
                  if(getBacklinks){
					  //set the page is as authority if using backlinks
                      page.setAuth(true);
                  }
                  linkStorage.insert(page);
                  relevantUrls.add(page.getIdentifier() + "\t" + String.valueOf(System.currentTimeMillis() / 1000L));
                  totalOnTopicPages++;
              } else{
                  if(!hardFocus){
                      if(getBacklinks){
                          if(page.isHub()){
                              linkStorage.insert(page);
                          }
                      } else{
                          linkStorage.insert(page);
                      }
                  } else{
                      nonRelevantUrls.add(page.getIdentifier() + "\t" + String.valueOf(prob) + "\t" + String.valueOf(System.currentTimeMillis() / 1000L));
                  }
              }
          } else{
                  page.setRelevance(1);
                  page.setAuth(true);
                  System.out.println(">>>PROCESSING: " + page.getIdentifier());
                  linkStorage.insert(page);
                  targetRepository.insert(page,totalOfPages);
                  totalOnTopicPages++;
          }


          //////Export crawler's status////////////////////////////////
          harvestRates.add(Integer.toString(totalOnTopicPages) + "\t" + 
                       String.valueOf(totalOfPages) + "\t" + 
                       String.valueOf(System.currentTimeMillis() / 1000L));
          if (refreshSync){
          	if(totalOnTopicPages % refreshFreq == 0) {
                 monitor.exportHarvestInfo(harvestRates);
                 harvestRates.clear();
                 monitor.exportCrawledPages(crawledUrls);
                 crawledUrls.clear();    
                 monitor.exportRelevantPages(relevantUrls);
                 relevantUrls.clear();
                 monitor.exportNonRelevantPages(nonRelevantUrls);
                 nonRelevantUrls.clear();
            }
      	  } else{
              if(totalOfPages % harvestinfoRefreshFreq == 0) {
                  monitor.exportHarvestInfo(harvestRates);
                  harvestRates.clear();
              }
              if(totalOfPages % crawledPageRefreshFreq == 0) {
                  monitor.exportCrawledPages(crawledUrls);
                  crawledUrls.clear();    
        	  }
              if(totalOnTopicPages % relevantPageRefreshFreq == 0) {
                  monitor.exportRelevantPages(relevantUrls);
                  relevantUrls.clear();

                  monitor.exportNonRelevantPages(nonRelevantUrls);
                  nonRelevantUrls.clear();
              }
          //////End exporting/////////////////////////////////////////////

          }
          if(totalOfPages > limitOfPages){
              System.exit(0);
          }
        }
        catch (CommunicationException ex) {
            ex.printStackTrace();
            throw new StorageException(ex.getMessage());
        } catch (TargetClassifierException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLimitPages(int limit){
        limitOfPages = limit;
    }


    private int initialIndex(List<String> urls, int number) {
        if(number > urls.size()) {
            return 0;
        } else {
            return urls.size() - number;
        }        
    
    }

    public static void main(String[] args) {
        try{
            String configPath = args[0];
            String modelPath = args[1];
            String dataPath = args[2];
            String targetConfFile = configPath + "/target_storage/target_storage.cfg";
            String linkConfFile = configPath + "/link_storage/link_storage.cfg";
            ParameterFile config = new ParameterFile(targetConfFile);
            String stoplistFile = configPath  + "/stoplist.txt"; //default
            StopList stoplist = new StopListArquivo(stoplistFile);
            boolean useClassifier = config.getParamBoolean("USE_CLASSIFIER");
            TargetClassifier targetClassifier = null;
            //if one wants to use a classifier
            if(useClassifier){
                String modelFile = modelPath + "/pageclassifier.model";
                String featureFile = modelPath + "/pageclassifier.features";
                ParameterFile featureConfig = new ParameterFile(featureFile);
                InputStream is = new FileInputStream(modelFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(is);
                Classifier classifier = (Classifier) objectInputStream.readObject();
                String[] attributes = featureConfig.getParam("ATTRIBUTES", " ");
                weka.core.FastVector vectorAtt = new weka.core.FastVector();
                for (int i = 0; i < attributes.length; i++) {
                    vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
                }
                String[] classValues = featureConfig.getParam("CLASS_VALUES", " ");
                weka.core.FastVector classAtt = new weka.core.FastVector();
                for (int i = 0; i < classValues.length; i++) {
                    classAtt.addElement(classValues[i]);
                }
                vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
                Instances insts = new Instances("target_classification", vectorAtt, 1);
                insts.setClassIndex(attributes.length);
                targetClassifier = new TargetClassifierImpl(classifier, insts, attributes, stoplist);
            }
            String targetDirectory = dataPath + "/" + config.getParam("TARGET_STORAGE_DIRECTORY");
			String data_format = config.getParam("DATA_FORMAT");
			TargetRepository targetRepository; 
			if (data_format.equals("CBOR")) {
            	targetRepository = new TargetCBORRepository(targetDirectory);
			}
			else {
			//Default data format is file
            	targetRepository = new TargetFileRepository(targetDirectory);
			}
            ParameterFile linkStorageConfig = new ParameterFile(linkConfFile);
            Storage linkStorage = new StorageCreator(linkStorageConfig).produce();
            int crawledFreq = config.getParamInt("CRAWLED_REFRESH_FREQUENCY");
            int relevantFreq = config.getParamInt("RELEVANT_REFRESH_FREQUENCY");
            int harvestinfoFreq = config.getParamInt("HARVESTINFO_REFRESH_FREQUENCY");
            int refreshFreq = config.getParamInt("SYNC_REFRESH_FREQUENCY");
            boolean isRefreshSync = config.getParamBoolean("REFRESH_SYNC");
            float relevanceThreshold = config.getParamFloat("RELEVANCE_THRESHOLD");
            TargetMonitor mnt = new TargetMonitor(dataPath + "/data_monitor/crawledpages.csv", 
                                                  dataPath + "/data_monitor/relevantpages.csv", 
                                                  dataPath + "/data_monitor/harvestinfo.csv",
                                                  dataPath + "/data_monitor/nonrelevantpages.csv");
            Storage targetStorage = new TargetStorage(targetClassifier,targetDirectory,targetRepository,
                    linkStorage,config.getParamInt("VISITED_PAGE_LIMIT"),config.getParamBoolean("HARD_FOCUS"),
                    config.getParamBoolean("BIPARTITE"), crawledFreq, relevantFreq, harvestinfoFreq, refreshFreq, isRefreshSync, relevanceThreshold, mnt);

            StorageBinder binder = new StorageBinder(config);
            binder.bind(targetStorage);
        }catch (java.io.IOException ex) {
            ex.printStackTrace();
        }catch (StorageBinderException ex) {
            ex.printStackTrace();
        }catch (StorageFactoryException ex) {
            ex.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
