package focusedCrawler.target;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.LinkStorage;
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
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.StorageException;

public class TargetStorage {
	
	public static final Logger logger = LoggerFactory.getLogger(TargetStorage.class);

    private TargetRepository targetRepository;
    private LinkStorage linkStorage;
    private TargetClassifier targetClassifier;
    private TargetStorageConfig config;
    private LangDetection langDetector = new LangDetection();
    private TargetStorageMonitor monitor;
    
    public TargetStorage(TargetClassifier targetClassifier,
                         TargetRepository targetRepository, 
                         LinkStorage linkStorage,
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
    public Object insert(Page page) {

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
        } catch (TargetClassifierException | StorageException e) {
            logger.error("Unexpected error while inserting page.", e);
        }
        return null;
    }

    public static TargetStorage create(String configPath, String modelPath, String dataPath,
            String esIndexName, String esTypeName, TargetStorageConfig config,
            LinkStorage linkStorage, MetricsManager metricsManager) throws IOException {
        
        // if one wants to use a classifier
        TargetClassifier targetClassifier = null;
        if (modelPath != null && !modelPath.isEmpty()) {
            targetClassifier = TargetClassifierFactory.create(modelPath);
        }

        TargetRepository targetRepository =
                createTargetRepository(dataPath, esIndexName, esTypeName, config);

        TargetStorageMonitor monitor = null;
        if(metricsManager != null) {
        	monitor = new TargetStorageMonitor(dataPath, metricsManager);
        }else {
        	monitor = new TargetStorageMonitor(dataPath);
        }

        return new TargetStorage(targetClassifier, targetRepository, linkStorage, monitor, config);
    }

    public static TargetRepository createTargetRepository(String dataPath, String esIndexName,
            String esTypeName, TargetStorageConfig config) throws IOException {
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
        return targetRepository;
    }

    private static TargetRepository createRepository(String dataFormat, String dataPath,
            String esIndexName, String esTypeName, TargetStorageConfig config) throws IOException {
        
        Path targetDirectory = Paths.get(dataPath, config.getTargetStorageDirectory());
        boolean compressData = config.getCompressData();
        boolean hashFilename = config.getHashFileName();

        logger.info("Loading repository with data_format={} from {}",
                dataFormat, targetDirectory.toString());

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
