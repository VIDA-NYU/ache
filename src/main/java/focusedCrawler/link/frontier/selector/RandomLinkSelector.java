package focusedCrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.List;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

/**
 * Implements a link selection strategy that picks links from the storage at random.
 */
public class RandomLinkSelector implements LinkSelector {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();
        List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();
        
        List<LinkRelevance> tempList = new ArrayList<LinkRelevance>();
        for (int i = 0; tempList.size() < numberOfLinks && i < tuples.size(); i++) {
            LinkRelevance linkRelevance = tuples.get(i).getValue();
            if (linkRelevance.getRelevance() > 0) {
                tempList.add(linkRelevance);
            }
        }

        LinkRelevance[] result = new LinkRelevance[tempList.size()];
        tempList.toArray(result);
        System.out.println(">> TOTAL LOADED: " + result.length);
        return result;
    }

}
