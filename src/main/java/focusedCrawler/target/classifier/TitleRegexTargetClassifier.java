package focusedCrawler.target.classifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import focusedCrawler.util.Page;
import focusedCrawler.util.Target;

/**
 * Classify whether a page is relevant to a topic by matching a RegExp against the title.
 */
public class TitleRegexTargetClassifier extends BaseTargetClassifier implements TargetClassifier {

    private Pattern pattern;

    public TitleRegexTargetClassifier(String regex) {
        regex = ".*" + regex + ".*";
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean classify(Target target) throws TargetClassifierException {
        Page page = (Page) target;
        return regexMatchesTitle(page);
    }
    
    public boolean regexMatchesTitle(Page page) {
        
        String title = page.getPageURL().titulo();
        
        if (title != null) {
            Matcher matcher = this.pattern.matcher(title);
            if (matcher.matches()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
        
    }
    
}
