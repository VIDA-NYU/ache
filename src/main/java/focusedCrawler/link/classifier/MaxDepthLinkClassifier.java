package focusedCrawler.link.classifier;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

public class MaxDepthLinkClassifier implements LinkClassifier {

    private int maxDepth;
    
    public MaxDepthLinkClassifier(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {
        List<LinkRelevance> links = new ArrayList<LinkRelevance>();
        URL[] urls = page.links();
        for (int i = 0; i < urls.length; i++) {
            
            URL url = urls[i];
            double linkRelevance = page.getRelevance() - 1;
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
