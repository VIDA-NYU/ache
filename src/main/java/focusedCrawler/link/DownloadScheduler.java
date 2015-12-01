package focusedCrawler.link;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import focusedCrawler.util.LinkRelevance;

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
    
    private int numberOfLinks = 0;

    public DownloadScheduler(int minimumAccessTimeInterval) {
        this.minimumAccessTime = minimumAccessTimeInterval;
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
    
    public synchronized void addLink(LinkRelevance link) {
        removeExpiredNodes();
        
        String domainName = link.getTopLevelDomainName();
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
        numberOfLinks++;
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
        
        synchronized(this) {
            DomainNode domainNode = domainsQueue.poll();
            if(domainNode == null) {
                return null;
            }
            
            linkRelevance = domainNode.links.removeFirst();
        
            expirationTime = domainNode.lastAccessTime + minimumAccessTime;
            domainNode.lastAccessTime = System.currentTimeMillis();
            
            if(domainNode.links.isEmpty()) {
                emptyDomainsQueue.add(domainNode);
            } else {
                domainsQueue.add(domainNode);
            }
            
            numberOfLinks--;
        }
        
        long waitTime = expirationTime - System.currentTimeMillis();
        if(waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(getClass()+" interrupted.", e);
            }
        }
        
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
        return numberOfLinks;
    }

    public boolean hasPendingLinks() {
        return numberOfLinks() > 0;
    }
    
}
