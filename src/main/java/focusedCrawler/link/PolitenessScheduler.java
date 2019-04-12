package focusedCrawler.link;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import focusedCrawler.link.frontier.LinkRelevance;

class DomainNode {

    public final String domainName;
    volatile public long lastAccessTime;
    private PriorityQueue<LinkRelevance> pendingQueue;
    private Set<String> urls = new HashSet<>();
    private Set<String> downloading = new HashSet<>();

    public DomainNode(String domainName, long lastAccessTime) {
        this.domainName = domainName;
        this.lastAccessTime = lastAccessTime;
        int initialCapacity = 50;
        this.pendingQueue = new PriorityQueue<>(initialCapacity,
                LinkRelevance.DESC_ABS_ORDER_COMPARATOR);
    }

    public boolean isEmpty() {
        return pendingQueue.isEmpty();
    }

    public boolean add(LinkRelevance link) {
        if (!urls.contains(link.getURL().toString())) {
            pendingQueue.add(link);
            urls.add(link.getURL().toString());
            return true;
        }
        return false;
    }

    public LinkRelevance selectFirst() {
        LinkRelevance link = pendingQueue.poll();
        downloading.add(link.getURL().toString());
        return link;
    }

    public int pendingSize() {
        return pendingQueue.size();
    }

    public void clearPendingQueue() {
        pendingQueue.clear();
        urls.clear();
    }

    public boolean contains(LinkRelevance link) {
        return urls.contains(link.getURL().toString());
    }

    public void removeDownloading(LinkRelevance link) {
        String url = link.getURL().toString();
        urls.remove(url);
        downloading.remove(url);
    }
}

/**
 * Makes sure that links are selected respecting politeness constraints: a link from the same host
 * is never selected twice within a given minimum access time interval. That means that the natural
 * order (based on link relevance) is modified so that links are selected based on last time that
 * the host was last accessed in order to respect the minimum access time limit.
 *
 * @author aeciosantos
 */
public class PolitenessScheduler {

    /**
     * Minimum links per domain to allow new links to be loaded in the scheduler. Ideally, the
     * scheduler should always have a reasonable number of links loaded per domain so that it can
     * always return a link to the downloader when requested.
     */
    private static final int MIN_LINKS_PER_DOMAIN_TO_ALLOW_LOAD = 2000;

    private final PriorityQueue<DomainNode> pendingQueue;
    private final Map<String, DomainNode> downloadingQueue;
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
        this.pendingQueue = createDomainPriorityQueue();
        this.downloadingQueue = new HashMap<>();
    }

    private PriorityQueue<DomainNode> createDomainPriorityQueue() {
        int initialCapacity = 10;
        return new PriorityQueue<>(initialCapacity, new Comparator<DomainNode>() {
            @Override
            public int compare(DomainNode o1, DomainNode o2) {
                return Long.compare(o1.lastAccessTime, o2.lastAccessTime);
            }
        });
    }

    public boolean addLink(LinkRelevance link) {

        removeExpiredNodes();

        if (numberOfLinks() >= maxLinksInScheduler) {
            return false; // ignore link
        }

        String domainName = link.getTopLevelDomainName();

        synchronized (this) {
            DomainNode domainNode = domains.get(domainName);
            if (domainNode == null) {
                domainNode = new DomainNode(domainName, 0l);
                domains.put(domainName, domainNode);
            }

            if (domainNode.isEmpty()) {
                emptyDomainsQueue.remove(domainNode);
                pendingQueue.add(domainNode);
            }

            if (domainNode.add(link)) {
                numberOfLinks.incrementAndGet();
            }
        }

        return true;
    }

    private synchronized void removeExpiredNodes() {
        while (true) {
            DomainNode node = emptyDomainsQueue.peek();
            if (node == null) {
                break;
            }

            long expirationTime = node.lastAccessTime + minimumAccessTime;
            if (System.currentTimeMillis() > expirationTime) {
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

            DomainNode domainNode = pendingQueue.peek();
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

            pendingQueue.poll();
            linkRelevance = domainNode.selectFirst();
            downloadingQueue.put(domainNode.domainName, domainNode);
        }

        return linkRelevance;
    }

    /**
     * Records the time when a link from the domain was last downloaded and returns the domain to
     * the the queue of pending domains (so that the next link can be downloaded) or to empty
     * domains list.
     */
    public void notifyDownloadFinished(LinkRelevance link) {
        String domainName = link.getTopLevelDomainName();
        synchronized (this) {
            DomainNode domainNode = downloadingQueue.get(domainName);
            if (domainNode == null) {
                return;
            }
            domainNode.lastAccessTime = System.currentTimeMillis();
            domainNode.removeDownloading(link);
            if (domainNode.isEmpty()) {
                emptyDomainsQueue.add(domainNode);
            } else {
                pendingQueue.add(domainNode);
            }
            downloadingQueue.remove(domainName);
            numberOfLinks.decrementAndGet();
        }
    }

    public int numberOfNonExpiredDomains() {
        removeExpiredNodes();
        return domains.size();
    }

    public int numberOfAvailableDomains() {
        int available = 0;
        for (DomainNode node : pendingQueue) {
            if (isAvailable(node)) {
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

    /**
     * Checks whether a link from the given domain can be downloaded now respecting the minimum
     * delay between requests.
     */
    private boolean isAvailable(DomainNode domainNode) {
        if (downloadingQueue.containsKey(domainNode.domainName)) {
            return false;
        }
        long now = System.currentTimeMillis();
        long timeSinceLastAccess = now - domainNode.lastAccessTime;
        if (timeSinceLastAccess < minimumAccessTime) {
            return false;
        }
        return true;
    }

    public boolean hasLinksAvailable() {
        // pick domain with longest access time
        DomainNode domainNode = pendingQueue.peek();
        if (domainNode == null) {
            return false;
        }
        return isAvailable(domainNode);
    }

    public synchronized void clearPendingQueue() {
        Iterator<Entry<String, DomainNode>> it = domains.entrySet().iterator();
        while (it.hasNext()) {
            DomainNode node = it.next().getValue();
            numberOfLinks.addAndGet(-node.pendingSize()); // adds negative value
            node.clearPendingQueue();
        }
        while (true) {
            DomainNode node = pendingQueue.poll();
            if (node == null) {
                break;
            }
            emptyDomainsQueue.add(node);
        }
    }

    /**
     * Checks whether the given link can be downloaded now according to the politeness constraints.
     * If the domain is not stored in the scheduler, it is because it was never downloaded or the
     * last time it was dowloaded was long time ago and thus it already expired. If the domain is
     * stored, than we check whether enough time has passed since the last time a link from the
     * domain was downloaded.
     */
    public boolean canDownloadNow(LinkRelevance link) {
        DomainNode domain = domains.get(link.getTopLevelDomainName());
        if (domain == null) {
            return true;
        } else {
            return isAvailable(domain);
        }
    }

    /**
     * Checks whether the given link can be inserted now. A link can be inserted if it is not
     * already present in the scheduler or if the number of links from this domain is bellow a given
     * minimum threshold. The goal is to always keep a significant number of links in the scheduler,
     * so that there is always a link available to return to the downloader when requested.
     */
    public boolean canInsertNow(LinkRelevance link) {
        String domainName = link.getTopLevelDomainName();
        DomainNode domain = domains.get(domainName);
        if (domain == null) {
            return true;
        } else {
            return domain.pendingSize() < MIN_LINKS_PER_DOMAIN_TO_ALLOW_LOAD && !domain.contains(link);
        }
    }

    public int numberOfDownloadingLinks() {
        return downloadingQueue.size();
    }
}
