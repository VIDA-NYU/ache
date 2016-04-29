package focusedCrawler.crawler.async.fetcher;

import java.net.MalformedURLException;
import java.net.URL;

import focusedCrawler.crawler.async.HttpDownloaderConfig;
import focusedCrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import focusedCrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import focusedCrawler.crawler.crawlercommons.fetcher.http.UserAgent;

public class FetcherFactory {
    
    public static BaseFetcher createFetcher(HttpDownloaderConfig config) {
        if(config.getTorProxy() != null) {
            return createTorProxyFetcher(config);
        } else {
            return createSimpleHttpFetcher(config);
        }
    }
    
    public static SimpleHttpFetcher createSimpleHttpFetcher(HttpDownloaderConfig config){
        // Adding some extra connections for URLs that have redirects
        // and thus creates more connections   
        int threadPoolSize = config.getDownloadThreadPoolSize();
        int connectionPoolSize = (int) (threadPoolSize * 2);
        UserAgent userAgent = new UserAgent(config.getUserAgentName(), "", config.getUserAgentUrl());
        
        SimpleHttpFetcher httpFetcher = new SimpleHttpFetcher(connectionPoolSize, userAgent);
        httpFetcher.setMaxConnectionsPerHost(1);
        httpFetcher.setSocketTimeout(30*1000);
        httpFetcher.setConnectionTimeout(5*60*1000);
        httpFetcher.setConnectionRequestTimeout(30*1000);
        httpFetcher.setMaxRetryCount(config.getMaxRetryCount());
        httpFetcher.setDefaultMaxContentSize(10*1024*1024);
        
        if(config.getValidMimeTypes() != null) {
            for (String mimeTypes : config.getValidMimeTypes()) {
                httpFetcher.addValidMimeType(mimeTypes);
            }
        }
        
        return httpFetcher;
    }
    
    public static TorProxyFetcher createTorProxyFetcher(HttpDownloaderConfig config) {
        
        SimpleHttpFetcher httpFetcher = FetcherFactory.createSimpleHttpFetcher(config);
        
        // TOR fetcher is just a simple HTTP fetcher through a proxy and different parameters
        SimpleHttpFetcher torFetcher = FetcherFactory.createSimpleHttpFetcher(config);
        
        URL torProxy;
        try {
            torProxy = new URL(config.getTorProxy());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL provide for TOR proxy: "+config.getTorProxy());
        }
        
        torFetcher.setProxy(torProxy.getProtocol(), torProxy.getHost(), torProxy.getPort());
        torFetcher.setSocketTimeout(1000*1000);
        
        return new TorProxyFetcher(torFetcher, httpFetcher);
    }

}
