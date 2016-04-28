package focusedCrawler.target.classifier;

import java.util.List;

import focusedCrawler.target.model.Page;
import focusedCrawler.util.RegexMatcher;

public class BodyRegexTargetClassifier implements TargetClassifier {

    private RegexMatcher matcher;

    public BodyRegexTargetClassifier(String regexFilename) {
        this.matcher = RegexMatcher.fromFile(regexFilename);
    }

    public BodyRegexTargetClassifier(List<String> patterns) {
        this.matcher = RegexMatcher.fromList(patterns);
    }

    @Override
    public TargetRelevance classify(Page page) throws TargetClassifierException {
        if (matcher.matches(page.getContent())) {
            return new TargetRelevance(true, 1.0);
        }
        return new TargetRelevance(false, 0.0);
    }
	
}
