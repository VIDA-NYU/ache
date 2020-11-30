package achecrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.MinMaxPriorityQueue;

import achecrawler.link.frontier.LinkRelevance;

/**
 * Implements a link selection strategy that tries to select the top-k links from all different
 * top-level domains.
 */
public class MaximizeWebsitesLinkSelector implements LinkSelector {
    
    private static final int MAX_LINKS_PER_DOMAIN = 5000;
    private static final double DEFAULT_MIN_RELEVANCE = 0.0d;

    private final double minRelevance;

    private Map<String, MinMaxPriorityQueue<LinkRelevance>> topkLinksPerDomain;
    private int numberOfLinks;

    public MaximizeWebsitesLinkSelector() {
        this(DEFAULT_MIN_RELEVANCE);
    }

    public MaximizeWebsitesLinkSelector(double minRelevance) {
        this.minRelevance = minRelevance;
    }

    @Override
    public void startSelection(int numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
        this.topkLinksPerDomain = new HashMap<>();
    }

    @Override
    public void evaluateLink(LinkRelevance link) {
        if (link.getRelevance() > minRelevance) {
            String domainName = link.getTopLevelDomainName();
            MinMaxPriorityQueue<LinkRelevance> domainQueue = topkLinksPerDomain.get(domainName);
            if (domainQueue == null) {
                domainQueue = newPriorityQueue(MAX_LINKS_PER_DOMAIN);
                topkLinksPerDomain.put(domainName, domainQueue);
            }
            domainQueue.add(link);
        }
    }

    @Override
    public List<LinkRelevance> getSelectedLinks() {
        List<LinkRelevance> links = new ArrayList<>();
        while (links.size() < numberOfLinks && !topkLinksPerDomain.isEmpty()) {
            // adds the URL with max score of each domain
            MinMaxPriorityQueue<LinkRelevance> topk = newPriorityQueue(numberOfLinks);
            Iterator<Entry<String, MinMaxPriorityQueue<LinkRelevance>>> it = topkLinksPerDomain.entrySet().iterator();
            while (it.hasNext()) {
                MinMaxPriorityQueue<LinkRelevance> domain = it.next().getValue();
                topk.add(domain.poll());
                if (domain.isEmpty()) {
                    it.remove();
                }
            }
            for(LinkRelevance link : topk) {
                links.add(link);
            }
        }
        this.topkLinksPerDomain = null; // clean-up reference
        return links;
    }
    
    private MinMaxPriorityQueue<LinkRelevance> newPriorityQueue(int maxSize) {
        return MinMaxPriorityQueue
                .orderedBy(LinkRelevance.DESC_ORDER_COMPARATOR)
                .maximumSize(maxSize)
                .create();
    }
    
}
