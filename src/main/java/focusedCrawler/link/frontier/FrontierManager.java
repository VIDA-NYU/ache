package focusedCrawler.link.frontier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import focusedCrawler.link.BipartiteGraphRepository;
import focusedCrawler.link.DownloadScheduler;
import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.link.backlink.BacklinkSurfer;
import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierException;
import focusedCrawler.link.classifier.LinkClassifierFactory;
import focusedCrawler.link.classifier.LinkClassifierHub;
import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.LogFile;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.persistence.TupleIterator;

/**
 * This class manages the crawler frontier
 * 
 * @author Luciano Barbosa
 * @version 1.0
 */

public class FrontierManager {

    private static final Logger logger = LoggerFactory.getLogger(FrontierManager.class);

    private BacklinkSurfer backlinkSurfer;
    private LinkClassifier backlinkClassifier;
    private LinkClassifier outlinkClassifier;
    private BipartiteGraphRepository graphRepository;
    
    private int maxPagesPerDomain;
    private HashMap<String, Integer> domainCounter;
    
    private final Frontier frontier;
    private final LinkFilter linkFilter;
    private final LinkSelector linkSelector;
    private final int linksToLoad;
    private final HostManager hostsManager;
    private final boolean downloadRobots;
    private final DownloadScheduler scheduler;
    private final LogFile schedulerLog;
    private final MetricsManager metricsManager;

    private boolean linksRejectedDuringLastLoad;
    private int availableLinksDuringLoad;
    private int rejectedLinksDuringLoad;
    private int uncrawledLinksDuringLoad;
    private int unavailableLinksDuringLoad;
	private Timer frontierLoadTimer;
	private Timer insertTimer;
	private Timer selectTimer;

    public FrontierManager(Frontier frontier, String dataPath, String modelPath,
                           LinkStorageConfig config, LinkSelector linkSelector,
                           LinkFilter linkFilter, MetricsManager metricsManager) {

        this.frontier = frontier;
        this.linkFilter = linkFilter;
        this.linkSelector = linkSelector;
        this.metricsManager = metricsManager;
        this.downloadRobots = config.getDownloadSitemapXml();
        this.linksToLoad = config.getSchedulerMaxLinks();
        this.maxPagesPerDomain = config.getMaxPagesPerDomain();
        this.domainCounter = new HashMap<String, Integer>();
        this.scheduler = new DownloadScheduler(config.getSchedulerHostMinAccessInterval(), linksToLoad);
        this.graphRepository = new BipartiteGraphRepository(dataPath);
        this.hostsManager = new HostManager(Paths.get(dataPath, "data_hosts"));;
        this.schedulerLog = new LogFile(Paths.get(dataPath, "data_monitor", "scheduledlinks.csv"));
        this.outlinkClassifier = LinkClassifierFactory.create(modelPath, config.getTypeOfClassifier());
        if (config.getBacklinks()) {
            this.backlinkSurfer = new BacklinkSurfer(config.getBackSurferConfig());
            this.backlinkClassifier = new LinkClassifierHub();
        }
        this.setupMetrics();
        this.loadQueue(linksToLoad);
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
        insertTimer = metricsManager.getTimer("frontier_manager.insert.time");
        selectTimer = metricsManager.getTimer("frontier_manager.select.time");

    }

    public void clearFrontier() {
        logger.info("Cleaning frontier... current queue size: " + scheduler.numberOfLinks());
        scheduler.clear();
        logger.info("# Queue size:" + scheduler.numberOfLinks());
    }

    private void loadQueue(int numberOfLinks) {
        logger.info("Loading more links from frontier into the scheduler...");
        scheduler.clear();
        frontier.commit();
        Context timerContext = frontierLoadTimer.time();
        try(TupleIterator<LinkRelevance> it = frontier.iterator()) {
            
            int rejectedLinks = 0;
            int uncrawledLinks = 0;
            int availableLinks = 0;
            int unavailableLinks = 0;

            linkSelector.startSelection(numberOfLinks);
            while(it.hasNext()) {
                Tuple<LinkRelevance> tuple = it.next();
                LinkRelevance link = tuple.getValue();
                
                // Links already downloaded or not relevant
                if (link.getRelevance() <= 0) {
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

    public boolean isRelevant(LinkRelevance elem) throws FrontierPersistentException {
        if (elem.getRelevance() <= 0) {
            return false;
        }

        Integer value = frontier.exist(elem);
        if (value != null) {
            return false;
        }

        String url = elem.getURL().toString();
        if (linkFilter.accept(url) == false) {
            return false;
        }

        return true;
    }

    public void insert(LinkRelevance[] linkRelevance) throws FrontierPersistentException {
        for (int i = 0; i < linkRelevance.length; i++) {
            LinkRelevance elem = linkRelevance[i];
            this.insert(elem);
        }
    }

    public boolean insert(LinkRelevance linkRelevance) throws FrontierPersistentException {
        Context timerContext = insertTimer.time();
        try {
            boolean insert = isRelevant(linkRelevance);
            if (insert) {
                if (downloadRobots) {
                    URL url = linkRelevance.getURL();
                    String hostName = url.getHost();
                    if (!hostsManager.isKnown(hostName)) {
                        hostsManager.insert(hostName);
                        try {
                            URL robotUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/robots.txt");
                            LinkRelevance sitemap = new LinkRelevance(robotUrl, 299, LinkRelevance.Type.ROBOTS);
                            frontier.insert(sitemap);
                        } catch (Exception e) {
                            logger.warn("Failed to insert robots.txt for host: " + hostName, e);
                        }
                    }
                }
                insert = frontier.insert(linkRelevance);
            }
            return insert;
        } finally {
            timerContext.stop();
        }
    }

    public LinkRelevance nextURL() throws FrontierPersistentException, DataNotFoundException {
        Context timerContext = selectTimer.time();
        try {
            if (!scheduler.hasLinksAvailable()) {
                loadQueue(linksToLoad);
            }

            LinkRelevance link = scheduler.nextLink();
            if (link == null) {
                if (scheduler.hasPendingLinks() || linksRejectedDuringLastLoad) {
                    throw new DataNotFoundException(false, "No links available for selection right now.");
                } else {
                    throw new DataNotFoundException(true, "Frontier run out of links.");
                }
            }

            frontier.delete(link);

            schedulerLog.printf("%d\t%.5f\t%s\n", System.currentTimeMillis(),
                                link.getRelevance(), link.getURL().toString());

            return link;
        } finally {
            timerContext.stop();
        }
    }

    public void close() {
        graphRepository.close();
        frontier.commit();
        frontier.close();
        hostsManager.close();
        schedulerLog.close();
    }

    public Frontier getFrontier() {
        return frontier;
    }

    public void addSeeds(String[] seeds) {
        if (seeds != null && seeds.length > 0) {
            int count = 0;
            logger.info("Adding {} seed URL(s)...", seeds.length);
            for (String seed : seeds) {

                URL seedUrl;
                try {
                    seedUrl = new URL(seed);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Invalid seed URL provided: " + seed, e);
                }
                LinkRelevance link = new LinkRelevance(seedUrl, LinkRelevance.DEFAULT_RELEVANCE);
                try {
                    boolean inserted = insert(link);
                    if (inserted) {
                        logger.info("Added seed URL: {}", seed);
                        count++;
                    }
                } catch (FrontierPersistentException e) {
                    throw new RuntimeException("Failed to insert seed URL: " + seed, e);
                }
            }
            logger.info("Number of seeds added: " + count);
        }
    }

    public void insertOutlinks(Page page)
            throws IOException, FrontierPersistentException, LinkClassifierException {

        LinkRelevance[] linksRelevance = outlinkClassifier.classify(page);

        ArrayList<LinkRelevance> temp = new ArrayList<LinkRelevance>();
        HashSet<String> relevantURLs = new HashSet<String>();

        for (int i = 0; i < linksRelevance.length; i++) {
            if (this.isRelevant(linksRelevance[i])) {

                String url = linksRelevance[i].getURL().toString();
                if (!relevantURLs.contains(url)) {

                    String domain = linksRelevance[i].getTopLevelDomainName();

                    Integer domainCount;
                    synchronized (domainCounter) {
                        domainCount = domainCounter.get(domain);
                        if (domainCount == null) {
                            domainCount = 0;
                        } else {
                            domainCount++;
                        }
                        domainCounter.put(domain, domainCount);
                    }

                    if (domainCount < maxPagesPerDomain) { // Stop Condition
                        relevantURLs.add(url);
                        temp.add(linksRelevance[i]);
                    }

                }
            }
        }

        LinkRelevance[] filteredLinksRelevance =
                temp.toArray(new LinkRelevance[relevantURLs.size()]);

        LinkNeighborhood[] lns = page.getParsedData().getLinkNeighborhood();
        for (int i = 0; i < lns.length; i++) {
            if (!relevantURLs.contains(lns[i].getLink().toString())) {
                lns[i] = null;
            }
        }

        graphRepository.insertOutlinks(page.getURL(), lns);
        this.insert(filteredLinksRelevance);
    }

    public void insertBacklinks(Page page)
            throws IOException, FrontierPersistentException, LinkClassifierException {
        URL url = page.getURL();
        BackLinkNeighborhood[] links = graphRepository.getBacklinks(url);
        if (links == null || (links != null && links.length < 10)) {
            links = backlinkSurfer.getLNBacklinks(url);
        }
        if (links != null && links.length > 0) {
            LinkRelevance[] linksRelevance = new LinkRelevance[links.length];
            for (int i = 0; i < links.length; i++) {
                BackLinkNeighborhood backlink = links[i];
                if (backlink != null) {
                    LinkNeighborhood ln = new LinkNeighborhood(new URL(backlink.getLink()));
                    String title = backlink.getTitle();
                    if (title != null) {
                        ln.setAround(tokenizeText(title));
                    }
                    linksRelevance[i] = backlinkClassifier.classify(ln);
                }
            }
            this.insert(linksRelevance);
        }
        URL normalizedURL = new URL(url.getProtocol(), url.getHost(), "/");
        graphRepository.insertBacklinks(normalizedURL, links);
    }

    private String[] tokenizeText(String text) {
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        Vector<String> anchorTemp = new Vector<String>();
        while (tokenizer.hasMoreTokens()) {
            anchorTemp.add(tokenizer.nextToken());
        }
        String[] aroundArray = new String[anchorTemp.size()];
        anchorTemp.toArray(aroundArray);
        return aroundArray;
    }

    public void setBacklinkClassifier(LinkClassifier classifier) {
        this.backlinkClassifier = classifier;
    }

    public void setOutlinkClassifier(LinkClassifier classifier) {
        this.outlinkClassifier = classifier;
    }

    public BipartiteGraphRepository getGraphRepository() {
        return this.graphRepository;
    }

}
