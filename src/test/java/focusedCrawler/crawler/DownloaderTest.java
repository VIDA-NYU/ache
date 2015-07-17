package focusedCrawler.crawler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.junit.Test;

public class DownloaderTest {

    ArrayList<String> urlsWithMime;
    String[] testURLs;

    public DownloaderTest() {

    }
    
    @Test
    public void shouldReturnOriginalAndRedirectedUrl() throws CrawlerException, MalformedURLException {

    // given
    URL originalUrl = new URL( "http://onesource.thomsonreuters.com/");
    URL expectedRedirectedUrl = new URL("https://tax.thomsonreuters.com/products/brands/onesource");

    // when
    Downloader downloader = new Downloader(originalUrl,"");

    //then
    assertEquals("is redirection not true",true, downloader.isRedirection());
    assertEquals("original url not equal",downloader.getOriginalUrl(),originalUrl);
    assertEquals("redirection url not true ",downloader.getRedirectionUrl(), expectedRedirectedUrl);

    }

    @Test
    public void shouldExtractMimeTypeWhenAvailable() throws MalformedURLException, CrawlerException {
        
        Downloader downloader = new Downloader("http://www.youtube.com/user/SamaritansPurseVideo","");
        
        assertEquals("Mime type extractor fails! ","text/html; charset=utf-8",downloader.getMimeType());
    }
   
  
    
    
}