package focusedCrawler.link.frontier;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.sf.ehcache.CacheException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.vsm.VSMElement;

public class SiteLinkSelector implements LinkSelectionStrategy {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable urlRelevance = frontier.getUrlRelevanceHashtable();
        int numberOfHostsInScope = frontier.getScope().size();
        
        LinkRelevance[] result = null;
        try {
            
            Iterator<VSMElement> keys = urlRelevance.orderedSet().iterator();
            
            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            
            HashMap<String, Integer> hostCount = new HashMap<String, Integer>();
            
            for (int count = 0; count < numberOfLinks && keys.hasNext();) {
                
                VSMElement elem = keys.next();
                
                String key = elem.getWord();
                String url = URLDecoder.decode(key, "UTF-8");
                
                if (url != null && !url.isEmpty()) {
                    
                    String host = filterServer(new URL(url).getHost());
                    
                    Integer intCount = hostCount.get(host);
                    if (intCount == null) {
                        hostCount.put(host, new Integer(0));
                    }
                    
                    int numOfLinks = hostCount.get(host).intValue();
                    if (numOfLinks < numberOfLinks / numberOfHostsInScope) {
                        hostCount.put(host, new Integer(numOfLinks + 1));
                        Integer relevInt = new Integer((int) elem.getWeight());
                        if (relevInt != null && relevInt.intValue() != -1) {
                            int relev = relevInt.intValue();
                            if (relev > 0) {
                                LinkRelevance linkRel = new LinkRelevance(new URL(url), relev);
                                tempList.add(linkRel);
                                count++;
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
        } catch (CacheException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
    private String filterServer(String server){
        if(server.lastIndexOf(".") != -1){
            String serverTemp = server.substring(0,server.lastIndexOf("."));
            int index = serverTemp.lastIndexOf(".");
            if(index != -1){
                server = server.substring(index+1);
            }
        }
        return server;
    }

}
