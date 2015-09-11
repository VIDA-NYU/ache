package focusedCrawler.link.frontier;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.ParameterFile;

public class FrontierManagerFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(FrontierManagerFactory.class);

    public static FrontierManager create(LinkStorageConfig config,
                                  String configPath,
                                  String dataPath,
                                  String seedFile,
                                  String stoplistFile) {
        
        String[] seedUrls = ParameterFile.getSeeds(seedFile);
        HashMap<String, Integer> scope = extractDomains(seedUrls);
        
        String directory = Paths.get(dataPath, config.getLinkDirectory()).toString();
        
        Frontier frontier = null;
        if (config.isUseScope()) {
            frontier = new Frontier(directory, config.getMaxCacheUrlsSize(), scope);
        } else {
            frontier = new Frontier(directory, config.getMaxCacheUrlsSize());
        }
        
        LinkFilter linkFilter = new LinkFilter(configPath);
        
        LinkSelectionStrategy linkSelector = createLinkSelector(config);
        
        return new FrontierManager(
                frontier,
                config.getMaxSizeLinkQueue(),
                config.getMaxSizeLinkQueue(),
                linkSelector,
                linkFilter);
    }

    private static LinkSelectionStrategy createLinkSelector(LinkStorageConfig config) {
        if (config.isUseScope()) {
            if (config.getTypeOfClassifier().contains("Baseline")) {
                return new SiteLinkSelectionStrategy();
            } else {
                return new FrontierTargetRepository();
            }
        } else {
            if (config.getTypeOfClassifier().contains("Baseline")) {
                return new NonRandomLinkSelection();
            } else {
                return new FrontierTargetRepository();
            }
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
