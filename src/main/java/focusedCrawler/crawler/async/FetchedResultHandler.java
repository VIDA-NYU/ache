package focusedCrawler.crawler.async;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.fetcher.AbortedFetchException;
import crawlercommons.fetcher.FetchedResult;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.Page;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.storage.Storage;

public class FetchedResultHandler implements HttpDownloader.Callback {
    
    private static final Logger logger = LoggerFactory.getLogger(FetchedResultHandler.class);
    
    private Storage targetStorage;

    public FetchedResultHandler(Storage targetStorage) {
        this.targetStorage = targetStorage;
    }
    
    @Override
    public void completed(final FetchedResult response) {

        int statusCode = response.getStatusCode();
        if(statusCode >= 200 && statusCode < 300) {
            logger.info("Successfully downloaded URL=["+response.getBaseUrl()+"] HTTP-Response-Code="+statusCode);
            processData(response);
        } else {
            // TODO: Update metadata about page visits in link storage
            logger.info("Server returned bad code for URL=["+response.getBaseUrl()+"] HTTP-Response-Code="+statusCode);
        }
    }
    
    @Override
    public void failed(String url, final Exception e) {
        if(e instanceof AbortedFetchException) {
            AbortedFetchException afe = (AbortedFetchException) e;
            logger.info("Download aborted: \n>URL: {}\n>Reason: {}", url, afe.getAbortReason());
        } else {
            logger.info("Failed to download URL: "+url, e);
        }
    }
    
    private void processData(FetchedResult response) {
        try {
            String body = new String(response.getContent());
            
            Page page = new Page(new URL(response.getFetchedUrl()), body);
            PaginaURL pageParser = new PaginaURL(page.getURL(), page.getContent());
            page.setPageURL(pageParser);
            
            LinkRelevance link = (LinkRelevance) response.getPayload().get(HttpDownloader.PAYLOAD_KEY);
            
            final double relevance = link.getRelevance();
            if(relevance > LinkRelevance.DEFAULT_HUB_RELEVANCE &&
               relevance < LinkRelevance.DEFAULT_AUTH_RELEVANCE){
                page.setHub(true);
            }
            
            page.setRelevance(relevance);
            
            System.err.println(relevance + " Sending page to TargetStorage: "+ response.getFetchedUrl());
            targetStorage.insert(page);
            
        } catch (Exception e) {
            logger.error("Problem while processing data.", e);
        }
    }
    
}
