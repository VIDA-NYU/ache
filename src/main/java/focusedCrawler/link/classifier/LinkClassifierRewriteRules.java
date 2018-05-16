package focusedCrawler.link.classifier;

import focusedCrawler.dedup.rules.UrlAlignment;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.LinkNeighborhood;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LinkClassifierRewriteRules implements LinkClassifier {

    private SmileOnlineClassifier<String> classifier;
    private List<UrlAlignment.RewriteRule> rules;

    public LinkClassifierRewriteRules(SmileOnlineClassifier<String> classifier) {
        this.classifier = classifier;
    }

    public LinkClassifierRewriteRules(List<UrlAlignment.RewriteRule> rules) {
        this.rules = rules;
    }

    @Override
    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        ParsedData parsedData = page.getParsedData();
        if (parsedData == null) {
            return new LinkRelevance[0];
        }

        URL[] lns = parsedData.getLinks();
        LinkNeighborhood ln = null;
        try {
            List<LinkRelevance> links = new ArrayList<>();
            for (int i = 0; i < lns.length; i++) {
                for (UrlAlignment.RewriteRule rule : rules) {
                    String url = lns[i].toString();
                    double relevance = 1.0d;
                    if (rule.matches(url)) {
//                        url = rule.rewrite(url);
                        relevance = 0.0;
                    }
                    links.add(new LinkRelevance(url, relevance));
                }
            }
            return links.toArray(new LinkRelevance[links.size()]);
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
