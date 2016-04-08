package focusedCrawler.crawler.async;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import crawlercommons.fetcher.FetchedResult;

public class HtmlUnitFetcherTest {
    
    @Test
    public void shouldExecuteInlineJavaScriptAndRenderContent() throws Exception {
        // given
        String basePath = HtmlUnitFetcherTest.class.getResource("js-test").getFile();
        
        HttpServer httpServer = new TestWebServerBuilder()
                .withStaticFolder("/static", basePath)
                .start();

        HtmlUnitFetcher fetcher = new HtmlUnitFetcher();
        
        String url = TestWebServerBuilder.address+"/static/inline-js.html";
        
        // when
        FetchedResult fetchedResult = fetcher.get(url);
        
        // then
        String html = new String(fetchedResult.getContent());
        assertThat(html, containsString("Some dynamic text"));
        assertThat(fetchedResult.getStatusCode(), is(200));
        assertThat(fetchedResult.getReasonPhrase(), is("OK"));
        assertThat(fetchedResult.getFetchTime()>0, is(true));
        
        httpServer.stop(0);
    }

}