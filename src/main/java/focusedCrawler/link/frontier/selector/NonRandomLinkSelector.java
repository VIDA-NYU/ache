package focusedCrawler.link.frontier.selector;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.vsm.VSMElement;

public class NonRandomLinkSelector implements LinkSelector {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable urlRelevance = frontier.getUrlRelevanceHashtable();
        
        LinkRelevance[] result = null;
        int[] classLimits = new int[] { 500, 1000, 5000 };
        int[] classCount = new int[classLimits.length];
        
        try {
            Iterator<VSMElement> keys = urlRelevance.orderedSet().iterator();
            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            int count = 0;
            for (int i = 0; count < numberOfLinks && keys.hasNext(); i++) {
                VSMElement elem = keys.next();
                String key = elem.getWord();
                String url = URLDecoder.decode(key, "UTF-8");
                if (url != null) {
                    Integer relevInt = new Integer((int) elem.getWeight());
                    if (relevInt != null && relevInt.intValue() != -1) {
                        int relev = relevInt.intValue();
                        if (relev > 0) {
                            int index = relev / 100;
                            if (classCount[index] < classLimits[index]) {
                                if (relev == 299 || i % 5 == 0) {
                                    LinkRelevance linkRel = new LinkRelevance(new URL(url), relev);
                                    tempList.add(linkRel);
                                    count++;
                                    classCount[index]++;
                                }
                            }
                        }
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
