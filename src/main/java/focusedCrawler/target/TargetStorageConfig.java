package focusedCrawler.target;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.StorageConfig;

public class TargetStorageConfig {
    
    public static class MonitorConfig {
        @JsonProperty("target_storage.monitor.sync")
        public boolean sync = true;
        @JsonProperty("target_storage.monitor.frequency")
        public int frequency = 100;
        @JsonProperty("target_storage.monitor.frequency_crawled")
        public int frequencyCrawled = 500;
        @JsonProperty("target_storage.monitor.frequency_relevant")
        public int frequencyRelevant = 500;
        @JsonProperty("target_storage.monitor.frequency_harvest_info")
        public int frequencyHarvestInfo = 100;
    }
    
    @JsonProperty("target_storage.target_directory")
    private String targetStorageDirectory = "data_target";
    @JsonProperty("target_storage.negative_directory")
    private String negativeStorageDirectory = "data_negative";
    
    @JsonProperty("target_storage.data_format.type")
    private String dataFormat = "FILE";
    @JsonProperty("target_storage.data_format.filesystem.hash_file_name")
    private boolean hashFileName = false;
    @JsonProperty("target_storage.data_format.filesystem.compress_data")
    private boolean compressData = false;
    
    @JsonProperty("target_storage.use_classifier")
    private boolean useClassifier = true;
    @JsonProperty("target_storage.relevance_threshold")
    private float relevanceThreshold = 0.9f;
    @JsonProperty("target_storage.visited_page_limit")
    private int visitedPageLimit = 90000000;
    @JsonProperty("target_storage.hard_focus")
    private boolean hardFocus = true;
    @JsonProperty("target_storage.bipartite")
    private boolean bipartite = false;
    
    @JsonProperty("target_storage.store_negative_pages")
    private boolean saveNegativePages = true;
    
    @JsonProperty("target_storage.english_language_detection_enabled")
    private boolean englishLanguageDetectionEnabled = true;
    
    @JsonUnwrapped
    private MonitorConfig monitor = new MonitorConfig();
    
    @JsonUnwrapped
    private ElasticSearchConfig elasticSearchConfig = new ElasticSearchConfig();
    
    private final StorageConfig serverConfig;
    
    @Deprecated
    public TargetStorageConfig(String filename) {
        this(new ParameterFile(filename));
    }

    @Deprecated
    public TargetStorageConfig(ParameterFile params) {
        this.useClassifier = params.getParamBoolean("USE_CLASSIFIER");
        this.targetStorageDirectory = params.getParam("TARGET_STORAGE_DIRECTORY");
        this.negativeStorageDirectory = params.getParam("NEGATIVE_STORAGE_DIRECTORY");
        this.dataFormat = params.getParamOrDefault("DATA_FORMAT", "FILE");
        
        this.monitor = new MonitorConfig();
        this.monitor.sync = params.getParamBoolean("REFRESH_SYNC");
        this.monitor.frequency = params.getParamInt("SYNC_REFRESH_FREQUENCY");
        this.monitor.frequencyCrawled = params.getParamInt("CRAWLED_REFRESH_FREQUENCY");
        this.monitor.frequencyRelevant = params.getParamInt("RELEVANT_REFRESH_FREQUENCY");
        this.monitor.frequencyHarvestInfo = params.getParamInt("HARVESTINFO_REFRESH_FREQUENCY");
        
        this.hashFileName = params.getParamBooleanOrDefault("HASH_FILE_NAME", false);
        this.compressData = params.getParamBooleanOrDefault("COMPRESS_DATA", false);
        this.relevanceThreshold = params.getParamFloat("RELEVANCE_THRESHOLD");
        this.visitedPageLimit = params.getParamInt("VISITED_PAGE_LIMIT");
        this.hardFocus = params.getParamBoolean("HARD_FOCUS");
        this.bipartite = params.getParamBoolean("BIPARTITE");
        this.saveNegativePages = params.getParamBoolean("SAVE_NEGATIVE_PAGES");
        this.englishLanguageDetectionEnabled = params.getParamBooleanOrDefault("ENGLISH_LANGUAGE_DETECTION_ENABLED", true);
        
        String elasticSearchHost = params.getParamOrDefault("ELASTICSEARCH_HOST", "localhost");
        int elasticSearchPort = params.getParamIntOrDefault("ELASTICSEARCH_PORT", 9300);
        String clusterName = params.getParamOrDefault("ELASTICSEARCH_CLUSTERNAME", "elasticsearch");
        this.elasticSearchConfig = new ElasticSearchConfig(elasticSearchHost, elasticSearchPort, clusterName);
        this.serverConfig = new StorageConfig(params);
    }

    public TargetStorageConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
        this.serverConfig = StorageConfig.create(config, "target_storage.server.");
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

    public int getCrawledRefreshFrequency() {
        return monitor.frequencyCrawled;
    }

    public int getRelevantRefreshFrequency() {
        return monitor.frequencyRelevant;
    }

    public int getHarvestInfoRefreshFrequency() {
        return monitor.frequencyHarvestInfo;
    }

    public int getRefreshFreq() {
        return monitor.frequency;
    }

    public boolean isRefreshSync() {
        return monitor.sync;
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

    public boolean isEnglishLanguageDetectionEnabled() {
        return englishLanguageDetectionEnabled;
    }

    public StorageConfig getStorageServerConfig() {
        return serverConfig;
    }

    public boolean getHashFileName() {
        return hashFileName;
    }

    public boolean getCompressData() {
        return compressData;
    }

}