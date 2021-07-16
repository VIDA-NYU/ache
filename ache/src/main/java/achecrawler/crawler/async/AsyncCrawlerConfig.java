package achecrawler.crawler.async;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AsyncCrawlerConfig {
    
    @JsonUnwrapped
    private HttpDownloaderConfig downloaderConfig = new HttpDownloaderConfig();

    public AsyncCrawlerConfig() {
        // Required for de-serialization
    }

    public AsyncCrawlerConfig(JsonNode config, ObjectMapper objectMapper) throws JsonProcessingException, IOException {
        objectMapper.readerForUpdating(this).readValue(config);
    }

    public HttpDownloaderConfig getDownloaderConfig() {
        return downloaderConfig;
    }

}