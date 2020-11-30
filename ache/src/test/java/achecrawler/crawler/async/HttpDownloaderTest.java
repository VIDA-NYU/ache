package achecrawler.crawler.async;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.sun.net.httpserver.HttpServer;

import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.util.MetricsManager;

@RunWith(Parameterized.class)
public class HttpDownloaderTest {

//    @Before
//    public void setUp() {
//        this.downloader = new HttpDownloader();
//    }

    @Parameter
    public HttpDownloader downloader;

    /*
     * This test runs multiple times for each of the following parameters,
     * to make sure that it works with all underlying fetcher implementations.
     */
    @Parameters
    public static Iterable<? extends Object> data() {
        HttpDownloader simple = new HttpDownloader();
        HttpDownloader okHttp = new HttpDownloader(new HttpDownloaderConfig("okHttp"), null, new MetricsManager(false));
        return Arrays.asList(simple, okHttp);
    }

    @Test
    public void shouldFollowRedirections() throws Exception {
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
        assertThat(result.getNumRedirects(), is(1));
        assertThat(result.getBaseUrl(), is(originalUrl));
        assertThat(result.getFetchedUrl(), is(expectedRedirectedUrl));
        assertThat(result.getNewBaseUrl(), is(expectedRedirectedUrl));
        assertThat(result.getStatusCode(), is(200));
        assertThat(result.getReasonPhrase(), is("OK"));
        assertThat(result.getContentType(), is("text/html; charset=utf-8"));
        assertThat(result.getContent(), is("Hello world!".getBytes()));
        
        httpServer.stop(0);
    }
    
    @Test
    public void shouldDownloadPageContentAndMetadata() throws Exception {
        // given
        String responseContent = "Hello world!";
        String originalUrl = TestWebServerBuilder.address+"/index.html";
        HttpServer httpServer = new TestWebServerBuilder()
            .with200OK("/index.html", responseContent)
            .start();

        // when
        FetchedResult result = downloader.dipatchDownload(originalUrl).get();
        
        // then
        assertThat(result.getNumRedirects(), is(0));
        assertThat(result.getBaseUrl(), is(originalUrl));
        assertThat(result.getFetchedUrl(), is(originalUrl));
        assertThat(result.getNewBaseUrl(), is(nullValue()));
        assertThat(result.getStatusCode(), is(200));
        assertThat(result.getReasonPhrase(), is("OK"));
        assertThat(result.getContentType(), is("text/html; charset=utf-8"));
        assertThat(result.getContent(), is(responseContent.getBytes()));
        
        httpServer.stop(0);
    }
    
    
    @Test
    public void shouldDownloadMultipleUrlsInParallel() throws Exception {
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
            assertThat(future.get().getStatusCode(), is(200));
        }
        
        httpServer.stop(0);
    }
    
    @Test
    public void shouldCallCompletedCallbackAfterDownloadFinishes() throws Exception {
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
        assertThat(requestsFinished.get(), is(numberOfRequests));
        
        httpServer.stop(0);
    }

}