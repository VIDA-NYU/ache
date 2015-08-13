package focusedCrawler.crawler.async;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.http.UserAgent;
import focusedCrawler.crawler.async.HttpDownloader;

public class HttpDownloaderTest {
    
    final UserAgent userAgent = new UserAgent("test", "test@test.com", "test@test.com");
    
    private HttpDownloader downloader;
    
    @Before
    public void setUp() {
        this.downloader = new HttpDownloader(userAgent);
    }

    @Test
    public void shouldReturnOriginalAndRedirectedUrl() throws Exception {
        // given
        String originalUrl = "http://onesource.thomsonreuters.com/";
        String expectedRedirectedUrl = "https://tax.thomsonreuters.com/products/brands/onesource";

        // when
        FetchedResult result = downloader.dipatchDownload(originalUrl).get();
        
        // then
        assertThat(result.getNumRedirects(), is(1));
        assertThat(result.getBaseUrl(), is(originalUrl));
        assertThat(result.getFetchedUrl(), is(expectedRedirectedUrl));
        assertThat(result.getNewBaseUrl(), is(expectedRedirectedUrl));
        assertThat(result.getContentType(), containsString("text/html"));
    }
    
    @Test
    public void shouldWorkWhenRedirectionsDoesntHappen() throws Exception {
        // given
        String originalUrl = "http://nua.ac.uk/";

        // when
        FetchedResult result = downloader.dipatchDownload(originalUrl).get();
        
        // then
        assertThat(result.getNumRedirects(), is(0));
        assertThat(result.getBaseUrl(), is(originalUrl));
        assertThat(result.getFetchedUrl(), is(originalUrl));
        assertThat(result.getNewBaseUrl(), is(nullValue()));
        assertThat(result.getContentType(), containsString("text/html"));
    }

    @Test
    public void shouldExtractMimeTypeWhenAvailable() throws Exception {
        final String originalUrl = "http://www.youtube.com/user/SamaritansPurseVideo";
        FetchedResult result = downloader.dipatchDownload(originalUrl).get();
        assertThat(result.getContentType(), containsString("text/html"));
    }

}