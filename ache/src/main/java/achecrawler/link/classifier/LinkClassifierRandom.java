package achecrawler.link.classifier;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implements a random crawling policy in which each link gets a random relevance score.
 */
public class LinkClassifierRandom implements LinkClassifier {

    private Random randomGenerator;

    public LinkClassifierRandom() {
        this.randomGenerator = new Random();
    }

    @Override
    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        List<LinkRelevance> linkRelevances = new ArrayList<>();
        URL[] links = page.getParsedData().getLinks();
        if (links != null && links.length > 0) {
            for (URL link : links) {
                LinkRelevance linkRelevance = createRandomLinkRelevance(link);
                linkRelevances.add(linkRelevance);
            }
        }
        return (LinkRelevance[]) linkRelevances.toArray(new LinkRelevance[linkRelevances.size()]);
    }

    private LinkRelevance createRandomLinkRelevance(URL link) {
        double relevance = randomGenerator.nextDouble();
        relevance = relevance + Double.MIN_VALUE; // avoids zero value
        return new LinkRelevance(link, relevance);
    }

    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        return createRandomLinkRelevance(ln.getLink());
    }

}
