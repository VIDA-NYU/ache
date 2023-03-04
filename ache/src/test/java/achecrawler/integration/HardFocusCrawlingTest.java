package achecrawler.integration;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sun.net.httpserver.HttpServer;

import achecrawler.Main;
import achecrawler.config.Configuration;
import achecrawler.crawler.async.TestWebServerBuilder;
import achecrawler.link.frontier.Frontier;
import achecrawler.link.frontier.LinkRelevance;

public class HardFocusCrawlingTest {

    @TempDir
    public File tempFolder;
    
    static String basePath = HardFocusCrawlingTest.class.getResource("hard_focus_test").getFile();

    private static HttpServer httpServer;

    @BeforeAll
    static void setupServer() throws IOException {
        System.out.println("HardFocusCrawlingTest");
        httpServer = new TestWebServerBuilder("127.0.0.1", 1234)
            .withStaticFolder(Paths.get(basePath, "html"))
            .start();
    }

    @AfterAll
    static void shutdownServer() {
        httpServer.stop(0);
    }

    @Test
    void shouldDownloadLinksOnlyFromRelevantPages() {

        String outputPath = tempFolder.toString();

        String configPath = basePath + "/config/";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";

        String crawlerId = "crawler0";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath, "-cid", crawlerId };
        Main.main(args);

        // then
        Frontier frontier = openFrontier(outputPath + "/" + crawlerId, configPath);

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
            LinkRelevance link = LinkRelevance.create("http://127.0.0.1:1234/" + url);
            assertThat(frontier.exist(link)).as("URL=" + link.getURL().toString()).isLessThan(0d);
        }

        for (String url : shouldNOTBeDownloaded) {
            LinkRelevance link = LinkRelevance.create("http://127.0.0.1:1234/" + url);
            assertThat(frontier.exist(link)).as("URL=" + link.getURL().toString()).isNull();
        }
    }


    @Test
    void shouldDownloadLinksFromAllPagesWhenFocusIsFalse() {

        String outputPath = tempFolder.toString();

        String configPath = basePath + "/config/hard_focus_false";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";

        String crawlerId = "crawler0";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath, "-cid", crawlerId };
        Main.main(args);

        // then
        Frontier frontier = openFrontier(outputPath + "/" + crawlerId, configPath);

        List<String> shouldBeDownloaded = asList(
                "index.html",
                "index_irrelevant.html",
                "index_relevant.html",
                "relevant_page1.html",
                "irrelevant_page1.html",
                "relevant_page2.html",
                "irrelevant_page2.html"
        );

        for (String url : shouldBeDownloaded) {
            LinkRelevance link = LinkRelevance.create("http://127.0.0.1:1234/" + url);
            assertThat(frontier.exist(link)).as("URL=" + link.getURL().toString()).isLessThan(0d);
        }
    }
    
    private Frontier openFrontier(String outputPath, String configPath) {
        Configuration config = new Configuration(configPath + "/ache.yml");
        String linkDirectory = config.getLinkStorageConfig().getLinkDirectory();
        String dir = Paths.get(outputPath, linkDirectory).toString();
        return new Frontier(dir, 1000,
                config.getLinkStorageConfig().getPersistentHashtableBackend());
    }
}
