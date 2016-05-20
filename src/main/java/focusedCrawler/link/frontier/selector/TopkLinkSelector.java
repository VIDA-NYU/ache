package focusedCrawler.link.frontier.selector;

import java.util.Iterator;

import com.google.common.collect.MinMaxPriorityQueue;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

public class TopkLinkSelector implements LinkSelector {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks)  {
        
        PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();
        Iterator<Tuple<LinkRelevance>> urls = urlRelevance.getTable().iterator();
        
        MinMaxPriorityQueue<LinkRelevance> topkLinks = MinMaxPriorityQueue
                .orderedBy(LinkRelevance.DESC_ORDER_COMPARATOR)
                .maximumSize(numberOfLinks)
                .create();
        
        while(urls.hasNext()) {
            LinkRelevance linkRelevance = urls.next().getValue();
            if(linkRelevance.getRelevance() > 0) {
                topkLinks.add(linkRelevance);
            }
        }
        
        return topkLinks.toArray(new LinkRelevance[topkLinks.size()]);
    }

}
