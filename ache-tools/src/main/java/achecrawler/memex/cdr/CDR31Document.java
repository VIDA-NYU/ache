package achecrawler.memex.cdr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.mime.MediaType;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a web page according the CDRv3.1 schema.
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_NULL)
public class CDR31Document implements Serializable {

    @JsonProperty("_id")
    private String _id;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("crawler")
    private String crawler;

    @JsonProperty("objects")
    private List<CDR31MediaObject> objects;

    @JsonProperty("raw_content")
    private String rawContent;

    @JsonProperty("response_headers")
    private Map<String, String> responseHeaders;

    @JsonProperty("team")
    private String team;

    @JsonProperty("timestamp_crawl")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date timestampCrawl;

    @JsonProperty("timestamp_index")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date timestampIndex;

    @JsonProperty("url")
    private String url;

    @JsonProperty("version")
    private final float version = 3.1f;

    private Map<String, Object> extraFields;

    public CDR31Document() {
        // required from JSON deserialization
    }

    public CDR31Document(Builder builder) {
        this._id = builder._id;
        this.contentType = builder.contentType;
        this.crawler = builder.crawler;
        this.objects = builder.objects;
        this.rawContent = builder.rawContent;
        this.responseHeaders = builder.responseHeaders;
        this.team = builder.team;
        this.timestampCrawl = builder.timestampCrawl;
        this.timestampIndex = builder.timestampIndex;
        this.url = builder.url;
        this.extraFields = builder.extraFields;
    }

    public String getUrl() {
        return url;
    }

    public Date getTimestampCrawl() {
        return timestampCrawl;
    }

    public Date getTimestampIndex() {
        return timestampIndex;
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

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public String getContentType() {
        return contentType;
    }

    @JsonProperty("_id")
    public String getId() {
        return this._id;
    }

    public float getVersion() {
        return version;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtraFields() {
        return this.extraFields;
    }

    public static class Builder {

        private static final TikaExtractor extractor = new TikaExtractor();
        private static final ObjectMapper jsonMapper = new ObjectMapper();

        private String _id;
        private String contentType;
        private String crawler;
        private List<CDR31MediaObject> objects;
        private String rawContent;
        private Map<String, String> responseHeaders;
        private String team;
        private Date timestampCrawl;
        private Date timestampIndex;
        private String url;
        private Map<String, Object> extraFields = new TreeMap<>();

        public CDR31Document build() {

            if (this.url == null)
                throw new IllegalArgumentException("Field 'url' is mandatory");
            if (this.rawContent == null)
                throw new IllegalArgumentException("Field 'raw_content' is mandatory");
            if (this.crawler == null)
                throw new IllegalArgumentException("Field 'crawler' is mandatory");
//            if (this.team == null)
//                throw new IllegalArgumentException("Field 'team' is mandatory");
            if (this.timestampIndex == null)
                throw new IllegalArgumentException("Field 'timestampIndex' is mandatory");

            if (this.contentType == null) {
                MediaType mediaType = extractor.detect(this.rawContent, this.url, this.contentType);
                this.contentType = mediaType.getBaseType().toString();
            }

            if (this.responseHeaders == null) {
                this.responseHeaders = new HashMap<>();
            }

            if (this.objects == null) {
                this.objects = new ArrayList<>();
            }

            if (this._id == null) {
                // auto-generate _id field
                this._id = computeId();
            }

            return new CDR31Document(this);
        }

        public String buildAsJson() throws JsonProcessingException {
            return jsonMapper.writeValueAsString(this.build());
        }

        private String computeId() {
            StringBuilder textForId = new StringBuilder();
            textForId.append(this.url);
            textForId.append("-");
            textForId.append(this.timestampCrawl);
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

        public Builder setTimestampCrawl(Date timestampCrawl) {
            this.timestampCrawl = timestampCrawl;
            return this;
        }

        public Builder setTimestampIndex(Date timestampIndex) {
            this.timestampIndex = timestampIndex;
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

        public Builder setResponseHeaders(Map<String, List<String>> responseHeaders) {
            Map<String, String> headers = new HashMap<>();
            for (Entry<String, List<String>> header : responseHeaders.entrySet()) {
                if (header.getValue() != null) {
                    StringJoiner joiner = new StringJoiner(",");
                    for (String value : header.getValue()) {
                        joiner.add(value);
                    }
                    headers.put(header.getKey(), joiner.toString());
                }
            }
            this.responseHeaders = headers;
            return this;
        }

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setObjects(List<CDR31MediaObject> mediaObjects) {
            this.objects = mediaObjects;
            return this;
        }

        public Builder addExtraField(String fieldName, Object fieldValue) {
            this.extraFields.put(fieldName, fieldValue);
            return this;
        }
    }
}
