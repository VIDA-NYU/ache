package focusedCrawler.target.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tika.metadata.Metadata;

import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.classifier.TargetRelevance;

@SuppressWarnings("serial")
public class Page implements Serializable {

    private URL url;
    private URL redirectedURL;
    private byte[] content;
    private String contentType;
    private Map<String, List<String>> responseHeaders;
    private long fetchTime;
    
    private LinkRelevance linkRelevance;
    private ParsedData parsedData;
    private TargetRelevance targetRelevance;

    private boolean auth = false;
    
    public Page() {
        // required for JSON serialization
    }
    
    public Page(URL url, String content) {
        this(url, content.getBytes(), null, null);
    }

    public Page(URL url, String content, Map<String, List<String>> responseHeaders) {
        this(url, content.getBytes(), responseHeaders, null);
    }
    
    public Page(URL url, byte[] content, Map<String, List<String>> responseHeaders, URL redirectedURL) {
        this.url = url;
        this.content = content;
        this.redirectedURL = redirectedURL;
        if (responseHeaders != null) {
            this.responseHeaders = responseHeaders;
            this.contentType = extractContentType(responseHeaders);
        }
    }

    public Page(TargetModelCbor target) throws MalformedURLException {
        this.url = new URL(target.url);
        this.content = ((String) target.response.get("body")).getBytes();
        this.fetchTime = target.timestamp * 1000;
    }

    public Page(TargetModelJson target) throws MalformedURLException {
        this.url = new URL(target.getUrl());
        this.redirectedURL = new URL(target.getRedirectedUrl());
        this.content = target.getContent();
        this.responseHeaders = target.getResponseHeaders();
        this.fetchTime = target.getFetchTime();
        this.contentType = target.getContentType();
        this.targetRelevance = target.getRelevance();

    }

    public Page(FetchedResult fetchedResult) throws MalformedURLException {
        this.url = new URL(fetchedResult.getBaseUrl());
        this.content = fetchedResult.getContent();
        this.fetchTime = fetchedResult.getFetchTime();
        if (fetchedResult.getNumRedirects() > 0) {
            this.redirectedURL = new URL(fetchedResult.getFetchedUrl());
        }
        parseResponseHeaders(fetchedResult.getHeaders());
    }

    public static String extractContentType(Map<String, List<String>> responseHeaders) {
        for (Entry<String, List<String>> header : responseHeaders.entrySet()) {
            if ("content-type".compareToIgnoreCase(header.getKey()) == 0) {
                List<String> values = header.getValue();
                if (!values.isEmpty()) {
                    return values.get(0);
                }
            }
        }
        return null;
    }

    private void parseResponseHeaders(Metadata headerAsMetadata) {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        String[] names = headerAsMetadata.names();
        if(names != null && names.length > 0) {
            for(String name : names) {
                List<String> values = Arrays.asList(headerAsMetadata.getValues(name));
                if(values.isEmpty()) {
                    continue;
                }
                responseHeaders.put(name, values);
                if("content-type".compareToIgnoreCase(name) == 0) {
                    this.contentType = values.get(0);
                }
            }
        }
        this.responseHeaders = responseHeaders;
    }

    public String getDomainName() {
        String domain = url.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
    

    public boolean isHtml() {
        return contentType != null && contentType.toLowerCase().contains("text/html");
    }

    public boolean isHub() {
        if (linkRelevance != null) {
            double relevance = linkRelevance.getRelevance();
            return relevance > LinkRelevance.DEFAULT_HUB_RELEVANCE &&
                   relevance < LinkRelevance.DEFAULT_AUTH_RELEVANCE;
        }
        return false;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public URL getURL() {
        return url;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    /*
     * Warning: using this method for non-textual mime-types might cause data corruption.
     */
    public String getContentAsString() {
        return new String(content);
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public URL getRedirectedURL() {
        return redirectedURL;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public LinkRelevance getLinkRelevance() {
        return linkRelevance;
    }

    public void setLinkRelevance(LinkRelevance linkRelevance) {
        this.linkRelevance = linkRelevance;
    }

    public ParsedData getParsedData() {
        return parsedData;
    }

    public void setParsedData(ParsedData parsedData) {
        this.parsedData = parsedData;
    }

    public TargetRelevance getTargetRelevance() {
        return targetRelevance;
    }

    public void setTargetRelevance(TargetRelevance targetRelevance) {
        this.targetRelevance = targetRelevance;
    }

    public String getRequestedUrl() {
        return url.toString();
    }

    public String getFinalUrl() {
        return redirectedURL == null ? url.toString() : redirectedURL.toString();
    }

}
