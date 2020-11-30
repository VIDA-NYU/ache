package achecrawler.crawler.async.fetcher;

import java.net.MalformedURLException;
import java.net.URL;

import achecrawler.crawler.crawlercommons.fetcher.BaseFetchException;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.crawler.crawlercommons.fetcher.Payload;
import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;

/**
 * This class downloads .onion links through a TOR proxy. This proxy should be
 * set up independently and then, the configuration details should be provided
 * to ACHE via the config file.
 * 
 * @author aeciosantos
 *
 */
@SuppressWarnings("serial")
public class TorProxyFetcher extends BaseFetcher {
    
    private final SimpleHttpFetcher torFetcher;
    private final SimpleHttpFetcher httpFetcher;

    public TorProxyFetcher(SimpleHttpFetcher torFetcher, SimpleHttpFetcher httpFetcher) {
        this.torFetcher = torFetcher;
        this.httpFetcher = httpFetcher;
    }

    /**
     * Downloads RL using a TOR proxy, when it is a onion URL, or using 
     * regular HTTP fetcher otherwise.
     */
    @Override
    public FetchedResult get(String url, Payload payload) throws BaseFetchException {
        URL realUrl;
        try {
            realUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalide URL provided: "+url);
        }
        String host = realUrl.getHost();
        String domain = host.substring(host.lastIndexOf('.')+1);
        if(domain.equals("onion")) {
            return torFetcher.get(url);
        } else{
            return httpFetcher.get(url);
        }
    }

    @Override
    public void abort() {
        httpFetcher.abort();
        torFetcher.abort();
    }

}
