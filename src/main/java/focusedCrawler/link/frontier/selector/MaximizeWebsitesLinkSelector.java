package focusedCrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.MinMaxPriorityQueue;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

/**
 * Implements a link selection strategy that tries to select links from
 * different top-level domains.
 */
public class MaximizeWebsitesLinkSelector implements LinkSelector {
    
    int maxLinksPerDomain = 5;
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();
        Map<String, MinMaxPriorityQueue<LinkRelevance>> topkLinksPerDomain = new HashMap<>();
        
        for(Tuple<LinkRelevance> tuple : urlRelevance.getTable()) {
            double relevance = tuple.getValue().getRelevance();
            if(relevance > 0) {
                LinkRelevance linkRelevance = tuple.getValue();
                
                String domainName = linkRelevance.getTopLevelDomainName();
                
                MinMaxPriorityQueue<LinkRelevance> domainQueue = topkLinksPerDomain.get(domainName);
                if(domainQueue == null) {
                    domainQueue = newMinMaxPriorityQueue(maxLinksPerDomain);
                    topkLinksPerDomain.put(domainName, domainQueue);
                } 
                domainQueue.add(linkRelevance);
            }
        }
        
        List<LinkRelevance> links = new ArrayList<>();
        while(links.size() < numberOfLinks && !topkLinksPerDomain.isEmpty()) {
            // add the URL with max score of each domain
            Iterator<Entry<String, MinMaxPriorityQueue<LinkRelevance>>> it = topkLinksPerDomain.entrySet().iterator();
            while(it.hasNext()) {
                MinMaxPriorityQueue<LinkRelevance> domain = it.next().getValue();
                links.add(domain.poll());
                if(domain.isEmpty()) {
                    it.remove();
                }
            }
        }
        
        if(links.size() == 0) {
            return new LinkRelevance[0];
        }
        
        return links.toArray(new LinkRelevance[links.size()]);
    }

    private MinMaxPriorityQueue<LinkRelevance> newMinMaxPriorityQueue(int maxSize) {
        return MinMaxPriorityQueue
                .orderedBy(new Comparator<LinkRelevance>() {
                    @Override
                    public int compare(LinkRelevance o1, LinkRelevance o2) {
                        return Double.compare(o2.getRelevance(), o1.getRelevance());
                    }
                })
                .maximumSize(maxSize)
                .create();
    }

}
