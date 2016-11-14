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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import focusedCrawler.crawler.async.SitemapXmlHandler.SitemapData;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;

public class SitemapXmlHandlerTest {
    
    @Test
    public void shouldParseLinksFromSitemapXml() throws Exception {
        // given
        EventBus eventBusMock = Mockito.mock(EventBus.class);
        SitemapXmlHandler handler = new SitemapXmlHandler(eventBusMock);
        
        String url = "http://www.example.com/sitemap.xml";
        Path sitemapFilePath = Paths.get(SitemapXmlHandler.class.getResource("sample-sitemap.xml").toURI());
        byte[] sitemapContent = Files.readAllBytes(sitemapFilePath);
        
        FetchedResult response = new FetchedResult(url, url, 1, new Metadata(), sitemapContent, "text/xml", 1, null, url, 0, "127.0.0.1", 200, "OK");

        LinkRelevance link = new LinkRelevance(new URL(url), 1, LinkRelevance.Type.SITEMAP);
        
        // when
        handler.completed(link , response);
        
        // then
        ArgumentCaptor<SitemapData> argument = ArgumentCaptor.forClass(SitemapData.class);
        Mockito.verify(eventBusMock).post(argument.capture());
        SitemapData sitemapData = argument.getValue();
        
        assertThat(sitemapData, is(notNullValue()));
        assertThat(sitemapData.sitemaps.size(), is(0));
        assertThat(sitemapData.links.size(), is(4));
    }
    
    @Test
    public void shouldParseChildSitemapsFromSitemapIndexes() throws Exception {
        // given
        EventBus eventBusMock = Mockito.mock(EventBus.class);
        
        SitemapXmlHandler handler = new SitemapXmlHandler(eventBusMock);
        
        String url = "http://www.example.com/sitemap.xml";
        Path sitemapFilePath = Paths.get(SitemapXmlHandler.class.getResource("sitemap-index.xml").toURI());
        byte[] sitemapContent = Files.readAllBytes(sitemapFilePath);
        
        FetchedResult response = new FetchedResult(url, url, 1, new Metadata(), sitemapContent, "text/xml", 1, null, url, 0, "127.0.0.1", 200, "OK");

        LinkRelevance link = new LinkRelevance(new URL(url), 1, LinkRelevance.Type.SITEMAP);
        
        // when
        handler.completed(link , response);
        
        // then
        ArgumentCaptor<SitemapData> argument = ArgumentCaptor.forClass(SitemapData.class);
        Mockito.verify(eventBusMock).post(argument.capture());
        SitemapData sitemapData = argument.getValue();
        
        assertThat(sitemapData, is(notNullValue()));
        assertThat(sitemapData.sitemaps.size(), is(3));
        assertThat(sitemapData.links.size(), is(0));
    }

}
