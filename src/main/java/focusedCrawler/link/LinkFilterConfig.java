package focusedCrawler.link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class LinkFilterConfig {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final String LINK_FILTERS_FILE = "link_filters.yml";

    static {
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @JsonProperty("global.type")
    private String type = "wildcard";

    @JsonProperty("global.whitelist")
    private List<String> whitelist = Collections.emptyList();

    @JsonProperty("global.blacklist")
    private List<String> blacklist = Collections.emptyList();

    private String fileLocation;

    public LinkFilterConfig() {
    }

    public LinkFilterConfig(String configPath) {
        this(Paths.get(configPath));
    }

    public LinkFilterConfig(Path linkFiltersPath) {
        Path linkFiltersFile;
        if (Files.isDirectory(linkFiltersPath)) {
            linkFiltersFile = linkFiltersPath.resolve(LINK_FILTERS_FILE);
        } else {
            linkFiltersFile = linkFiltersPath;
        }
        try {
            init(yamlMapper.readTree(linkFiltersFile.toFile()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read config from file: " + linkFiltersFile.toString(), e);
        }
    }

    private void init(JsonNode linkFilters) throws IOException {
        yamlMapper.readerForUpdating(this).readValue(linkFilters);
    }

    public String getType() {
        return type;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setLinkFilterType(String type) {
        this.type = type;
    }

    public void setWhiteList(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public void setBlackList(List<String> blacklist) {
        this.blacklist = blacklist;
    }

    public String getFileLocation() {
        if (StringUtils.isNotEmpty(fileLocation)) {
            return fileLocation.trim();
        } else {
            return "";
        }
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
}