package achecrawler.link.classifier;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;

/**
 * This class implements the link classifier for the hub links.
 * 
 * @author lbarbosa
 *
 */
public class LinkClassifierHub implements LinkClassifier {

    private LNClassifier classifier;

    public LinkClassifierHub() {}

    public LinkClassifierHub(LNClassifier classifier) {
        this.classifier = classifier;
    }

    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        LinkRelevance result = null;
        try {
            if (classifier == null) {
                result = new LinkRelevance(ln.getLink(), LinkRelevance.DEFAULT_HUB_RELEVANCE + 1);
            } else {
                double[] prob = classifier.classify(ln);
                double relevance = LinkRelevance.DEFAULT_HUB_RELEVANCE + prob[0] * 100;
                result = new LinkRelevance(ln.getLink(), relevance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        // TODO Auto-generated method stub
        return null;
    }

}
