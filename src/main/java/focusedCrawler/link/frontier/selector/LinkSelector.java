package focusedCrawler.link.frontier.selector;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;

public interface LinkSelector {
    
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks);

}
