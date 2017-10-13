package focusedCrawler.target;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.Configuration;
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
import focusedCrawler.target.repository.MultipleTargetRepositories;
import focusedCrawler.target.repository.TargetRepository;
import focusedCrawler.target.repository.WarcTargetRepository;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import focusedCrawler.target.repository.kafka.KafkaTargetRepository;
import focusedCrawler.util.CommunicationException;
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.MetricsManager;
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
            page.setTargetRelevance(TargetRelevance.IRRELEVANT);
            targetRepository.insert(page);
            monitor.countPage(page, false, 0.0d);
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
                                 String indexName, String typeName, Configuration config) {
        try {
            TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();

            StorageConfig linkStorageConfig = config.getLinkStorageConfig().getStorageServerConfig();
            Storage linkStorage = new StorageCreator(linkStorageConfig).produce();

            Storage targetStorage = createTargetStorage(configPath, modelPath, dataPath, indexName,
                    typeName, targetStorageConfig, linkStorage, null);

            StorageBinder binder = new StorageBinder(targetStorageConfig.getStorageServerConfig());
            binder.bind(targetStorage);

        } catch (Exception e) {
            logger.error("Error while starting TargetStorage", e);
        }
    }
    
	public static Storage createTargetStorage(String configPath, String modelPath, String dataPath,
                                              String esIndexName, String esTypeName, 
                                              TargetStorageConfig config, Storage linkStorage, MetricsManager metricsManager)
                                              throws IOException {
        
        // if one wants to use a classifier
        TargetClassifier targetClassifier = null;
        if (modelPath != null && !modelPath.isEmpty()) {
            targetClassifier = TargetClassifierFactory.create(modelPath);
        }

        List<String> dataFormats = config.getDataFormats();

        List<TargetRepository> repositories = new ArrayList<>();
        for (String dataFormat : dataFormats) {
            TargetRepository targetRepository =
                    createRepository(dataFormat, dataPath, esIndexName, esTypeName, config);
            repositories.add(targetRepository);
        }

        TargetRepository targetRepository = null;
        if (repositories.size() == 1) {
            targetRepository = repositories.get(0);
        } else if (repositories.size() > 1) {
            // create pool of repositories
            targetRepository = new MultipleTargetRepositories(repositories);
        } else {
            throw new IllegalArgumentException("No valid data formats configured.");
        }

        TargetStorageMonitor monitor = null;
        if(metricsManager != null) {
        	monitor = new TargetStorageMonitor(dataPath, metricsManager);
        }else {
        	monitor = new TargetStorageMonitor(dataPath);
        }

        Storage targetStorage = new TargetStorage(targetClassifier, targetRepository,
                                                  linkStorage, monitor, config);
        
        return targetStorage;
    }

    private static TargetRepository createRepository(String dataFormat, String dataPath,
            String esIndexName, String esTypeName, TargetStorageConfig config) throws IOException {
        
        Path targetDirectory = Paths.get(dataPath, config.getTargetStorageDirectory());
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
            case "WARC":
                return new WarcTargetRepository(targetDirectory, config.getWarcMaxFileSize(),
                                                config.getCompressWarc());
            case "KAFKA":
                return new KafkaTargetRepository(config.getKafkaConfig());
            case "ELASTICSEARCH":
                ElasticSearchConfig esconfig = config.getElasticSearchConfig();
                if (esIndexName != null && !esIndexName.isEmpty()) {
                    esconfig.setIndexName(esIndexName);
                }
                if (esTypeName != null && !esTypeName.isEmpty()) {
                    esconfig.setTypeName(esTypeName);
                }
                if (esconfig.getRestApiHosts() == null) {
                    return new ElasticSearchTargetRepository(esconfig);
                } else {
                    return new ElasticSearchRestTargetRepository(esconfig);
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
