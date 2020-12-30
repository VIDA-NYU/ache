package achecrawler.memex.cdr;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;


public class CDR31MediaObject {

    private String objOriginalUrl;
    private String objStoredUrl;
    private String contentType;
    private Date timestampCrawl;
    private Map<String, String> responseHeaders;

    public void setObjOriginalUrl(String objOriginalUrl) {
        this.objOriginalUrl = objOriginalUrl;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setObjStoredUrl(String objStoredUrl) {
        this.objStoredUrl = objStoredUrl;
    }

    public void setTimestampCrawl(Date timestampCrawl) {
        this.timestampCrawl = timestampCrawl;
    }   

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        this.responseHeaders = new HashMap<>();
        for (Entry<String, List<String>> header : responseHeaders.entrySet()) {
            if (header.getValue() != null) {
                StringJoiner joiner = new StringJoiner(",");
                for (String value : header.getValue()) {
                    joiner.add(value);
                }
                this.responseHeaders.put(header.getKey(), joiner.toString());
            }
        }
    }

    @JsonProperty("obj_original_url")
    public String getObjOriginalUrl() {
        return this.objOriginalUrl;
    }

    @JsonProperty("obj_stored_url")
    public String getObjStoredUrl() {
        return this.objStoredUrl;
    }

    @JsonProperty("content_type")
    public String getContentType() {
        return this.contentType;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonProperty("timestamp_crawl")
    public Date getTimestampCrawl() {
        return this.timestampCrawl;
    }

    @JsonProperty("response_headers")
    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

}
