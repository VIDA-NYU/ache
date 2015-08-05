package focusedCrawler.target.classifier;

import java.util.List;

import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.LinkFilter.LinkBlackList;
import focusedCrawler.util.LinkFilter.LinkWhiteList;
import focusedCrawler.util.Target;

public class UrlRegexTargetClassifier extends BaseTargetClassifier implements TargetClassifier {

    private LinkFilter linkFilter;

    public UrlRegexTargetClassifier(LinkFilter linkfilter) {
        this.linkFilter = linkfilter;
    }
    
    public UrlRegexTargetClassifier(String regexFilename) {
        this.linkFilter = new LinkFilter(new LinkWhiteList(regexFilename));
    }

    public UrlRegexTargetClassifier(List<String> urlPatterns) {
        this.linkFilter = new LinkFilter(urlPatterns);
    }

    @Override
	public boolean classify(Target target) throws TargetClassifierException {
		return linkFilter.accept(target.getIdentifier());
	}
	
    public static UrlRegexTargetClassifier fromRegularExpressions(List<String> regularExpressions) {
        return new UrlRegexTargetClassifier(regularExpressions);
    }
    
    public static UrlRegexTargetClassifier fromWhitelistFile(String whitelistFilename) {
        LinkFilter linkfilter = new LinkFilter(new LinkWhiteList(whitelistFilename));
        return new UrlRegexTargetClassifier(linkfilter);
    }
    
    public static UrlRegexTargetClassifier fromBlacklistFile(String blacklistFilename) {
        LinkFilter linkfilter = new LinkFilter(new LinkBlackList(blacklistFilename));
        return new UrlRegexTargetClassifier(linkfilter);
    }
    
    public static UrlRegexTargetClassifier fromWhitelistAndBlacklistFiles(String whitelistFilename,
                                                                  String blacklistFilename) {
        LinkFilter linkfilter = new LinkFilter(new LinkWhiteList(whitelistFilename),
                                               new LinkBlackList(blacklistFilename));
        return new UrlRegexTargetClassifier(linkfilter);
    }
  
}
