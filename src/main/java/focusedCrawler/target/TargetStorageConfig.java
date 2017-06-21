package focusedCrawler.target;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.storage.StorageConfig;

public class TargetStorageConfig {
    
    @JsonProperty("target_storage.target_directory")
    private String targetStorageDirectory = "data_pages";
    @JsonProperty("target_storage.data_format.type")
    private String dataFormat = "FILES";
    @JsonProperty("target_storage.data_format.filesystem.hash_file_name")
    private boolean hashFileName = false;
    @JsonProperty("target_storage.data_format.filesystem.compress_data")
    private boolean compressData = false;
    
    @JsonProperty("target_storage.data_format.files.max_file_size")
    private long maxFileSize = 256*1024*1024;
    
    @JsonProperty("target_storage.visited_page_limit")
    private int visitedPageLimit = Integer.MAX_VALUE;
    @JsonProperty("target_storage.hard_focus")
    private boolean hardFocus = true;
    @JsonProperty("target_storage.bipartite")
    private boolean bipartite = false;
    
    @JsonProperty("target_storage.store_negative_pages")
    private boolean saveNegativePages = true;
    
    @JsonProperty("target_storage.english_language_detection_enabled")
    private boolean englishLanguageDetectionEnabled = false;
    
    @JsonUnwrapped
    private ElasticSearchConfig elasticSearchConfig = new ElasticSearchConfig();
    
    private final StorageConfig serverConfig;
    
    public TargetStorageConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
        this.serverConfig = StorageConfig.create(config, "target_storage.server.");
    }

    public String getTargetStorageDirectory() {
        return targetStorageDirectory;
    }

    public String getDataFormat() {
        return dataFormat;
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
    
    public long getMaxFileSize() {
        return maxFileSize;
    }

}