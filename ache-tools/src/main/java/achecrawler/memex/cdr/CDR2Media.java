package achecrawler.memex.cdr;

import java.io.Serializable;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@JsonInclude(Include.NON_NULL)
public class CDR2Media implements Serializable {

    @JsonProperty("_id")
    final private String id;
    
    @JsonProperty("content_type")
    final private String contentType;

    final private String crawler;
    
    final private String team;
    
    final private long timestamp;
    
    final private String version;
    
    @JsonProperty("obj_original_url")
    final private String objOriginalUrl;
    
    @JsonProperty("obj_parent")
    final private String objParent;
    
    @JsonProperty("obj_stored_url")
    final private String objStoredUrl;

    /**
     * Use {@link CDR2Media.Builder} to construct a CDRMedia object instead.
     */
    protected CDR2Media(Builder builder) {
        this.id = builder.id;
        this.contentType = builder.contentType;
        this.crawler = builder.crawler;
        this.team = builder.team;
        this.timestamp = builder.timestamp;
        this.version = builder.version;
        this.objOriginalUrl = builder.objOriginalUrl;
        this.objParent = builder.objParent;
        this.objStoredUrl = builder.objStoredUrl;
    }
    
    public String getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCrawler() {
        return crawler;
    }

    public String getTeam() {
        return team;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getVersion() {
        return version;
    }

    public String getObjOriginalUrl() {
        return objOriginalUrl;
    }

    public String getObjParent() {
        return objParent;
    }

    public String getObjStoredUrl() {
        return objStoredUrl;
    }
    
    public static class Builder {
        
        public String id;
        public String contentType;
        public String crawler;
        public String team;
        public long timestamp = -1;
        public String version;
        public String objOriginalUrl;
        public String objParent;
        public String objStoredUrl;
        
        public CDR2Media build() {
            if(this.contentType == null) throw new IllegalStateException("content_type can not be null");
            if(this.timestamp == -1) throw new IllegalStateException("timestamp needs to be set");
            if(this.crawler == null) throw new IllegalStateException("crawler can not be null");
            if(this.objParent == null) throw new IllegalStateException("obj_parent can not be null");
            if(this.objOriginalUrl == null) throw new IllegalStateException("obj_original_url can not be null");
            if(this.objStoredUrl == null) throw new IllegalStateException("obj_stored_url can not be null");
            if(this.team == null) throw new IllegalStateException("team can not be null");
            if(this.version == null) throw new IllegalStateException("version can not be null");
            if(this.id == null) this.id = generateId();
            return new CDR2Media(this);
        }
        
        public String generateId() {
            return generateId(this.objOriginalUrl, this.objParent, this.timestamp);
        }
        
        public static String generateId(String url, String parentId, long timestamp) {
            StringBuilder textForId = new StringBuilder();
            textForId.append(url);
            textForId.append(parentId);
            textForId.append(timestamp);
            return DigestUtils.sha1Hex(textForId.toString()).toUpperCase();
        }
        
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
        
        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
        
        public Builder setCrawler(String crawler) {
            this.crawler = crawler;
            return this;
        }
        
        public Builder setTeam(String team) {
            this.team = team;
            return this;
        }
        
        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }
        
        public Builder setObjOriginalUrl(String objOriginalUrl) {
            this.objOriginalUrl = objOriginalUrl;
            return this;
        }
        
        public Builder setObjParent(String objParent) {
            this.objParent = objParent;
            return this;
        }
        
        public Builder setObjStoredUrl(String objStoredUrl) {
            this.objStoredUrl = objStoredUrl;
            return this;
        }
        
    }

}
