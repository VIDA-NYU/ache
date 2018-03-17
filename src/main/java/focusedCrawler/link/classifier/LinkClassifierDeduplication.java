package focusedCrawler.link.classifier;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.github.jparkie.pdd.ProbabilisticDeDuplicator;
import com.github.jparkie.pdd.impl.RLBSBFDeDuplicator;

import focusedCrawler.learn.classifier.smile.DoubleVectorizer;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.HashingVectorizer;
import focusedCrawler.learn.vectorizer.Vectorizer;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.AlphaNumTokenizer;
import focusedCrawler.util.parser.LinkNeighborhood;

public class LinkClassifierDeduplication implements LinkClassifier {

    private static final int NODUP = 0;
    private static final int DUP = 1;
    private static final int[] CLASS_VALUES = new int[] {NODUP, DUP};

    private HashingVectorizer vectorizer = new HashingVectorizer();

    private SmileOnlineClassifier<String> classifier;
    private DoubleVectorizer<String> wekaVectorizer = new DoubleVectorizer<String>() {
        @Override
        public double[] toInstance(String object) {
            return convertToWekaInstance(object, vectorizer);
        }
    };

    final long NUM_BITS = 128 * 8L * 1024L * 1024L;
    final ProbabilisticDeDuplicator deduper = RLBSBFDeDuplicator.create(NUM_BITS, 0.03D);

    public LinkClassifierDeduplication() {
        Learner classifierImpl = Learner.SVM;
        String[] features = vectorizer.getFeaturesAsArray();
        this.classifier = new SmileOnlineClassifier<>(classifierImpl, features, CLASS_VALUES, wekaVectorizer);
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
        int instanceClass = isDuplicate(md5hex) ? DUP : NODUP;
        classifier.updateModel(url, instanceClass);
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
        double[] probabilities = classifier.classify(url);
        double nonDupProbability = probabilities[0];
        System.out.println("SCORE nondup-prob: " + nonDupProbability + " dup-prob:"
                + probabilities[1] + " url: " + url);
        return new LinkRelevance(ln.getLink(), nonDupProbability);
    }

    public static double[] convertToWekaInstance(String url, Vectorizer vectorizer) {
        double[] inst = new double[vectorizer.numberOfFeatures()];
        List<String> urlTokens = AlphaNumTokenizer.parseTokens(url);
        for (String token : urlTokens) {
            int index = vectorizer.getIndexOfFeature(token);
            if (index >= 0) {
                inst[index] = 1;
            }
        }
        return inst;
    }

}
