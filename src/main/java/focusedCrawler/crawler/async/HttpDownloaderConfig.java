package focusedCrawler.crawler.async;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpDownloaderConfig {

    public static class Cookie {
        public String cookie = null;
        public String domain = null;
        public String path = "/";
    }

    @JsonProperty("crawler_manager.downloader.use_okhttp3_fetcher")
    private String okHttpFetcher = null;

    @JsonProperty("crawler_manager.downloader.download_thread_pool_size")
    private int downloadThreadPoolSize = 100;

    @JsonProperty("crawler_manager.downloader.connection_pool_size")
    private int connectionPoolSize = 10000;

    @JsonProperty("crawler_manager.downloader.max_retry_count")
    private int maxRetryCount = 2;

    @JsonProperty("crawler_manager.downloader.valid_mime_types")
    private String[] validMimeTypes = null;

    @JsonProperty("crawler_manager.downloader.user_agent.name")
    private String userAgentName = "ACHE";

    @JsonProperty("crawler_manager.downloader.user_agent.url")
    private String userAgentUrl = "https://github.com/ViDA-NYU/ache";

    @JsonProperty("crawler_manager.downloader.user_agent.email")
    private String userAgentEmail = null;

    @JsonProperty("crawler_manager.downloader.user_agent.string")
    private String userAgentString = null;

    @JsonProperty("crawler_manager.downloader.torproxy")
    private String torProxy = null;

    @JsonProperty("crawler_manager.downloader.cookies")
    private List<Cookie> cookies = null;
    
    public HttpDownloaderConfig() {
        // Required for de-serialization
    }

    public HttpDownloaderConfig(String okHttpFetcher){
        if (okHttpFetcher.equals("okHttp")){
            this.okHttpFetcher = "True";
        }
    }

    public List<Cookie> getCookies() {
        return this.cookies;
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

    public String getUserAgentString() {
        return userAgentString;
    }

    public String getUserAgentEmail() {
        return userAgentEmail;
    }

    public String getOkHttpFetcher() {
        return okHttpFetcher;
    }
}
