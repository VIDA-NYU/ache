package focusedCrawler.crawler.async;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpDownloaderConfig {

    @JsonProperty("crawler_manager.downloader.download_thread_pool_size")
    private int downloadThreadPoolSize = 100;
    
    @JsonProperty("crawler_manager.downloader.connection_pool_size")
    private int connectionPoolSize = 1000;

    @JsonProperty("crawler_manager.downloader.max_retry_count")
    private int maxRetryCount = 2;

    @JsonProperty("crawler_manager.downloader.valid_mime_types")
    private String[] validMimeTypes = null;

    @JsonProperty("crawler_manager.downloader.user_agent.name")
    private String userAgentName = "ACHE";

    @JsonProperty("crawler_manager.downloader.user_agent.url")
    private String userAgentUrl = "https://github.com/ViDA-NYU/ache";

    @JsonProperty("crawler_manager.downloader.torproxy")
    private String torProxy = null;
    
    public HttpDownloaderConfig() {
    }

    public HttpDownloaderConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
    }

    public int getDownloadThreadPoolSize() {
        return this.downloadThreadPoolSize;
    }
    
    public int getConnectionPoolSize() {
        return this.connectionPoolSize;
    }

    public int getMaxRetryCount() {
        return this.maxRetryCount;
    }

    public String getUserAgentName() {
        return this.userAgentName;
    }

    public String getUserAgentUrl() {
        return this.userAgentUrl;
    }

    public String[] getValidMimeTypes() {
        return this.validMimeTypes;
    }
    
    public String getTorProxy() {
        return this.torProxy;
    }

}