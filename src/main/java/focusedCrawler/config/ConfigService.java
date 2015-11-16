package focusedCrawler.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import focusedCrawler.link.LinkStorageConfig;

public class ConfigService {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private JsonNode config;
    
    public ConfigService(String configFile) throws IOException {
        this.config = yamlMapper.readTree(new File(configFile));
    }

    public TargetStorageConfig getTargetStorageConfig() throws IOException {
        return new TargetStorageConfig(this.config, yamlMapper);
    }

    public LinkStorageConfig getLinkStorageConfig() throws IOException {
        return new LinkStorageConfig(this.config, yamlMapper);
    }

}
