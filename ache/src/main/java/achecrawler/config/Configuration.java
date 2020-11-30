package achecrawler.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import achecrawler.crawler.async.AsyncCrawlerConfig;
import achecrawler.link.LinkStorageConfig;
import achecrawler.rest.RestConfig;
import achecrawler.target.TargetStorageConfig;

public class Configuration {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    static {
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @JsonUnwrapped
    private TargetStorageConfig targetStorageConfig;
    @JsonUnwrapped
    private LinkStorageConfig linkStorageConfig;
    @JsonUnwrapped
    private AsyncCrawlerConfig crawlerConfig;
    @JsonUnwrapped
    private RestConfig restConfig;


    public Configuration(String configPath) {
        this(Paths.get(configPath));
    }

    public Configuration(Path configPath) {
        Path configFile;
        if (Files.isDirectory(configPath)) {
            configFile = configPath.resolve("ache.yml");
        } else {
            configFile = configPath;
        }
        try {
            init(yamlMapper.readTree(configFile.toFile()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read config from file: " + configFile.toString(), e);
        }
    }

    public Configuration(Map<?, ?> configMap) {
        try {
            init(yamlMapper.valueToTree(configMap));
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalArgumentException("Could not read settings from map: "+configMap, e);
        }
    }

    public Configuration() {
        this.targetStorageConfig = new TargetStorageConfig();
        this.linkStorageConfig = new LinkStorageConfig();
        this.crawlerConfig = new AsyncCrawlerConfig();
        this.restConfig = new RestConfig();
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

    /**
     * Makes a copy of this configuration object and replaces the configurations present in the
     * parameter in the newly created configuration copy.
     */
    public Configuration copyUpdating(InputStream newSettings) {
        try {
            // Make a copy
            JsonNode copiedTree = yamlMapper.valueToTree(this).deepCopy();
            JsonNode newSettingsTree = yamlMapper.readTree(newSettings);
            mergeJsonNodes(copiedTree, newSettingsTree);
            return yamlMapper.treeToValue(copiedTree, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new configuration.", e);
        }
    }
    
    /**
     * Replaces the values from the source node into the nodeToBeUpdated.
     */
    protected static JsonNode mergeJsonNodes(JsonNode nodeTobeUpdate, JsonNode sourceNode) {

        Iterator<String> fieldNames = sourceNode.fieldNames();

        while (fieldNames.hasNext()) {
            String sourceFieldName = fieldNames.next();
            JsonNode fieldToBeUpdated = nodeTobeUpdate.get(sourceFieldName);
            JsonNode sourceValue = sourceNode.get(sourceFieldName);

            // If the node is an @ArrayNode
            if (fieldToBeUpdated != null && fieldToBeUpdated.isArray() && sourceValue.isArray()) {
                // running a loop for all elements of the updated ArrayNode
                for (int i = 0; i < sourceValue.size(); i++) {
                    JsonNode updatedChildNode = sourceValue.get(i);
                    // Create a new Node in the node that should be updated, if there was no
                    // corresponding node in it
                    // Use-case - where the updateNode will have a new element in its Array
                    if (fieldToBeUpdated.size() <= i) {
                        ((ArrayNode) fieldToBeUpdated).add(updatedChildNode);
                    }
                    // getting reference for the node to be updated
                    JsonNode childNodeToBeUpdated = fieldToBeUpdated.get(i);
                    mergeJsonNodes(childNodeToBeUpdated, updatedChildNode);
                }
            }
            // if the Node is an @ObjectNode
            else if (fieldToBeUpdated != null && fieldToBeUpdated.isObject()) {
                mergeJsonNodes(fieldToBeUpdated, sourceValue);
            }
            // do the replacement
            else {
                if (nodeTobeUpdate instanceof ObjectNode) {
                    ((ObjectNode) nodeTobeUpdate).replace(sourceFieldName, sourceValue);
                }
            }
        }
        return nodeTobeUpdate;
    }

    /**
     * Make a copy of this configuration object.
     */
    public Configuration copy() throws IOException {
        JsonNode treeCopy = yamlMapper.valueToTree(this).deepCopy();
        return yamlMapper.treeToValue(treeCopy,  Configuration.class);
    }

    /**
     * Writes this configuration object to the file path provided as parameter.
     */
    public void writeToFile(Path configPath) {
        try {
            String configString = yamlMapper.writeValueAsString(this);
            Files.write(configPath, configString.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config to file: " + configPath.toString());
        }
    }

}
