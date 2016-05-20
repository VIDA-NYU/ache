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

public class SitemapXmlHandlerTest {
    
    static class LinkStorageMock extends StorageDefault {
        public SitemapXmlHandler.SitemapData sitemapData = null;
        @Override
        public synchronized Object insert(Object obj) throws StorageException, CommunicationException {
            if(obj instanceof SitemapXmlHandler.SitemapData) {
                this.sitemapData = (SitemapXmlHandler.SitemapData) obj;
            }
            return null;
        }
    };

    @Test
    public void shouldParseLinksFromSitemapXml() throws Exception {
        // given
        LinkStorageMock linkStorageMock = new LinkStorageMock(); 
        SitemapXmlHandler handler = new SitemapXmlHandler(linkStorageMock);
        
        String url = "http://www.example.com/sitemap.xml";
        Path sitemapFilePath = Paths.get(SitemapXmlHandler.class.getResource("sample-sitemap.xml").toURI());
        byte[] sitemapContent = Files.readAllBytes(sitemapFilePath);
        
        FetchedResult response = new FetchedResult(url, url, 1, new Metadata(), sitemapContent, "text/xml", 1, null, url, 0, "127.0.0.1", 200, "OK");

        LinkRelevance link = new LinkRelevance(new URL(url), 1, LinkRelevance.Type.SITEMAP);
        
        // when
        handler.completed(link , response);
        
        // then
        assertThat(linkStorageMock.sitemapData, is(notNullValue()));
        assertThat(linkStorageMock.sitemapData.sitemaps.size(), is(0));
        assertThat(linkStorageMock.sitemapData.links.size(), is(4));
    }
    
    @Test
    public void shouldParseChildSitemapsFromSitemapIndexes() throws Exception {
        // given
        LinkStorageMock linkStorageMock = new LinkStorageMock(); 
        
        SitemapXmlHandler handler = new SitemapXmlHandler(linkStorageMock);
        
        String url = "http://www.example.com/sitemap.xml";
        Path sitemapFilePath = Paths.get(SitemapXmlHandler.class.getResource("sitemap-index.xml").toURI());
        byte[] sitemapContent = Files.readAllBytes(sitemapFilePath);
        
        FetchedResult response = new FetchedResult(url, url, 1, new Metadata(), sitemapContent, "text/xml", 1, null, url, 0, "127.0.0.1", 200, "OK");

        LinkRelevance link = new LinkRelevance(new URL(url), 1, LinkRelevance.Type.SITEMAP);
        
        // when
        handler.completed(link , response);
        
        // then
        assertThat(linkStorageMock.sitemapData, is(notNullValue()));
        assertThat(linkStorageMock.sitemapData.sitemaps.size(), is(3));
        assertThat(linkStorageMock.sitemapData.links.size(), is(0));
    }

}
