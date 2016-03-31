package focusedCrawler.link;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.util.storage.StorageConfig;

public class LinkStorageConfig {

    public static class BackSurferConfig {
        
        @JsonProperty("link_storage.backsurfer.pattern_ini")
        private String patternIni = ",\"uu\":";
        
        @JsonProperty("link_storage.backsurfer.pattern_end")
        private String patternEnd = "\"}";
        
        @JsonProperty("link_storage.backsurfer.pattern_ini_title")
        private String patternIniTitle = ",\"ut\":\"";
        
        @JsonProperty("link_storage.backsurfer.pattern_end_title")
        private String patternEndTitle = "\",\"uu\":";

        @JsonProperty("link_storage.backsurfer.moz.access_id")
        private String mozAccessId = "";
        
        @JsonProperty("link_storage.backsurfer.moz.secret_key")
        private String mozKey = "";
        
        public BackSurferConfig() { }
        
        public String getMozAccessId() {
            return mozAccessId;
        }
        
        public String getMozKey() {
            return mozKey;
        }

        public String getPatternIni() {
            return patternIni;
        }

        public String getPatternEnd() {
            return patternEnd;
        }
        
        public String getPatternIniTitle() {
            return patternIniTitle;
        }

        public String getPatternEndTitle() {
            return patternEndTitle;
        }
        
    }
    
    public static class BiparitieGraphRepConfig {
        
        private String authGraphDirectory = "data_backlinks/auth_graph";
        private String urlIdDirectory = "data_backlinks/url";
        private String authIdDirectory = "data_backlinks/auth_id";
        private String hubIdDirectory = "data_backlinks/hub_id";
        private String hubGraphDirectory = "data_backlinks/hub_graph";
        
        public BiparitieGraphRepConfig() { }
        
        public String getAuthGraphDirectory() {
            return authGraphDirectory;
        }
        
        public String getUrlIdDirectory() {
            return urlIdDirectory;
        }
        
        public String getAuthIdDirectory() {
            return authIdDirectory;
        }
        
        public String getHubIdDirectory() {
            return hubIdDirectory;
        }
        
        public String getHubGraphDirectory() {
            return hubGraphDirectory;
        }
        
    }
    
    @JsonProperty("link_storage.max_pages_per_domain")
    private int maxPagesPerDomain = 100;
    
    @JsonProperty("link_storage.link_classifier.type")
    private String typeOfClassifier = "LinkClassifierBaseline";
    
    
    @JsonProperty("link_storage.link_strategy.outlinks")
    private boolean getOutlinks = true;
    
    @JsonProperty("link_storage.link_strategy.use_scope")
    private boolean useScope = false;
    
    
    @JsonProperty("link_storage.directory")
    private String linkDirectory = "data_url/dir";
    
    @JsonProperty("link_storage.max_size_cache_urls")
    private int maxCacheUrlsSize = 200000;
    
    @JsonProperty("link_storage.max_size_link_queue")
    private int maxSizeLinkQueue = 100000;
    
    @JsonProperty("link_storage.link_strategy.backlinks")
    private boolean getBacklinks = false;
    
    
    @JsonProperty("link_storage.online_learning.enabled")
    private boolean useOnlineLearning = false;
    
    @JsonProperty("link_storage.online_learning.type")
    private String onlineMethod = "FORWARD_CLASSIFIER_BINARY";
    
    @JsonProperty("link_storage.online_learning.learning_limit")
    private int learningLimit = 500;
    
    
    @JsonProperty("link_storage.link_selector")
    private String linkSelector = "TopkLinkSelector";
    
    // TODO Remove target storage folder dependency from link storage
    private String targetStorageDirectory = "data_target/";
    
    private BackSurferConfig backSurferConfig = new BackSurferConfig();
    private BiparitieGraphRepConfig biparitieGraphRepConfig = new BiparitieGraphRepConfig();
    
    private final StorageConfig serverConfig;
    
    public LinkStorageConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
        this.serverConfig = StorageConfig.create(config, "link_storage.server.");
    }

    public int getMaxPagesPerDomain() {
        return maxPagesPerDomain;
    }
    
    public String getTypeOfClassifier() {
        return typeOfClassifier;
    }
    
    public boolean getOutlinks() {
        return getOutlinks;
    }
    
    public boolean isUseScope() {
        return useScope;
    }
    
    public String getLinkDirectory() {
        return linkDirectory;
    }
    
    public int getMaxCacheUrlsSize() {
        return maxCacheUrlsSize;
    }
    
    public int getMaxSizeLinkQueue() {
        return maxSizeLinkQueue;
    }
    
    public boolean getBacklinks() {
        return getBacklinks;
    }
    
    public boolean isUseOnlineLearning() {
        return useOnlineLearning;
    }
    
    public String getOnlineMethod() {
        return onlineMethod;
    }
    
    public int getLearningLimit() {
        return learningLimit;
    }
    
    public String getTargetStorageDirectory() {
        return targetStorageDirectory;
    }
    
    public BiparitieGraphRepConfig getBiparitieGraphRepConfig() {
        return biparitieGraphRepConfig;
    }
    
    public BackSurferConfig getBackSurferConfig() {
        return backSurferConfig;
    }

    public String getLinkSelector() {
        return linkSelector;
    }

    public StorageConfig getStorageServerConfig() {
        return serverConfig;
    }

}
