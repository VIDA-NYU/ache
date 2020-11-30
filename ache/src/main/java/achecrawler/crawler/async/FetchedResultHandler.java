package achecrawler.crawler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.TargetStorage;
import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.parser.PaginaURL;

public class FetchedResultHandler implements HttpDownloader.Callback {
    
    private static final Logger logger = LoggerFactory.getLogger(FetchedResultHandler.class);
    
    private String crawlerId;
    private TargetStorage targetStorage;

    public FetchedResultHandler(String crawlerId, TargetStorage targetStorage) {
        this.crawlerId = crawlerId;
        this.targetStorage = targetStorage;
    }
    
    @Override
    public void completed(LinkRelevance link, FetchedResult response) {

        int statusCode = response.getStatusCode();
        if(statusCode >= 200 && statusCode < 300) {
            processData(link, response);
        }
        //else {
        //     TODO: Update metadata about page visits in link storage
        //}
    }
    
    @Override
    public void failed(LinkRelevance link, Exception e) {
        if(e instanceof AbortedFetchException) {
            AbortedFetchException afe = (AbortedFetchException) e;
            logger.info("Download aborted: \n>URL: {}\n>Reason: {}", link.getURL().toString(), afe.getAbortReason());
        } else {
            logger.info("Failed to download URL: {}\n>Reason: {}", link.getURL().toString(), e.getMessage());
        }
    }
    
    private void processData(LinkRelevance link, FetchedResult response) {
        try {
            Page page = new Page(response);
            page.setLinkRelevance(link);
            page.setCrawlerId(crawlerId);
            if (page.isHtml()) {
                PaginaURL pageParser = new PaginaURL(page);
                page.setParsedData(new ParsedData(pageParser));
            }
            targetStorage.insert(page);
            
        } catch (Exception e) {
            logger.error("Problem while processing data.", e);
        }
    }

}
