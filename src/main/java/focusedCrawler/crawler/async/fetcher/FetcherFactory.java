package focusedCrawler.crawler.async.fetcher;

import focusedCrawler.crawler.async.HttpDownloaderConfig.Cookie;
import focusedCrawler.crawler.async.cookieHandler.ConcurrentCookieJar;
import focusedCrawler.crawler.async.cookieHandler.CookieHandler;
import focusedCrawler.crawler.async.cookieHandler.OkHttpCookieJar;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Arrays;
import java.util.List;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import focusedCrawler.crawler.async.HttpDownloaderConfig;
import focusedCrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import focusedCrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import focusedCrawler.crawler.crawlercommons.fetcher.http.UserAgent;

public class FetcherFactory {
    
    public static BaseFetcher createFetcher(HttpDownloaderConfig config) {
        if(config.getTorProxy() != null) {
            return createTorProxyFetcher(config);
        } else if(config.getOkHttpFetcher() != null){
            return createOkHttpFetcher(config);
        } else {
            return createSimpleHttpFetcher(config);
        }
    }

    public static SimpleHttpFetcher createSimpleHttpFetcher(HttpDownloaderConfig config){
        UserAgent userAgent = new UserAgent.Builder()
                .setAgentName(config.getUserAgentName())
                .setEmailAddress(config.getUserAgentEmail())
                .setWebAddress(config.getUserAgentUrl())
                .setUserAgentString(config.getUserAgentString())
                .build();
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

        // sets maximum file size: download of files larger than this will be aborted
        // (note: should NOT be smaller than 50MB if crawling sitemap files)
        int defaultMaxContentSize = 51 * 1024 * 1024;
        httpFetcher.setDefaultMaxContentSize(defaultMaxContentSize);
        
        if(config.getValidMimeTypes() != null) {
            for (String mimeTypes : config.getValidMimeTypes()) {
                httpFetcher.addValidMimeType(mimeTypes);
            }
        }

        CookieHandler store = createCookieStore(config);
        if (store != null) {
            httpFetcher.setCookieStore((CookieStore) store);
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
        torFetcher.setMaxRetryCount(3);
        torFetcher.setSocketTimeout(5*60*1000);
        torFetcher.setConnectionTimeout(5*60*1000);
        torFetcher.setConnectionRequestTimeout(5*60*1000);
        return new TorProxyFetcher(torFetcher, httpFetcher);
    }

    public static CookieHandler createCookieStore(HttpDownloaderConfig config) {
        List<HttpDownloaderConfig.Cookie> cookies = config.getCookies();
        if (cookies == null) {
            return null;
        }
        CookieHandler store;
        if (config.getOkHttpFetcher() == null) {
            store = new ConcurrentCookieJar();
            for (HttpDownloaderConfig.Cookie cookie : cookies) {
                String[] values = cookie.cookie.split("; ");
                for (int i = 0; i < values.length; i++) {
                    String[] kv = values[i].split("=", 2);
                    BasicClientCookie cc = new BasicClientCookie(kv[0], kv[1]);
                    cc.setPath(cookie.path);
                    cc.setDomain(cookie.domain);
                    ((CookieStore)store).addCookie(cc);
                }
            }
        }else {
            store = new OkHttpCookieJar();
            // need to add config cookies
            // should convert 'config cookies' into 'okhttp cookies'
        }
        return store;
    }

    public static OkHttpFetcher createOkHttpFetcher(HttpDownloaderConfig config){
        UserAgent userAgent = new UserAgent.Builder()
                .setAgentName(config.getUserAgentName())
                .setEmailAddress(config.getUserAgentEmail())
                .setWebAddress(config.getUserAgentUrl())
                .setUserAgentString(config.getUserAgentString())
                .build();
        int connectionPoolSize = config.getConnectionPoolSize();

        OkHttpCookieJar store = (OkHttpCookieJar) createCookieStore(config);

        OkHttpFetcher httpFetcher = new OkHttpFetcher(connectionPoolSize, userAgent, store);
        httpFetcher.setMaxRedirects(config.getMaxRetryCount());
        httpFetcher.setMaxConnectionsPerHost(1);
        int defaultMaxContentSize = 51 * 1024 * 1024;
        httpFetcher.setDefaultMaxContentSize(defaultMaxContentSize);
        if(config.getValidMimeTypes() != null) {
            for (String mimeTypes : config.getValidMimeTypes()) {
                httpFetcher.addValidMimeType(mimeTypes);
            }
        }
        return httpFetcher;
    }

}
