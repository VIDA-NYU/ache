package focusedCrawler.crawler.async;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.tika.metadata.Metadata;
import org.junit.Test;

import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.CommunicationException;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;

public class RobotsTxtHandlerTest {

    static class LinkStorageMock extends StorageDefault {
        public RobotsTxtHandler.RobotsData robotsData = null;
        @Override
        public synchronized Object insert(Object obj) throws StorageException, CommunicationException {
            if(obj instanceof RobotsTxtHandler.RobotsData) {
                this.robotsData = (RobotsTxtHandler.RobotsData) obj;
            }
            return null;
        }
    };

    @Test
    public void shouldParseLinksFromSitemapXml() throws Exception {
        // given
        LinkStorageMock linkStorageMock = new LinkStorageMock(); 
        RobotsTxtHandler handler = new RobotsTxtHandler(linkStorageMock, "TestAgent");
        
        String url = "http://www.example.com/robots.txt";
        Path robotsFilePath = Paths.get(RobotsTxtHandler.class.getResource("sample-robots.txt").toURI());
        byte[] robotsContent = Files.readAllBytes(robotsFilePath);
        
        FetchedResult response = new FetchedResult(url, url, 1, new Metadata(), robotsContent, "text/plain", 1, null, url, 0, "127.0.0.1", 200, "OK");

        LinkRelevance link = new LinkRelevance(new URL(url), 1, LinkRelevance.Type.ROBOTS);
        
        // when
        handler.completed(link , response);
        
        // then
        assertThat(linkStorageMock.robotsData, is(notNullValue()));
        assertThat(linkStorageMock.robotsData.sitemapUrls.size(), is(2));
        assertThat(linkStorageMock.robotsData.sitemapUrls.get(0), is("http://www.example.com/example-sitemap/sitemap.xml"));
        assertThat(linkStorageMock.robotsData.sitemapUrls.get(1), is("http://www.example.com/example-sitemap/sitemap-news.xml"));
    }

}
