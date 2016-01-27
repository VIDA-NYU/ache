package focusedCrawler.link.frontier;

import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Vector;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

/**
 * Implements a link selection strategy that picks links from the storage at random.
 */
public class RandomLinkSelector implements LinkSelectionStrategy {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable urlRelevance = frontier.getUrlRelevanceHashtable();
        
        LinkRelevance[] result = null;
        try {
            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            int count = 0;
            
            List<Tuple> tuples = urlRelevance.getTable();
            
            for (int i = 0; count < numberOfLinks && i < tuples.size(); i++) {
                Tuple tuple = tuples.get(i);
                String url = URLDecoder.decode(tuple.getKey(), "UTF-8");
                if (url != null) {
                    Integer relevInt = new Integer(tuple.getValue());
                    if (relevInt != null && relevInt > 0) {
                        LinkRelevance linkRel = new LinkRelevance(new URL(url), relevInt.intValue());
                        tempList.add(linkRel);
                        count++;
                    }
                }
            }

            result = new LinkRelevance[tempList.size()];
            tempList.toArray(result);
            
            System.out.println(">> TOTAL LOADED: " + result.length);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

}
