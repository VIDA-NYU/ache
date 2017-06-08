package focusedCrawler.link.frontier;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.link.frontier.selector.MaximizeWebsitesLinkSelector;
import focusedCrawler.link.frontier.selector.MultiLevelLinkSelector;
import focusedCrawler.link.frontier.selector.NonRandomLinkSelector;
import focusedCrawler.link.frontier.selector.RandomLinkSelector;
import focusedCrawler.link.frontier.selector.SitemapsRecrawlSelector;
import focusedCrawler.link.frontier.selector.TopkLinkSelector;
import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.ParameterFile;

public class FrontierManagerFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(FrontierManagerFactory.class);

    public static FrontierManager create(LinkStorageConfig config,
                                         String configPath,
                                         String dataPath,
                                         String modelPath,
                                         String seedFile,
                                         MetricsManager metricsManager) {
        
        String[] seedUrls = ParameterFile.getSeeds(seedFile);
        
        String directory = Paths.get(dataPath, config.getLinkDirectory()).toString();
        
        Frontier frontier = null;
        if (config.isUseScope()) {
            Map<String, Integer> scope = extractDomains(seedUrls);
            frontier = new Frontier(directory, config.getMaxCacheUrlsSize(), scope);
        } else {
            frontier = new Frontier(directory, config.getMaxCacheUrlsSize());
        }
        
        LinkFilter linkFilter = new LinkFilter(configPath);
        
        LinkSelector linkSelector = createLinkSelector(config);
        logger.info("LINK_SELECTOR: " + linkSelector.getClass().getName());
        
        LinkSelector recrawlSelector = createRecrawlSelector(config);
        
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                                                              linkSelector, recrawlSelector, linkFilter,
                                                              metricsManager);
        frontierManager.addSeeds(seedUrls);
        return frontierManager;
    }

    private static LinkSelector createLinkSelector(LinkStorageConfig config) {
        String linkSelector = config.getLinkSelector();
        if (linkSelector == null || linkSelector.isEmpty()) {
            throw new IllegalArgumentException("Link selector not configured: " + linkSelector);
        }
        
        if (linkSelector.equals("TopkLinkSelector")) {
            return new TopkLinkSelector();
        } else if (linkSelector.equals("RandomLinkSelector")) {
            return new RandomLinkSelector();
        } else if (linkSelector.equals("NonRandomLinkSelector")) {
            return new NonRandomLinkSelector();
        } else if (linkSelector.equals("MultiLevelLinkSelector")) {
            return new MultiLevelLinkSelector();
        } else if (linkSelector.equals("MaximizeWebsitesLinkSelector")) {
            return new MaximizeWebsitesLinkSelector();
        } else {
            throw new IllegalArgumentException("Unknown link selector configured: " + linkSelector);
        }
    }
    
    private static LinkSelector createRecrawlSelector(LinkStorageConfig config) {
        String recrawlSelector = config.getRecrawlSelector();
        if (recrawlSelector == null || recrawlSelector.isEmpty()) {
            return null;
        }
        switch(recrawlSelector) {
            case "SitemapsRecrawlSelector":
                return new SitemapsRecrawlSelector(config.getSitemapsRecrawlInterval());
            default:
                throw new IllegalArgumentException("Unknown recrawl selector configured: " + recrawlSelector);
        }
    }

    private static HashMap<String, Integer> extractDomains(String[] urls) {
        HashMap<String, Integer> scope = new HashMap<String, Integer>();
        for (int i = 0; i < urls.length; i++) {
            try {
                URL url = new URL(urls[i]);
                String host = url.getHost();
                scope.put(host, new Integer(1));
            } catch (MalformedURLException e) {
                logger.warn("Invalid URL in seeds file. Ignoring URL: " + urls[i]);
            }
        }
        logger.info("Using scope of following domains:");
        for (String host: scope.keySet()) {
            logger.info(host);
        }
        return scope;
    }

}
