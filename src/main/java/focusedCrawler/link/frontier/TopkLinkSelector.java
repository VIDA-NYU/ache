package focusedCrawler.link.frontier;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Comparator;
import java.util.Iterator;

import com.google.common.collect.MinMaxPriorityQueue;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

public class TopkLinkSelector implements LinkSelectionStrategy {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks)  {
        
        PersistentHashtable urlRelevance = frontier.getUrlRelevanceHashtable();
        
        Iterator<Tuple> urls = urlRelevance.getTable().iterator();
        
        MinMaxPriorityQueue<LinkRelevance> topkLinks = MinMaxPriorityQueue
                .orderedBy(new Comparator<LinkRelevance>() {
                    @Override
                    public int compare(LinkRelevance o1, LinkRelevance o2) {
                        return Double.compare(o2.getRelevance(), o1.getRelevance());
                    }
                })
                .maximumSize(numberOfLinks)
                .create();
        
        while(urls.hasNext()) {
            Tuple tuple = urls.next();
            
            String url;
            try {
                url = URLDecoder.decode(tuple.getKey(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Encoding not supported!", e);
            }
            
            Double relevance = new Double(tuple.getValue());
            try {
                if(relevance > 0) {
                    topkLinks.add(new LinkRelevance(new URL(url), relevance.doubleValue()));
                }
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Invalid URL in frontier.", e);
            }
        }
        
        return topkLinks.toArray(new LinkRelevance[topkLinks.size()]);
    }

}
