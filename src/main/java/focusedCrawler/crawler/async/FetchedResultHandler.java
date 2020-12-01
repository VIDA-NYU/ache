package focusedCrawler.crawler.async;

import focusedCrawler.crawler.async.HttpDownloader.Callback;
import focusedCrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchedResultHandler implements HttpDownloader.Callback {

    private static final Logger logger = LoggerFactory.getLogger(FetchedResultHandler.class);

    private final SitemapXmlHandler sitemapXmlHandler;
    private final ForwardLinkHandler forwardLinkHandler;
    private final RobotsTxtHandler robotsTxtHandler;
    private LinkStorage linkStorage;

    public FetchedResultHandler(String crawlerId, TargetStorage targetStorage,
            LinkStorage linkStorage, String userAgentName) {
        this.linkStorage = linkStorage;
        this.forwardLinkHandler = new ForwardLinkHandler(crawlerId, targetStorage);
        this.sitemapXmlHandler = new SitemapXmlHandler(linkStorage);
        this.robotsTxtHandler = new RobotsTxtHandler(linkStorage, userAgentName);
    }

    @Override
    public void completed(LinkRelevance link, FetchedResult response) {
        linkStorage.notifyDownloadFinished(link);
        Callback handler = getDownloadHandler(link);
        handler.completed(link, response);
    }

    @Override
    public void failed(LinkRelevance link, Exception e) {
        linkStorage.notifyDownloadFinished(link);
        if (e instanceof AbortedFetchException) {
            AbortedFetchException afe = (AbortedFetchException) e;
            logger.info("Download aborted: \n>URL: {}\n>Reason: {}", link.getURL().toString(),
                    afe.getAbortReason());
        } else {
            logger.info("Failed to download URL: {}\n>Reason: {}", link.getURL().toString(),
                    e.getMessage());
        }
        Callback handler = getDownloadHandler(link);
        handler.failed(link, e);
    }

    private Callback getDownloadHandler(LinkRelevance link) {
        switch (link.getType()) {
            case FORWARD:
                return forwardLinkHandler;
            case ROBOTS:
                return robotsTxtHandler;
            case SITEMAP:
                return sitemapXmlHandler;
            default:
                // There should be a handler for each link type, so this shouldn't happen
                throw new IllegalStateException("No handler for link type: " + link.getType());
        }
    }

}