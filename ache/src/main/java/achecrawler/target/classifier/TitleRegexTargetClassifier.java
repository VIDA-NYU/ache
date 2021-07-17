package achecrawler.target.classifier;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.model.Page;

/**
 * Classify whether a page is relevant to a topic by matching a RegExp against the title.
 */
public class TitleRegexTargetClassifier implements TargetClassifier {

    private Pattern pattern;

    public TitleRegexTargetClassifier(String regex) {
        regex = ".*" + regex + ".*";
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public TargetRelevance classify(Page page) throws TargetClassifierException {
        if(regexMatchesTitle(page)) {
            return TargetRelevance.RELEVANT;
        } else {
            return TargetRelevance.IRRELEVANT;
        }
    }
    
    public boolean regexMatchesTitle(Page page) {
        
        String title = page.getParsedData().getTitle();
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
    
    static class TitleRegexClassifierConfig {
        public String regular_expression;
    }
    
    public static class Builder {

        public TargetClassifier build(Path basePath, ObjectMapper yaml, JsonNode parameters) throws JsonProcessingException {
            TitleRegexClassifierConfig params = yaml.treeToValue(parameters, TitleRegexClassifierConfig.class);
            if (params.regular_expression != null && !params.regular_expression.trim().isEmpty()) {
                return new TitleRegexTargetClassifier(params.regular_expression.trim());
            } else {
                return null;
            }
        }

    }
    
}
