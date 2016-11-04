package focusedCrawler.memex.cdr;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
{
    'url': <full URL of the web page>,
    'timestamp': <timestamp for data when scraped, in epoch milliseconds>,
    'team': <name of crawling team>,
    'crawler': <name of crawler; each type of crawler should have a distinct name or reference>,
    'raw_content': <full data of raw crawled page; source page that can be reproduced for system purposes (e.g. provenance, law enforcement evidence)>,
    'content_type': <mime-type of data in stored in raw_content>,
    'crawl_data': <source page from crawler that extracts full text but not full layout; full-text>,
    'extracted_metadata': {
      // Metadata extracted by Tika/other extractors
    },
    ‘extracted_text’: {
      // Text extracted from the document if applicable for that mime type
     }
}
*/
@SuppressWarnings("serial")
@JsonInclude(Include.NON_NULL)
public class CDRDocument implements Serializable {

    private String _id;
    private String url;
    private long timestamp;
    private String team;
    private String crawler;
    private String rawContent;
    private String contentType;
    private Object crawlData;
    private Map<String, String> extractedMetadata;
    private String extractedText;
    private String version;

    public CDRDocument() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getCrawler() {
        return crawler;
    }

    public void setCrawler(String crawler) {
        this.crawler = crawler;
    }

    @JsonProperty("raw_content")
    public String getRawContent() {
        return rawContent;
    }

    @JsonProperty("raw_content")
    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    @JsonProperty("content_type")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("content_type")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonProperty("crawl_data")
    public Object getCrawlData() {
        return crawlData;
    }

    @JsonProperty("crawl_data")
    public void setCrawlData(Object crawlData) {
        this.crawlData = crawlData;
    }

    @JsonProperty("extracted_metadata")
    public Map<String, String> getExtractedMetadata() {
        return extractedMetadata;
    }

    @JsonProperty("extracted_metadata")
    public void setExtractedMetadata(Map<String, String> extractedMetadata) {
        this.extractedMetadata = extractedMetadata;
    }

    @JsonProperty("extracted_text")
    public String getExtractedText() {
        return extractedText;
    }

    @JsonProperty("extracted_text")
    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    @JsonProperty("_id")
    public void setId(String _id) {
        this._id = _id;
    }

    @JsonProperty("_id")
    public String getId() {
        return this._id;
    }
    
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }
}
