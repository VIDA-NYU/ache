package focusedCrawler.target;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.MissingArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.TargetStorageConfig;
import focusedCrawler.target.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifier.TargetRelevance;
import focusedCrawler.target.classifier.TargetClassifierException;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.dashboard.TargetMonitor;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
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
    private TargetStorageConfig config;
    private LangDetection langDetector = new LangDetection();
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
    }

    /**
     * Inserts a page into the repository.
     */
    public synchronized Object insert(Object obj) throws StorageException {
        Page page = (Page) obj;

        if (config.isEnglishLanguageDetectionEnabled()) {
            // Only accept English language
            if (this.langDetector.isEnglish(page) == false) {
                logger.info("Ignoring non-English page: " + page.getIdentifier());
                return null;
            }
        }

        try {
            TargetRelevance relevance;
            if (targetClassifier != null) {
                relevance = targetClassifier.classify(page);
                logger.info("\n> PROCESSING: " + page.getIdentifier() +
                            "\n> PROB:" + relevance.getRelevance());
            } else {
                relevance = new TargetRelevance(true, 1.0d);
            }

            page.setRelevance(relevance.getRelevance());

            if (relevance.isRelevant()) {
                if (config.isBipartite()) {
                    // set the page is as authority if using backlinks
                    page.setAuth(true);
                }
                targetRepository.insert(page);
                linkStorage.insert(page);
            } else {
                if (config.isSaveNegativePages()) {
                    negativeRepository.insert(page);
                }
                if (!config.isHardFocus()) {
                    if (config.isBipartite()) {
                        if (page.isHub()) {
                            linkStorage.insert(page);
                        }
                    } else {
                        linkStorage.insert(page);
                    }
                }
            }

            monitor.countPage(page, relevance.isRelevant(), relevance.getRelevance());

            if (monitor.getTotalOfPages() > config.getVisitedPageLimit()) {
                logger.info("Visited page limit exceeded. Exiting crawler. pagelimit=" + config.getVisitedPageLimit());
                System.exit(0);
            }
        } catch (CommunicationException ex) {
            logger.error("Communication error while inserting.", ex);
            throw new StorageException(ex.getMessage(), ex);
        } catch (TargetClassifierException tce) {
            logger.error("Classification error while inserting.", tce);
        }
        return null;
    }

    public static void run(String configPath, String modelPath, String dataPath, String indexName) {
        try{
            Path targetConf = Paths.get(configPath, "/target_storage/target_storage.cfg");
            ParameterFile targetStorageConfig = new ParameterFile(targetConf.toFile());
            
            Path linkConf = Paths.get(configPath, "/link_storage/link_storage.cfg");
            ParameterFile linkStorageConfig = new ParameterFile(linkConf.toFile());
            
            Storage linkStorage = new StorageCreator(linkStorageConfig).produce();
            
            Storage targetStorage = createTargetStorage(configPath, modelPath, dataPath, indexName, targetStorageConfig, linkStorage);

            StorageBinder binder = new StorageBinder(targetStorageConfig);
            binder.bind(targetStorage);
            
        } catch (Exception e) {
        	logger.error("Error while starting TargetStorage", e);
        }
    }
    
	@SuppressWarnings("deprecation")
	public static Storage createTargetStorage(String configPath,
                                              String modelPath,
                                              String dataPath,
                                              String indexName,
                                              ParameterFile params,
                                              Storage linkStorage)
                                              throws IOException,
                                                     MissingArgumentException {
        
        TargetStorageConfig config = new TargetStorageConfig(params);
        
        //if one wants to use a classifier
        TargetClassifier targetClassifier = null;
        if(config.isUseClassifier()){
            targetClassifier = TargetClassifierFactory.create(
                    modelPath, config.getRelevanceThreshold(), configPath+"/stoplist.txt");
        }

        Path targetDirectory = Paths.get(dataPath, config.getTargetStorageDirectory());
        Path negativeDirectory = Paths.get(dataPath, config.getNegativeStorageDirectory());
        
        TargetRepository targetRepository; 
        TargetRepository negativeRepository;
        
        String dataFormat = config.getDataFormat();
        logger.info("Using DATA_FORMAT: "+dataFormat);
        if(dataFormat.equals("FILESYSTEM_JSON")) {
        	targetRepository = new FileSystemTargetRepository(targetDirectory, DataFormat.JSON);
			negativeRepository = new FileSystemTargetRepository(negativeDirectory, DataFormat.JSON);
        }
        else if (dataFormat.equals("FILESYSTEM_CBOR")) {
        	targetRepository = new FileSystemTargetRepository(targetDirectory, DataFormat.CBOR);
        	negativeRepository = new FileSystemTargetRepository(negativeDirectory, DataFormat.CBOR);
        }
        else if(dataFormat.equals("FILESYSTEM_HTML")) {
        	targetRepository = new FileSystemTargetRepository(targetDirectory, DataFormat.FILE);
        	negativeRepository = new FileSystemTargetRepository(negativeDirectory, DataFormat.FILE);
        }
        else if(dataFormat.equals("ELASTICSEARCH")) {
        	if(indexName == null) {
        		throw new MissingArgumentException("ElasticSearch index name not provided!");
        	}
        	ElasticSearchConfig esconfig = config.getElasticSearchConfig();
        	targetRepository = new TargetElasticSearchRepository(esconfig, indexName, "target");
        	negativeRepository = new TargetElasticSearchRepository(esconfig, indexName, "negative");
        }
        else if (dataFormat.equals("CBOR")) {
			targetRepository = new TargetCBORRepository(targetDirectory);
			negativeRepository = new TargetCBORRepository(negativeDirectory);
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
