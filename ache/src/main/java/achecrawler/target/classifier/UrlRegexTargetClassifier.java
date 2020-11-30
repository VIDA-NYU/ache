package achecrawler.target.classifier;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.model.Page;
import achecrawler.util.LinkFilter;

public class UrlRegexTargetClassifier implements TargetClassifier {

    
    private LinkFilter linkFilter;

    public UrlRegexTargetClassifier(LinkFilter linkfilter) {
        this.linkFilter = linkfilter;
    }
    
    @Override
	public TargetRelevance classify(Page page) throws TargetClassifierException {
		if(linkFilter.accept(page.getURL().toString())) {
		    return new TargetRelevance(true, 1.0);
		} else {
		    return new TargetRelevance(false, 0.0);
		}
	}
	
    public static UrlRegexTargetClassifier fromRegularExpressions(List<String> regexes) {
        LinkFilter linkFilter = new LinkFilter.Builder()
                .withWhitelistRegexes(regexes)
                .build();
        return new UrlRegexTargetClassifier(linkFilter);
    }
    
    public static UrlRegexTargetClassifier fromWhitelistFile(String whitelistFilename) {
        LinkFilter linkfilter = new LinkFilter.Builder()
                .withWhitelistFile(whitelistFilename)
                .build();
        return new UrlRegexTargetClassifier(linkfilter);
    }
    
    public static UrlRegexTargetClassifier fromBlacklistFile(String blacklistFilename) {
        LinkFilter linkfilter = new LinkFilter.Builder()
                .withBlacklistFile(blacklistFilename)
                .build();
        return new UrlRegexTargetClassifier(linkfilter);
    }
    
    public static UrlRegexTargetClassifier fromWhitelistAndBlacklistFiles(String whitelistFilename,
            String blacklistFilename) {
        LinkFilter linkfilter = new LinkFilter.Builder()
                .withWhitelistFile(whitelistFilename)
                .withBlacklistFile(blacklistFilename)
                .build();
        return new UrlRegexTargetClassifier(linkfilter);
    }
    

    static class UrlRegexClassifierConfig {
        public List<String> regular_expressions;
        public String whitelist_file;
        public String blacklist_file;
    }
    
    public static class Builder {
        
        public TargetClassifier build(Path basePath, ObjectMapper yaml, JsonNode parameters) throws JsonProcessingException {
            
            UrlRegexClassifierConfig params = yaml.treeToValue(parameters,
                                                               UrlRegexClassifierConfig.class);
            TargetClassifier classifier = null;
            
            if(params.regular_expressions != null && params.regular_expressions.size() > 0) {
                classifier = UrlRegexTargetClassifier.fromRegularExpressions(params.regular_expressions);
            }
            
            if(params.whitelist_file != null && params.blacklist_file != null) {
                params.whitelist_file = basePath.resolve(params.whitelist_file).toString();
                params.blacklist_file = basePath.resolve(params.blacklist_file).toString();
                classifier = UrlRegexTargetClassifier.fromWhitelistAndBlacklistFiles(
                    params.whitelist_file,
                    params.blacklist_file
                );
            }
            
            if(params.whitelist_file != null && params.blacklist_file == null) {
                params.whitelist_file = basePath.resolve(params.whitelist_file).toString();
                classifier = UrlRegexTargetClassifier.fromWhitelistFile(params.whitelist_file);
            }
            
            if(params.whitelist_file == null && params.blacklist_file != null) {
                params.blacklist_file = basePath.resolve(params.blacklist_file).toString();
                classifier = UrlRegexTargetClassifier.fromBlacklistFile(params.blacklist_file);
            }
            
            return classifier;
        }
        
    }
  
}
