package focusedCrawler.config;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.target.elasticsearch.ElasticSearchConfig;
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