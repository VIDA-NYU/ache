package focusedCrawler.target.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.archive.format.warc.WARCConstants.WARCRecordType;
import org.archive.io.warc.WARCRecordInfo;


import focusedCrawler.target.classifier.TargetRelevance;


public class TargetModelWarcRecord extends WARCRecordInfo {
	
	private TargetRelevance relevance;
	private long fetchTime;
	private String redirectedUrl;
	private String contentType;
	private Map<String, List<String>> responseHeaders;
	
	public TargetModelWarcRecord() {
		
	}
	
	public TargetModelWarcRecord(Page page) throws IOException {
		if (page.getURL() != null)
            this.url = page.getURL().toString();
        if (page.getRedirectedURL() != null)
            this.setRedirectedUrl(page.getRedirectedURL().toString());
        byte[] content = page.getContent();
        this.contentLength = (long) content.length;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(content);
        
        this.contentStream = new ByteArrayInputStream(baos.toByteArray()); 
        this.setResponseHeaders(page.getResponseHeaders());
        this.setFetchTime(page.getFetchTime());
        this.setContentType(page.getContentType());

        this.relevance = page.getTargetRelevance();
        this.type = WARCRecordType.response;
	}
	
	/**
	 * Sets the target relevance in Warc Record
	 * @param target
	 */
	public void setRelevance(TargetRelevance target) {
		relevance = target; 
	}
	
	/**
	 * Returns the relevance of the page
	 * @return
	 */
	public TargetRelevance getRelevance() {
		return relevance;
	}

	/**
	 * Returns the fetch time of the page
	 * @return
	 */
	public long getFetchTime() {
		return fetchTime;
	}

	/**
	 * Sets the fetch time for that web page
	 * @param fetchTime
	 */
	public void setFetchTime(long fetchTime) {
		this.fetchTime = fetchTime;
	}
	

	/**
	 * Returns the redirected URL
	 * @return
	 */
	public String getRedirectedUrl() {
		return redirectedUrl;
	}

	/**
	 * Sets the redirected url
	 * @param redirectedUrl
	 */
	public void setRedirectedUrl(String redirectedUrl) {
		this.redirectedUrl = redirectedUrl;
	}

	/**
	 * Returns content type
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type of the page
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, List<String>> getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

}
