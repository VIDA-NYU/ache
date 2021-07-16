package achecrawler.target.classifier;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.model.Page;
import achecrawler.util.RegexMatcher;

public class BodyRegexTargetClassifier implements TargetClassifier {

    private RegexMatcher matcher;

    public BodyRegexTargetClassifier(String regexFilename) {
        this.matcher = RegexMatcher.fromWhitelistFile(regexFilename);
    }

    public BodyRegexTargetClassifier(List<String> patterns) {
        this.matcher = RegexMatcher.fromWhitelist(patterns);
    }

    @Override
    public TargetRelevance classify(Page page) throws TargetClassifierException {
        if (matcher.matches(page.getContentAsString())) {
            return TargetRelevance.RELEVANT;
        }
        return TargetRelevance.IRRELEVANT;
    }

    public static class BodyRegexClassifierConfig {
        public List<String> regular_expressions;
    }

    public static class Builder {

        public TargetClassifier build(Path basePath, ObjectMapper yaml, JsonNode parameters)
                throws JsonProcessingException {
            BodyRegexClassifierConfig params =
                    yaml.treeToValue(parameters, BodyRegexClassifierConfig.class);
            if (params.regular_expressions != null) {
                return new BodyRegexTargetClassifier(params.regular_expressions);
            } else {
                return null;
            }
        }

    }
    
}
