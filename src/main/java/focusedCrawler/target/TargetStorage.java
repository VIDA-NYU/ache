package focusedCrawler.target;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.MissingArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.ConfigService;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifier.TargetRelevance;
import focusedCrawler.target.classifier.TargetClassifierException;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.repository.ElasticSearchTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.target.repository.TargetRepository;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.CommunicationException;
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageConfig;
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
    private TargetStorageMonitor monitor;
    
    public TargetStorage(TargetClassifier targetClassifier,
                         TargetRepository targetRepository, 
                         Storage linkStorage,
                       	 TargetStorageMonitor monitor,
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
    public Object insert(Object obj) throws StorageException {
        Page page = (Page) obj;

        if (config.isEnglishLanguageDetectionEnabled()) {
            // Only accept English language
            if (this.langDetector.isEnglish(page) == false) {
                logger.info("Ignoring non-English page: " + page.getURL().toString());
                return null;
            }
        }

        try {
            TargetRelevance relevance;
            if (targetClassifier != null) {
                relevance = targetClassifier.classify(page);
            } else {
                relevance = new TargetRelevance(true, 1.0d);
            }

            page.setTargetRelevance(relevance);

            if (relevance.isRelevant()) {
                targetRepository.insert(page);
            } else if (config.isSaveNegativePages()) {
                negativeRepository.insert(page);
            }
            
            if (relevance.isRelevant()) {
                if (config.isBipartite()) {
                    // set the page is as authority if using backlinks
                    page.setAuth(true);
                }
                linkStorage.insert(page);
            } else {
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

    public static void runServer(String configPath, String modelPath, String dataPath, String indexName, ConfigService config) {
        try{
            
            TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();
            
            StorageConfig linkStorageConfig = config.getLinkStorageConfig().getStorageServerConfig();
            Storage linkStorage = new StorageCreator(linkStorageConfig).produce();
            
            Storage targetStorage = createTargetStorage(configPath, modelPath, dataPath, indexName, targetStorageConfig, linkStorage);

            StorageBinder binder = new StorageBinder(targetStorageConfig.getStorageServerConfig());
            binder.bind(targetStorage);
            
        } catch (Exception e) {
        	logger.error("Error while starting TargetStorage", e);
        }
    }
    
	public static Storage createTargetStorage(String configPath,
                                              String modelPath,
                                              String dataPath,
                                              String indexName,
                                              TargetStorageConfig config,
                                              Storage linkStorage)
                                              throws IOException,
                                                     MissingArgumentException {
        
        //if one wants to use a classifier
        TargetClassifier targetClassifier = null;
        if(config.isUseClassifier()){
            targetClassifier = TargetClassifierFactory.create(modelPath);
        }

        Path targetDirectory = Paths.get(dataPath, config.getTargetStorageDirectory());
        Path negativeDirectory = Paths.get(dataPath, config.getNegativeStorageDirectory());
        
        TargetRepository targetRepository; 
        TargetRepository negativeRepository;
        
        String dataFormat = config.getDataFormat();
        boolean compressData = config.getCompressData();
        
        logger.info("Using DATA_FORMAT: "+dataFormat);
        if(dataFormat.equals("FILES")) {
            targetRepository = new FilesTargetRepository(targetDirectory, config.getMaxFileSize());
            negativeRepository = new FilesTargetRepository(negativeDirectory, config.getMaxFileSize());
        } else if(dataFormat.equals("FILESYSTEM_JSON")) {
        	boolean hashFilename = config.getHashFileName();
            targetRepository = new FileSystemTargetRepository(targetDirectory, DataFormat.JSON, hashFilename, compressData);
			negativeRepository = new FileSystemTargetRepository(negativeDirectory, DataFormat.JSON, hashFilename, compressData);
        }
        else if (dataFormat.equals("FILESYSTEM_CBOR")) {
            boolean hashFilename = config.getHashFileName();
        	targetRepository = new FileSystemTargetRepository(targetDirectory, DataFormat.CBOR, hashFilename, compressData);
        	negativeRepository = new FileSystemTargetRepository(negativeDirectory, DataFormat.CBOR, hashFilename, compressData);
        }
        else if(dataFormat.equals("FILESYSTEM_HTML")) {
            boolean hashFilename = config.getHashFileName();
        	targetRepository = new FileSystemTargetRepository(targetDirectory, DataFormat.HTML, hashFilename, compressData);
        	negativeRepository = new FileSystemTargetRepository(negativeDirectory, DataFormat.HTML, hashFilename, compressData);
        }
        else if(dataFormat.equals("ELASTICSEARCH")) {
        	if(indexName == null) {
        		throw new MissingArgumentException("ElasticSearch index name not provided!");
        	}
        	ElasticSearchConfig esconfig = config.getElasticSearchConfig();
        	targetRepository = new ElasticSearchTargetRepository(esconfig, indexName, "target");
        	negativeRepository = new ElasticSearchTargetRepository(esconfig, indexName, "negative");
        }
        else {
        	throw new IllegalArgumentException("Invalid data format provided: "+dataFormat);
        }
        
        TargetStorageMonitor monitor = new TargetStorageMonitor(dataPath);
        
        Storage targetStorage = new TargetStorage(targetClassifier, targetRepository, linkStorage, 
                                                  monitor, negativeRepository, config);
        
        return targetStorage;
    }

    public void close() {
        targetRepository.close();
        negativeRepository.close();
        monitor.close();
    }

}
