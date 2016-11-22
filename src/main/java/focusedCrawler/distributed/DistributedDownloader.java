package focusedCrawler.distributed;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import focusedCrawler.crawler.async.Downloader;
import focusedCrawler.crawler.async.RobotsTxtHandler.RobotsData;
import focusedCrawler.crawler.async.SitemapXmlHandler.SitemapData;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.StorageException;

public class DistributedDownloader implements Downloader {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedDownloader.class);

    private final TargetStorage targetStorage;
    private final LinkStorage linkStorage;
    
    private final AtomicInteger numberOfDownloads = new AtomicInteger(0);

    private final IQueue<Object> linkRelevanceQueue;
    private final ITopic<SitemapData> sitemapTopic;
    private final ITopic<RobotsData> robotsTopic;
    private final ITopic<Page> pageTopic;

    private final String sitemapListenerId;
    private final String robotsListenerId;
    private final String pageListenerId;

    private HazelcastInstance hz;
    private int maxQueueSize = 200;
    
    public DistributedDownloader(TargetStorage targetStorage, LinkStorage linkStorage,
                                 HazelcastService clusterService, MetricsManager metrics) {
        this.targetStorage = targetStorage;
        this.linkStorage = linkStorage;
        
        this.hz = clusterService.getInstance();
        this.linkRelevanceQueue = hz.getQueue(QueueNames.LINK_RELEVANCE);
        this.sitemapTopic = hz.getTopic(QueueNames.SITEMAPS);
        this.robotsTopic = hz.getTopic(QueueNames.ROBOTS);
        this.pageTopic = hz.getTopic(QueueNames.PAGE);

        this.sitemapListenerId = this.sitemapTopic.addMessageListener(new SitemapDataListener());
        this.robotsListenerId = this.robotsTopic.addMessageListener(new RobotsDataListener());
        this.pageListenerId = this.pageTopic.addMessageListener(new PageListener());
        
        setupMetrics(metrics);
    }
    
    private void setupMetrics(MetricsManager metrics) {
        Gauge<Integer> numberOfDownloadsGauge = () -> new Integer(numberOfDownloads.get());
        metrics.register("distributed.downloader.number_of_downloads", numberOfDownloadsGauge);
        
        Gauge<Integer> linkRelevanceQueueGauge = () -> linkRelevanceQueue.size();
        metrics.register("distributed.downloader.link_relevance_queue.size", linkRelevanceQueueGauge);
//        
//        
//        fetchTimer   = metrics.getTimer("downloader.fetch.time");
//        handlerTimer = metrics.getTimer("downloader.handler.time");
//        counterAborted = metrics.getCounter("downloader.fetches.aborted");
//        counterSuccess = metrics.getCounter("downloader.fetches.successes");
//        counterErrors  = metrics.getCounter("downloader.fetches.errors");
//        counterHttpStatus2xx = metrics.getCounter("downloader.http_response.status.2xx");
//        
//        Gauge<Integer> downloadQueueGauge = () -> downloadQueue.size();
//        metrics.register("downloader.download_queue.size", downloadQueueGauge);
//        
//        Gauge<Integer> dispatchQueueGauge = () -> dispatchQueue.size();
//        metrics.register("downloader.dispatch_queue.size", dispatchQueueGauge);
//        
//        Gauge<Integer> numberOfDownloadsGauge = () -> numberOfDownloads.get();
//        metrics.register("downloader.pending_downloads", numberOfDownloadsGauge);
//        
//        Gauge<Integer> runningRequestsGauge = () -> runningRequests.get();
//        metrics.register("downloader.running_requests", runningRequestsGauge);
//        
//        Gauge<Integer> runningHandlersGauge = () -> runningHandlers.get();
//        metrics.register("downloader.running_handlers", runningHandlersGauge);
    }

    @Override
    public void dispatchDownload(LinkRelevance link) {
        try {
            while(numberOfDownloads.get() >= maxQueueSize) {
                Thread.sleep(10);
            }
            linkRelevanceQueue.put(link);
            numberOfDownloads.incrementAndGet();
        } catch (InterruptedException e) {
            logger.error("Failed to add link relevance to distributed queue", e);
        }
    }

    @Override
    public boolean hasPendingDownloads() {
        return linkRelevanceQueue.size() > 0;
    }

    @Override
    public void close() {
        await();
        hz.removeDistributedObjectListener(sitemapListenerId);
        hz.removeDistributedObjectListener(robotsListenerId);
        hz.removeDistributedObjectListener(pageListenerId);
    }
    
    private void await() {
        try {
            logger.info("Waiting downloads be finalized...");
            long timeWaited = 0;
            while(linkRelevanceQueue.size() > 0) {
                Thread.sleep(100);
                timeWaited = 100;
                if(timeWaited % 5000 == 0) {
                    logger.info("Still waiting to finish downloads...");
                }
            }
            long maxWaitTime = 5*60*1000;
            while(numberOfDownloads.get() > 0 || timeWaited > maxWaitTime ) {
                Thread.sleep(10);
                timeWaited = 10;
                if(timeWaited % 5000 == 0) {
                    logger.info("Still waiting to process downloaded pages...");
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted while waiting downloader threads finalize.", e);
        }
    }
    
    private class SitemapDataListener implements MessageListener<SitemapData> {
        @Override
        public void onMessage(Message<SitemapData> message) {
            try {
                linkStorage.insert(message.getMessageObject());
            } finally {
                numberOfDownloads.decrementAndGet();
            }
        }
    }

    private class RobotsDataListener implements MessageListener<RobotsData> {
        @Override
        public void onMessage(Message<RobotsData> message) {
            try {
                linkStorage.insert(message.getMessageObject());
            } finally {
                numberOfDownloads.decrementAndGet();
            }
        }
    }

    private class PageListener implements MessageListener<Page> {
        @Override
        public void onMessage(Message<Page> message) {
            try {
                targetStorage.insert(message.getMessageObject());
            } catch (StorageException e) {
                logger.error("Failed to insert page into Target Storage", e);
            } finally {
                numberOfDownloads.decrementAndGet();
            }
        }
    }

}
