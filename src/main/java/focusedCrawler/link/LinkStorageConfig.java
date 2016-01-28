package focusedCrawler.link;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.util.ParameterFile;
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

        public BackSurferConfig() { }
        
        public BackSurferConfig(ParameterFile params) {
            patternIni = params.getParam("PATTERN_INI");
            patternEnd = params.getParam("PATTERN_END");
            patternIniTitle = params.getParam("PATTERN_INI_TITLE");
            patternEndTitle = params.getParam("PATTERN_END_TITLE");
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
        
        public BiparitieGraphRepConfig(ParameterFile params) {
            this.authGraphDirectory = params.getParam("AUTH_GRAPH_DIRECTORY");
            this.urlIdDirectory = params.getParam("URL_ID_DIRECTORY");
            this.authIdDirectory = params.getParam("AUTH_ID_DIRECTORY");
            this.hubIdDirectory = params.getParam("HUB_ID_DIRECTORY");
            this.hubGraphDirectory = params.getParam("HUB_GRAPH_DIRECTORY");
        }
        
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
    
    
    public LinkStorageConfig(ParameterFile params) {
        this.maxPagesPerDomain = params.getParamInt("MAX_PAGES_PER_DOMAIN");
        this.typeOfClassifier = params.getParam("TYPE_OF_CLASSIFIER");
        this.getOutlinks = params.getParamBoolean("GRAB_LINKS");
        this.useScope = params.getParamBoolean("USE_SCOPE");
        this.linkDirectory = params.getParam("LINK_DIRECTORY");
        this.maxCacheUrlsSize = params.getParamInt("MAX_CACHE_URLS_SIZE");
        this.maxSizeLinkQueue = params.getParamInt("MAX_SIZE_LINK_QUEUE");
        this.getBacklinks = params.getParamBoolean("SAVE_BACKLINKS");
        if(getBacklinks) {
            String backlinkConfig = params.getParam("BACKLINK_CONFIG");
            backSurferConfig = new BackSurferConfig(new ParameterFile(backlinkConfig));
        } else {
            backSurferConfig = null;
        }
        
        this.useOnlineLearning = params.getParamBoolean("ONLINE_LEARNING");
        this.onlineMethod = params.getParam("ONLINE_METHOD");
        this.learningLimit = params.getParamInt("LEARNING_LIMIT");
        this.targetStorageDirectory = params.getParam("TARGET_STORAGE_DIRECTORY");
        this.linkSelector = params.getParam("LINK_SELECTOR");
        
        this.biparitieGraphRepConfig = new BiparitieGraphRepConfig(params);
        this.serverConfig = new StorageConfig(params);
    }
    
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
