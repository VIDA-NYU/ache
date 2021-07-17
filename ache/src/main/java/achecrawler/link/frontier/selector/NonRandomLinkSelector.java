package achecrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.List;

import achecrawler.link.frontier.LinkRelevance;

public class NonRandomLinkSelector implements LinkSelector {
    
    private int[] classLimits = new int[] { 500, 1000, 5000 };
    private int[] classCount = new int[classLimits.length];
    
    private int numberOfLinks;
    private List<LinkRelevance> links;
    private int count;
    
    @Override
    public void startSelection(int numberOfLinks) {
        this.links = new ArrayList<LinkRelevance>();
        this.numberOfLinks = numberOfLinks;
        this.count = 0;
    }

    @Override
    public void evaluateLink(LinkRelevance linkRelevance) {
        if(links.size() >= numberOfLinks) {
            return;
        }
        int relevance = (int) linkRelevance.getRelevance();
        if (relevance > 0) {
            int index = relevance / 100;
            if (classCount[index] < classLimits[index]) {
                if (relevance == 299 || count % 5 == 0) {
                    links.add(linkRelevance);
                    classCount[index]++;
                }
            }
        }
        count++;
    }

    @Override
    public List<LinkRelevance> getSelectedLinks() {
        List<LinkRelevance> selectedLinks = links;
        this.links = null; // clean-up reference
        return selectedLinks;
    }

}
