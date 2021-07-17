package achecrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;

public class LinkClassifierBreadthSearch implements LinkClassifier {

    private Random randomGenerator;

    public LinkClassifierBreadthSearch() {
        this.randomGenerator = new Random();
    }

    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        try {
            URL[] urls = page.getParsedData().getLinks();
            List<LinkRelevance> links = new ArrayList<>();
            for(int i = 0; i < urls.length; i++) {
                String url = urls[i].toString();
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
