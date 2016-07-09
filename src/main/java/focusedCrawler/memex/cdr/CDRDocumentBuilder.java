package focusedCrawler.memex.cdr;

import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CDRDocumentBuilder {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private CDRDocument data = new CDRDocument();
    
    public CDRDocumentBuilder withId(String id) {
        data.setId(id);
        return this;
    }

    public CDRDocumentBuilder withUrl(String url) {
        data.setUrl(url);
        return this;
    }

    public CDRDocumentBuilder withTimestamp(long timestamp) {
        data.setTimestamp(timestamp);
        return this;
    }

    public CDRDocumentBuilder withTeam(String team) {
        data.setTeam(team);
        return this;
    }

    public CDRDocumentBuilder withCrawler(String crawler) {
        data.setCrawler(crawler);
        return this;
    }

    public CDRDocumentBuilder withRawContent(String rawContent) {
        data.setRawContent(rawContent);
        return this;
    }

    public CDRDocumentBuilder withContentType(String contentType) {
        data.setContentType(contentType);
        return this;
    }

    public CDRDocumentBuilder withCrawlData(Object crawlData) {
        data.setCrawlData(crawlData);
        return this;
    }

    public CDRDocumentBuilder withMetadata(Map<String, String> metadata) {
        data.setExtractedMetadata(metadata);
        return this;
    }

    public CDRDocumentBuilder withContent(String content) {
        data.setExtractedText(content);
        return this;
    }

    public CDRDocumentBuilder withVersion(String version) {
        data.setVersion(version);
        return this;
    }

    public CDRDocument build() {

        if (data.getUrl() == null) {
            throw new IllegalArgumentException("Field 'url' is mandatory");
        }
        if (data.getRawContent() == null) {
            throw new IllegalArgumentException("Field 'raw_content' is mandatory");
        }
        if (data.getCrawler() == null) {
            throw new IllegalArgumentException("Field 'crawler' is mandatory");
        }
        if (data.getTeam() == null) {
            throw new IllegalArgumentException("Field 'team' is mandatory");
        }
        if (data.getVersion() == null) {
            throw new IllegalArgumentException("Field 'version' is mandatory");
        }
        if (data.getTimestamp() == 0) {
            throw new IllegalArgumentException("Field 'timestamp' is mandatory");
        }
        
        if (data.getExtractedMetadata() == null || data.getExtractedText() == null) {
            // auto-generate extracted_metadata field using Tika
            TikaExtractor extractor = new TikaExtractor(data.getRawContent());
            if (data.getExtractedMetadata() == null) {
                data.setExtractedMetadata(extractor.getMetadata());
            }
            // auto-generate extracted_text field using Tika
            if (data.getExtractedText() == null) {
                data.setExtractedText(extractor.getPlainText());
            }
        }
        
        if (data.getId() == null) {
            // auto-generate _id field
            data.setId(computeId());
        }

        return data;
    }

    public String buildAsJson() throws JsonProcessingException {
        return jsonMapper.writeValueAsString(this.build());
    }
    
    private String computeId() {
        StringBuilder textForId = new StringBuilder();
        textForId.append(data.getUrl());
        textForId.append("-");
        textForId.append(data.getTimestamp());
        return DigestUtils.sha256Hex(textForId.toString()).toUpperCase();
    }

}
