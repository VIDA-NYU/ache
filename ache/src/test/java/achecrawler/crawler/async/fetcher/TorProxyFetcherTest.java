package achecrawler.crawler.async.fetcher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import achecrawler.config.Configuration;
import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.TestWebServerBuilder;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetchException;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;

public class TorProxyFetcherTest {

    String torProxyAcheYml = TorProxyFetcherTest.class.getResource("tor-proxy-ache.yml").getFile();
    
    @Test
    public void shouldCreateTorProxyFetcher() {
        // given
        Configuration configService = new Configuration(torProxyAcheYml);
        
        // when
        HttpDownloaderConfig config = configService.getCrawlerConfig().getDownloaderConfig();
        TorProxyFetcher fetcher = (TorProxyFetcher) FetcherFactory.createFetcher(config);
        
        // then
        assertThat(config.getTorProxy(), is("http://localhost:1234"));
        assertThat(fetcher, is(notNullValue()));
    }
    
    
    @Test
    public void torProxyShouldUseProxyForOnionLinks() throws BaseFetchException, IOException {
        // given
        Configuration configService = new Configuration(torProxyAcheYml);
        HttpDownloaderConfig config = configService.getCrawlerConfig().getDownloaderConfig();
        TorProxyFetcher fetcher = (TorProxyFetcher) FetcherFactory.createFetcher(config);
        
        HttpServer torProxy = new TestWebServerBuilder("localhost", 1234)
                .with200OK("/", "tor-proxy")
                .start();
        
        HttpServer httpServer = new TestWebServerBuilder("localhost", 1111)
                .with200OK("/", "regular")
                .start();
        
        String onionLink = "http://sOm3Ex4mple.onion/index.html";
        String regularLink = "http://localhost:1111/index.html";
        
        // when
        FetchedResult onion = fetcher.get(onionLink);
        FetchedResult regular = fetcher.get(regularLink);
        
        // then
        assertThat(new String(onion.getContent()), containsString("tor-proxy"));
        assertThat(new String(regular.getContent()), containsString("regular"));
        
        // finally
        torProxy.stop(0);
        httpServer.stop(0);
    }

}
