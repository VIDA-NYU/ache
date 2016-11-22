package focusedCrawler.distributed;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;

import focusedCrawler.crawler.async.AsyncCrawlerConfig;
import focusedCrawler.crawler.async.FetchedResultHandler;
import focusedCrawler.crawler.async.HttpDownloader;
import focusedCrawler.crawler.async.HttpDownloader.Callback;
import focusedCrawler.crawler.async.RobotsTxtHandler;
import focusedCrawler.crawler.async.RobotsTxtHandler.RobotsData;
import focusedCrawler.crawler.async.SitemapXmlHandler;
import focusedCrawler.crawler.async.SitemapXmlHandler.SitemapData;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.MetricsManager;

public class FetcherNode {
	
    private static Logger logger = LoggerFactory.getLogger(FetcherNode.class);
    
    private volatile boolean stop = false;
    private Object running = new Object();

	private final HttpDownloader downloader;
    private final Map<LinkRelevance.Type, HttpDownloader.Callback> handlers = new HashMap<>();
    
	private final EventBus eventBus;
    private final IQueue<LinkRelevance> linkRelevanceQueue;
    private final ITopic<SitemapData> sitemapTopic;
    private final ITopic<RobotsData> robotsTopic;
    private final ITopic<Page> pageTopic;
	 
    public FetcherNode(AsyncCrawlerConfig crawlerConfig, String dataPath,
                       MetricsManager metricsManager, HazelcastService clusterService) {
        
        this.eventBus = new EventBus();
        this.eventBus.register(this);
        
        HazelcastInstance hz = clusterService.getInstance();
        this.linkRelevanceQueue = hz.getQueue(QueueNames.LINK_RELEVANCE);
        this.sitemapTopic = hz.getTopic(QueueNames.SITEMAPS);
        this.robotsTopic = hz.getTopic(QueueNames.ROBOTS);
        this.pageTopic = hz.getTopic(QueueNames.PAGE);
        
		this.downloader = new HttpDownloader(crawlerConfig.getDownloaderConfig(), dataPath, metricsManager);
        
        this.handlers.put(LinkRelevance.Type.FORWARD, new FetchedResultHandler(eventBus));
        this.handlers.put(LinkRelevance.Type.SITEMAP, new SitemapXmlHandler(eventBus));
        this.handlers.put(LinkRelevance.Type.ROBOTS,  new RobotsTxtHandler(eventBus, crawlerConfig.getDownloaderConfig().getUserAgentName()));
    }
    
    public void start() {
        synchronized (running) {
            while (!stop) {
                try {
                    LinkRelevance link = linkRelevanceQueue.poll(100, TimeUnit.MILLISECONDS);
                    if(link == null) {
                        continue;
                    }
                    
                    Callback handler = handlers.get(link.getType());
                    if (handler == null) {
                        logger.error("No registered handler for link type: " + link.getType());
                        continue;
                    }
                    
                    downloader.dipatchDownload(link, handler);
                    
                }
                catch (InterruptedException e) {
                    logger.error("Failed to get link from queue due to interruption.", e);
                }
                catch (HazelcastInstanceNotActiveException e) {
                    logger.info("Hazelcast has been shutdown.", e);
                    this.stop = true;
                }
            }
        }
    }
    
    public synchronized void stop() {
        logger.info("Stoping FetcherNode...");
        this.stop = true;
        synchronized (running) {
            this.downloader.close();
        }
        logger.info("FetcherNode stopped.");
    }
    
	@Subscribe
	public void sendToRemote(SitemapData sitemapData) {
		this.sitemapTopic.publish(sitemapData);
	}
    
	@Subscribe
	public void sendToRemote(RobotsData robotsData) {
		this.robotsTopic.publish(robotsData);
	}
	
	@Subscribe
	public void sendToRemote(Page page) {
	    this.pageTopic.publish(page);
	}

}
