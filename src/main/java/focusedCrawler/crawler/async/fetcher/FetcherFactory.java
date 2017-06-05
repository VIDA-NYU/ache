package focusedCrawler.crawler.async.fetcher;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;
import java.util.Date;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

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

    public static CookieStore getCookieStore(HttpDownloaderConfig config) {
        List<HttpDownloaderConfig.Cookie> cookies = config.getCookies();
        if (cookies == null)
            return null;
        
        CookieStore store = new BasicCookieStore();
        for (HttpDownloaderConfig.Cookie cookie: cookies){
            String[] values = cookie.cookie.split("; ");
            for (int i=0; i<values.length; i++) {
                String[] kv = values[i].split("=", 2);
                BasicClientCookie cc = new BasicClientCookie(kv[0], kv[1]);
                cc.setPath("/");
                cc.setDomain(cookie.domain);
                //Date date = new Date(118, 5, 5); //Read expiry date from config
                //cc.setExpiryDate(date);
                store.addCookie(cc);
            }
        }
        return store;
    }

    public static SimpleHttpFetcher createSimpleHttpFetcher(HttpDownloaderConfig config){
        UserAgent userAgent = new UserAgent(config.getUserAgentName(), "", config.getUserAgentUrl());
        int connectionPoolSize = config.getConnectionPoolSize();
        SimpleHttpFetcher httpFetcher = new SimpleHttpFetcher(connectionPoolSize, userAgent);
        // timeout for inactivity between two consecutive data packets
        httpFetcher.setSocketTimeout(30*1000);
        // timeout for establishing a new connection
        httpFetcher.setConnectionTimeout(30*1000);
        // timeout for requesting a connection from httpclient's connection manager
        httpFetcher.setConnectionRequestTimeout(1*60*1000);
        httpFetcher.setMaxConnectionsPerHost(1);
        httpFetcher.setMaxRetryCount(config.getMaxRetryCount());
        httpFetcher.setDefaultMaxContentSize(10*1024*1024);
        
        if(config.getValidMimeTypes() != null) {
            for (String mimeTypes : config.getValidMimeTypes()) {
                httpFetcher.addValidMimeType(mimeTypes);
            }
        }

        CookieStore store = FetcherFactory.getCookieStore(config);
        if (store != null) {
            httpFetcher.setCookie(store);
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
