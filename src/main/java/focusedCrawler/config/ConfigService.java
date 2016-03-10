package focusedCrawler.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import focusedCrawler.crawler.CrawlerManagerConfig;
import focusedCrawler.link.LinkStorageConfig;

public class ConfigService {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    static {
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    private TargetStorageConfig targetStorageConfig;
    private LinkStorageConfig linkStorageConfig;
    private CrawlerManagerConfig crawlerManagerConfig;
    
    
    public ConfigService(String configFilePath) {
        this(Paths.get(configFilePath));
    }
    
    public ConfigService(Path configFilePath) {
        try {
            JsonNode config = yamlMapper.readTree(configFilePath.toFile());
            this.targetStorageConfig = new TargetStorageConfig(config, yamlMapper);
            this.linkStorageConfig = new LinkStorageConfig(config, yamlMapper);
            this.crawlerManagerConfig = new CrawlerManagerConfig(config, yamlMapper);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read settings from file: "+configFilePath, e);
        }
    }

    public TargetStorageConfig getTargetStorageConfig() {
        return targetStorageConfig;
    }

    public LinkStorageConfig getLinkStorageConfig() {
        return linkStorageConfig;
    }
    
    public CrawlerManagerConfig getCrawlerManagerConfig() {
        return crawlerManagerConfig;
    }
    
}
