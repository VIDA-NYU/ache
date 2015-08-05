package focusedCrawler.crawler;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

public class DownloaderTest {

    @Test
    public void shouldReturnOriginalAndRedirectedUrl() throws Exception {
        // given
        URL originalUrl = new URL("http://onesource.thomsonreuters.com/");
        URL expectedRedirectedUrl = new URL("https://tax.thomsonreuters.com/products/brands/onesource");

        // when
        Downloader downloader = new Downloader(originalUrl);

        // then
        assertThat(downloader.isRedirection(), is(true));
        assertThat(downloader.getOriginalUrl(), is(originalUrl));
        assertThat(downloader.getRedirectionUrl(), is(expectedRedirectedUrl));
        assertThat(downloader.getMimeType(), containsString("text/html"));
    }
    
    @Test
    public void shouldWorkWhenRedirectionsDoesntHappen() throws Exception {
        // given
        URL originalUrl = new URL("http://www.nyu.edu");

        // when
        Downloader downloader = new Downloader(originalUrl);

        // then
        assertThat(downloader.isRedirection(), is(false));
        assertThat(downloader.getOriginalUrl(), is(originalUrl));
        assertThat(downloader.getRedirectionUrl(), is(nullValue()));
        assertThat(downloader.getMimeType(), containsString("text/html"));
    }

    @Test
    public void shouldExtractMimeTypeWhenAvailable() throws Exception {
        Downloader downloader = new Downloader("http://www.youtube.com/user/SamaritansPurseVideo");
        // Downloader downloader = new Downloader("http://www.nyu.edu");
        assertThat(downloader.getMimeType(), containsString("text/html"));
    }

}