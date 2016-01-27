package focusedCrawler.link;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.StorageConfig;

public class LinkStorageConfig {

    public static class BackSurferConfig {

        private final String patternIni;
        private final String patternEnd;
        private final String patternIniTitle;
        private final String patternEndTitle;

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
        
        private final String authGraphDirectory;
        private final String urlIdDirectory;
        private final String authIdDirectory;
        private final String hubIdDirectory;
        private final String hubGraphDirectory;
        
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
    
    private final int maxPagesPerDomain;
    private final String typeOfClassifier;
    private final boolean getOutlinks;
    private final boolean useScope;
    private final String linkDirectory;
    private final int maxCacheUrlsSize;
    private final int maxSizeLinkQueue;
    private final boolean getBacklinks;
    
    private final boolean useOnlineLearning;
    private final String onlineMethod;
    private final int learningLimit;
    private final String targetStorageDirectory;
    private final String linkSelector;
    
    private final BackSurferConfig backSurferConfig;
    private final BiparitieGraphRepConfig biparitieGraphRepConfig;
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
        JsonNode linkStorageNode = config.get("link_storage");
        
        JsonNode serverNode = linkStorageNode.get("server");
        if(serverNode != null) {
            this.serverConfig = objectMapper.treeToValue(serverNode, StorageConfig.class);
        } else {
            this.serverConfig = new StorageConfig();
        }
        
        if (linkStorageNode.get("max_pages_per_domain") != null)
            this.maxPagesPerDomain = linkStorageNode.get("max_pages_per_domain").asInt();
        else
            this.maxPagesPerDomain = 100;
        
        
        if (linkStorageNode.get("max_size_link_queue") != null)
            this.maxSizeLinkQueue = linkStorageNode.get("max_size_link_queue").asInt();
        else
            this.maxSizeLinkQueue = 100000;
        
        if (linkStorageNode.get("max_size_cache_urls") != null)
            this.maxCacheUrlsSize = linkStorageNode.get("max_size_cache_urls").asInt();
        else
            this.maxCacheUrlsSize = 100;
        
        if (linkStorageNode.get("link_classifier") != null &&
            linkStorageNode.get("link_classifier").get("type") != null) {
            this.typeOfClassifier = linkStorageNode.get("link_classifier").get("type").asText();
        } else {
            this.typeOfClassifier = "LinkClassifierBaseline";
        }
        
        if (linkStorageNode.get("link_strategy") != null &&
            linkStorageNode.get("link_strategy").get("outlinks") != null)
            this.getOutlinks = linkStorageNode.get("link_strategy").get("outlinks").asBoolean();
        else
            this.getOutlinks = true;
        
        if (linkStorageNode.get("link_strategy") != null &&
            linkStorageNode.get("link_strategy").get("backlinks") != null)
            this.getBacklinks = linkStorageNode.get("link_strategy").get("backlinks").asBoolean();
        else
            this.getBacklinks = false;
        
        if (linkStorageNode.get("link_strategy") != null &&
            linkStorageNode.get("link_strategy").get("use_scope") != null)
            this.useScope = linkStorageNode.get("link_strategy").get("use_scope").asBoolean();
        else
            this.useScope = false;
        
        if (linkStorageNode.get("online_learning") != null &&
            linkStorageNode.get("online_learning").get("enabled") != null)
            this.useOnlineLearning = linkStorageNode.get("online_learning").get("enabled").asBoolean();
        else
            this.useOnlineLearning = false;
        
        if (linkStorageNode.get("online_learning") != null &&
            linkStorageNode.get("online_learning").get("type") != null)
            this.onlineMethod = linkStorageNode.get("online_learning").get("type").asText();
        else
            this.onlineMethod = "FORWARD_CLASSIFIER_BINARY";
        
        if (linkStorageNode.get("online_learning") != null &&
            linkStorageNode.get("online_learning").get("type") != null)
            this.learningLimit = linkStorageNode.get("online_learning").get("learning_limit").asInt();
        else
            this.learningLimit = 500;
        
        
        if (linkStorageNode.get("link_selector") != null)
            this.linkSelector = linkStorageNode.get("link_selector").asText();
        else
            this.linkSelector = "TopkLinkSelector";
        
        
        // FIXME
        this.targetStorageDirectory = "data_target/";
        this.linkDirectory = "data_url/dir";
        this.backSurferConfig = null;
        this.biparitieGraphRepConfig = null;

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
