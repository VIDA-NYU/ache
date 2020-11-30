package achecrawler.link.frontier;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import achecrawler.link.BipartiteGraphRepository;
import achecrawler.link.LinkStorageConfig;
import achecrawler.link.backlink.BacklinkSurfer;
import achecrawler.link.classifier.LinkClassifier;
import achecrawler.link.classifier.LinkClassifierException;
import achecrawler.link.classifier.LinkClassifierFactory;
import achecrawler.link.classifier.LinkClassifierHub;
import achecrawler.link.frontier.selector.LinkSelector;
import achecrawler.target.model.Page;
import achecrawler.util.DataNotFoundException;
import achecrawler.util.LinkFilter;
import achecrawler.util.LogFile;
import achecrawler.util.MetricsManager;
import achecrawler.util.parser.BackLinkNeighborhood;
import achecrawler.util.parser.LinkNeighborhood;

/**
 * This class manages the crawler frontier
 * 
 * @author Luciano Barbosa
 * @version 1.0
 */

public class FrontierManager {

    private static final Logger logger = LoggerFactory.getLogger(FrontierManager.class);

    private final int maxPagesPerDomain;
    private final int linksToLoad;
    private final boolean downloadRobots;
    private final boolean insertSitemaps;
    private final boolean disallowSitesInRobotsFile;
    private final boolean useScope;

    private BacklinkSurfer backlinkSurfer;
    private LinkClassifier backlinkClassifier;
    private LinkClassifier outlinkClassifier;
    private HashMap<String, Integer> domainCounter;

    private final BipartiteGraphRepository graphRepository;
    private final CrawlScheduler scheduler;
    private final Frontier frontier;
    private final LinkFilter linkFilter;
    private final HostManager hostsManager;
    private final LogFile schedulerLog;
    private final MetricsManager metricsManager;
    private final Set<String> scope = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private Timer insertTimer;
	private Timer selectTimer;

    private PrintStream seedScopeFile;

    public FrontierManager(Frontier frontier, String dataPath, String modelPath,
                           LinkStorageConfig config, LinkSelector linkSelector,
                           LinkSelector recrawlSelector, LinkFilter linkFilter,
                           MetricsManager metricsManager) {

        this.frontier = frontier;
        this.linkFilter = linkFilter;
        this.metricsManager = metricsManager;
        this.insertSitemaps = config.getDownloadSitemapXml();
        this.disallowSitesInRobotsFile = config.getDisallowSitesInRobotsFile();
        this.downloadRobots = getDownloadRobots();
        this.linksToLoad = config.getSchedulerMaxLinks();
        this.maxPagesPerDomain = config.getMaxPagesPerDomain();
        this.domainCounter = new HashMap<String, Integer>();
        this.scheduler = new CrawlScheduler(linkSelector, recrawlSelector, frontier, metricsManager,
                                            config.getSchedulerHostMinAccessInterval(), linksToLoad);
        this.graphRepository = new BipartiteGraphRepository(dataPath, config.getPersistentHashtableBackend());
        this.hostsManager = new HostManager(Paths.get(dataPath, "data_hosts"), config.getPersistentHashtableBackend());;
        this.schedulerLog = new LogFile(Paths.get(dataPath, "data_monitor", "scheduledlinks.csv"));
        this.outlinkClassifier = LinkClassifierFactory.create(modelPath, config);
        if (config.getBacklinks()) {
            this.backlinkSurfer = new BacklinkSurfer(config.getBackSurferConfig());
            this.backlinkClassifier = new LinkClassifierHub();
        }
        this.useScope = config.isUseScope();
        this.openSeedScopeFile(dataPath);
        this.setupMetrics();
    }

    private void openSeedScopeFile(String dataPath) {
        Path path = Paths.get(dataPath, "seeds_scope.txt");
        try {
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    scope.add(new URL(line).getHost());
                }
            }
            this.seedScopeFile = new PrintStream(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file: " + path.toString());
        }
    }

    private void setupMetrics() {
        this.insertTimer = metricsManager.getTimer("frontier_manager.insert.time");
        this.selectTimer = metricsManager.getTimer("frontier_manager.select.time");
    }

    public void forceReload() {
        scheduler.reload();
    }

    public boolean isRelevant(LinkRelevance link) throws FrontierPersistentException {
        if (link.getRelevance() <= 0) {
            return false;
        }

        if (useScope && !scope.contains(link.getURL().getHost())) {
            return false;
        }

        if (disallowSitesInRobotsFile && frontier.isDisallowedByRobots(link)) {
            return false;
        }

        Double value = frontier.exist(link);
        if (value != null) {
            return false;
        }

        if (!linkFilter.accept(link)) {
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
            if (linkRelevance == null) {
                return false;
            }
            boolean insert = isRelevant(linkRelevance);
            if (insert) {
                if (downloadRobots) {
                    URL url = linkRelevance.getURL();
                    String hostName = url.getHost();
                    if (!hostsManager.isKnown(hostName)) {
                        hostsManager.insert(hostName);
                        try {
                            URL robotsUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/robots.txt");
                            LinkRelevance sitemap = LinkRelevance.createRobots(robotsUrl.toString(), 299);
                            frontier.insert(sitemap);
                        } catch (Exception e) {
                            logger.warn("Failed to insert robots.txt for host: " + hostName, e);
                        }
                    }
                }
                insert = frontier.insert(linkRelevance);
                scheduler.notifyLinkInserted();
            }
            return insert;
        } finally {
            timerContext.stop();
        }
    }

    public LinkRelevance nextURL() throws FrontierPersistentException, DataNotFoundException {
        return nextURL(false);
    }
    
    public LinkRelevance nextURL(boolean asyncLoad) throws FrontierPersistentException, DataNotFoundException {
        Context timerContext = selectTimer.time();
        try {
            LinkRelevance link = scheduler.nextLink(asyncLoad);
            if (link == null) {
                if (scheduler.hasPendingLinks()) {
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
        seedScopeFile.close();
    }

    public Frontier getFrontier() {
        return frontier;
    }

    public void addSeeds(List<String> seeds) {
        if (seeds != null && seeds.size() > 0) {
            int count = 0;
            int errors = 0;
            logger.info("Adding {} seed URL(s)...", seeds.size());
            for (String seed : seeds) {
                try {
                    LinkRelevance link = LinkRelevance.createForward(seed, LinkRelevance.DEFAULT_RELEVANCE);
                    if (link == null) {
                        logger.warn("Invalid seed URL provided: " + seed);
                        errors++;
                        continue;
                    }
                    addSeedScope(link);
                    boolean inserted = insert(link);
                    if (inserted) {
                        logger.info("Added seed URL: {}", seed);
                        count++;
                    }
                } catch (FrontierPersistentException e) {
                    throw new RuntimeException("Failed to insert seed URL: " + seed, e);
                }
            }
            frontier.commit();
            logger.info("Number of seeds added: " + count);
            if (errors > 0) {
                logger.info("Number of invalid seeds: " + errors);
            }
            logger.info("Using scope of following domains:");
            for (String host : scope) {
                logger.info(host);
            }
        }
    }

    public void addSeedScope(LinkRelevance link) {
        if (useScope) {
            scope.add(link.getURL().getHost());
            seedScopeFile.println(link.getURL().toString());
            seedScopeFile.flush();
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
    
    public void updateOutlinkClassifier(LinkClassifier classifier) throws Exception {
        this.outlinkClassifier = classifier;
        graphRepository.visitLNs((LinkNeighborhood ln) -> {
            try {
                LinkRelevance lr = outlinkClassifier.classify(ln);
                frontier.update(lr);
            } catch (Exception e) {
                logger.error("Failed to classify link neighborhood while updating classifier.", e);
            }
        });
        frontier.commit();
    }

    public BipartiteGraphRepository getGraphRepository() {
        return this.graphRepository;
    }

    /**
     * Returns true if either the property to include sitemaps is true or disallow sites in
     * robots.txt is true
     * 
     * @return
     */
    private boolean getDownloadRobots() {
        return insertSitemaps || disallowSitesInRobotsFile;
    }

}
