package focusedCrawler.link.classifier.online;

import focusedCrawler.dedup.DupDetector;
import focusedCrawler.dedup.DupDetector.DupData;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;
import focusedCrawler.learn.vectorizer.IndexedVectorizer;
import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierDeduplication;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.target.model.Page;
import focusedCrawler.tokenizers.Tokenizers;
import focusedCrawler.util.Sampler;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeduplicationOnlineLearning extends OnlineLearning {

    private static Logger logger = LoggerFactory.getLogger(DeduplicationOnlineLearning.class);

    private FrontierManager frontierManager;

    private static final int NODUP = 0;
    private static final int DUP = 1;
    private static final int[] CLASS_VALUES = new int[] {NODUP, DUP};

    private int totalDups = 0;
    private int maxSamples = 500;
    private int instancesCounter = 0;
    private int buildModelThreshold = 50;

    private Learner classifierType = Learner.SVM;
    private IndexedVectorizer vectorizer;
    private SmileOnlineClassifier<String> classifier;
    private DupDetector dupDetector;

    public DeduplicationOnlineLearning(int learnLimit, boolean async,
            FrontierManager frontierManager, DupDetector dupDetector,
            Learner classifierType) {
        super(learnLimit, async, frontierManager);
        this.frontierManager = frontierManager;
        this.dupDetector = dupDetector;
        this.classifierType = classifierType;
    }

    @Override
    public synchronized void execute() throws Exception {
        logger.info("Building outlink classifier...");
        LinkClassifier outlinkClassifier = this.buildModel();

        logger.info("Updating links's scores from frontier using new link classifier...");
        frontierManager.updateOutlinkClassifier(outlinkClassifier);
    }

    @Override
    public void pageCrawledEvent(Page page) {
        updateClassifierModel(page);
    }

    private void updateClassifierModel(Page page) {
//        String url = page.getURL().toString();
//        byte[] content = page.getParsedData().getCleanText().getBytes();
//        boolean isDup = dupDetector.detectAndIndex(url, content);
//        if (isDup) {
//        if (page.isNearDuplicate()) {
//            System.out.println("DUP: " + DigestUtils.md5Hex(content) + " URL: " + url);
//            totalDups++;
//        }
//        instancesCounter++;
//        double dupsPercent = 100 * totalDups / (double) instancesCounter;
//        System.out.printf("total: %d dups-percent: %.4f %%\n", instancesCounter, dupsPercent);
    }

    public LinkClassifier buildModel() {

        Sampler<String> duplicates = new Sampler<>(maxSamples);
        Sampler<String> unique = new Sampler<>(maxSamples);
        DupData dupData = dupDetector.getDuplicationSample();
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

        vectorizer = new BinaryTextVectorizer(Tokenizers.alphaNumeric(), true);
        vectorizer.fit(trainingData);
        String[] features = vectorizer.getFeaturesAsArray();

        classifier =
                new SmileOnlineClassifier<>(classifierType, features, CLASS_VALUES, vectorizer);
        classifier.buildModel(trainingData, labels);

        return new LinkClassifierDeduplication(classifier);
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

}
