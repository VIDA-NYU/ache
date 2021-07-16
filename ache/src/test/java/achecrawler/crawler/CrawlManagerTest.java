package achecrawler.crawler;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpServer;

import achecrawler.config.Configuration;
import achecrawler.crawler.CrawlersManager.CrawlContext;
import achecrawler.crawler.CrawlersManager.CrawlType;
import achecrawler.crawler.async.TestWebServerBuilder;
import achecrawler.integration.HardFocusCrawlingTest;
import achecrawler.target.repository.FilesTargetRepository;

public class CrawlManagerTest {


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    static String basePath = HardFocusCrawlingTest.class.getResource("hard_focus_test").getFile();

    private static HttpServer httpServer;

    @BeforeClass
    public static void setupServer() throws IOException, InterruptedException {
        httpServer = new TestWebServerBuilder("127.0.0.1", 1234)
                .withStaticFolder(Paths.get(basePath, "html"))
                .start();
    }

    @AfterClass
    public static void shutdownServer() throws IOException {
        httpServer.stop(0);
    }


    @Test
    public void createAndStartDeepCrawl() throws Exception {
        // given
        // String baseDataPath = "/tmp/junit34rda3dd";
        String baseDataPath = tempFolder.newFolder().toString();
        Map<?, ?> props = ImmutableMap.of(
                "link_storage.scheduler.host_min_access_interval", 0);
        Configuration baseConfig = new Configuration(props);
        CrawlersManager manager = new CrawlersManager(baseDataPath, baseConfig);

        String crawlerId = "crawl1";
        List<String> seeds = asList("http://127.0.0.1:1234/index.html");

        // when
        manager.createCrawler(crawlerId, CrawlType.DeepCrawl, seeds, null);

        CrawlContext crawlContext = manager.getCrawl(crawlerId);
        Map<String, CrawlContext> crawsList = manager.getCrawls();

        // then
        assertThat(crawlContext, notNullValue());
        assertThat(crawlContext.getCrawler(), notNullValue());
        assertThat(crawsList, notNullValue());
        assertThat(crawsList.size(), is(1));

        // when
        manager.startCrawl(crawlerId);
        manager.getCrawl(crawlerId).getCrawler().awaitTerminated();


        // then
        String targetDirectory = baseDataPath + "/" + crawlerId + "/"
                + baseConfig.getTargetStorageConfig().getTargetStorageDirectory();

        Set<String> crawledPages = listCrawledPages(baseConfig, targetDirectory);

        List<String> allPages = asList(
                "http://127.0.0.1:1234/index.html",
                "http://127.0.0.1:1234/index_irrelevant.html",
                "http://127.0.0.1:1234/index_relevant.html",
                "http://127.0.0.1:1234/relevant_page1.html",
                "http://127.0.0.1:1234/irrelevant_page2.html",
                "http://127.0.0.1:1234/irrelevant_page1.html",
                "http://127.0.0.1:1234/relevant_page2.html");

        for (String page : allPages) {
            assertTrue(page, crawledPages.contains(page));
        }

    }

    private Set<String> listCrawledPages(Configuration baseConfig, String targetDirectory) {

        FilesTargetRepository repository = new FilesTargetRepository(targetDirectory,
                baseConfig.getTargetStorageConfig().getMaxFileSize());

        Set<String> crawledPages = new TreeSet<>();
        repository.pagesIterator().forEachRemaining((p -> {
            crawledPages.add(p.getFinalUrl());
        }));

        return crawledPages;
    }

}
