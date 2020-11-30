package achecrawler.crawler.async.fetcher;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.impl.cookie.BasicClientCookie;

import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.cookies.ConcurrentCookieJar;
import achecrawler.crawler.cookies.OkHttpCookieJar;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import achecrawler.crawler.crawlercommons.fetcher.http.UserAgent;
import okhttp3.Cookie;

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
        httpFetcher.setSocketTimeout(config.getSocketTimeout());
        // timeout for establishing a new connection
        httpFetcher.setConnectionTimeout(config.getConnectionTimeout());
        // timeout for requesting a connection from httpclient's connection manager
        httpFetcher.setConnectionRequestTimeout(config.getConnectionRequestTimeout());
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

        ConcurrentCookieJar store = createApacheCookieStore(config);
        if (store != null) {
            httpFetcher.setCookieStore(store);
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

    public static ConcurrentCookieJar createApacheCookieStore(HttpDownloaderConfig config) {
        List<HttpDownloaderConfig.Cookie> cookies = config.getCookies();
        if (cookies == null) {
            return null;
        }
        ConcurrentCookieJar store = new ConcurrentCookieJar();
        for (HttpDownloaderConfig.Cookie cookie : cookies) {
            String[] values = cookie.cookie.split("; ");
            for (int i = 0; i < values.length; i++) {
                String[] kv = values[i].split("=", 2);
                BasicClientCookie cc = new BasicClientCookie(kv[0], kv[1]);
                cc.setPath(cookie.path);
                cc.setDomain(cookie.domain);
                store.addCookie(cc);
            }
        }
        return store;
    }

    public static OkHttpFetcher createOkHttpFetcher(HttpDownloaderConfig config) {

        UserAgent userAgent = new UserAgent.Builder()
                .setAgentName(config.getUserAgentName())
                .setEmailAddress(config.getUserAgentEmail())
                .setWebAddress(config.getUserAgentUrl())
                .setUserAgentString(config.getUserAgentString())
                .build();

        int connectionPoolSize = config.getConnectionPoolSize();

        OkHttpCookieJar cookieStore = createOkHttpCookieHandler(config);

        OkHttpFetcher httpFetcher = new OkHttpFetcher(
                connectionPoolSize,
                userAgent,
                cookieStore,
                config.getConnectTimeout(),
                config.getReadTimeout(),
                config.getOkHttpFetcherProxyHost(),
                config.getOkHttpFetcherProxyPort(),
                config.getOkHttpFetcherProxyUsername(),
                config.getOkHttpFetcherProxyPassword());

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

    private static OkHttpCookieJar createOkHttpCookieHandler(HttpDownloaderConfig config) {
        List<HttpDownloaderConfig.Cookie> cookies = config.getCookies();
        if (cookies == null) {
            return null;
        }
        OkHttpCookieJar store = new OkHttpCookieJar();
        for (HttpDownloaderConfig.Cookie cookie : cookies) {
            String[] values = cookie.cookie.split("; ");
            for (int i = 0; i < values.length; i++) {
                String[] kv = values[i].split("=", 2);
                String name = kv[0];
                String value = kv[1];
                Cookie okhttp3Cookie = new Cookie.Builder()
                        .domain(cookie.domain)
                        .path(cookie.path)
                        .name(name)
                        .value(value)
                        .build();
                store.addCookieForDomain(cookie.domain, okhttp3Cookie);
            }
        }
        return store;
    }

}
