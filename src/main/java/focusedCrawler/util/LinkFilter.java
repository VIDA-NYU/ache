package focusedCrawler.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import focusedCrawler.link.frontier.LinkRelevance;

public class LinkFilter {

    private static final Logger logger = LoggerFactory.getLogger(LinkFilter.class);

    private LinkWhiteList whitelist;
    private LinkBlackList blacklist;
    private Map<String, LinkWhiteList> hostsWhitelists;
    private Map<String, LinkBlackList> hostsBlacklists;

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

        LinkWhiteList hostWhitelist = hostsWhitelists.get(domain);
        if (hostWhitelist != null && !hostWhitelist.accept(url)) {
            return false;
        }
        LinkBlackList hostBlacklist = hostsBlacklists.get(domain);
        if (hostBlacklist != null && !hostBlacklist.accept(url)) {
            return false;
        }
        if (whitelist != null && !whitelist.accept(url)) {
            return false;
        }
        if (blacklist != null && !blacklist.accept(url)) {
            return false;
        }

        return true;
    }

    public static class LinkWhiteList extends RegexMatcher {
        
        public LinkWhiteList(List<String> urlPatterns) {
            super(urlPatterns);
        }
        
        public LinkWhiteList(String filename) {
            super(filename);
        }
        
        public boolean accept(String link) {
            return patterns == null || patterns.size() == 0 || matches(link);
        }
        
    }
    
    public static class LinkBlackList extends RegexMatcher {
        
        public LinkBlackList(String filename) {
            super(filename);
        }
        
        public LinkBlackList(List<String> urlPatterns) {
            super(urlPatterns);
        }
        
        public boolean accept(String link) {
            return patterns == null || patterns.size() == 0 || !super.matches(link);
        }

    }

    public static class PatternParams {
        public String type = "regex";
        public List<String> whitelist = null;
        public List<String> blacklist = null;
    }

    public static class Builder {

        private LinkWhiteList whitelist;
        private LinkBlackList blacklist;
        private Map<String, LinkWhiteList> hostsWhitelists = new HashMap<>();
        private Map<String, LinkBlackList> hostsBlacklists = new HashMap<>();

        public Builder withWhitelistFile(String file) {
            this.whitelist = new LinkWhiteList(file);
            return this;
        }

        public Builder withBlacklistFile(String file) {
            this.blacklist = new LinkBlackList(file);
            return this;
        }

        public Builder withWhitelistRegexes(List<String> regexes) {
            this.whitelist = new LinkWhiteList(regexes);
            return this;
        }

        public Builder withBlacklistRegexes(List<String> regexes) {
            this.blacklist = new LinkBlackList(regexes);
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
                this.whitelist = new LinkWhiteList(path.resolve("link_whitelist.txt").toString());
                this.blacklist = new LinkBlackList(path.resolve("link_blacklist.txt").toString());
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
                                    this.whitelist = new LinkWhiteList(pattern.whitelist);
                                if (pattern.blacklist != null)
                                    this.blacklist = new LinkBlackList(pattern.blacklist);
                                break;
                            case "wildcard":
                                if (pattern.whitelist != null)
                                    this.whitelist = new LinkWhiteList(
                                            convertWildcardToRegex(pattern.whitelist));
                                if (pattern.blacklist != null)
                                    this.blacklist = new LinkBlackList(
                                            convertWildcardToRegex(pattern.blacklist));
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Invalid value for global.type: " + pattern.type);
                        }
                    } else {
                        switch (pattern.type.toLowerCase()) {
                            case "regex":
                                hostsWhitelists.put(entry.getKey(),
                                        new LinkWhiteList(pattern.whitelist));
                                hostsBlacklists.put(entry.getKey(),
                                        new LinkBlackList(pattern.blacklist));
                                break;
                            case "wildcard":
                                hostsWhitelists.put(entry.getKey(), new LinkWhiteList(
                                        convertWildcardToRegex(pattern.whitelist)));
                                hostsBlacklists.put(entry.getKey(), new LinkBlackList(
                                        convertWildcardToRegex(pattern.blacklist)));
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Invalid value for global.type: " + pattern.type);
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

        private List<String> convertWildcardToRegex(List<String> wildcards) {
            List<String> result = new ArrayList<>();
            if (wildcards == null || wildcards.isEmpty())
                return result;
            for (String wildcard : wildcards) {
                StringBuilder regex = new StringBuilder();
                int lastIndex = 0;
                int index = wildcard.indexOf("*");
                while (index >= 0) {
                    if (index > lastIndex) {
                        regex.append(Pattern.quote(wildcard.substring(lastIndex, index)));
                    }
                    regex.append(".*");
                    lastIndex = index + 1;
                    index = wildcard.indexOf("*", lastIndex);
                }
                if (lastIndex < wildcard.length()) {
                    regex.append(Pattern.quote(wildcard.substring(lastIndex, wildcard.length())));
                }
                result.add(regex.toString());
            }
            return result;
        }

        public LinkFilter build() {
            return new LinkFilter(this);
        }

    }

}
