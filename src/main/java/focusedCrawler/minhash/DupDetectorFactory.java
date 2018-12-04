package focusedCrawler.minhash;

import focusedCrawler.config.Configuration;
import focusedCrawler.dedup.DupDetector;
import focusedCrawler.dedup.HashMapDupDetector;
import focusedCrawler.dedup.ProbabilisticExactDupDetector;
import focusedCrawler.link.classifier.online.DeduplicationOnlineLearning.DuplicationType;
import focusedCrawler.target.TargetStorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DupDetectorFactory {

    private static final Logger logger = LoggerFactory.getLogger(DupDetectorFactory.class);

    public static DupDetector create(Configuration config, String dataPath) {
        DupDetector dupDetector = null;
        TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();
        if (targetStorageConfig.isNearDuplicateDetectionEnabled()) {
            double similarity = targetStorageConfig.getNearDuplicatesSimilarityThreshold();
            DuplicationType duplicationType = targetStorageConfig.getDuplicateDetectorType();
            switch (duplicationType) {
                case NEAR_DUP:
                    dupDetector = new DuplicatePageIndexer.Builder()
                            .setDataPath(dataPath)
                            .setMinJaccardSimilarity(similarity).build();
                    break;
                case PROBABILISTIC_EXACT_DUP:
                    dupDetector = new ProbabilisticExactDupDetector();
                    break;
                case EXACT_DUP:
                default:
                    dupDetector = new HashMapDupDetector();
                    break;
            }
            logger.info("Created duplicates detector: " + duplicationType);
        }
        return dupDetector;
    }

}
