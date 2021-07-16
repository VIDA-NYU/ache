package achecrawler.link;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import achecrawler.link.frontier.LinkRelevance;

class DomainNode {
    
    public final String domainName;
    volatile public long lastAccessTime;
    private PriorityQueue<LinkRelevance> links;
    private Set<String> urls = new HashSet<>();
    
    public DomainNode(String domainName, long lastAccessTime) {
        this.domainName = domainName;
        this.lastAccessTime = lastAccessTime;
        int initialCapacity = 50;
        this.links = new PriorityQueue<LinkRelevance>(initialCapacity, LinkRelevance.DESC_ABS_ORDER_COMPARATOR);
    }
    
    public boolean isEmpty() {
        return links.isEmpty();
    }
    
    public boolean add(LinkRelevance link) {
        if(!urls.contains(link.getURL().toString())) {
            links.add(link);
            urls.add(link.getURL().toString());
            return true;
        }
        return false;
    }
    
    public LinkRelevance removeFirst() {
        LinkRelevance link = links.poll();
        urls.remove(link.getURL().toString());
        return link;
    }

    public int size() {
        return links.size();
    }

    public void clear() {
        links.clear();
        urls.clear();
    }
    
}

/**
 * Makes sure that links are selected respecting politeness constraints: a link from the same host
 * is never selected twice within a given minimum access time interval. That means that the natural
 * order (based on link relevance) is modified so that links are selected based on last time that
 * the host was last accessed in order to respect the minimum access time limit.
 * 
 * @author aeciosantos
 *
 */
public class PolitenessScheduler {

    private static final int MIN_LINKS_PER_DOMAIN_TO_ALLOW_LOAD = 2000;

    private final PriorityQueue<DomainNode> domainsQueue;
    private final PriorityQueue<DomainNode> emptyDomainsQueue;
    private final Map<String, DomainNode> domains;
    private final long minimumAccessTime;
    private final int maxLinksInScheduler;
    
    private AtomicInteger numberOfLinks = new AtomicInteger(0);

    public PolitenessScheduler(int minimumAccessTimeInterval, int maxLinksInScheduler) {
        this.minimumAccessTime = minimumAccessTimeInterval;
        this.maxLinksInScheduler = maxLinksInScheduler;
        this.domains = new HashMap<>();
        this.emptyDomainsQueue = createDomainPriorityQueue();
        this.domainsQueue = createDomainPriorityQueue();
    }

    private PriorityQueue<DomainNode> createDomainPriorityQueue() {
        int initialCapacity = 10;
        return new PriorityQueue<DomainNode>(initialCapacity, new Comparator<DomainNode>() {
            @Override
            public int compare(DomainNode o1, DomainNode o2) {
                return Long.compare(o1.lastAccessTime, o2.lastAccessTime);
            }
        });
    }
    
    public boolean addLink(LinkRelevance link) {

        removeExpiredNodes();
        
        if(numberOfLinks() >= maxLinksInScheduler) {
            return false; // ignore link
        }
        
        String domainName = link.getTopLevelDomainName();
        
        synchronized(this) {
            DomainNode domainNode = domains.get(domainName);
            if(domainNode == null) {
                domainNode = new DomainNode(domainName, 0l);
                domains.put(domainName, domainNode);
            }
            
            if(domainNode.isEmpty()) {
                emptyDomainsQueue.remove(domainNode);
                domainsQueue.add(domainNode);
            }
            
            if(domainNode.add(link)) {
                numberOfLinks.incrementAndGet();
            }
        }
        
        return true;
    }

    private synchronized void removeExpiredNodes() {
        while(true) {
            DomainNode node = emptyDomainsQueue.peek();
            if(node == null) {
                break;
            }
            
            long expirationTime = node.lastAccessTime + minimumAccessTime;
            if(System.currentTimeMillis() > expirationTime) {
                emptyDomainsQueue.poll();
                domains.remove(node.domainName);
            } else {
                break;
            }
        }
    }

    public LinkRelevance nextLink() {
        LinkRelevance linkRelevance;
        
        synchronized (this) {

            DomainNode domainNode = domainsQueue.peek();
            if (domainNode == null) {
                // no domains available to be crawled
                return null;
            }

            long now = System.currentTimeMillis();
            long timeSinceLastAccess = now - domainNode.lastAccessTime;
            if (timeSinceLastAccess < minimumAccessTime) {
                // the domain with longest access time cannot be crawled right now
                return null;
            }
            
            domainsQueue.poll(); 
            linkRelevance = domainNode.removeFirst();
            domainNode.lastAccessTime = System.currentTimeMillis();
            if (domainNode.isEmpty()) {
                emptyDomainsQueue.add(domainNode);
            } else {
                domainsQueue.add(domainNode);
            }

        }

        numberOfLinks.decrementAndGet();

        return linkRelevance;
    }
    
    public int numberOfNonExpiredDomains() {
        removeExpiredNodes();
        return domains.size();
    }
    
    public int numberOfAvailableDomains() {
        int available = 0;
        for(DomainNode node : domainsQueue) {
            if(isAvailable(node)){
                available++;
            }
        }
        return available;
    }

    public int numberOfEmptyDomains() {
        return emptyDomainsQueue.size();
    }

    public int numberOfLinks() {
        return numberOfLinks.get();
    }

    public boolean hasPendingLinks() {
        return numberOfLinks() > 0;
    }

    public boolean hasLinksAvailable() {
        // pick domain with longest access time 
        DomainNode domainNode = domainsQueue.peek();
        if(domainNode == null) {
            return false;
        }
        return isAvailable(domainNode);
    }

    private boolean isAvailable(DomainNode domainNode) {
        long now = System.currentTimeMillis();
        long timeSinceLastAccess = now - domainNode.lastAccessTime;
        if(timeSinceLastAccess < minimumAccessTime) {
            return false;
        }
        return true;
    }

    public synchronized void clear() {
        Iterator<Entry<String, DomainNode>> it = domains.entrySet().iterator();
        while(it.hasNext()) {
            DomainNode node = it.next().getValue();
            numberOfLinks.addAndGet(-node.size()); // adds negative value
            node.clear();
        }
        while(true) {
            DomainNode node = domainsQueue.poll();
            if(node == null) {
                break;
            }
            emptyDomainsQueue.add(node);
        }
    }

    public boolean canDownloadNow(LinkRelevance link) {
        DomainNode domain = domains.get(link.getTopLevelDomainName());
        if(domain == null) {
            return true;
        } else {
            return isAvailable(domain);
        }
    }

    public boolean canInsertNow(LinkRelevance link) {
        DomainNode domain = domains.get(link.getTopLevelDomainName());
        if (domain == null) {
            return true;
        } else {
            return domain.size() < MIN_LINKS_PER_DOMAIN_TO_ALLOW_LOAD;
        }
    }

}
