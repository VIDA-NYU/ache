package achecrawler.target.classifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.model.Page;

/**
 * Classify whether a page is relevant by matching lists of regular expressions against multiples
 * fields of the web page.
 */
public class RegexTargetClassifier implements TargetClassifier {

    private RegexClassifierConfig params;
    private Pattern[] urlPatterns;
    private Pattern[] titlePatterns;
    private Pattern[] contentPatterns;
    private Pattern[] contentTypePatterns;
    private boolean OR;
    private boolean AND;

    public RegexTargetClassifier(RegexClassifierConfig params) {
        this.params = params;
        this.urlPatterns = compilePatterns(params.url.regexes);
        this.titlePatterns = compilePatterns(params.title.regexes);
        this.contentPatterns = compilePatterns(params.content.regexes);
        this.contentTypePatterns = compilePatterns(params.content_type.regexes);
        this.OR  = "OR".equals(params.boolean_operator);
        this.AND = "AND".equals(params.boolean_operator);
    }

    private Pattern[] compilePatterns(List<String> regexes) {
        if (regexes != null && !regexes.isEmpty()) {
            Pattern[] patterns = new Pattern[regexes.size()];
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = Pattern.compile(regexes.get(i), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            }
            return patterns;
        }
        return null;
    }

    @Override
    public TargetRelevance classify(Page page) throws TargetClassifierException {
        boolean matchesAll = true;
        boolean matchesOne = false;
        if(titlePatterns != null && titlePatterns.length > 0) {
            boolean matches = regexMatchesField(page.getParsedData().getTitle(), params.title.boolean_operator, titlePatterns);
            matchesAll = (matchesAll && matches);
            matchesOne = (matchesOne || matches);
            if( matchesOne && OR)   return TargetRelevance.RELEVANT;
            if(!matchesAll && AND)  return TargetRelevance.IRRELEVANT;
        }
        if(urlPatterns != null && urlPatterns.length > 0) {
            boolean matches = regexMatchesField(page.getURL().toString(), params.url.boolean_operator, urlPatterns);
            matchesAll = (matchesAll && matches);
            matchesOne = (matchesOne || matches);
            if( matchesOne && OR)   return TargetRelevance.RELEVANT;
            if(!matchesAll && AND)  return TargetRelevance.IRRELEVANT;
        }
        if(contentPatterns != null && contentPatterns.length > 0) {
            boolean matches = regexMatchesField(page.getContentAsString(), params.content.boolean_operator, contentPatterns);
            matchesAll = (matchesAll && matches);
            matchesOne = (matchesOne || matches);
            if( matchesOne && OR)   return TargetRelevance.RELEVANT;
            if(!matchesAll && AND)  return TargetRelevance.IRRELEVANT;
        }
        if(contentTypePatterns != null && contentTypePatterns.length > 0) {
            boolean matches = regexMatchesField(page.getContentType(), params.content.boolean_operator, contentTypePatterns);
            matchesAll = (matchesAll && matches);
            matchesOne = (matchesOne || matches);
            if( matchesOne && OR)   return TargetRelevance.RELEVANT;
            if(!matchesAll && AND)  return TargetRelevance.IRRELEVANT;
        }

        if(AND)
            return matchesAll ? TargetRelevance.RELEVANT : TargetRelevance.IRRELEVANT;
        else
            return matchesOne ? TargetRelevance.RELEVANT : TargetRelevance.IRRELEVANT;
    }
    
    public boolean regexMatchesField(String field, String boolOp, Pattern[] patterns) {
        
        if (field == null || field.isEmpty()) {
            return false;
        }
        
        boolean OR = "OR".equals(boolOp);
        boolean AND = !OR;
        boolean matchesAll = true;
        boolean matchesOne = false;
        for(int i = 0; i < patterns.length; i++) {
            boolean matches = patterns[i].matcher(field).matches();
            matchesAll = (matchesAll && matches);
            matchesOne = (matchesOne || matches);
            if( matchesOne && OR)  return true;
            if(!matchesAll && AND) return false;
        }
        
        if(AND)
            return matchesAll;
        else
            return matchesOne;
    }

    public static class RegexList {
        public String boolean_operator = "AND";
        public List<String> regexes = new ArrayList<>();
    }

    public static class RegexClassifierConfig {
        public String boolean_operator = "AND";
        public RegexList url = new RegexList();
        public RegexList title = new RegexList();
        public RegexList content = new RegexList();
        public RegexList content_type = new RegexList();
    }

    public static class Builder {
        
        public TargetClassifier build(Path basePath, ObjectMapper yaml, JsonNode parameters) throws JsonProcessingException {
            RegexClassifierConfig params = yaml.treeToValue(parameters, RegexClassifierConfig.class);
            if (params.url.regexes.isEmpty() && 
                params.title.regexes.isEmpty() && 
                params.content.regexes.isEmpty() &&
                params.content_type.regexes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Failed to configure " + getClass().getSimpleName() +
                        ". At least one regular expression needs to be provided.");
            }
            return new RegexTargetClassifier(params);
        }
        
    }
    
}
