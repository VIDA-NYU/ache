package focusedCrawler.link;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
        int initialCapacity = 10;
        return new PriorityQueue<DomainNode>(initialCapacity, new Comparator<DomainNode>() {
            @Override
            public int compare(DomainNode o1, DomainNode o2) {
                return Long.compare(o1.lastAccessTime, o2.lastAccessTime);
            }
        });
    }
    
    public void addLink(LinkRelevance link) {
        numberOfLinks.incrementAndGet();

        removeExpiredNodes();
        
        while(numberOfLinks() > maxLinksInScheduler) {
            // block until number of links is lower than max number of links
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while adding link.", e);
            }
        }
        
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
        
        long expirationTime;
        LinkRelevance linkRelevance;
        long waitTime = 0;
        
        synchronized(this) {
            DomainNode domainNode = domainsQueue.poll();
            if(domainNode == null) {
                return null;
            }
            
            linkRelevance = domainNode.links.removeFirst();
            
            if(domainNode.links.isEmpty()) {
                emptyDomainsQueue.add(domainNode);
            } else {
                domainsQueue.add(domainNode);
            }
            
            expirationTime = domainNode.lastAccessTime + minimumAccessTime;
            long now = System.currentTimeMillis();
            waitTime = expirationTime - now;
            
            if(waitTime > 0) {
                domainNode.lastAccessTime = now + waitTime;
            } else {
                domainNode.lastAccessTime = now;
            }
        }
        
        if(waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(getClass()+" interrupted.", e);
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
    
}
