package focusedCrawler.link.frontier;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.link.frontier.selector.MaximizeWebsitesLinkSelector;
import focusedCrawler.link.frontier.selector.MinRelevanceRecrawlSelector;
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

    public static FrontierManager create(LinkStorageConfig config, String configPath,
            String dataPath, String modelPath, String seedFile, MetricsManager metricsManager) {

        String directory = Paths.get(dataPath, config.getLinkDirectory()).toString();

        Frontier frontier = new Frontier(directory, config.getMaxCacheUrlsSize(),
                config.getPersistentHashtableBackend());

        LinkFilter linkFilter = new LinkFilter.Builder().withConfigPath(configPath).build();

        LinkSelector linkSelector = createLinkSelector(config);
        logger.info("LINK_SELECTOR: " + linkSelector.getClass().getName());

        LinkSelector recrawlSelector = createRecrawlSelector(config);

        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, recrawlSelector, linkFilter,
                metricsManager);
        if (seedFile != null) {
            frontierManager.addSeeds(ParameterFile.getSeeds(seedFile));
        }
        return frontierManager;
    }

    private static LinkSelector createLinkSelector(LinkStorageConfig config) {
        String linkSelector = config.getLinkSelector();
        if (linkSelector == null || linkSelector.isEmpty()) {
            throw new IllegalArgumentException("Link selector not configured: " + linkSelector);
        }
        if (linkSelector.equals("TopkLinkSelector")) {
            return new TopkLinkSelector(config.getLinkSelectorMinRelevance());
        } else if (linkSelector.equals("RandomLinkSelector")) {
            return new RandomLinkSelector();
        } else if (linkSelector.equals("NonRandomLinkSelector")) {
            return new NonRandomLinkSelector();
        } else if (linkSelector.equals("MultiLevelLinkSelector")) {
            return new MultiLevelLinkSelector();
        } else if (linkSelector.equals("MaximizeWebsitesLinkSelector")) {
            return new MaximizeWebsitesLinkSelector(config.getLinkSelectorMinRelevance());
        } else {
            throw new IllegalArgumentException("Unknown link selector configured: " + linkSelector);
        }
    }

    private static LinkSelector createRecrawlSelector(LinkStorageConfig config) {
        String recrawlSelector = config.getRecrawlSelector();
        if (recrawlSelector == null || recrawlSelector.isEmpty()) {
            return null;
        }
        switch (recrawlSelector) {
            case "SitemapsRecrawlSelector":
                return new SitemapsRecrawlSelector(config.getSitemapsRecrawlInterval());
            case "MinRelevanceRecrawlSelector":
                return new MinRelevanceRecrawlSelector(config.getRecrawlMinRelevanceInterval(),
                        config.getRecrawlMinRelevance(), config.getMinRelevanceRecrawlRobots(),
                        config.getMinRelevanceRecrawlSitemaps());
            default:
                throw new IllegalArgumentException(
                        "Unknown recrawl selector configured: " + recrawlSelector);
        }
    }

}
