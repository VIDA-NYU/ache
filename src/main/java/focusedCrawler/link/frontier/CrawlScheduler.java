package focusedCrawler.link.frontier;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import focusedCrawler.link.PolitenessScheduler;
import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.persistence.TupleIterator;

public class CrawlScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(CrawlScheduler.class);

    private final LinkSelector linkSelector;
    private final Frontier frontier;
    private final PolitenessScheduler scheduler;
    private final MetricsManager metricsManager;
    private final int linksToLoad;

    private boolean linksRejectedDuringLastLoad;
    private int availableLinksDuringLoad;
    private int rejectedLinksDuringLoad;
    private int uncrawledLinksDuringLoad;
    private int unavailableLinksDuringLoad;
    private Timer frontierLoadTimer;

    private LinkSelector recrawlSelector;
    
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

        Gauge<Integer> unavailableLinksGauge = () -> unavailableLinksDuringLoad;
        metricsManager.register("frontier_manager.last_load.unavailable", unavailableLinksGauge);
        
        Gauge<Integer> rejectedLinksGauge = () -> rejectedLinksDuringLoad;
        metricsManager.register("frontier_manager.last_load.rejected", rejectedLinksGauge);
        
        Gauge<Integer> uncrawledLinksGauge = () -> uncrawledLinksDuringLoad;
        metricsManager.register("frontier_manager.last_frontier_load.uncrawled", uncrawledLinksGauge);
        
        frontierLoadTimer = metricsManager.getTimer("frontier_manager.load.time");
    }
    
    private void loadQueue(int numberOfLinks) {
        logger.info("Loading more links from frontier into the scheduler...");
//        scheduler.clear();
        frontier.commit();
        Context timerContext = frontierLoadTimer.time();
        try(TupleIterator<LinkRelevance> it = frontier.iterator()) {
            
            int rejectedLinks = 0;
            int uncrawledLinks = 0;
            int availableLinks = 0;
            int unavailableLinks = 0;

            linkSelector.startSelection(numberOfLinks);
            if(recrawlSelector != null) {
                recrawlSelector.startSelection(numberOfLinks);
            }
            while(it.hasNext()) {
                Tuple<LinkRelevance> tuple = it.next();
                LinkRelevance link = tuple.getValue();
                
                String domainName = link.getTopLevelDomainName().trim();
                if (domainName != null && frontier.getRobotRulesMap().get(domainName) != null
                        && !frontier.getRobotRulesMap().get(domainName).isAllowed(link.getURL().toString())) {
                    logger.info(
                            "Ignoring link " + link.getURL().toString() + " as it is present in the robots file which is disallowed.");
                    continue;
                }
                
                // Links already downloaded or not relevant
                if (link.getRelevance() <= 0) {
                    if(recrawlSelector != null) {
                        recrawlSelector.evaluateLink(link);
                    }
                    continue;
                }
                
                uncrawledLinks++;
                // check whether link can be download now according to politeness constraints 
                if(scheduler.canDownloadNow(link)) {
                    // consider link to  be downloaded
                    linkSelector.evaluateLink(link);
                    availableLinks++;
                } else {
                    unavailableLinks++;
                    rejectedLinks++;
                }
            }
            
            if(recrawlSelector != null) {
                for(LinkRelevance link : recrawlSelector.getSelectedLinks()) {
                    scheduler.addLink(link);
                }
            }
            
            List<LinkRelevance> selectedLinks = linkSelector.getSelectedLinks();
            
            int linksAdded = 0;
            for (LinkRelevance link : selectedLinks) {
                boolean addedLink = scheduler.addLink(link);
                if(addedLink) {
                    linksAdded++;
                } else {
                    rejectedLinks++;
                }
            }
            
            this.availableLinksDuringLoad = availableLinks;
            this.unavailableLinksDuringLoad = unavailableLinks;
            this.uncrawledLinksDuringLoad = uncrawledLinks;
            this.rejectedLinksDuringLoad = rejectedLinks;
            this.linksRejectedDuringLastLoad = rejectedLinks > 0;
            
            logger.info("Loaded {} links.", linksAdded);
        } catch (Exception e) {
            logger.error("Failed to read items from the frontier.", e);
        } finally {
            timerContext.stop();
        }
    }
    
    public boolean hasPendingLinks() {
        return scheduler.hasPendingLinks() || linksRejectedDuringLastLoad || recrawlSelector != null;
    }

    public LinkRelevance nextLink() {
        if (!scheduler.hasLinksAvailable()) {
            loadQueue(linksToLoad);
        }
        return scheduler.nextLink();
    }

    public void reload() {
        loadQueue(linksToLoad);
    }

}
