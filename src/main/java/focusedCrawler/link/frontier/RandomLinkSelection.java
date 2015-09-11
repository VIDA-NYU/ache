package focusedCrawler.link.frontier;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;

/**
 * Implements a link selection strategy that picks links from the storage at random.
 */
public class RandomLinkSelection implements LinkSelectionStrategy {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable urlRelevance = frontier.getUrlRelevanceHashtable();
        
        LinkRelevance[] result = null;
        try {
            Iterator<String> keys = urlRelevance.getKeys();
            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            int count = 0;
            
            while(keys.hasNext() && count < numberOfLinks) {
                
                String url = URLDecoder.decode(keys.next(), "UTF-8");
                if (url != null) {
                    Integer relevInt = new Integer(urlRelevance.get(url));
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

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }

}
