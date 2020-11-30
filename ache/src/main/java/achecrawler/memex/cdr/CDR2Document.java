package achecrawler.memex.cdr;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.mime.MediaType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.memex.cdr.TikaExtractor.ParsedData;

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
public class CDR2Document implements Serializable {

    private String _id;
    
    private String url;
    
    private long timestamp;
    
    private String team;
    
    private String crawler;
    
    @JsonProperty("raw_content")
    private String rawContent;
    
    @JsonProperty("content_type")
    private String contentType;
    
    @JsonProperty("crawl_data")
    private Object crawlData;
    
    @JsonProperty("extracted_metadata")
    private Map<String, String> extractedMetadata;
    
    @JsonProperty("extracted_text")
    private String extractedText;
    
    private String version;

    public CDR2Document() {
    }

    public CDR2Document(Builder builder) {
        this._id = builder._id;
        this.url = builder.url;
        this.timestamp = builder.timestamp;
        this.team = builder.team;
        this.crawler = builder.crawler;
        this.rawContent = builder.rawContent;
        this.contentType = builder.contentType;
        this.crawlData = builder.crawlData;
        this.extractedMetadata = builder.extractedMetadata;
        this.extractedText = builder.extractedText;
        this.version = builder.version;
    }

    public String getUrl() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTeam() {
        return team;
    }

    public String getCrawler() {
        return crawler;
    }

    
    public String getRawContent() {
        return rawContent;
    }

    public String getContentType() {
        return contentType;
    }

    public Object getCrawlData() {
        return crawlData;
    }

    public Map<String, String> getExtractedMetadata() {
        return extractedMetadata;
    }

    public String getExtractedText() {
        return extractedText;
    }

    @JsonIgnore
    public String getId() {
        return this._id;
    }
    
    public String getVersion() {
        return version;
    }
    
    public static class Builder {

        private static final TikaExtractor extractor = new TikaExtractor();
        private static final ObjectMapper jsonMapper = new ObjectMapper();

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
        

        public CDR2Document build() {

            if (this.url == null) throw new IllegalArgumentException("Field 'url' is mandatory");
            if (this.rawContent == null) throw new IllegalArgumentException("Field 'raw_content' is mandatory");
            if (this.crawler == null) throw new IllegalArgumentException("Field 'crawler' is mandatory");
            if (this.team == null) throw new IllegalArgumentException("Field 'team' is mandatory");
            if (this.version == null) throw new IllegalArgumentException("Field 'version' is mandatory");
            if (this.timestamp == 0) throw new IllegalArgumentException("Field 'timestamp' is mandatory");
            
            if(this.contentType == null) {
                MediaType mediaType = extractor.detect(this.rawContent, this.url, this.contentType);
                this.contentType = mediaType.getBaseType().toString();
            }
            
            if (this.extractedMetadata == null || this.extractedText == null) {
                MediaType mediaType = MediaType.parse(this.contentType);
                if(mediaType.getBaseType().equals(MediaType.TEXT_HTML)) {
                    // auto-generate extracted_metadata field using Tika
                    ParsedData parsedData = extractor.parse(this.rawContent, this.url, this.contentType);
                    if (this.extractedMetadata == null && parsedData != null) {
                        this.extractedMetadata = parsedData.getMetadata();
                    }
                    // auto-generate extracted_text field using Tika
                    if (this.extractedText == null && parsedData != null) {
                        this.extractedText = parsedData.getPlainText();
                    }
                }
            }
            
            if (this._id == null) {
                // auto-generate _id field
                this._id = computeId();
            }

            return new CDR2Document(this);
        }

        public String buildAsJson() throws JsonProcessingException {
            return jsonMapper.writeValueAsString(this.build());
        }
        
        private String computeId() {
            StringBuilder textForId = new StringBuilder();
            textForId.append(this.url);
            textForId.append("-");
            textForId.append(this.timestamp);
            return DigestUtils.sha256Hex(textForId.toString()).toUpperCase();
        }

        public Builder setId(String id) {
            this._id = id;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setTeam(String team) {
            this.team = team;
            return this;
        }

        public Builder setCrawler(String crawler) {
            this.crawler = crawler;
            return this;
        }

        public Builder setRawContent(String rawContent) {
            this.rawContent = rawContent;
            return this;
        }

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setCrawlData(Object crawlData) {
            this.crawlData = crawlData;
            return this;
        }

        public Builder setExtractedMetadata(Map<String, String> extractedMetadata) {
            this.extractedMetadata = extractedMetadata;
            return this;
        }

        public Builder setExtractedText(String extractedText) {
            this.extractedText = extractedText;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

    }

}
