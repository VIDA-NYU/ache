package achecrawler.link.classifier;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;

public class MaxDepthLinkClassifier implements LinkClassifier {

    private int maxDepth;
    
    public MaxDepthLinkClassifier(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        List<LinkRelevance> links = new ArrayList<LinkRelevance>();
        URL[] urls = page.getParsedData().getLinks();
        for (int i = 0; i < urls.length; i++) {
            
            URL url = urls[i];
            double linkRelevance = Math.abs(page.getLinkRelevance().getRelevance()) - 1;
            int currentDepth = (int) (LinkRelevance.DEFAULT_RELEVANCE - linkRelevance);
            if(currentDepth <= maxDepth) {
                links.add(new LinkRelevance(url, linkRelevance));
            }
        }
        return links.toArray(new LinkRelevance[links.size()]);
    }

    @Override
    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        throw new java.lang.UnsupportedOperationException("Method classify(LinkNeighborhood ln) not yet implemented.");
    }

}
