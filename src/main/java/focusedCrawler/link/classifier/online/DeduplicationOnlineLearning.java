package focusedCrawler.link.classifier.online;

import focusedCrawler.dedup.DupDetector;
import focusedCrawler.dedup.DupDetector.DupData;
import focusedCrawler.dedup.rules.RewriteRule;
import focusedCrawler.dedup.rules.Sequence;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;
import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierDeduplication;
import focusedCrawler.link.classifier.LinkClassifierRewriteRules;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.util.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DeduplicationOnlineLearning extends OnlineLearning {

    private static Logger logger = LoggerFactory.getLogger(DeduplicationOnlineLearning.class);

    private FrontierManager frontierManager;

    private static final int NODUP = 0;
    private static final int DUP = 1;
    private static final int[] CLASS_VALUES = new int[] {NODUP, DUP};

    private int maxSamples = 5000;

    private final LearningType learningType;
    private final Learner classifierType;
    private SmileOnlineClassifier<String> classifier;
    private DupDetector dupDetector;

    public DeduplicationOnlineLearning(int learnLimit, boolean async,
            FrontierManager frontierManager, DupDetector dupDetector,
            LearningType learningType, Learner classifierType) {
        super(learnLimit, async, frontierManager);
        this.frontierManager = frontierManager;
        this.dupDetector = dupDetector;
        this.learningType = learningType;
        this.classifierType = classifierType;
    }

    @Override
    public synchronized void execute() throws Exception {
        logger.info("Building outlink classifier...");
        LinkClassifier outlinkClassifier = this.buildModel();

        logger.info("Updating links's scores from frontier using new link classifier...");
        frontierManager.updateOutlinkClassifier(outlinkClassifier);
    }

    public LinkClassifier buildModel() {
        DupData dupData = dupDetector.getDuplicationSample();
        if (learningType == LearningType.RULES) {
            return buildRulesLinkClassifier(dupData);
        } else {
            return buildMachineLearningLinkClassifier(dupData);
        }
    }

    private LinkClassifier buildMachineLearningLinkClassifier(DupData dupData) {

        Sampler<String> duplicates = new Sampler<>(maxSamples);
        Sampler<String> unique = new Sampler<>(maxSamples);
        for (List<String> dupCluster : dupData.duplicates) {
            for (String url : dupCluster) {
                duplicates.sample(url);
            }
        }
        for (String url : dupData.unique) {
            unique.sample(url);
        }

        List<String> trainingData = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        trainingData.addAll(unique.getSamples());
        labels.addAll(createListOfLabels(NODUP, unique.getSamples().size()));

        trainingData.addAll(duplicates.getSamples());
        labels.addAll(createListOfLabels(DUP, duplicates.getSamples().size()));

        int minDocFrequency = (int) Math.min(5, 0.05 * trainingData.size());
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer.Builder()
                .withMinDocFrequency(minDocFrequency)
                .withQuadraticFeatures(true)
                .withMaxFeatures(5000)
                .withWeightType(BinaryTextVectorizer.WeightType.BINARY)
                .build();

        vectorizer.fit(trainingData);
        String[] features = vectorizer.getFeaturesAsArray();

        classifier =
                new SmileOnlineClassifier<>(classifierType, features, CLASS_VALUES, vectorizer);
        classifier.buildModel(trainingData, labels);

        return new LinkClassifierDeduplication(classifier);
    }

    private LinkClassifier buildRulesLinkClassifier(DupData dupData) {
        List<RewriteRule> rules = new ArrayList<>();
        for (List<String> dupCluster : dupData.duplicates) {
            rules.add(new RewriteRule(dupCluster));
        }
        return new LinkClassifierRewriteRules(rules);
    }

    private List<Integer> createListOfLabels(int value, int size) {
        List<Integer> labels = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            labels.add(value);
        }
        return labels;
    }

    public enum DuplicationType {
        EXACT_DUP, PROBABILISTIC_EXACT_DUP, NEAR_DUP
    }

    public enum LearningType {
        CLASSIFIER, RULES
    }

}
