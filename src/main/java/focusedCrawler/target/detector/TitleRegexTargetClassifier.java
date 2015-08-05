package focusedCrawler.target.detector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifierException;
import focusedCrawler.util.Page;
import focusedCrawler.util.Target;

/**
 * Classify whether a page is relevant to a topic by matching a RegExp against the title.
 */
public class TitleRegexTargetClassifier implements TargetClassifier {

    private Pattern pattern;

    public TitleRegexTargetClassifier(String regex) {
        regex = ".*" + regex + ".*";
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public boolean detect(Page page) {
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

    @Override
    public boolean classify(Target target) throws TargetClassifierException {
        Page page = (Page) target;
        return regexMatchesTitle(page);
    }

    public double[] distributionForInstance(Target target) throws TargetClassifierException {
        double ontopicPropability;
        double offTopicPropability;

        boolean ontopic = classify(target);
        if (ontopic) {
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
    
}
