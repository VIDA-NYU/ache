package focusedCrawler.link.frontier.selector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MinMaxPriorityQueue;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;


/**
 * This class implements a top-k link selector with politeness restrictions. At most a
 * given number of links in the same domain are selected at a time. Furthermore,
 * links from same domain are not selected if they were selected before in a
 * time interval before the minimum access time specified.
 * 
 * @author aeciosantos
 *
 */
public class PoliteTopkLinkSelector implements LinkSelector {
    
    private static final Logger logger = LoggerFactory.getLogger(PoliteTopkLinkSelector.class);
    
    private final int maxUrlPerDomain;
    private final long minimumAccessInterval;
    private final Cache<String, Long> domainAccessCache;

    private boolean remainsLinksInFrontier;

    public PoliteTopkLinkSelector(int maxUrlPerDomain) {
        this(maxUrlPerDomain, 0);
    }

    public PoliteTopkLinkSelector(int maxUrlPerDomain, long minimumAccessInterval) {
        Preconditions.checkArgument(minimumAccessInterval >= 0,
                                    "Access interval must be non-negative: %s",
                                    minimumAccessInterval);
        this.maxUrlPerDomain = maxUrlPerDomain;
        this.minimumAccessInterval = minimumAccessInterval;
        if(minimumAccessInterval > 0) {
            // this cache maintain the access times of the domains and remove
            // them automatically after the specified minimumAccessInterval
            this.domainAccessCache = CacheBuilder.newBuilder()
                    .maximumSize(100000)
                    .expireAfterWrite(minimumAccessInterval, TimeUnit.MILLISECONDS)
                    .build();
        } else {
            this.domainAccessCache = null;
        }
    }
    
    @Override
    public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
        
        final long startTime = System.currentTimeMillis();
        
        LinkRelevance[] result = selectTopk(frontier, numberOfLinks);
        while(result.length == 0 && this.remainsLinksInFrontier) {
            try {
                logger.info("No link can be select right now. " +
                            "Sleeping {}ms before trying again.", minimumAccessInterval);
                Thread.sleep(minimumAccessInterval);
            } catch (InterruptedException e) {
                // just give up and return normally
                break;
            }
            result = selectTopk(frontier, numberOfLinks);
        }
        
        final long totalTime = System.currentTimeMillis() - startTime;
        final int numberOfLoadedLinks = result != null ? result.length : 0;
        logger.info("Loaded {} links in {} ms", numberOfLoadedLinks, totalTime);
        
        return result;
    }

    private LinkRelevance[] selectTopk(Frontier frontier, int numberOfLinks) {
        
        PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();
        List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();
        
        Map<String, MinMaxPriorityQueue<LinkRelevance>> topkLinksPerDomain = new HashMap<>();
        this.remainsLinksInFrontier = false;
        
        for(Tuple<LinkRelevance> tuple : tuples) {
            
            LinkRelevance linkRelevance = tuple.getValue();
            
            double relevance = linkRelevance.getRelevance();
            if(relevance > 0) {
                
                this.remainsLinksInFrontier = true;
                
                String domainName = linkRelevance.getTopLevelDomainName();
                
                if(domainAccessCache != null) {
                    Long lastAccessTime = domainAccessCache.getIfPresent(domainName);
                    if(lastAccessTime != null && lastAccessTime+minimumAccessInterval > System.currentTimeMillis()) {
                        // link can't be selected right now, pass to next URL
                        continue;
                    }
                }
                
                MinMaxPriorityQueue<LinkRelevance> domainQueue = topkLinksPerDomain.get(domainName);
                if(domainQueue == null) {
                    domainQueue = newMinMaxPriorityQueue(maxUrlPerDomain);
                    topkLinksPerDomain.put(domainName, domainQueue);
                } 
                domainQueue.add(linkRelevance);
            }
        }
        
        MinMaxPriorityQueue<LinkRelevance> topkLinks = newMinMaxPriorityQueue(numberOfLinks);
        for (MinMaxPriorityQueue<LinkRelevance> domainQueue : topkLinksPerDomain.values()) {
            topkLinks.addAll(domainQueue);
        }
        
        if(topkLinks.size() == 0) {
            return new LinkRelevance[0];
        }
        
        final LinkRelevance[] result = topkLinks.toArray(new LinkRelevance[topkLinks.size()]);
        if(result.length > 0 && domainAccessCache != null) {
            for(LinkRelevance link : result) {
                domainAccessCache.put(link.getTopLevelDomainName(), System.currentTimeMillis());
            }
        }
        return result;
    }

    private MinMaxPriorityQueue<LinkRelevance> newMinMaxPriorityQueue(int maxSize) {
        return MinMaxPriorityQueue
                .orderedBy(new Comparator<LinkRelevance>() {
                    @Override
                    public int compare(LinkRelevance o1, LinkRelevance o2) {
                        return Double.compare(o2.getRelevance(), o1.getRelevance());
                    }
                })
                .maximumSize(maxSize)
                .create();
    }

}
