package focusedCrawler.config;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.target.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.StorageConfig;

public class TargetStorageConfig {
    
    public static class MonitorConfig {
        public boolean sync = true;
        public int frequency = 100;
        @JsonProperty("frequency_crawled")
        public int frequencyCrawled = 500;
        @JsonProperty("frequency_relevant")
        public int frequencyRelevant = 500;
        @JsonProperty("frequency_harvest_info")
        public int frequencyHarvestInfo = 100;
    }
    
    private boolean useClassifier = true;
    private String targetStorageDirectory = "data_target";
    private String negativeStorageDirectory = "data_negative";
    private String dataFormat = "FILE";
    
    private float relevanceThreshold = 0.9f;
    private int visitedPageLimit = 90000000;
    private boolean hardFocus = true;
    private boolean bipartite = false;
    private boolean saveNegativePages = true;
    private boolean englishLanguageDetectionEnabled = true;
    private boolean hashFileName = false;
    private boolean compressData = false;
    
    
    private MonitorConfig monitor = new MonitorConfig();
    
    private ElasticSearchConfig elasticSearchConfig = new ElasticSearchConfig();
    private StorageConfig serverConfig = new StorageConfig();
    
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
        JsonNode targetStorageNode = config.get("target_storage");
        
        if(targetStorageNode.get("use_classifier") != null)
            this.useClassifier = targetStorageNode.get("use_classifier").asBoolean(true);
            
        if(targetStorageNode.get("target_directory") != null)
            this.targetStorageDirectory = targetStorageNode.get("target_directory").asText("data_target");
            
        if(targetStorageNode.get("negative_directory") != null)
            this.negativeStorageDirectory = targetStorageNode.get("negative_directory").asText("data_negative");
        
        JsonNode dataFormatNode = targetStorageNode.get("data_format");
        if(dataFormatNode != null) {
            this.dataFormat = dataFormatNode.get("type").asText();
            if(this.dataFormat.equalsIgnoreCase("elasticsearch")) {
                JsonNode elasticsearchNode = dataFormatNode.get("parameters");
                if(elasticsearchNode != null) {
                    this.elasticSearchConfig = objectMapper.treeToValue(elasticsearchNode, ElasticSearchConfig.class);
                }
            }
            if(this.dataFormat.equalsIgnoreCase("filesystem")) {
                if(targetStorageNode.get("hash_filename") != null)
                    this.hashFileName = targetStorageNode.get("hash_filename").asBoolean(false);
                
                if(targetStorageNode.get("compress_data") != null)
                    this.compressData = targetStorageNode.get("compress_data").asBoolean(false);
            }
        }
        
        
        JsonNode monitorNode = targetStorageNode.get("monitor");
        if(monitorNode != null) {
            this.monitor = objectMapper.treeToValue(monitorNode, MonitorConfig.class);
        }
        
        if(targetStorageNode.get("relevance_threshold") != null)
            this.relevanceThreshold = (float) targetStorageNode.get("relevance_threshold").asDouble(0.5d);
        
        if(targetStorageNode.get("visited_page_limit") != null)
            this.visitedPageLimit = targetStorageNode.get("visited_page_limit").asInt(90000000);
        
        if(targetStorageNode.get("hard_focus") != null)
            this.hardFocus = targetStorageNode.get("hard_focus").asBoolean(true);
        
        if(targetStorageNode.get("bipartite") != null)
            this.bipartite = targetStorageNode.get("bipartite").asBoolean();
        
        if(targetStorageNode.get("store_negative_pages") != null)
            this.saveNegativePages = targetStorageNode.get("store_negative_pages").asBoolean(true);
        
        if(targetStorageNode.get("english_language_detection_enabled") != null)
            this.englishLanguageDetectionEnabled = targetStorageNode.get("english_language_detection_enabled").asBoolean(true);

        JsonNode serverNode = targetStorageNode.get("server");
        if(serverNode != null) {
            this.serverConfig = objectMapper.treeToValue(serverNode, StorageConfig.class);
        }
        
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