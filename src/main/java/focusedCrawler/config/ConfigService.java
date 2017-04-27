package focusedCrawler.config;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import focusedCrawler.crawler.async.AsyncCrawlerConfig;
import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.rest.RestConfig;
import focusedCrawler.target.TargetStorageConfig;

public class ConfigService {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    static {
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    private TargetStorageConfig targetStorageConfig;
    private LinkStorageConfig linkStorageConfig;
    private AsyncCrawlerConfig crawlerConfig;
    private RestConfig restConfig;
    
    public ConfigService(String configFilePath) {
        try {
            init(yamlMapper.readTree(Paths.get(configFilePath).toFile()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read settings from file: "+configFilePath, e);
        }
    }
    
    public ConfigService(Map<String, String> configMap) {
        try {
            init(yamlMapper.valueToTree(configMap));
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalArgumentException("Could not read settings from map: "+configMap, e);
        }
    }
    
    private void init(JsonNode config) throws IOException {
        this.targetStorageConfig = new TargetStorageConfig(config, yamlMapper);
        this.linkStorageConfig = new LinkStorageConfig(config, yamlMapper);
        this.crawlerConfig = new AsyncCrawlerConfig(config, yamlMapper);
        this.restConfig = new RestConfig(config, yamlMapper);
    }

    public TargetStorageConfig getTargetStorageConfig() {
        return targetStorageConfig;
    }

    public LinkStorageConfig getLinkStorageConfig() {
        return linkStorageConfig;
    }
    
    public AsyncCrawlerConfig getCrawlerConfig() {
        return crawlerConfig;
    }
    
    public RestConfig getRestConfig() {
        return restConfig;
    }
    
}
