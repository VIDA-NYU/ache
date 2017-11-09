package focusedCrawler.link.classifier;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.github.jparkie.pdd.ProbabilisticDeDuplicator;
import com.github.jparkie.pdd.impl.RLBSBFDeDuplicator;

import focusedCrawler.learn.classifier.weka.WekaOnlineClassifier;
import focusedCrawler.learn.classifier.weka.WekaVectorizer;
import focusedCrawler.learn.vectorizer.HashingVectorizer;
import focusedCrawler.learn.vectorizer.Vectorizer;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.AlphaNumTokenizer;
import focusedCrawler.util.parser.LinkNeighborhood;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.SparseInstance;

public class LinkClassifierDeduplication implements LinkClassifier {

    private static final String NODUP = "nodup";
    private static final String DUP = "dup";
    private static final String[] CLASS_VALUES = new String[] {NODUP, DUP};

    private HashingVectorizer vectorizer = new HashingVectorizer();

    private WekaOnlineClassifier<String> wekaClassifier;
    private WekaVectorizer<String> wekaVectorizer = new WekaVectorizer<String>() {
        @Override
        public Instance toInstance(String object) {
            return convertToWekaInstance(object, vectorizer);
        }
    };

    final long NUM_BITS = 128 * 8L * 1024L * 1024L;
    final ProbabilisticDeDuplicator deduper = RLBSBFDeDuplicator.create(NUM_BITS, 0.03D);

    public LinkClassifierDeduplication() {
        Classifier classifierImpl = new NaiveBayesUpdateable();
        String[] features = vectorizer.getFeaturesAsArray();
        this.wekaClassifier =
                new WekaOnlineClassifier<>(classifierImpl, features, CLASS_VALUES, wekaVectorizer);
        this.vectorizer = new HashingVectorizer();
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
        String md5hex = DigestUtils.md5Hex(page.getContent());
        String url = page.getURL().toString();
        String instanceClass = isDuplicate(md5hex) ? DUP : NODUP;
        wekaClassifier.updateModel(url, instanceClass);
    }

    private boolean isDuplicate(String fingerprint) {
        byte[] bytes = fingerprint.getBytes(StandardCharsets.UTF_8);
        // classifies and updates seen fingerprints
        boolean isDistinct = deduper.classifyDistinct(bytes);
        return !isDistinct;
    }

    @Override
    public LinkRelevance classify(LinkNeighborhood ln) {
        String url = ln.getLink().toString();
        double[] probabilities = wekaClassifier.classify(url);
        double nonDupProbability = probabilities[0];
        System.out.println("SCORE nondup-prob: " + nonDupProbability + " dup-prob:"
                + probabilities[1] + " url: " + url);
        return new LinkRelevance(ln.getLink(), nonDupProbability);
    }

    public static Instance convertToWekaInstance(String url, Vectorizer vectorizer) {
        List<String> urlTokens = AlphaNumTokenizer.parseTokens(url);
        Instance inst = new SparseInstance(vectorizer.numberOfFeatures() + 1);
        for (String token : urlTokens) {
            int index = vectorizer.getIndexOfFeature(token);
            if (index >= 0) {
                inst.setValue(index, 1);
            }
        }
        return inst;
    }

}
