package focusedCrawler.link.frontier;

import focusedCrawler.util.LinkRelevance;

public interface LinkSelectionStrategy {
    
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks);

}
