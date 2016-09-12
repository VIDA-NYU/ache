package focusedCrawler.integration;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.lessThan;
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
import focusedCrawler.config.ConfigService;
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
        httpServer = new TestWebServerBuilder("localhost", 1234)
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

        // then
        ConfigService config = new ConfigService(configPath + "/ache.yml");
        String linkDirectory = config.getLinkStorageConfig().getLinkDirectory();
        String dir = Paths.get(outputPath, linkDirectory).toString();
        Frontier frontier = new Frontier(dir, 1000);

        List<String> shouldBeDownloaded = asList(
                "index.html",
                "page-listed-on-sitemap-1.html",
                "page-listed-on-sitemap-2.html"
        );

        List<String> shouldNOTBeDownloaded = asList(
                "not-listed-on-sitemaps.html"
        );

        for (String url : shouldBeDownloaded) {
            LinkRelevance link = LinkRelevance.create("http://localhost:1234/" + url);
            assertThat(frontier.exist(link), is(lessThan(0)));
        }

        for (String url : shouldNOTBeDownloaded) {
            LinkRelevance link = LinkRelevance.create("http://localhost:1234/" + url);
            assertThat(frontier.exist(link), is(nullValue()));
        }
    }
    

}
