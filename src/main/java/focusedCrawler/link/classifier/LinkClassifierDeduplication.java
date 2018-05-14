package focusedCrawler.link.classifier;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import focusedCrawler.dedup.DupDetector;
import focusedCrawler.dedup.DupDetector.DupData;
import focusedCrawler.dedup.HashMapDupDetector;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;
import focusedCrawler.learn.vectorizer.IndexedVectorizer;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.minhash.DuplicatePageIndexer;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.tokenizers.Tokenizers;
import focusedCrawler.util.Sampler;
import focusedCrawler.util.parser.LinkNeighborhood;

public class LinkClassifierDeduplication implements LinkClassifier {

    private SmileOnlineClassifier<String> classifier;

    public LinkClassifierDeduplication(SmileOnlineClassifier<String> classifier) {
        this.classifier = classifier;
    }

    public LinkRelevance[] classify(Page page) throws LinkClassifierException {

        ParsedData parsedData = page.getParsedData();
        if (parsedData == null) {
            return new LinkRelevance[0];
        }

        LinkNeighborhood[] lns = parsedData.getLinkNeighborhood();
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

    @Override
    public LinkRelevance classify(LinkNeighborhood ln) {
        URL url = ln.getLink();
        double nonDupProbability = classifyURL(url.toString());
        return new LinkRelevance(url, nonDupProbability);
    }

    public double classifyURL(String url) {
        double nonDupProbability;
        if (classifier == null) {
            nonDupProbability = 1.0;
        } else {
            double[] probabilities = classifier.classify(url);
            nonDupProbability = probabilities[0];
        }
//        if (nonDupProbability < 0.5) {
//            System.out.printf("nondup-prob: %.5f dup-prob: %.5f url: %s\n", nonDupProbability,
//                    1.0 - nonDupProbability, url);
//        }
        return nonDupProbability;
    }


}
