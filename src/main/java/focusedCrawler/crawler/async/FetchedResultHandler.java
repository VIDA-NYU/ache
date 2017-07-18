package focusedCrawler.crawler.async;

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

import javax.net.ssl.SSLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FetchedResultHandler implements HttpDownloader.Callback {
    
    private static final Logger logger = LoggerFactory.getLogger(FetchedResultHandler.class);
    
    private Storage targetStorage;
    private LinkStorage linkStorage;
    private final Set<String> sslExceptions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public FetchedResultHandler(Storage linkStorage, Storage targetStorage) {
        this.linkStorage = (LinkStorage) linkStorage;
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
        }else if(e.getCause() instanceof SSLException){
            if(!sslExceptions.contains(link.toString())){
                handleSSLExceptions(link);
            }else{
                logger.info("Failed to download URL: {}\n>Tried both HTTP and HTTPS. Reason: {}", link.getURL().toString(), e.getMessage());
            }
        }else {
            logger.info("Failed to download URL: {}\n>Reason: {}", link.getURL().toString(), e.getMessage());
        }
    }
    private void handleSSLExceptions(LinkRelevance link){
        try {
            sslExceptions.add(link.toString());
            URL url = null;
            if(link.getURL().getProtocol().equals("http")){
                url = new URL("https://"+link.getURL().getHost()+link.getURL().getPath());
            }else if(link.getURL().getProtocol().equals("https")){
                url = new URL("http://"+link.getURL().getHost()+link.getURL().getPath());
            }
            if(url!=null) {
                linkStorage.insertLink(new LinkRelevance(url,link.getRelevance()));
            }
        }catch (Exception f){}
    }
    private void processData(LinkRelevance link, FetchedResult response) {
        try {
            Page page = new Page(response);
            page.setLinkRelevance(link);

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
