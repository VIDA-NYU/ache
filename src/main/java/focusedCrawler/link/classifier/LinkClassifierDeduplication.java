package focusedCrawler.link.classifier;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.github.jparkie.pdd.ProbabilisticDeDuplicator;
import com.github.jparkie.pdd.impl.RLBSBFDeDuplicator;

import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.HashingVectorizer;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.tokenizers.Tokenizers;
import focusedCrawler.util.Sampler;
import focusedCrawler.util.parser.LinkNeighborhood;

public class LinkClassifierDeduplication implements LinkClassifier {

    private static final int NODUP = 0;
    private static final int DUP = 1;
    private static final int[] CLASS_VALUES = new int[] {NODUP, DUP};

    private HashingVectorizer hashingVectorizer = new HashingVectorizer(Tokenizers.alphaNumeric());
    private SmileOnlineClassifier<String> classifier;
    
    private int maxSamples = 500;
    private int instancesCounter = 0;
    private int buildModelThreshold = 50;
    
    private Sampler<String> duplicates = new Sampler<>(maxSamples);
    private Sampler<String> unique = new Sampler<>(maxSamples);
    
    final long NUM_BITS = 128 * 8L * 1024L * 1024L;
    final ProbabilisticDeDuplicator deduper = RLBSBFDeDuplicator.create(NUM_BITS, 0.03D);

    public LinkClassifierDeduplication() {
        Learner classifierType = Learner.SVM;
        String[] features = hashingVectorizer.getFeaturesAsArray();
        this.classifier = new SmileOnlineClassifier<>(classifierType, features, CLASS_VALUES, hashingVectorizer);
        this.hashingVectorizer = new HashingVectorizer();
    }

    public LinkRelevance[] classify(Page page) throws LinkClassifierException {

        updateClassifierModel(page);

        LinkNeighborhood[] lns = page.getParsedData().getLinkNeighborhood();
        LinkNeighborhood ln = null;
        try {
            LinkRelevance[] linkRelevance = new LinkRelevance[lns.length];
            for (int i = 0; i < lns.length; i++) {
                ln = lns[i];
                linkRelevance[i] = classify(ln);
            }
            return linkRelevance;
        } catch (Exception ex) {
            throw new LinkClassifierException("Failed to classify link [" + ln.getLink().toString()
                    + "] from page: " + page.getURL().toString(), ex);
        }
    }

    private void updateClassifierModel(Page page) {
        String url = page.getURL().toString();
        byte[] content = page.getContent();
        updateClassifier(url, content);
    }

    private void updateClassifier(String url, byte[] content) {
        String md5hex = DigestUtils.md5Hex(content);
        int instanceClass = isDuplicate(md5hex) ? DUP : NODUP;
        if (instanceClass == DUP) {
            duplicates.sample(url);
        } else {
            unique.sample(url);
        }
        if (instancesCounter % buildModelThreshold == 0) {
            buildModel();
        }
        //classifier.updateModel(url, instanceClass);
    }

    public void buildModel() {
        List<String> trainingData = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        trainingData.addAll(unique.getSamples());
        for (int i = 0; i < unique.getSamples().size(); i++) {
            labels.add(NODUP);
        }
        trainingData.addAll(duplicates.getSamples());
        for (int i = 0; i < duplicates.getSamples().size(); i++) {
            labels.add(DUP);
        }
        classifier.buildModel(trainingData, labels);
    }

    private boolean isDuplicate(String fingerprint) {
        byte[] bytes = fingerprint.getBytes(StandardCharsets.UTF_8);
        // classifies and updates seen fingerprints
        boolean isDistinct = deduper.classifyDistinct(bytes);
        return !isDistinct;
    }

    @Override
    public LinkRelevance classify(LinkNeighborhood ln) {
        URL url = ln.getLink();
        double nonDupProbability = classifyURL(url.toString());
        return new LinkRelevance(url, nonDupProbability);
    }

    public double classifyURL(String url) {
        double[] probabilities = classifier.classify(url);
        double nonDupProbability = probabilities[0];
        System.out.printf("nondup-prob: %.5f dup-prob: %.5f url: %s\n",
                nonDupProbability , probabilities[1], url);
        return nonDupProbability;
    }

}
