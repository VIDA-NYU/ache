package achecrawler.crawler.async;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpDownloaderConfig {

    public static class Cookie {
        public String cookie = null;
        public String domain = null;
        public String path = "/";
    }

    public static class TorFetcherConfig {
        @JsonProperty("crawler_manager.downloader.tor.max_retry_count")
        public int maxRetryCount = 3;

        @JsonProperty("crawler_manager.downloader.tor.socket_timeout")
        public int socketTimeout = 5 * 60 * 1000;

        @JsonProperty("crawler_manager.downloader.tor.connection_timeout")
        public int connectionTimeout = 5 * 60 * 1000;

        @JsonProperty("crawler_manager.downloader.tor.connection_request_timeout")
        public int connectionRequestTimeout = 5 * 60 * 1000;
    }

    public static class OkHttpFetcherConfig {
        @JsonProperty("crawler_manager.downloader.okhttp3.proxy_host")
        public String proxyHost = null;

        @JsonProperty("crawler_manager.downloader.okhttp3.proxy_username")
        public String proxyUsername = null;

        @JsonProperty("crawler_manager.downloader.okhttp3.proxy_password")
        public String proxyPassword = null;

        @JsonProperty("crawler_manager.downloader.okhttp3.proxy_port")
        public int proxyPort = 8080;

        @JsonProperty("crawler_manager.downloader.okhttp.connect_timeout")
        public int connectTimeout = 30000;

        @JsonProperty("crawler_manager.downloader.okhttp.read_timeout")
        public int readTimeout = 30000;
    }

    public static class HttpClientFetcherConfig {
        @JsonProperty("crawler_manager.downloader.httpclient.socket_timeout")
        public int socketTimeout = 30000;

        @JsonProperty("crawler_manager.downloader.httpclient.connection_timeout")
        public int connectionTimeout = 30000;

        @JsonProperty("crawler_manager.downloader.httpclient.connection_request_timeout")
        public int connectionRequestTimeout = 60000;
    }

    public static class UserAgentConfig {
        @JsonProperty("crawler_manager.downloader.user_agent.name")
        public String name = "ACHE";

        @JsonProperty("crawler_manager.downloader.user_agent.url")
        public String url = "https://github.com/ViDA-NYU/ache";

        @JsonProperty("crawler_manager.downloader.user_agent.email")
        public String email = null;

        @JsonProperty("crawler_manager.downloader.user_agent.string")
        public String string = null;
    }

    @JsonProperty("crawler_manager.downloader.download_thread_pool_size")
    private int downloadThreadPoolSize = 100;

    @JsonProperty("crawler_manager.downloader.connection_pool_size")
    private int connectionPoolSize = 10000;

    @JsonProperty("crawler_manager.downloader.max_retry_count")
    private int maxRetryCount = 2;

    @JsonProperty("crawler_manager.downloader.valid_mime_types")
    private String[] validMimeTypes = null;

    @JsonProperty("crawler_manager.downloader.torproxy")
    private String torProxy = null;

    @JsonProperty("crawler_manager.downloader.cookies")
    private List<Cookie> cookies = null;

    @JsonProperty("crawler_manager.downloader.use_okhttp3_fetcher")
    private boolean useOkHttpFetcher = false;

    @JsonUnwrapped
    private OkHttpFetcherConfig okHttpFetcherConfig = new OkHttpFetcherConfig();

    @JsonUnwrapped
    private HttpClientFetcherConfig httpClientFetcherConfig = new HttpClientFetcherConfig();

    @JsonUnwrapped
    private TorFetcherConfig torFetcherConfig = new TorFetcherConfig();

    @JsonUnwrapped
    private UserAgentConfig userAgentConfig = new UserAgentConfig();

    public HttpDownloaderConfig() {
        // Required for de-serialization
    }

    public HttpDownloaderConfig(String useOkHttpFetcher){
        if (useOkHttpFetcher.equals("okHttp")){
            this.useOkHttpFetcher = true;
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

    public String[] getValidMimeTypes() {
        return this.validMimeTypes;
    }
    
    public String getTorProxy() {
        return this.torProxy;
    }

    public boolean getUseOkHttpFetcher() {
        return useOkHttpFetcher;
    }

    public OkHttpFetcherConfig getOkHttpFetcherConfig() {
        return okHttpFetcherConfig;
    }

    public TorFetcherConfig getTorFetcherConfig() {
        return torFetcherConfig;
    }

    public HttpClientFetcherConfig getHttpClientFetcherConfig() {
        return httpClientFetcherConfig;
    }

    public UserAgentConfig getUserAgentConfig() {
        return userAgentConfig;
    }
}
