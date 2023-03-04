package achecrawler.crawler.async;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;

import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.util.MetricsManager;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpDownloaderTest {
    /*
     * This test runs multiple times for each of the following parameters,
     * to make sure that it works with all underlying fetcher implementations.
     */
    public static Iterable<?> data() {
        HttpDownloader simple = new HttpDownloader();
        HttpDownloader okHttp = new HttpDownloader(new HttpDownloaderConfig("okHttp"), null, new MetricsManager(false));
        return Arrays.asList(simple, okHttp);
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldFollowRedirections(HttpDownloader downloader) throws Exception {
        // given
        HttpServer httpServer = new TestWebServerBuilder()
            .withRedirect("/index.html", "/new/location.html")
            .with200OK("/new/location.html", "Hello world!")
            .start();
        
        String originalUrl = TestWebServerBuilder.address+"/index.html";
        String expectedRedirectedUrl = TestWebServerBuilder.address+"/new/location.html";
        
        // when
        FetchedResult result = downloader.dipatchDownload(originalUrl).get();
        
        // then
        assertThat(result.getNumRedirects()).isEqualTo(1);
        assertThat(result.getBaseUrl()).isEqualTo(originalUrl);
        assertThat(result.getFetchedUrl()).isEqualTo(expectedRedirectedUrl);
        assertThat(result.getNewBaseUrl()).isEqualTo(expectedRedirectedUrl);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getReasonPhrase()).isEqualTo("OK");
        assertThat(result.getContentType()).isEqualTo("text/html; charset=utf-8");
        assertThat(result.getContent()).isEqualTo("Hello world!".getBytes());
        
        httpServer.stop(0);
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldDownloadPageContentAndMetadata(HttpDownloader downloader) throws Exception {
        // given
        String responseContent = "Hello world!";
        String originalUrl = TestWebServerBuilder.address+"/index.html";
        HttpServer httpServer = new TestWebServerBuilder()
            .with200OK("/index.html", responseContent)
            .start();

        // when
        FetchedResult result = downloader.dipatchDownload(originalUrl).get();
        
        // then
        assertThat(result.getNumRedirects()).isEqualTo(0);
        assertThat(result.getBaseUrl()).isEqualTo(originalUrl);
        assertThat(result.getFetchedUrl()).isEqualTo(originalUrl);
        assertThat(result.getNewBaseUrl()).isNull();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getReasonPhrase()).isEqualTo("OK");
        assertThat(result.getContentType()).isEqualTo("text/html; charset=utf-8");
        assertThat(result.getContent()).isEqualTo(responseContent.getBytes());
        
        httpServer.stop(0);
    }


    @MethodSource("data")
    @ParameterizedTest
    void shouldDownloadMultipleUrlsInParallel(HttpDownloader downloader) throws Exception {
        // given
        String originalUrl = TestWebServerBuilder.address+"/index.html";
        HttpServer httpServer = new TestWebServerBuilder()
            .with200OK("/index.html", "Hello world!")
            .start();

        // when
        List<Future<FetchedResult>> results = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Future<FetchedResult> futureResult = downloader.dipatchDownload(originalUrl);
            results.add(futureResult);
        }
        
        // then
        for (Future<FetchedResult> future : results) {
            assertThat(future.get().getStatusCode()).isEqualTo(200);
        }
        
        httpServer.stop(0);
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldCallCompletedCallbackAfterDownloadFinishes(HttpDownloader downloader) throws Exception {
        // given
        String originalUrl = TestWebServerBuilder.address+"/index.html";
        HttpServer httpServer = new TestWebServerBuilder()
            .with200OK("/index.html", "Hello world!")
            .start();
        final int numberOfRequests = 5;
        final AtomicInteger requestsFinished = new AtomicInteger(0);
        
        // when
        for (int i = 0; i < numberOfRequests; i++) {
            downloader.dipatchDownload(new URL(originalUrl), new HttpDownloader.Callback() {
                @Override
                public void failed(LinkRelevance link, Exception e) {
                }
                @Override
                public void completed(LinkRelevance link, FetchedResult result) {
                    // increment counter when download finishes
                    requestsFinished.incrementAndGet();
                }
            });
        }
        while(downloader.hasPendingDownloads()) {
            // wait until all downloads are finished
            Thread.sleep(5);
        }
        
        // then
        assertThat(requestsFinished.get()).isEqualTo(numberOfRequests);
        
        httpServer.stop(0);
    }
}