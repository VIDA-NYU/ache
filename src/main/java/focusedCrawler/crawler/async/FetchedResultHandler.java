package focusedCrawler.crawler.async;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.storage.Storage;

public class FetchedResultHandler implements HttpDownloader.Callback {
    
    private static final Logger logger = LoggerFactory.getLogger(FetchedResultHandler.class);
    
    private Storage targetStorage;

    public FetchedResultHandler(Storage targetStorage) {
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
            Page page;
            if(response.getNumRedirects() == 0) {
                page = new Page(
                    new URL(response.getBaseUrl()),
                    new String(response.getContent()),
                    parseResponseHeaders(response.getHeaders())
                );
            } else {
                page = new Page(
                    new URL(response.getBaseUrl()),
                    new String(response.getContent()),
                    parseResponseHeaders(response.getHeaders()),
                    new URL(response.getFetchedUrl())
                );
            }
            page.setFetchTime(response.getFetchTime());
            
            PaginaURL pageParser = new PaginaURL(page.getURL(), page.getContent());
            ParsedData parsedData = new ParsedData(pageParser);
            
            page.setParsedData(parsedData);
            page.setLinkRelevance(link);
            
            targetStorage.insert(page);
            
        } catch (Exception e) {
            logger.error("Problem while processing data.", e);
        }
    }

    private Map<String, List<String>> parseResponseHeaders(Metadata headerAsMetadata) {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        String[] names = headerAsMetadata.names();
        if(names != null && names.length > 0) {
            for(String name : names) {
                responseHeaders.put(name, Arrays.asList(headerAsMetadata.getValues(name)));
            }
        }
        return responseHeaders;
    }
    
}
