package achecrawler.crawler.async;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.mockito.ArgumentCaptor;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import achecrawler.crawler.async.SitemapXmlHandler.SitemapData;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.crawler.crawlercommons.util.Headers;
import achecrawler.link.LinkStorage;
import achecrawler.link.frontier.LinkRelevance;

class SitemapXmlHandlerTest {

    @Test
    void shouldParseLinksFromSitemapXml() throws Exception {
        // given
        LinkStorage linkStorageMock = Mockito.mock(LinkStorage.class);
        SitemapXmlHandler handler = new SitemapXmlHandler(linkStorageMock);

        String url = "http://www.example.com/sitemap.xml";
        Path sitemapFilePath =
                Paths.get(SitemapXmlHandler.class.getResource("sample-sitemap.xml").toURI());
        byte[] sitemapContent = Files.readAllBytes(sitemapFilePath);

        FetchedResult response = new FetchedResult(url, url, 1, new Headers(), sitemapContent,
                "text/xml", 1, null, url, 0, "127.0.0.1", 200, "OK");

        LinkRelevance link = new LinkRelevance(new URL(url), 1, LinkRelevance.Type.SITEMAP);

        // when
        handler.completed(link, response);

        // then
        ArgumentCaptor<SitemapData> argument = ArgumentCaptor.forClass(SitemapData.class);
        Mockito.verify(linkStorageMock).insert(argument.capture());
        SitemapData sitemapData = argument.getValue();

        assertThat(sitemapData, is(notNullValue()));
        assertThat(sitemapData.sitemaps.size(), is(0));
        assertThat(sitemapData.links.size(), is(4));
    }

    @Test
    void shouldParseChildSitemapsFromSitemapIndexes() throws Exception {
        // given
        LinkStorage linkStorageMock = Mockito.mock(LinkStorage.class);

        SitemapXmlHandler handler = new SitemapXmlHandler(linkStorageMock);

        String url = "http://www.example.com/sitemap.xml";
        Path sitemapFilePath =
                Paths.get(SitemapXmlHandler.class.getResource("sitemap-index.xml").toURI());
        byte[] sitemapContent = Files.readAllBytes(sitemapFilePath);

        FetchedResult response = new FetchedResult(url, url, 1, new Headers(), sitemapContent,
                "text/xml", 1, null, url, 0, "127.0.0.1", 200, "OK");

        LinkRelevance link = new LinkRelevance(new URL(url), 1, LinkRelevance.Type.SITEMAP);

        // when
        handler.completed(link, response);

        // then
        ArgumentCaptor<SitemapData> argument = ArgumentCaptor.forClass(SitemapData.class);
        Mockito.verify(linkStorageMock).insert(argument.capture());
        SitemapData sitemapData = argument.getValue();

        assertThat(sitemapData, is(notNullValue()));
        assertThat(sitemapData.sitemaps.size(), is(3));
        assertThat(sitemapData.links.size(), is(0));
    }

}
