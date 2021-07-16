package achecrawler.target;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.repository.elasticsearch.ElasticSearchConfig;
import achecrawler.target.repository.kafka.KafkaConfig;

public class TargetStorageConfig {

    @JsonProperty("target_storage.data_formats")
    private List<String> dataFormats = asList("FILES");

    @JsonProperty("target_storage.target_directory")
    private String targetStorageDirectory = "data_pages";

    // FILESYSTEM_* repositories
    @JsonProperty("target_storage.data_format.filesystem.hash_file_name")
    private boolean hashFileName = false;

    @JsonProperty("target_storage.data_format.filesystem.compress_data")
    private boolean compressData = false;

    // FILES repository
    @JsonProperty("target_storage.data_format.files.max_file_size")
    private long maxFileSize = 256 * 1024 * 1024;

    // WARC repository
    @JsonProperty("target_storage.data_format.warc.max_file_size")
    private long warcMaxFileSize = 250 * 1024 * 1024;

    @JsonProperty("target_storage.data_format.warc.compress")
    private boolean compressWarc = true;

    // ELASTICSEARCH repository
    @JsonUnwrapped
    private ElasticSearchConfig elasticSearchConfig = new ElasticSearchConfig();

    // Other settings
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

    @JsonProperty("target_storage.near_duplicate_detection.enabled")
    private boolean nearDuplicateDetectionEnabled = false;

    @JsonProperty("target_storage.near_duplicate_detection.store")
    private boolean storeNearDuplicates = false;

    @JsonProperty("target_storage.near_duplicate_detection.similarity")
    private double nearDuplicatesSimilarityThreshold = 0.9;

    @JsonUnwrapped
    private KafkaConfig kafkaConfig;

    public TargetStorageConfig() {
    }

    public TargetStorageConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
    }

    public String getTargetStorageDirectory() {
        return targetStorageDirectory;
    }

    public List<String> getDataFormats() {
        return dataFormats;
    }

    /*
     * Setter to keep compatibility with previous crawler versions that accepted only a single data
     * format.
     */
    @JsonSetter("target_storage.data_format.type")
    private void setDataFormat(String dataFormat) {
        this.dataFormats = asList(dataFormat);
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

    public boolean getHashFileName() {
        return hashFileName;
    }

    public boolean getCompressData() {
        return compressData;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public boolean getCompressWarc() {
        return compressWarc;
    }

    public long getWarcMaxFileSize() {
        return warcMaxFileSize;
    }

    public KafkaConfig getKafkaConfig() {
        return kafkaConfig;
    }

    @JsonIgnore
    public boolean isElasticsearchRestEnabled() {
        if (dataFormats.contains("ELASTICSEARCH")) {
            List<String> hosts = elasticSearchConfig.getRestApiHosts();
            if (hosts != null && !hosts.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isNearDuplicateDetectionEnabled() {
        return nearDuplicateDetectionEnabled;
    }

    public boolean ignoreNearDuplicates() {
        return storeNearDuplicates;
    }

    public double getNearDuplicatesSimilarityThreshold() {
        return nearDuplicatesSimilarityThreshold;
    }

}
