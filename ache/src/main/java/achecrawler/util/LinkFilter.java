package achecrawler.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import achecrawler.link.frontier.LinkRelevance;

public class LinkFilter {

    private static final Logger logger = LoggerFactory.getLogger(LinkFilter.class);

    private TextMatcher whitelist;
    private TextMatcher blacklist;
    private Map<String, TextMatcher> hostsWhitelists;
    private Map<String, TextMatcher> hostsBlacklists;

    private LinkFilter(Builder builder) {
        this.whitelist = builder.whitelist;
        this.blacklist = builder.blacklist;
        this.hostsWhitelists = builder.hostsWhitelists;
        this.hostsBlacklists = builder.hostsBlacklists;
    }

    public boolean accept(String link) {
        try {
            return accept(new URL(link));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invavid URL provided: " + link);
        }
    }

    public boolean accept(LinkRelevance link) {
        return accept(link.getURL());
    }

    public boolean accept(URL link) {

        String url = link.toString();
        String domain = LinkRelevance.getTopLevelDomain(link.getHost());

        TextMatcher hostWhitelist = hostsWhitelists.get(domain);
        if (hostWhitelist != null && !hostWhitelist.matches(url)) {
            return false;
        }
        TextMatcher hostBlacklist = hostsBlacklists.get(domain);
        if (hostBlacklist != null && !hostBlacklist.matches(url)) {
            return false;
        }
        if (whitelist != null && !whitelist.matches(url)) {
            return false;
        }
        if (blacklist != null && !blacklist.matches(url)) {
            return false;
        }

        return true;
    }

    public static class PatternParams {
        public String type = "wildcard";
        public List<String> whitelist = null;
        public List<String> blacklist = null;
    }

    public static class Builder {

        private TextMatcher whitelist;
        private TextMatcher blacklist;
        private Map<String, TextMatcher> hostsWhitelists = new HashMap<>();
        private Map<String, TextMatcher> hostsBlacklists = new HashMap<>();

        public Builder withWhitelistFile(String file) {
            this.whitelist = RegexMatcher.fromWhitelistFile(file);
            return this;
        }

        public Builder withBlacklistFile(String file) {
            this.blacklist = RegexMatcher.fromBlacklistFile(file);
            return this;
        }

        public Builder withWhitelistRegexes(List<String> regexes) {
            this.whitelist = RegexMatcher.fromWhitelist(regexes);
            return this;
        }

        public Builder withBlacklistRegexes(List<String> regexes) {
            this.blacklist = RegexMatcher.fromBlacklist(regexes);
            return this;
        }

        public Builder withConfigPath(String configPath) {
            Path path = Paths.get(configPath);
            Path yamlConfig = path.resolve("link_filters.yml");
            if (Files.exists(yamlConfig) && Files.isRegularFile(yamlConfig)) {
                logger.info("Loading link patterns from link_filters.yml file at {}", configPath);
                fromYamlFile(yamlConfig.toString());
            } else {
                logger.info("Loading link patterns from link_whitelist.txt and"
                        + " link_blacklist.txt at {}", configPath);
                this.whitelist = RegexMatcher.fromWhitelistFile(path.resolve("link_whitelist.txt").toString());
                this.blacklist = RegexMatcher.fromBlacklistFile(path.resolve("link_blacklist.txt").toString());
            }
            return this;
        }

        public Builder fromYamlFile(String file) {
            ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
            try {
                TypeReference<HashMap<String, PatternParams>> typeRef =
                        new TypeReference<HashMap<String, PatternParams>>() {};
                Map<String, PatternParams> params = yaml.readValue(new File(file), typeRef);

                params = normalizeDomain(params);

                for (Entry<String, PatternParams> entry : params.entrySet()) {
                    PatternParams pattern = entry.getValue();
                    if ("global".equals(entry.getKey())) {
                        switch (pattern.type.toLowerCase()) {
                            case "regex":
                                if (pattern.whitelist != null)
                                    this.whitelist = RegexMatcher.fromWhitelist(pattern.whitelist);
                                if (pattern.blacklist != null)
                                    this.blacklist = RegexMatcher.fromBlacklist(pattern.blacklist);
                                break;
                            case "wildcard":
                                if (pattern.whitelist != null)
                                    this.whitelist = WildcardMatcher.fromWhitelist(pattern.whitelist);
                                if (pattern.blacklist != null)
                                    this.blacklist = WildcardMatcher.fromBlacklist(pattern.blacklist);
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Invalid value for global.type: " + pattern.type);
                        }
                    } else {
                        switch (pattern.type.toLowerCase()) {
                            case "regex":
                                if (pattern.whitelist != null)
                                    hostsWhitelists.put(entry.getKey(), RegexMatcher.fromWhitelist(pattern.whitelist));
                                if (pattern.blacklist != null)
                                    hostsBlacklists.put(entry.getKey(), RegexMatcher.fromBlacklist(pattern.blacklist));
                                break;
                            case "wildcard":
                                if (pattern.whitelist != null)
                                    hostsWhitelists.put(entry.getKey(), WildcardMatcher.fromWhitelist(pattern.whitelist));
                                if (pattern.blacklist != null)
                                    hostsBlacklists.put(entry.getKey(), WildcardMatcher.fromBlacklist(pattern.blacklist));
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Invalid value for type: " + pattern.type);
                        }

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read YAML file: " + file.toString(), e);
            }
            return this;
        }

        private Map<String, PatternParams> normalizeDomain(Map<String, PatternParams> params) {
            logger.info("Loading link filter patterns for top-private domains:");
            Map<String, PatternParams> result = new HashMap<>();
            for (Entry<String, PatternParams> p : params.entrySet()) {
                String tpd = LinkRelevance.getTopLevelDomain(p.getKey());
                logger.info(tpd);
                result.put(tpd, p.getValue());
            }
            return result;
        }

        public LinkFilter build() {
            return new LinkFilter(this);
        }

    }

}
