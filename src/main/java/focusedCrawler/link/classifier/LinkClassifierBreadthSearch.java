package focusedCrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.classifier.util.Instance;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

public class LinkClassifierBreadthSearch implements LinkClassifier {

    private WrapperNeighborhoodLinks wrapper;
    private String[] attributes;
    private Random randomGenerator;

    public LinkClassifierBreadthSearch(WrapperNeighborhoodLinks wrapper, String[] attribute) {
        this.wrapper = wrapper;
        this.attributes = attribute;
        this.randomGenerator = new Random();
    }

    public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {

        try {
            HashMap<String, Instance> urlWords = wrapper.extractLinks(page, attributes);

            LinkRelevance[] linkRelevance = new LinkRelevance[urlWords.size()];
            
            int count = 0;
            for(String url : urlWords.keySet()) {
                
                int level = (int) (page.getRelevance() / 100);
                double relevance = (level - 1) * 100 + randomGenerator.nextInt(100);
                if (relevance < -1) {
                    relevance = -1;
                }
                linkRelevance[count] = new LinkRelevance(new URL(url), relevance);
                count++;
            }
            return linkRelevance;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new LinkClassifierException(ex.getMessage());
        }
    }

    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        // TODO Auto-generated method stub
        return null;
    }

}
