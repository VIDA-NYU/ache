package focusedCrawler.target.classifier;

import java.util.List;

import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.LinkFilter.LinkBlackList;
import focusedCrawler.util.LinkFilter.LinkWhiteList;
import focusedCrawler.util.Target;

public class UrlRegexTargetClassifier implements TargetClassifier {

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

	public double[] distributionForInstance(Target target) throws TargetClassifierException{
		double ontopicPropability;
		double offTopicPropability;
		
		boolean ontopic = classify(target);
		if(ontopic) {
		    ontopicPropability = 1d;
	        offTopicPropability = 0d;
		} else {
		    ontopicPropability = 0d;
            offTopicPropability = 1d;
		}
		
		double[] result = new double[2];
        result[0] = ontopicPropability;
        result[1] = offTopicPropability;
	    return result;
	}
	
    public static TargetClassifier fromRegularExpressions(List<String> regularExpressions) {
        return new UrlRegexTargetClassifier(regularExpressions);
    }
    
    public static TargetClassifier fromWhitelistFile(String whitelistFilename) {
        LinkFilter linkfilter = new LinkFilter(new LinkWhiteList(whitelistFilename));
        return new UrlRegexTargetClassifier(linkfilter);
    }
    
    public static TargetClassifier fromBlacklistFile(String blacklistFilename) {
        LinkFilter linkfilter = new LinkFilter(new LinkBlackList(blacklistFilename));
        return new UrlRegexTargetClassifier(linkfilter);
    }
    
    public static TargetClassifier fromWhitelistAndBlacklistFiles(String whitelistFilename,
                                                                  String blacklistFilename) {
        LinkFilter linkfilter = new LinkFilter(new LinkWhiteList(whitelistFilename),
                                               new LinkBlackList(blacklistFilename));
        return new UrlRegexTargetClassifier(linkfilter);
    }
  
}
