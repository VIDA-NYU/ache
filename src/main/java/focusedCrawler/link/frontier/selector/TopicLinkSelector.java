package focusedCrawler.link.frontier.selector;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;
import net.sf.ehcache.CacheException;

public class TopicLinkSelector implements LinkSelector {

    int[] classLimits = new int[] { 100, 100, 1500 };

    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();

        LinkRelevance[] result = null;

        int[] classCount = new int[classLimits.length];
        try {
            Iterator<Tuple<LinkRelevance>> keys = urlRelevance.getTable().iterator();
            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            int count = 0;
            for (; count < numberOfLinks && keys.hasNext();) {
                Tuple<LinkRelevance> tuple = keys.next();
                String url = URLDecoder.decode(tuple.getKey(), "UTF-8");
                if (url != null) {
                    LinkRelevance linkRelevance = tuple.getValue();
                    int relevance = (int) linkRelevance.getRelevance();
                    if (relevance > 100) {
                        int index = relevance / 100;
                        if (index < 3 && classCount[index] < classLimits[index]) {
                            tempList.add(linkRelevance);
                            count++;
                            classCount[index]++;
                        }
                    }
                }
            }
            if (classCount[2] < classLimits[2] && classLimits[1] < 1000) {
                classLimits[1] = classLimits[1] + 50;
            }

            result = new LinkRelevance[tempList.size()];
            tempList.toArray(result);
            System.out.println(">> TOTAL LOADED: " + result.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CacheException ex) {
            ex.printStackTrace();
        }
        return result;

    }

}
