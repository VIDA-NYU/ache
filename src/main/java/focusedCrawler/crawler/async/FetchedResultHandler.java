package focusedCrawler.crawler.async;

import focusedCrawler.crawler.crawlercommons.fetcher.*;
import focusedCrawler.link.*;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.storage.Storage;

public class FetchedResultHandler implements HttpDownloader.Callback {
    
    private static final Logger logger = LoggerFactory.getLogger(FetchedResultHandler.class);
    
    private Storage targetStorage;
    private Storage linkStorage;

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
        }else if (e.getCause() instanceof UnknownHostException){
            IOFetchException iofe = (IOFetchException) e;
            ((LinkStorage)linkStorage).addToBlackList(iofe.getUrl());
            logger.info("UnknownHostException - Domain added to Blacklist. URL: "+iofe.getUrl());
        }
        else {
            logger.info("Failed to download URL: {}\n>Reason: {}", link.getURL().toString(), e.getMessage());
        }
    }
    
    private void processData(LinkRelevance link, FetchedResult response) {
        try {
            Page page = new Page(response);
            
            // TODO Check whether page is HTML before trying to parse HTML
            PaginaURL pageParser = new PaginaURL(page);
            page.setParsedData(new ParsedData(pageParser));
            page.setLinkRelevance(link);
            
            targetStorage.insert(page);
            
        } catch (Exception e) {
            logger.error("Problem while processing data.", e);
        }
    }

}
