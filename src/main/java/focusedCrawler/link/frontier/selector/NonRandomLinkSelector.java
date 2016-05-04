package focusedCrawler.link.frontier.selector;

import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

public class NonRandomLinkSelector implements LinkSelector {
    
    int[] classLimits = new int[] { 500, 1000, 5000 };
    int[] classCount = new int[classLimits.length];
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();
        
        Iterator<Tuple<LinkRelevance>> keys = urlRelevance.orderedSet(LinkRelevance.DESC_ORDER_COMPARATOR).iterator();
        
        Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
        for (int i = 0; tempList.size() < numberOfLinks && keys.hasNext(); i++) {
            Tuple<LinkRelevance> tuple = keys.next();
                LinkRelevance linkRelevance = tuple.getValue();
                int relevance = (int) linkRelevance.getRelevance();
                if (relevance > 0) {
                    int index = relevance / 100;
                    if (classCount[index] < classLimits[index]) {
                        if (relevance == 299 || i % 5 == 0) {
                            tempList.add(linkRelevance);
                            classCount[index]++;
                        }
                    }
                }
        }
        return (LinkRelevance[]) tempList.toArray(new LinkRelevance[tempList.size()]);
    }

}
