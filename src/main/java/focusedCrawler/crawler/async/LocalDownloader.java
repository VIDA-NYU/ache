package focusedCrawler.crawler.async;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import focusedCrawler.crawler.async.HttpDownloader.Callback;
import focusedCrawler.crawler.async.RobotsTxtHandler.RobotsData;
import focusedCrawler.crawler.async.SitemapXmlHandler.SitemapData;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.StorageException;

public class LocalDownloader implements Downloader {
    
    private static Logger logger = LoggerFactory.getLogger(LocalDownloader.class);
    
    private final TargetStorage targetStorage;
    private final LinkStorage linkStorage;
    private final HttpDownloader downloader;
    private final Map<LinkRelevance.Type, HttpDownloader.Callback> handlers = new HashMap<>();
    private final EventBus eventBus;

    public LocalDownloader(AsyncCrawlerConfig crawlerConfig, String dataPath,
                           TargetStorage targetStorage, LinkStorage linkStorage,
                           MetricsManager metricsManager) {
        
        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
        this.downloader = new HttpDownloader(crawlerConfig.getDownloaderConfig(), dataPath, metricsManager);
        
        this.eventBus = new EventBus();
        this.eventBus.register(this);
        this.handlers.put(LinkRelevance.Type.FORWARD, new FetchedResultHandler(eventBus));
        this.handlers.put(LinkRelevance.Type.SITEMAP, new SitemapXmlHandler(eventBus));
        this.handlers.put(LinkRelevance.Type.ROBOTS,  new RobotsTxtHandler(eventBus, crawlerConfig.getDownloaderConfig().getUserAgentName()));
    }

    @Override
    public void dispatchDownload(LinkRelevance link) {
        Callback handler = handlers.get(link.getType());
        if(handler != null) {
            downloader.dipatchDownload(link, handler);
        } else {
            logger.error("No registered handler for link type: "+link.getType());
        }
    }
    
    @Subscribe
    public void sendToStorage(SitemapData sitemapData) {
        linkStorage.insert(sitemapData);
    }
    
    @Subscribe
    public void sendToStorage(RobotsData robotsData) {
        linkStorage.insert(robotsData);
    }
    
    @Subscribe
    public void sendToStorage(Page page) throws StorageException {
        targetStorage.insert(page);
    }

    @Override
    public boolean hasPendingDownloads() {
        return downloader.hasPendingDownloads();
    }

    @Override
    public void close() {
        downloader.close();
    }

}
