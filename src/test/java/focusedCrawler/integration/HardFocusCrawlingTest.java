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

public class HardFocusCrawlingTest {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    static String basePath = HardFocusCrawlingTest.class.getResource("hard_focus_test").getFile();

    private static HttpServer httpServer;
    
    @BeforeClass
    public static void setupServer() throws IOException, InterruptedException {
        System.out.println("HardFocusCrawlingTest");
        httpServer = new TestWebServerBuilder("localhost", 1234)
            .withStaticFolder(Paths.get(basePath, "html"))
            .start();
    }
    
    @AfterClass
    public static void shutdownServer() throws IOException {
        httpServer.stop(0);
    }

    @Test
    public void shouldDownloadLinksOnlyFromRelevantPages() throws Exception {

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
                "index_irrelevant.html",
                "index_relevant.html",
                "relevant_page1.html",
                "irrelevant_page1.html"
        );

        List<String> shouldNOTBeDownloaded = asList(
                "relevant_page2.html",
                "irrelevant_page2.html"
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
