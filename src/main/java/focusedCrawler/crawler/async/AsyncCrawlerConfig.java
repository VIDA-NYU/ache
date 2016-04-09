package focusedCrawler.crawler.async;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AsyncCrawlerConfig {
    
    @JsonProperty("crawler_manager.scheduler.host_min_access_interval")
    private int hostMinAccessInterval = 5000;
    
    @JsonProperty("crawler_manager.scheduler.max_links")
    private int maxLinksInScheduler = 10000;
    
    @JsonUnwrapped
    private HttpDownloaderConfig downloaderConfig = new HttpDownloaderConfig();

    public AsyncCrawlerConfig(JsonNode config, ObjectMapper objectMapper) throws JsonProcessingException, IOException {
        objectMapper.readerForUpdating(this).readValue(config);
    }

    public int getHostMinAccessInterval() {
        return hostMinAccessInterval;
    }

    public int getMaxLinksInScheduler() {
        return maxLinksInScheduler;
    }

    public HttpDownloaderConfig getDownloaderConfig() {
        return downloaderConfig;
    }

}