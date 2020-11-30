package achecrawler.link;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.util.persistence.PersistentHashtable;
import achecrawler.util.persistence.PersistentHashtable.DB;

public class LinkStorageConfig {

    public static class BackSurferConfig {

        @JsonProperty("link_storage.backsurfer.moz.access_id")
        private String mozAccessId = null;

        @JsonProperty("link_storage.backsurfer.moz.secret_key")
        private String mozKey = null;

        public BackSurferConfig() {
        }

        public String getMozAccessId() {
            return mozAccessId;
        }

        public String getMozKey() {
            return mozKey;
        }

    }

    @JsonProperty("link_storage.max_pages_per_domain")
    private int maxPagesPerDomain = Integer.MAX_VALUE;

    @JsonProperty("link_storage.link_classifier.type")
    private String typeOfClassifier = "LinkClassifierBreadthSearch";

    @JsonProperty("link_storage.link_strategy.outlinks")
    private boolean getOutlinks = true;

    @JsonProperty("link_storage.link_strategy.use_scope")
    private boolean useScope = false;

    @JsonProperty("link_storage.directory")
    private String linkDirectory = "data_url/dir";

    @JsonProperty("link_storage.max_size_cache_urls")
    private int maxCacheUrlsSize = 200000;

    @JsonProperty("link_storage.link_strategy.backlinks")
    private boolean getBacklinks = false;

    @JsonProperty("link_storage.online_learning.enabled")
    private boolean useOnlineLearning = false;

    @JsonProperty("link_storage.online_learning.async")
    private boolean onlineLearningAsync = true;

    @JsonProperty("link_storage.online_learning.type")
    private String onlineMethod = "FORWARD_CLASSIFIER_BINARY";

    @JsonProperty("link_storage.online_learning.learning_limit")
    private int learningLimit = 500;

    @JsonProperty("link_storage.link_selector")
    private String linkSelector = "TopkLinkSelector";

    @JsonProperty("link_storage.link_selector.min_relevance")
    private double linkSelectorMinRelevance = 0.0d;

    // TODO Remove target storage folder dependency from link storage
    private String targetStorageDirectory = "data_target/";

    @JsonUnwrapped
    private BackSurferConfig backSurferConfig = new BackSurferConfig();

    @JsonProperty("link_storage.download_sitemap_xml")
    private boolean downloadSitemapXml = false;

    @JsonProperty("link_storage.disallow_sites_in_robots_file")
    private boolean disallowSitesInRobotsFile = false;

    @JsonProperty("link_storage.recrawl_selector")
    private String recrawlSelector = null;

    @JsonProperty("link_storage.recrawl_selector.sitemaps.interval")
    private int sitemapsRecrawlInterval = 60;

    @JsonProperty("link_storage.recrawl_selector.relevance.min_relevance")
    private double minRelevanceRecrawl = 299d;

    @JsonProperty("link_storage.recrawl_selector.relevance.interval")
    private int recrawlMinRelevanceInterval = 60;

    @JsonProperty("link_storage.recrawl_selector.relevance.recrawl_sitemaps")
    private boolean recrawlRobots = true;

    @JsonProperty("link_storage.recrawl_selector.relevance.recrawl_robots")
    private boolean recrawlSitemaps =  true;

    @JsonProperty("link_storage.scheduler.host_min_access_interval")
    private int schedulerHostMinAccessInterval = 5000;

    @JsonProperty("link_storage.scheduler.max_links")
    private int schedulerMaxLinks = 100000;

    @JsonProperty("link_storage.persistent_hashtable.backend")
    private PersistentHashtable.DB persistentHashtableBackend = PersistentHashtable.DB.ROCKSDB;

    @JsonProperty("link_storage.link_classifier.max_depth")
    private int maxDepth;

    public LinkStorageConfig() {
    }

    public LinkStorageConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
    }

    public int getMaxPagesPerDomain() {
        return maxPagesPerDomain;
    }

    public String getTypeOfClassifier() {
        return typeOfClassifier;
    }

    @JsonProperty("link_storage.link_strategy.outlinks")
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

    @JsonProperty("link_storage.link_strategy.backlinks")
    public boolean getBacklinks() {
        return getBacklinks;
    }

    public boolean isUseOnlineLearning() {
        return useOnlineLearning;
    }

    public boolean isOnlineLearningAsync() {
        return onlineLearningAsync;
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

    public BackSurferConfig getBackSurferConfig() {
        return backSurferConfig;
    }

    public String getLinkSelector() {
        return linkSelector;
    }

    public double getLinkSelectorMinRelevance() {
        return linkSelectorMinRelevance;
    }

    public String getRecrawlSelector() {
        return recrawlSelector;
    }

    public int getSitemapsRecrawlInterval() {
        return sitemapsRecrawlInterval;
    }

    public double getRecrawlMinRelevance() {
        return minRelevanceRecrawl;
    }

    public int getRecrawlMinRelevanceInterval() {
        return recrawlMinRelevanceInterval;
    }

    public boolean getMinRelevanceRecrawlRobots() {
        return recrawlRobots;
    }

    public boolean getMinRelevanceRecrawlSitemaps() {
        return recrawlSitemaps;
    }

    public boolean getDownloadSitemapXml() {
        return downloadSitemapXml;
    }

    /**
     * Returns true if the user wants the disallowed sites in robots.txt to be skipped
     * 
     * @return
     */
    public boolean getDisallowSitesInRobotsFile() {
        return disallowSitesInRobotsFile;
    }

    public int getSchedulerHostMinAccessInterval() {
        return schedulerHostMinAccessInterval;
    }

    public int getSchedulerMaxLinks() {
        return schedulerMaxLinks;
    }

    /**
     * Returns the value of the persistent hashtable to be used in the backend.
     * The default value is RocksDB.
     * @return
     */
    public DB getPersistentHashtableBackend() {
        return persistentHashtableBackend;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

}
