package focusedCrawler.crawler.async;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.tika.metadata.Metadata;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import crawlercommons.fetcher.BaseFetchException;
import crawlercommons.fetcher.BaseFetcher;
import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.HttpFetchException;
import crawlercommons.fetcher.Payload;

@SuppressWarnings("serial")
public class HtmlUnitFetcher extends BaseFetcher {
    
    private WebClient client;

    public HtmlUnitFetcher() {
        
        client = new WebClient();
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setAppletEnabled(false);
        client.getOptions().setPrintContentOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setGeolocationEnabled(false);
        client.getOptions().setDoNotTrackEnabled(false);
    }

    @Override
    public FetchedResult get(String url, Payload payload) throws BaseFetchException {
        
        URL urlObject = null;
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL provided: "+url);
        }
        
        int statusCode = -1;
        String reasonPhrase = null;
        Metadata headers = null;
        try {
            HtmlPage page = client.getPage(urlObject);
            
            String contentType = page.getWebResponse().getContentType();
            statusCode = page.getWebResponse().getStatusCode();
            reasonPhrase = page.getWebResponse().getStatusMessage();
            
            long fetchTime = System.currentTimeMillis();
            String content = page.asXml();
            
            String redirectedUrl = url; // TODO
            int responseRate = 0; // TODO
            String newBaseUrl = url; // TODO
            int numRedirects = 0; // TODO
            String hostAddress = ""; // TODO
            
            headers = new Metadata();
            List<NameValuePair> response = page.getWebResponse().getResponseHeaders();
            for (NameValuePair header : response) {
                headers.add(header.getName(), header.getValue());
            }
            
            return new FetchedResult(url, redirectedUrl, fetchTime, headers, content.getBytes(), contentType, responseRate, payload, newBaseUrl, numRedirects, hostAddress, statusCode, reasonPhrase);
            
        } catch (FailingHttpStatusCodeException | IOException e) {
            throw new HttpFetchException(url, "Error fetching " + url + " due to \"" +  reasonPhrase + "\"", statusCode, headers);
        }
    }

    @Override
    public void abort() {
        // TODO Try to actually abort the request
    }

}
