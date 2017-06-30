package focusedCrawler.memex.cdr;

import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringJoiner;


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

    public String getObjOriginalUrl() {
        return this.objOriginalUrl;
    }

    public String getObjStoredUrl() {
        return this.objStoredUrl;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Date getTimestampCrawl() {
        return this.timestampCrawl;
    }

    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }
}
