package focusedCrawler.target;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.TargetStorageConfig;
import focusedCrawler.target.classifier.ClassifierFactory;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifierException;
import focusedCrawler.target.detector.RegexBasedDetector;
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.dashboard.TargetMonitor;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.StorageFactoryException;
import focusedCrawler.util.storage.distribution.StorageBinder;
import focusedCrawler.util.storage.distribution.StorageCreator;

/**
 * This class runs a socket server responsible to store pages coming from the crawler client.
 * @author lbarbosa
 */
public class TargetStorage extends StorageDefault {
	
	public static final Logger logger = LoggerFactory.getLogger(TargetStorage.class);

    private TargetRepository targetRepository;
    private TargetRepository negativeRepository;
    private Storage linkStorage;
    private TargetClassifier targetClassifier;
    private RegexBasedDetector regexDetector;
    private TargetStorageConfig config;

    private LangDetection langDetect;
    private TargetMonitor monitor;
    
    public TargetStorage(TargetClassifier targetClassifier,
                         TargetRepository targetRepository, 
                         Storage linkStorage,
                       	 TargetMonitor monitor,
                       	 TargetRepository negativeRepository,
                       	 TargetStorageConfig config) {
        
        this.targetClassifier = targetClassifier;
        this.targetRepository = targetRepository;
        this.negativeRepository = negativeRepository;
        this.linkStorage = linkStorage;
        this.config = config;
        this.monitor = monitor;
        
        this.langDetect = new LangDetection();
        //this.langDetect.init("libs/profiles/");//This is hard coded, should be fixed
        
        ClassLoader cl = this.getClass().getClassLoader();
        try {
			this.langDetect.init(cl.getResource("profiles").toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			logger.error(" Unable to load profiles! ",e);;
			e.printStackTrace();
		}
        
        
        
        //if one wants to use regex based classifier
        if (config.isUseRegex()) {
            this.regexDetector = new RegexBasedDetector(config.getRegex());
        }
    }

    /**
     * Inserts a page into the repository. 
     */
    public synchronized Object insert(Object obj) throws StorageException {
        Page page = (Page) obj;
        
		//Only accept English
    	if (this.langDetect.detect_page(page) == false){
    		logger.info("Ignoring non-English page: " + page.getIdentifier());
      		return null;
    	}
        
    	boolean isRelevant;
    	double prob;
        try {
        ///////////////////IF USING REGEX INSTEAD OF CLASSIFIER/////////////////////////////
          if(regexDetector != null){
              isRelevant = regexDetector.detect(page);
              if (isRelevant) {
                  prob = 1.0;
                  page.setRelevance(prob);
                  logger.info("\n> PROCESSING: " + page.getIdentifier() + "\n> PROB:" + prob);
                  targetRepository.insert(page);
                  linkStorage.insert(page);
              }
              else{
                  prob = 0.0;
                  page.setRelevance(prob);
                  
                  if (config.isSaveNegativePages()){
                      negativeRepository.insert(page);
                  }
              }
          }
	      ////////////////END USING REGEX////////////////////////////////////////////////
          else {
          ////////////////IF USING CLASSIFIER////////////////////////////////////////
          if(targetClassifier != null){
              prob = targetClassifier.distributionForInstance(page)[0];
              page.setRelevance(prob);
              
              logger.info("\n> PROCESSING: " + page.getIdentifier() +
                          "\n> PROB:" + prob);
              
              isRelevant = prob > config.getRelevanceThreshold();
              
              if(isRelevant){
                  targetRepository.insert(page);
                  if(config.isBipartite()){
					  //set the page is as authority if using backlinks
                      page.setAuth(true);
                  }
                  linkStorage.insert(page);
              } else {
                  if (config.isSaveNegativePages()){
                      page.setRelevance(prob);
                      negativeRepository.insert(page);
                  }
                  if(!config.isHardFocus()){
                      if(config.isBipartite()){
                          if(page.isHub()){
                              linkStorage.insert(page);
                          }
                      } else{
                          linkStorage.insert(page);
                      }
                  }
              }
          //////////////////END USING CLASSIFIER//////////////////////////////////////////
          } else{
                  isRelevant = true;
                  prob = 1.0;
                  page.setRelevance(prob);
                  page.setAuth(true);
                  logger.info("\n> PROCESSING: " + page.getIdentifier());
                  linkStorage.insert(page);
                  targetRepository.insert(page, monitor.getTotalOfPages());
          }
		  }
          
          monitor.countPage(page, isRelevant, prob);
          
          if(monitor.getTotalOfPages() > config.getVisitedPageLimit()){
              logger.info("Visited page limit exceeded. Exiting crawler. pagelimit="+config.getVisitedPageLimit());
              System.exit(0);
          }
        }
        catch (CommunicationException ex) {
            logger.error("Communication error while inserting.", ex);
            throw new StorageException(ex.getMessage(), ex);
        } catch (TargetClassifierException tce) {
        	logger.error("Classification error while inserting.", tce);
        }
        return null;
    }

    public static void main(String[] args) {
        try{
            String configPath = args[0];
            String modelPath = args[1];
            String dataPath = args[2];
            
            String targetConfFile = configPath + "/target_storage/target_storage.cfg";
            ParameterFile targetStorageConfig = new ParameterFile(targetConfFile);
            
            String linkConfFile = configPath + "/link_storage/link_storage.cfg";
            ParameterFile linkStorageConfig = new ParameterFile(linkConfFile);
            
            Storage linkStorage = new StorageCreator(linkStorageConfig).produce();
            
            Storage targetStorage = createTargetStorage(configPath, modelPath, dataPath, targetStorageConfig, linkStorage);

            StorageBinder binder = new StorageBinder(targetStorageConfig);
            binder.bind(targetStorage);
            
        } catch (Exception e) {
        	logger.error("Error while starting TargetStorage", e);
        }
    }
    
    public static Storage createTargetStorage(String configPath,
                                              String modelPath,
                                              String dataPath,
                                              ParameterFile params,
                                              Storage linkStorage)
                                              throws IOException, StorageFactoryException {
        
        TargetStorageConfig config = new TargetStorageConfig(params);
        
        //if one wants to use a classifier
        TargetClassifier targetClassifier = null;
        if(config.isUseClassifier()){
            targetClassifier = ClassifierFactory.create(modelPath, configPath+"/stoplist.txt");
        }

        String targetDirectory = dataPath + "/" + config.getTargetStorageDirectory();
        String negativeDirectory = dataPath + "/" + config.getNegativeStorageDirectory();
        
        TargetRepository targetRepository; 
        TargetRepository negativeRepository;
        
        String dataFormat = config.getDataFormat();
        if (dataFormat.equals("CBOR")) {
			targetRepository = new TargetCBORRepository(targetDirectory, config.getTargetDomain());
			negativeRepository = new TargetCBORRepository(negativeDirectory, config.getTargetDomain());
        }
        else {
        	//Default data format is file
        	targetRepository = new TargetFileRepository(targetDirectory);
        	negativeRepository = new TargetFileRepository(negativeDirectory);
        }
        
        TargetMonitor monitor = new TargetMonitor(dataPath, config);
        
        Storage targetStorage = new TargetStorage(targetClassifier, targetRepository, linkStorage, 
                                                  monitor, negativeRepository, config);
        
        return targetStorage;
    }

}
