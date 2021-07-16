package achecrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import achecrawler.link.frontier.LinkRelevance;

public class TopkLinkSelector implements LinkSelector {

    private final double minRelevance;

    private MinMaxPriorityQueue<LinkRelevance> topkLinks;

    public TopkLinkSelector() {
        this(0.0d);
    }

    public TopkLinkSelector(double minRelevance) {
        this.minRelevance = minRelevance;
    }

    @Override
    public void startSelection(int numberOfLinks) {
        this.topkLinks = MinMaxPriorityQueue
                .orderedBy(LinkRelevance.DESC_ORDER_COMPARATOR)
                .maximumSize(numberOfLinks) // keep only top-k items
                .create();
    }

    @Override
    public void evaluateLink(LinkRelevance link) {
        if(link.getRelevance() > minRelevance) {
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
