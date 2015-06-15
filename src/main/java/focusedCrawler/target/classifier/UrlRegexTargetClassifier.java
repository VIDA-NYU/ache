package focusedCrawler.target.classifier;

import java.util.List;

import focusedCrawler.util.LinkFilter.LinkMatcher;
import focusedCrawler.util.Target;

public class UrlRegexTargetClassifier implements TargetClassifier {

    private LinkMatcher matcher;

    public UrlRegexTargetClassifier(LinkMatcher matcher) {
        this.matcher = matcher;
    }
    public UrlRegexTargetClassifier(String regexFilename) {
        this.matcher = LinkMatcher.fromFile(regexFilename);
    }

    @Override
	public boolean classify(Target target) throws TargetClassifierException {
		return matcher.matches(target.getIdentifier());
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
        return new UrlRegexTargetClassifier(new LinkMatcher(regularExpressions));
    }
    
    public static TargetClassifier fromRegularExpressionsFile(String filename) {
        return new UrlRegexTargetClassifier(filename);
    }
  
}
