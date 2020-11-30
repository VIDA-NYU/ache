package achecrawler.link.frontier.selector;

import java.util.List;

import achecrawler.link.frontier.LinkRelevance;

public interface LinkSelector {
    
    public void startSelection(int numberOfLinks);
    
    public void evaluateLink(LinkRelevance link);
    
    public List<LinkRelevance> getSelectedLinks();

}
