package focusedCrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

public class SiteLinkSelector implements LinkSelector {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {

        PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();
        int numberOfHostsInScope = frontier.getScope().size();

        Iterator<Tuple<LinkRelevance>> keys = urlRelevance.orderedSet(LinkRelevance.DESC_ORDER_COMPARATOR).iterator();

        HashMap<String, Integer> hostCount = new HashMap<String, Integer>();
        List<LinkRelevance> tempList = new ArrayList<LinkRelevance>();

        while (tempList.size() < numberOfLinks && keys.hasNext()) {

            Tuple<LinkRelevance> elem = keys.next();
            LinkRelevance linkRelevance = elem.getValue();

            String host = filterServer(linkRelevance.getURL().getHost());

            Integer intCount = hostCount.get(host);
            if (intCount == null) {
                hostCount.put(host, new Integer(0));
            }

            int numOfLinks = hostCount.get(host).intValue();

            if (numOfLinks < numberOfLinks / numberOfHostsInScope) {
                hostCount.put(host, new Integer(numOfLinks + 1));
                if (linkRelevance.getRelevance() > 0) {
                    tempList.add(linkRelevance);
                }
            }
        }

        LinkRelevance[] result = new LinkRelevance[tempList.size()];
        tempList.toArray(result);
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
