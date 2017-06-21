package focusedCrawler.crawler.async;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.drew.lang.annotations.NotNull;
import com.google.common.collect.ImmutableMap;
import focusedCrawler.config.ConfigService;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.FrontierPersistentException;
import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.link.frontier.selector.RandomLinkSelector;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.persistence.PersistentHashtable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

public class HttpDownloaderTest {
    
    private HttpDownloader downloader;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        this.downloader = new HttpDownloader();
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

    @Test
    public void SSLExceptionsTests() throws IOException, FrontierPersistentException{
        int minimumAccessTimeInterval = 0;
        int schedulerMaxLinks = 2;
        boolean downloadSitemapXml = false;
        LinkFilter emptyLinkFilter = new LinkFilter(new ArrayList<String>());
        MetricsManager metricsManager = new MetricsManager();
        LinkSelector linkSelector = new RandomLinkSelector();
        Frontier frontier = new Frontier(tempFolder.newFolder().toString(), 1000, PersistentHashtable.DB.ROCKSDB);
        String dataPath = tempFolder.newFolder().toString();
        String modelPath = tempFolder.newFolder().toString();
        Map<?, ?> props = ImmutableMap.of(
                    "link_storage.scheduler.max_links", schedulerMaxLinks,
                    "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
                    "link_storage.download_sitemap_xml", downloadSitemapXml
                    );
        LinkStorageConfig config = new ConfigService(props).getLinkStorageConfig();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                                linkSelector, null, emptyLinkFilter, metricsManager);
        LinkStorage linkStorage = new LinkStorage(config,frontierManager);

        FetchedResultHandler frh = new FetchedResultHandler(linkStorage,null);

//      check if HTTP saved in frontier as HTTPS incase of SSL exception
        LinkRelevance link = new LinkRelevance("http://www.sikla.co.uk/",1d);
        frh.failed(link,new SSLException("handshake_failure",new SSLHandshakeException("handshake_failure")));

        LinkRelevance newLink = null;
        try {
            newLink = (LinkRelevance) frontierManager.nextURL();
        }catch (DataNotFoundException dnfe){}

        assertThat(newLink,is(notNullValue()));
        assertThat(newLink.getURL().toString(),is("https://www.sikla.co.uk/"));

//      check if HTTPS saved in frontier as HTTP incase of SSL exception
        LinkRelevance link2 = new LinkRelevance("https://www.pseg.com/",1d);
        frh.failed(link2,new SSLException("handshake_failure",new SSLHandshakeException("handshake_failure")));
        LinkRelevance newLink2 = null;
        try {
            newLink2 = (LinkRelevance) frontierManager.nextURL();
        }catch (DataNotFoundException dnfe){}

        assertThat(newLink2,is(notNullValue()));
        assertThat(newLink2.getURL().toString(),is("http://www.pseg.com/"));

//      verify link not saved again when SSL exception occurs after trying both HTTP and HTTPS versions
        frh.failed(link,new SSLException("handshake_failure",new SSLHandshakeException("handshake_failure")));
        DataNotFoundException dnfe = null;
        try {
            linkStorage.select(null);
        }catch (Exception e) {
            if(e instanceof DataNotFoundException){
                dnfe = (DataNotFoundException) e;
            }
        }

        assertThat(dnfe, is(notNullValue()));
        assertThat(dnfe.ranOutOfLinks(), is(true));



    }
}