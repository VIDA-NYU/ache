package focusedCrawler.config;

import focusedCrawler.target.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.ParameterFile;

public class TargetStorageConfig {
    
    private final boolean useClassifier;
    private final String targetStorageDirectory;
    private final String negativeStorageDirectory;
    private final String dataFormat;
    private final String targetDomain;
    private final int crawledRefreshFrequency;
    private final int relevantRefreshFrequency;
    private final int harvestInfoRefreshFrequency;
    private final int refreshFreq;
    private final boolean isRefreshSync;
    private final float relevanceThreshold;
    private final int visitedPageLimit;
    private final boolean hardFocus;
    private final boolean bipartite;
    private final boolean saveNegativePages;
    
    private final ElasticSearchConfig elasticSearchConfig;
    
    public TargetStorageConfig(String filename) {
        this(new ParameterFile(filename));
    }

    public TargetStorageConfig(ParameterFile params) {
        this.useClassifier = params.getParamBoolean("USE_CLASSIFIER");
        this.targetStorageDirectory = params.getParam("TARGET_STORAGE_DIRECTORY");
        this.negativeStorageDirectory = params.getParam("NEGATIVE_STORAGE_DIRECTORY");
        this.dataFormat = params.getParamOrDefault("DATA_FORMAT", "FILE");
        this.targetDomain = params.getParam("TARGET_DOMAIN");
        this.crawledRefreshFrequency = params.getParamInt("CRAWLED_REFRESH_FREQUENCY");
        this.relevantRefreshFrequency = params.getParamInt("RELEVANT_REFRESH_FREQUENCY");
        this.harvestInfoRefreshFrequency = params.getParamInt("HARVESTINFO_REFRESH_FREQUENCY");
        this.refreshFreq = params.getParamInt("SYNC_REFRESH_FREQUENCY");
        this.isRefreshSync = params.getParamBoolean("REFRESH_SYNC");
        this.relevanceThreshold = params.getParamFloat("RELEVANCE_THRESHOLD");
        this.visitedPageLimit = params.getParamInt("VISITED_PAGE_LIMIT");
        this.hardFocus = params.getParamBoolean("HARD_FOCUS");
        this.bipartite = params.getParamBoolean("BIPARTITE");
        this.saveNegativePages = params.getParamBoolean("SAVE_NEGATIVE_PAGES");
        
        String elasticSearchHost = params.getParamOrDefault("ELASTICSEARCH_HOST", "localhost");
        int elasticSearchPort = params.getParamIntOrDefault("ELASTICSEARCH_PORT", 9300);
        String clusterName = params.getParamOrDefault("ELASTICSEARCH_CLUSTERNAME", "elasticsearch");
        this.elasticSearchConfig = new ElasticSearchConfig(elasticSearchHost, elasticSearchPort, clusterName);
    }

    public boolean isUseClassifier() {
        return useClassifier;
    }

    public String getTargetStorageDirectory() {
        return targetStorageDirectory;
    }

    public String getNegativeStorageDirectory() {
        return negativeStorageDirectory;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public String getTargetDomain() {
        return targetDomain;
    }

    public int getCrawledRefreshFrequency() {
        return crawledRefreshFrequency;
    }

    public int getRelevantRefreshFrequency() {
        return relevantRefreshFrequency;
    }

    public int getHarvestInfoRefreshFrequency() {
        return harvestInfoRefreshFrequency;
    }

    public int getRefreshFreq() {
        return refreshFreq;
    }

    public boolean isRefreshSync() {
        return isRefreshSync;
    }

    public float getRelevanceThreshold() {
        return relevanceThreshold;
    }

    public int getVisitedPageLimit() {
        return visitedPageLimit;
    }

    public boolean isHardFocus() {
        return hardFocus;
    }

    public boolean isBipartite() {
        return bipartite;
    }

    public boolean isSaveNegativePages() {
        return saveNegativePages;
    }

    public ElasticSearchConfig getElasticSearchConfig() {
        return elasticSearchConfig;
    }
    
}