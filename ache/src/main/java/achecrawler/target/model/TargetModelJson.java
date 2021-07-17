package achecrawler.target.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import achecrawler.target.classifier.TargetRelevance;

@SuppressWarnings("serial")
public class TargetModelJson implements Serializable {

    @JsonProperty("url")
    private String url;

    @JsonProperty("redirected_url")
    private String redirectedUrl;

    @JsonProperty("content")
    private byte[] content;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("response_headers")
    private Map<String, List<String>> responseHeaders;

    @JsonProperty("fetch_time")
    private long fetchTime;
    
    @JsonProperty("relevance")
    private TargetRelevance relevance;

    @JsonProperty("crawler_id")
    private String crawlerId;

    public TargetModelJson() {
        // required for JSON deserialization
    }

    public TargetModelJson(Page page) {
        if (page.getURL() != null)
            this.url = page.getURL().toString();
        if (page.getRedirectedURL() != null)
            this.redirectedUrl = page.getRedirectedURL().toString();
        this.content = page.getContent();
        this.responseHeaders = page.getResponseHeaders();
        this.fetchTime = page.getFetchTime();
        this.contentType = page.getContentType();
        this.relevance = page.getTargetRelevance();
        this.crawlerId = page.getCrawlerId();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRedirectedUrl() {
        return redirectedUrl;
    }

    public void setRedirectedUrl(String redirectedUrl) {
        this.redirectedUrl = redirectedUrl;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public String getContentType() {
        return this.contentType;
    }

    @JsonIgnore
    public String getContentAsString() {
        return new String(content);
    }

    public TargetRelevance getRelevance() {
        return relevance;
    }

    public void setRelevance(TargetRelevance relevance) {
        this.relevance = relevance;
    }

    public String getCrawlerId() {
        return crawlerId;
    }

    public void setCrawlerId(String crawlerId) {
        this.crawlerId = crawlerId;
    }

    /*
     * This method was maintained for backwards compatibility only. JSON objects that were
     * serialized before this field was removed, are converted to the new model during
     * deserialization. Use {@link #setContent()} instead.
     */
    @Deprecated
    @JsonProperty("response_body")
    public void setResponseBody(String responseBody) {
        this.content = responseBody.getBytes();
        if (this.contentType == null && responseHeaders != null) {
            this.contentType = Page.extractContentType(responseHeaders);
        }
    }

}
