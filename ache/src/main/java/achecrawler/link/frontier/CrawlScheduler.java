package achecrawler.link.frontier;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import achecrawler.link.PolitenessScheduler;
import achecrawler.link.frontier.selector.LinkSelector;
import achecrawler.util.MetricsManager;
import achecrawler.util.persistence.TupleIterator;

/**
 * This class manages the crawler scheduling process. All links known by the crawler are stored in
 * the Frontier (which is usually very large to fit in main memory). These links are periodically
 * loaded into the scheduler (in-memory) based on their link priorities. Not all links can be
 * loaded, so LinkSelector are used to determine which links should be loaded from the frontier to
 * the scheduler. In order to be efficient and keep a high crawling rate, the scheduler should
 * always have links available to be returned to the crawler agent
 * (#{@link achecrawler.crawler.async.AsyncCrawler).
 * 
 * @author aeciosantos
 *
 */
public class CrawlScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CrawlScheduler.class);

    private final LinkSelector linkSelector;
    private final LinkSelector recrawlSelector;
    private final Frontier frontier;
    private final PolitenessScheduler scheduler;
    private final MetricsManager metricsManager;
    private final int linksToLoad;

    private boolean hasUncrawledLinks = true;
    private int availableLinksDuringLoad = -1;
    private int rejectedLinksDuringLoad = -1;
    private int uncrawledLinksDuringLoad = -1;
    private Timer frontierLoadTimer;
    private AtomicBoolean loadIsRunning = new AtomicBoolean(false);
    
    public CrawlScheduler(LinkSelector linkSelector, LinkSelector recrawlSelector,
                          Frontier frontier, MetricsManager metricsManager, 
                          int minAccessTime, int linksToLoad) {
        this.linkSelector = linkSelector;
        this.recrawlSelector = recrawlSelector;
        this.frontier = frontier;
        this.metricsManager = metricsManager;
        this.linksToLoad = linksToLoad;
        this.scheduler = new PolitenessScheduler(minAccessTime, linksToLoad);
        this.setupMetrics();
        this.loadQueue(this.linksToLoad);
    }

    private void setupMetrics() {
        Gauge<Integer> numberOfLinksGauge = () -> scheduler.numberOfLinks();
        metricsManager.register("frontier_manager.scheduler.number_of_links", numberOfLinksGauge);
        
        Gauge<Integer> nonExpiredDomainsGauge = () -> scheduler.numberOfNonExpiredDomains();
        metricsManager.register("frontier_manager.scheduler.non_expired_domains", nonExpiredDomainsGauge);
        
        Gauge<Integer> emptyDomainsGauge = () -> scheduler.numberOfEmptyDomains();
        metricsManager.register("frontier_manager.scheduler.empty_domains", emptyDomainsGauge);
        
        Gauge<Integer> availableLinksGauge = () -> availableLinksDuringLoad;
        metricsManager.register("frontier_manager.last_load.available", availableLinksGauge);

        Gauge<Integer> rejectedLinksGauge = () -> rejectedLinksDuringLoad;
        metricsManager.register("frontier_manager.last_load.rejected", rejectedLinksGauge);
        
        Gauge<Integer> uncrawledLinksGauge = () -> uncrawledLinksDuringLoad;
        metricsManager.register("frontier_manager.last_load.uncrawled", uncrawledLinksGauge);
        
        frontierLoadTimer = metricsManager.getTimer("frontier_manager.load.time");
    }
    
    /**
     * This method loads links from the frontier (stored in disk) to the scheduler. The is scheduler
     * is a in-memory data structure that prioritizes links base on score and also politeness
     * constraints. Which links are selected to be inserted in the frontier is determined the policy
     * implemented by the LinkSelector configured.
     * 
     * @param numberOfLinks
     */
    private synchronized void loadQueue(int numberOfLinks) {
        logger.info("Loading more links from frontier into the scheduler...");
        frontier.commit();

        Context timerContext = frontierLoadTimer.time();
        try (TupleIterator<LinkRelevance> it = frontier.iterator()) {

            int rejectedLinks = 0;
            int uncrawledLinks = 0;
            int linksAvailable = 0;

            this.startSelection(numberOfLinks);

            while (it.hasNext()) {
                try {
                    LinkRelevance link = it.next().getValue();

                    if (frontier.isDisallowedByRobots(link)) {
                        continue;
                    }

                    // Links already downloaded or not relevant
                    if (link.getRelevance() <= 0) {
                        if (recrawlSelector != null) {
                            recrawlSelector.evaluateLink(link);
                        }
                        continue;
                    }

                    uncrawledLinks++;

                    // check whether link can be download now according to politeness constraints
                    if (scheduler.canInsertNow(link)) {
                        // consider link to be downloaded
                        linkSelector.evaluateLink(link);
                        linksAvailable++;
                    } else {
                        rejectedLinks++;
                    }
                } catch (Exception e) {
                    // just log the exception and continue the load even when some link fails
                    logger.error("Failed to load link in frontier.", e);
                }
            }

            this.addSelectedLinksToScheduler(recrawlSelector);
            rejectedLinks += this.addSelectedLinksToScheduler(linkSelector);

            this.uncrawledLinksDuringLoad = uncrawledLinks;
            this.rejectedLinksDuringLoad = rejectedLinks;
            this.availableLinksDuringLoad = linksAvailable;
            
            this.hasUncrawledLinks = rejectedLinks != 0 || uncrawledLinks != 0;

        } catch (Exception e) {
            logger.error("Failed to read items from the frontier.", e);
        } finally {
            timerContext.stop();
        }
    }
    
    public void notifyLinkInserted() {
        this.hasUncrawledLinks = true;
    }

    private void startSelection(int numberOfLinks) {
        if (linkSelector != null) {
            linkSelector.startSelection(numberOfLinks);
        }
        if (recrawlSelector != null) {
            recrawlSelector.startSelection(numberOfLinks);
        }
    }

    private int addSelectedLinksToScheduler(LinkSelector selector) {
        int rejectedLinks = 0;
        int linksAdded = 0;
        if (selector != null) {
            List<LinkRelevance> links = selector.getSelectedLinks();
            for (LinkRelevance link : links) {
                if (scheduler.addLink(link)) {
                    linksAdded++;
                } else {
                    rejectedLinks++;
                }
            }
            logger.info("Loaded {} links.", linksAdded);
        }
        return rejectedLinks;
    }

    public boolean hasPendingLinks() {
        return hasPendingLinksInFrontier() || scheduler.hasPendingLinks();
    }

    private boolean hasPendingLinksInFrontier() {
        return hasUncrawledLinks || recrawlSelector != null || loadIsRunning.get();
    }

    public LinkRelevance nextLink(boolean asyncLoad) {
        if (!scheduler.hasLinksAvailable()) {
            maybeLoadQueue(asyncLoad);
        }
        return scheduler.nextLink();
    }

    private void maybeLoadQueue(boolean asyncLoad) {
        if (!hasPendingLinksInFrontier()) {
            return;
        }
        if (!asyncLoad) {
            reload();
            return;
        }
        if (loadIsRunning.compareAndSet(false, true)) {
            Thread loaderThread = new Thread(() -> {
                try {
                    logger.info("Starting scheduler queues reload...");
                    reload();
                } finally {
                    loadIsRunning.set(false);
                    logger.info("Reload done.");
                }
            });
            loaderThread.setName("FrontierLinkLoader");
            loaderThread.start();
        }
    }

    public void reload() {
        loadQueue(linksToLoad);
    }

}
