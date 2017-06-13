package focusedCrawler.target;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.ConfigService;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifierException;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.classifier.TargetRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.repository.ElasticSearchRestTargetRepository;
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

public class TargetStorage extends StorageDefault {
	
	public static final Logger logger = LoggerFactory.getLogger(TargetStorage.class);

    private TargetRepository targetRepository;
    private Storage linkStorage;
    private TargetClassifier targetClassifier;
    private TargetStorageConfig config;
    private LangDetection langDetector = new LangDetection();
    private TargetStorageMonitor monitor;
    
    public TargetStorage(TargetClassifier targetClassifier,
                         TargetRepository targetRepository, 
                         Storage linkStorage,
                       	 TargetStorageMonitor monitor,
                       	 TargetStorageConfig config) {
        
        this.targetClassifier = targetClassifier;
        this.targetRepository = targetRepository;
        this.linkStorage = linkStorage;
        this.config = config;
        this.monitor = monitor;
    }

    /**
     * Inserts a page into the repository.
     */
    @Override
    public Object insert(Object obj) throws StorageException {
        Page page = (Page) obj;

        // non-html pages saved directly
        if (!page.isHtml()) {
            page.setTargetRelevance(TargetRelevance.RELEVANT);
            targetRepository.insert(page);
            logger.info("Non-HTML content found at: "+page.getURL()+" - saved content type: "+page.getContentType());
            monitor.countPage(page, false, 0d);
            return null;
        }

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
                relevance = TargetRelevance.RELEVANT;
            }

            page.setTargetRelevance(relevance);

            if (relevance.isRelevant() || config.isSaveNegativePages()) {
                targetRepository.insert(page);
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

    public static void runServer(String configPath, String modelPath, String dataPath,
                                 String indexName, String typeName, ConfigService config) {
        try {
            TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();

            StorageConfig linkStorageConfig = config.getLinkStorageConfig().getStorageServerConfig();
            Storage linkStorage = new StorageCreator(linkStorageConfig).produce();

            Storage targetStorage = createTargetStorage(configPath, modelPath, dataPath, indexName,
                    typeName, targetStorageConfig, linkStorage);

            StorageBinder binder = new StorageBinder(targetStorageConfig.getStorageServerConfig());
            binder.bind(targetStorage);

        } catch (Exception e) {
            logger.error("Error while starting TargetStorage", e);
        }
    }
    
	public static Storage createTargetStorage(String configPath, String modelPath, String dataPath,
                                              String esIndexName, String esTypeName, 
                                              TargetStorageConfig config, Storage linkStorage)
                                              throws IOException {
        
        //if one wants to use a classifier
        TargetClassifier targetClassifier = null;
        if (modelPath != null && !modelPath.isEmpty()) {
            targetClassifier = TargetClassifierFactory.create(modelPath);
        }

        TargetRepository targetRepository = createTargetRepository(dataPath, esIndexName,
                                                                   esTypeName, config);
        
        TargetStorageMonitor monitor = new TargetStorageMonitor(dataPath);
        
        Storage targetStorage = new TargetStorage(targetClassifier, targetRepository,
                                                  linkStorage, monitor, config);
        
        return targetStorage;
    }

    private static TargetRepository createTargetRepository(String dataPath,
                                                           String esIndexName,
                                                           String esTypeName,
                                                           TargetStorageConfig config) {
        
        Path targetDirectory = Paths.get(dataPath, config.getTargetStorageDirectory());
        
        String dataFormat = config.getDataFormat();
        boolean compressData = config.getCompressData();
        boolean hashFilename = config.getHashFileName();

        logger.info("Using DATA_FORMAT: " + dataFormat);
        switch (dataFormat) {
            case "FILES":
                return new FilesTargetRepository(targetDirectory, config.getMaxFileSize());
            case "FILESYSTEM_JSON":
                return new FileSystemTargetRepository(targetDirectory, DataFormat.JSON,
                                                      hashFilename, compressData);
            case "FILESYSTEM_CBOR":
                return new FileSystemTargetRepository(targetDirectory, DataFormat.CBOR,
                                                      hashFilename, compressData);
            case "FILESYSTEM_HTML":
                return new FileSystemTargetRepository(targetDirectory, DataFormat.HTML,
                                                      hashFilename, compressData);
            case "ELASTICSEARCH":
                if (esIndexName == null || esIndexName.isEmpty()) {
                    throw new IllegalArgumentException("ElasticSearch index name not provided!");
                }
                if (esTypeName == null || esTypeName.isEmpty()) {
                    esTypeName = "page";
                }
                ElasticSearchConfig esconfig = config.getElasticSearchConfig();
                if (esconfig.getRestApiHosts() == null) {
                    return new ElasticSearchTargetRepository(esconfig, esIndexName, esTypeName);
                } else {
                    return new ElasticSearchRestTargetRepository(esconfig, esIndexName, esTypeName);
                }
            default:
                throw new IllegalArgumentException("Invalid data format provided: " + dataFormat);
        }
    }

    public void close() {
        targetRepository.close();
        monitor.close();
    }

}
