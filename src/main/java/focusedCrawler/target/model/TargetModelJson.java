package focusedCrawler.target.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
public class TargetModelJson implements Serializable {
	
	@JsonProperty("url")
	private String url;
	
	@JsonProperty("redirected_url")
	private String redirectedUrl;
	
	@JsonProperty("response_body")
	private String responseBody;
	
	@JsonProperty("response_headers")
	private Map<String, List<String>> responseHeaders;
	
	@JsonProperty("fetch_time")
	private long fetchTime;
	
	public TargetModelJson() {
		// required for JSON serialization
	}
	
	public TargetModelJson(Page page) {
		if(page.getURL() != null)
			this.url = page.getURL().toString();
		if(page.getRedirectedURL() != null)
			this.redirectedUrl = page.getRedirectedURL().toString();
		this.responseBody = page.getSource();
		this.responseHeaders = page.getResponseHeaders();
		this.fetchTime = page.getFetchTime();
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

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
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
    
    @JsonIgnore
    public String getContentType() {
        List<String> contentTypeHeader = this.getResponseHeaders().get("Content-Type");
        if (contentTypeHeader == null) {
            contentTypeHeader = this.getResponseHeaders().get("CONTENT-TYPE");
        }
        if (contentTypeHeader == null) {
            contentTypeHeader = this.getResponseHeaders().get("content-type");
        }
        if(contentTypeHeader == null || contentTypeHeader.isEmpty()) {
            return null;
        } else {
            return contentTypeHeader.iterator().next();
        }
    }
	
}