package focusedCrawler.link;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

import focusedCrawler.link.frontier.LinkRelevance;

public class DownloadScheduler {
    
    private static class DomainNode {
        
        final String domainName;
        final Deque<LinkRelevance> links;
        long lastAccessTime;
        
        public DomainNode(String domainName, long lastAccessTime) {
            this.domainName = domainName;
            this.links = new LinkedList<>();
            this.lastAccessTime = lastAccessTime;
        }
        
    }
    
    private final PriorityQueue<DomainNode> domainsQueue;
    private final PriorityQueue<DomainNode> emptyDomainsQueue;
    private final Map<String, DomainNode> domains;
    private final long minimumAccessTime;
    private final int maxLinksInScheduler;
    
    private AtomicInteger numberOfLinks = new AtomicInteger(0);

    public DownloadScheduler(int minimumAccessTimeInterval, int maxLinksInScheduler) {
        this.minimumAccessTime = minimumAccessTimeInterval;
        this.maxLinksInScheduler = maxLinksInScheduler;
        this.domains = new HashMap<>();
        this.emptyDomainsQueue = createDomainPriorityQueue();
        this.domainsQueue = createDomainPriorityQueue();
    }

    private PriorityQueue<DomainNode> createDomainPriorityQueue() {
        return new PriorityQueue<DomainNode>(new Comparator<DomainNode>() {
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
        numberOfLinks.incrementAndGet();
        
        String domainName = link.getTopLevelDomainName();
        
        synchronized(this) {
            DomainNode domainNode = domains.get(domainName);
            if(domainNode == null) {
                domainNode = new DomainNode(domainName, 0l);
                domains.put(domainName, domainNode);
            }
            
            if(domainNode.links.isEmpty()) {
                emptyDomainsQueue.remove(domainNode);
                domainsQueue.add(domainNode);
            }
            
            domainNode.links.addLast(link);
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
            linkRelevance = domainNode.links.removeFirst();
            domainNode.lastAccessTime = System.currentTimeMillis();
            if (domainNode.links.isEmpty()) {
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
        DomainNode domainNode = domainsQueue.peek();
        if(domainNode == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long timeSinceLastAccess = now - domainNode.lastAccessTime;
        if(timeSinceLastAccess < minimumAccessTime) {
            // the domain with longest access time is still not available
            return false;
        }
        return true;
    }

    public synchronized void clear() {
        Iterator<Entry<String, DomainNode>> it = domains.entrySet().iterator();
        while(it.hasNext()) {
            DomainNode node = it.next().getValue();
            numberOfLinks.addAndGet(-node.links.size()); // adds negative value
            node.links.clear();
        }
        while(true) {
            DomainNode node = domainsQueue.poll();
            if(node == null) {
                break;
            }
            emptyDomainsQueue.add(node);
        }
    }
    
}
