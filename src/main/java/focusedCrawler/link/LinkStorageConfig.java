package focusedCrawler.link;

import focusedCrawler.util.ParameterFile;

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
    private final int frontierRefreshFrequency;
    private final boolean getBacklinks;
    
    private final boolean useOnlineLearning;
    private final String onlineMethod;
    private final int learningLimit;
    private final String targetStorageDirectory;
    private final String linkSelector;
    
    private final BackSurferConfig backSurferConfig;
    private final BiparitieGraphRepConfig biparitieGraphRepConfig;
    
    
    public LinkStorageConfig(ParameterFile params) {
        this.maxPagesPerDomain = params.getParamInt("MAX_PAGES_PER_DOMAIN");
        this.typeOfClassifier = params.getParam("TYPE_OF_CLASSIFIER");
        this.getOutlinks = params.getParamBoolean("GRAB_LINKS");
        this.useScope = params.getParamBoolean("USE_SCOPE");
        this.linkDirectory = params.getParam("LINK_DIRECTORY");
        this.maxCacheUrlsSize = params.getParamInt("MAX_CACHE_URLS_SIZE");
        this.maxSizeLinkQueue = params.getParamInt("MAX_SIZE_LINK_QUEUE");
        this.frontierRefreshFrequency = params.getParamInt("FRONTIER_REFRESH_FREQUENCY");
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
    
    public int getFrontierRefreshFrequency() {
        return frontierRefreshFrequency;
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

    
}
