package achecrawler.integration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import achecrawler.util.persistence.PersistentHashtable;
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

public class RobotsAndSitemapTest {

    @TempDir
    public File tempFolder;
    
    static String basePath = RobotsAndSitemapTest.class.getResource("robots_and_sitemap_test").getFile();

    private static HttpServer httpServer;

    @BeforeAll
    static void setupServer() throws IOException {
        httpServer = new TestWebServerBuilder("127.0.0.1", 1234)
            .withStaticFolder(Paths.get(basePath, "static"))
            .start();
    }

    @AfterAll
    static void shutdownServer() {
        httpServer.stop(0);
    }

    @Test
    void shouldDownloadLinksListedOnSitemapsXml() throws Exception {

        String outputPath = tempFolder.toString();

        String configPath = basePath + "/config/";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";
        String crawlerId = "crawl1";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath, "-cid", crawlerId };
        Main.main(args);

        Frontier frontier = openFrontier(outputPath + "/" + crawlerId, configPath);

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
            LinkRelevance link = new LinkRelevance("http://127.0.0.1:1234/" + url, LinkRelevance.DEFAULT_RELEVANCE);
            System.out.println(link);
            assertThat("URL="+url, frontier.exist(link), is(nullValue()));
        }
    }

    @Test
    void test1ToNotToDownloadSitesDisallowedOnRobots() {

        String outputPath = tempFolder.toString();

        String configPath = basePath + "/config/";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";
        String crawlerId = "crawl1";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath, "-cid", crawlerId };
        Main.main(args);
        Frontier frontier = openFrontier(outputPath + "/" + crawlerId, configPath);

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
    void test2ToNotToDownloadSitesDisallowedOnRobotsWithSitemapsFalse() {

        String outputPath = tempFolder.toString();

        String configPath = basePath + "/config2/";
        String seedPath = basePath + "/seeds.txt";
        String modelPath = basePath + "/model/";
        String crawlerId = "crawl1";

        // when
        String[] args = { "startCrawl", "-c", configPath, "-m", modelPath, "-o", outputPath, "-s", seedPath, "-cid", crawlerId };
        Main.main(args);

        // then
        Frontier frontier = openFrontier(outputPath + "/" + crawlerId, configPath);

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

    @Test
    void testKryoSerializationAndDeserialization() {
        final String simpleRobotsTxt = "User-agent: *" + "\r\n" + "Disallow:";

        SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = robotParser.parseContent("http://domain.com",
                simpleRobotsTxt.getBytes(UTF_8), "text/plain", "Any-darn-crawler");

        String outputPath = tempFolder.toString();

        PersistentHashtable<SimpleRobotRules> robotRulesMap = new PersistentHashtable<>(outputPath, 0,
                SimpleRobotRules.class);
        robotRulesMap.put("robots", rules);
        robotRulesMap.commit();
        rules = robotRulesMap.get("robots");

        assertNotNull(rules);
        assertTrue(rules.isAllowed("http://www.domain.com/anypage.html"));
    }

    private void assertWasCrawled(String url, Frontier frontier) {
        LinkRelevance link = LinkRelevance.create("http://127.0.0.1:1234/" + url);
        assertThat("URL=" + url, frontier.exist(link), is(lessThan(0d)));
    }

    private void assertWasNotCrawled(String url, Frontier frontier) {
        LinkRelevance link = LinkRelevance.create(url);
        assertThat("URL=" + url, frontier.exist(link), is(not(lessThan(0d))));
    }

    private Frontier openFrontier(String outputPath, String configPath) {
        Configuration config = new Configuration(configPath + "/ache.yml");
        String linkDirectory = config.getLinkStorageConfig().getLinkDirectory();
        String dir = Paths.get(outputPath, linkDirectory).toString();
        return new Frontier(dir, 1000,
                config.getLinkStorageConfig().getPersistentHashtableBackend());
    }
}
