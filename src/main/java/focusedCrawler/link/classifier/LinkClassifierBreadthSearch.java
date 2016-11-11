package focusedCrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.parser.LinkNeighborhood;

public class LinkClassifierBreadthSearch implements LinkClassifier {

    private LinkNeighborhoodWrapper wrapper;
    private String[] attributes;
    private Random randomGenerator;

    public LinkClassifierBreadthSearch(LinkNeighborhoodWrapper wrapper, String[] attribute) {
        this.wrapper = wrapper;
        this.attributes = attribute;
        this.randomGenerator = new Random();
    }

    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        try {
            HashMap<String, Instance> urlWords = wrapper.extractLinks(page, attributes);

            List<LinkRelevance> links = new ArrayList<>();
            for(String url : urlWords.keySet()) {
                
                int level = (int) (page.getLinkRelevance().getRelevance() / 100);
                double relevance = (level - 1) * 100 + randomGenerator.nextInt(100);
                if (relevance < -1) {
                    relevance = -1;
                }
                
                links.add(new LinkRelevance(new URL(url), relevance));
            }
            return (LinkRelevance[]) links.toArray(new LinkRelevance[links.size()]);
        } catch (MalformedURLException e) {
            throw new LinkClassifierException(e.getMessage(), e);
        }
    }

    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        // TODO Auto-generated method stub
        return null;
    }

}
