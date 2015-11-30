package focusedCrawler.crawler.async;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.http.UserAgent;

public class HttpDownloaderTest {
    
    static class TestWebServerBuilder {
        
        private static final int port = 8345;
        private static final String address = "http://localhost:"+port;
        private HttpServer server;
        
        public TestWebServerBuilder() throws IOException {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        }
        
        public TestWebServerBuilder withHandler(String path, HttpHandler handler) {
            server.createContext(path, handler);
            return this;
        }
    
        private HttpServer start() {
            server.setExecutor(null); // creates a default executor
            server.start();
            return server;
        }
    }
    
    static class OkHandler implements HttpHandler {
        
        private final String responseContent;

        public OkHandler(String responseContent) {
            this.responseContent = responseContent;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseContent.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(responseContent.getBytes());
            os.close();
            t.close();
        }
    }
    
    static class RedirectionHandler implements HttpHandler {
        
        private String newLocation;

        public RedirectionHandler(String newLocation) {
            this.newLocation = newLocation;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Location", newLocation);
            t.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, 0);
            t.close();
        }
    }
    
    final UserAgent userAgent = new UserAgent("test", "test@test.com", "test@test.com");
    
    private HttpDownloader downloader;
    
    @Before
    public void setUp() {
        this.downloader = new HttpDownloader(userAgent);
    }

    @Test
    public void shouldFollowRedirections() throws Exception {
        // given
        HttpServer httpServer = new TestWebServerBuilder()
            .withHandler("/index.html", new RedirectionHandler("/new/location.html"))
            .withHandler("/new/location.html", new OkHandler("Hello world!"))
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
            .withHandler("/index.html", new OkHandler(responseContent))
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
            .withHandler("/index.html", new OkHandler("Hello world!"))
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
            .withHandler("/index.html", new OkHandler("Hello world!"))
            .start();
        final int numberOfRequests = 5;
        final AtomicInteger requestsFinished = new AtomicInteger(0);
        
        // when
        for (int i = 0; i < numberOfRequests; i++) {
            downloader.dipatchDownload(new URL(originalUrl), new HttpDownloader.Callback() {
                @Override
                public void failed(String url, Exception e) {
                }
                @Override
                public void completed(FetchedResult result) {
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