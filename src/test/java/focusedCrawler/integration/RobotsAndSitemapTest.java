package focusedCrawler.integration;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.net.httpserver.HttpServer;

import focusedCrawler.Main;
import focusedCrawler.config.Configuration;
import focusedCrawler.crawler.async.TestWebServerBuilder;
import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;

public class RobotsAndSitemapTest {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    static String basePath = RobotsAndSitemapTest.class.getResource("robots_and_sitemap_test").getFile();

    private static HttpServer httpServer;
    
    @BeforeClass
    public static void setupServer() throws IOException, InterruptedException {
        httpServer = new TestWebServerBuilder("127.0.0.1", 1234)
            .withStaticFolder(Paths.get(basePath, "static"))
            .start();
    }
    
    @AfterClass
    public static void shutdownServer() throws IOException {
        httpServer.stop(0);
    }

    @Test
    public void shouldDownloadLinksListedOnSitemapsXml() throws Exception {

        String outputPath = tempFolder.newFolder().toString();

        String configPath = basePath + "/config/";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath };
        Main.main(args);

        Frontier frontier = openFrontier(outputPath, configPath);

        List<String> shouldBeDownloaded = asList(
                "index.html",
                "allowed-link-1.html",
                "page-listed-on-sitemap-1.html",
                "page-listed-on-sitemap-2.html"
        );

        List<String> shouldNOTBeDownloaded = asList(
                "not-listed-on-sitemaps.html",
                "http://.invalid-url.com/sitemap.xml",
                "http://.invalid-url.com/invalid-url.xml"
        );

        for (String url : shouldBeDownloaded) {
            assertWasCrawled(url, frontier);
        }

        for (String url : shouldNOTBeDownloaded) {
            LinkRelevance link = LinkRelevance.create("http://127.0.0.1:1234/" + url);
            assertThat("URL="+url, frontier.exist(link), is(nullValue()));
        }
    }
    
    @Test
    public void test1ToNotToDownloadSitesDisallowedOnRobots() throws Exception {

        String outputPath = tempFolder.newFolder().toString();

        String configPath = basePath + "/config/";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath };
        Main.main(args);

        Frontier frontier = openFrontier(outputPath, configPath);

        List<String> shouldBeDownloaded = asList(
                "index.html",
                "allowed-link-1.html",
                "page-listed-on-sitemap-1.html",
                "page-listed-on-sitemap-2.html"
        );

        List<String> shouldNOTBeDownloaded = asList(
                "not-listed-on-sitemaps.html"
        );

        for (String url : shouldBeDownloaded) {
            assertWasCrawled(url, frontier);
        }

        for (String url : shouldNOTBeDownloaded) {
            assertWasNotCrawled("http://127.0.0.1:1234/" + url, frontier);
        }

        assertWasNotCrawled("http://127.0.0.1:1234/disallowed-link-1.html", frontier);
        assertWasNotCrawled("http://127.0.0.1:1234/disallowed-link-2.html", frontier);
    }

    @Test
    public void test2ToNotToDownloadSitesDisallowedOnRobotsWithSitemapsFalse() throws Exception {

        String outputPath = tempFolder.newFolder().toString();

        String configPath = basePath + "/config2/";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath };
        Main.main(args);

        // then
        Frontier frontier = openFrontier(outputPath, configPath);

        List<String> shouldNOTBeDownloaded = asList(
                "page-listed-on-sitemap-1.html",
                "page-listed-on-sitemap-2.html",
                "not-listed-on-sitemaps.html"
        );
        
        List<String> shouldBeDownloaded = asList(
                "index.html",
                "allowed-link-1.html"
        );

        for (String url : shouldBeDownloaded) {
            assertWasCrawled(url, frontier);
        }
        
        for (String url : shouldNOTBeDownloaded) {
            assertWasNotCrawled("http://127.0.0.1:1234/" + url, frontier);
        }

        assertWasNotCrawled("http://127.0.0.1:1234/disallowed-link-1.html", frontier);
        assertWasNotCrawled("http://127.0.0.1:1234/disallowed-link-2.html", frontier);
    }

    private void assertWasCrawled(String url, Frontier frontier) throws Exception {
        LinkRelevance link = LinkRelevance.create("http://127.0.0.1:1234/" + url);
        assertThat("URL=" + url, frontier.exist(link), is(lessThan(0d)));
    }

    private void assertWasNotCrawled(String url, Frontier frontier) throws Exception {
        LinkRelevance link = LinkRelevance.create(url);
        assertThat("URL=" + url, frontier.exist(link), is(not(lessThan(0d))));
    }

    private Frontier openFrontier(String outputPath, String configPath) {
        Configuration config = new Configuration(configPath + "/ache.yml");
        String linkDirectory = config.getLinkStorageConfig().getLinkDirectory();
        String dir = Paths.get(outputPath, linkDirectory).toString();
        Frontier frontier = new Frontier(dir, 1000,
                config.getLinkStorageConfig().getPersistentHashtableBackend());
        return frontier;
    }

}
