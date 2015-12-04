package focusedCrawler.link.frontier;

import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.Tuple;

/**
 * Implements a link selection strategy that tries to select links from
 * different top-level domains.
 */
public class MaximizeWebsitesLinkSelector implements LinkSelectionStrategy {
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        try {
            List<LinkRelevance> links = new ArrayList<LinkRelevance>();
            
            List<Tuple> tuples = frontier.getUrlRelevanceHashtable().getTable();
            
            Set<String> selectedDomains = new HashSet<>();
            
            for (int i = 0; links.size() < numberOfLinks && i < tuples.size(); i++) {
                Tuple tuple = tuples.get(i);
            
                String url = URLDecoder.decode(tuple.getKey(), "UTF-8");
                Integer relevance = new Integer(tuple.getValue());
                
                LinkRelevance linkRel = new LinkRelevance(new URL(url), relevance);
                
                String domainName = linkRel.getTopLevelDomainName();
                if(!selectedDomains.contains(domainName) && relevance > 0) {
                    links.add(linkRel);
                    selectedDomains.add(domainName);
                }
                
            }

            System.out.println(">> TOTAL LOADED: " + links.size());

            return (LinkRelevance[]) links.toArray(new LinkRelevance[links.size()]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

}
