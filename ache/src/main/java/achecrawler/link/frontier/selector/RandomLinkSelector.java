package achecrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.MinMaxPriorityQueue;

import achecrawler.link.frontier.LinkRelevance;

/**
 * Implements a link selection strategy that picks links from the storage at random.
 */
public class RandomLinkSelector implements LinkSelector {

    private Random random = new Random();
    private MinMaxPriorityQueue<RandomLink> links;
    
    private class RandomLink {
        double relevance;
        final LinkRelevance link;
        public RandomLink(LinkRelevance link, double relevance) {
            this.link = link;
            this.relevance = relevance;
        }
    }

    @Override
    public void startSelection(int numberOfLinks) {
        links = MinMaxPriorityQueue
            .orderedBy(new Comparator<RandomLink>() {
                @Override
                public int compare(RandomLink o1, RandomLink o2) {
                    return Double.compare(o1.relevance, o2.relevance);
                }
            })
            .maximumSize(numberOfLinks) // keep only top-k items
            .create();
    }

    @Override
    public void evaluateLink(LinkRelevance link) {
        if (link.getRelevance() > 0) {
            this.links.add(new RandomLink(link, random.nextDouble()));
        }
    }

    @Override
    public List<LinkRelevance> getSelectedLinks() {
        List<LinkRelevance> selectedLinks = new ArrayList<>();
        for (RandomLink link : this.links) {
            selectedLinks.add(link.link);
        }
        this.links = null; // clean-up reference
        return selectedLinks;
    }

}
