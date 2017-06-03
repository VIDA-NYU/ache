package focusedCrawler.crawler.async;

import focusedCrawler.crawler.crawlercommons.fetcher.IOFetchException;
import focusedCrawler.link.LinkStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.storage.Storage;

import java.net.UnknownHostException;

public class FetchedResultHandler implements HttpDownloader.Callback {

    private static final Logger logger = LoggerFactory.getLogger(FetchedResultHandler.class);

    private Storage linkStorage;
    private Storage targetStorage;

    public FetchedResultHandler(Storage linkStorage, Storage targetStorage) {
        this.linkStorage = linkStorage;
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
        }else if(e.getCause() instanceof UnknownHostException){
            IOFetchException iofe = (IOFetchException) e;
            ((LinkStorage) linkStorage).addToBlackList(iofe.getUrl());
            logger.info("UnknownHostException - Domain added to blacklist. URL: "+iofe.getUrl());
        }else {
            logger.info("Failed to download URL: {}\n>Reason: {}", link.getURL().toString(), e.getMessage());
        }
    }

    private void processData(LinkRelevance link, FetchedResult response) {
        try {
            Page page = new Page(response);
            if (page.getContentType().toLowerCase().contains("text/html")) {
                PaginaURL pageParser = new PaginaURL(page);
                page.setParsedData(new ParsedData(pageParser));
                page.setLinkRelevance(link);
            }else {
                logger.info("non-HTML content found at: "+link.getURL()+"\nsaving content type: "+page.getContentType());
                // using an identical implementation of pages for nonHTML
                // content too as this one already stores URL and Content
            }
            targetStorage.insert(page);
        } catch (Exception e) {
            logger.error("Problem while processing data.", e);
        }
    }

}
