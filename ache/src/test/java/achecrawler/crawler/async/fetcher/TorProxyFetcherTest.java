package achecrawler.crawler.async.fetcher;

import java.io.IOException;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

import achecrawler.config.Configuration;
import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.TestWebServerBuilder;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetchException;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;

import static org.assertj.core.api.Assertions.assertThat;

class TorProxyFetcherTest {

    String torProxyAcheYml = TorProxyFetcherTest.class.getResource("tor-proxy-ache.yml").getFile();

    @Test
    void shouldCreateTorProxyFetcher() {
        // given
        Configuration configService = new Configuration(torProxyAcheYml);
        
        // when
        HttpDownloaderConfig config = configService.getCrawlerConfig().getDownloaderConfig();
        TorProxyFetcher fetcher = (TorProxyFetcher) FetcherFactory.createFetcher(config);
        
        // then
        assertThat(config.getTorProxy()).isEqualTo("http://localhost:1234");
        assertThat(fetcher).isNotNull();
    }


    @Test
    void torProxyShouldUseProxyForOnionLinks() throws BaseFetchException, IOException {
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
        assertThat(new String(onion.getContent())).contains("tor-proxy");
        assertThat(new String(regular.getContent())).contains("regular");
        
        // finally
        torProxy.stop(0);
        httpServer.stop(0);
    }

}
