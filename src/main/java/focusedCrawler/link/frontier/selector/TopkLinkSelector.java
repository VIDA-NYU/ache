package focusedCrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import focusedCrawler.link.frontier.LinkRelevance;

public class TopkLinkSelector implements LinkSelector {
    
    private MinMaxPriorityQueue<LinkRelevance> topkLinks;

    @Override
    public void startSelection(int numberOfLinks) {
        this.topkLinks = MinMaxPriorityQueue
                .orderedBy(LinkRelevance.DESC_ORDER_COMPARATOR)
                .maximumSize(numberOfLinks) // keep only top-k items
                .create();
    }

    @Override
    public void evaluateLink(LinkRelevance link) {
        if(link.getRelevance() > 0) {
            topkLinks.add(link);
        }
    }

    @Override
    public List<LinkRelevance> getSelectedLinks() {
        List<LinkRelevance> selectedLinks = new ArrayList<>(topkLinks);
        this.topkLinks = null; // clean-up reference
        return selectedLinks;
    }

}
