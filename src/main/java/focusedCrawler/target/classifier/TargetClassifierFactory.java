package focusedCrawler.target.classifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class TargetClassifierFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(TargetClassifierFactory.class);

    static class WekaClassifierConfig {
        public String features_file = "pageclassifier.features";
        public String model_file = "pageclassifier.features";
        public String stopwords_file = "stoplist.txt";
        public double relevanceThreshold = 0.9;
    }
    
    static class UrlRegexClassifierConfig {
        public List<String> regular_expressions;
        public String whitelist_file;
        public String blacklist_file;
    }
    
    static class TitleRegexClassifierConfig {
        public String regular_expression;
    }
    
    static class BodyRegexClassifierConfig {
        public List<String> regular_expressions;
    }

    public static TargetClassifier create(String modelPath) throws IOException {
        return create(modelPath, 0.0, null);
    }
    
    public static TargetClassifier create(String modelPath,
                                          double relevanceThreshold,
                                          String stoplist) throws IOException {
        
        logger.info("Loading TargetClassifier...");
        
        Path basePath = Paths.get(modelPath);
        Path configPath = Paths.get(modelPath, "/pageclassifier.yml");
        File configFile = Paths.get(modelPath, "pageclassifier.yml").toFile();
        
        if(configFile.exists() && configFile.canRead()) {
        
            ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
            
            JsonNode tree = yaml.readTree(configFile);
            String classifierType = tree.get("type").asText();
            JsonNode parameters = tree.get("parameters");
            
            logger.info("TARGET_CLASSIFIER: "+classifierType);
            
            TargetClassifier classifier = null;
            
            if("url_regex".equals(classifierType)) {
                classifier = createUrlRegexClassifier(basePath, yaml, parameters);
            }
            
            if("title_regex".equals(classifierType)) {
                classifier = createTitleRegexClassifier(yaml, parameters);
            }
            
            if("body_regex".equals(classifierType)) {
                classifier = createBodyRegexClassifier(yaml, parameters);
            }
            
            if("keep_link_relevance".equals(classifierType)) {
                classifier = createKeepLinkRelevanceClassifier(basePath, yaml, parameters);
            }
            
            if("weka".equals(classifierType)) {
                classifier = createWekaClassifier(basePath, yaml, parameters);
            }
            
            if(classifier != null) {
                return classifier;
            } else {
                String errorMsg = "Could not instantiate classifier using config: " + configPath;
                throw new IllegalArgumentException(errorMsg);
            }
        }
        
        // create classic weka classifer to maintain compatibility with older versions
        return WekaTargetClassifier.create(modelPath, relevanceThreshold, stoplist);
    }

    private static TargetClassifier createKeepLinkRelevanceClassifier(Path basePath,
            ObjectMapper yaml, JsonNode parameters) throws JsonProcessingException, IOException {
        if(parameters != null) {
            TargetClassifier wekaClassifier = createWekaClassifier(basePath, yaml, parameters);
            return new KeepLinkRelevanceTargetClassifier(wekaClassifier);
        } else {
            return new KeepLinkRelevanceTargetClassifier(null);
        }
    }

    private static TargetClassifier createUrlRegexClassifier(Path basePath, ObjectMapper yaml,
            JsonNode parameters) throws JsonProcessingException {
        
        UrlRegexClassifierConfig params = yaml.treeToValue(parameters,
                                                           UrlRegexClassifierConfig.class);
        TargetClassifier classifier = null;
        
        if(params.regular_expressions != null && !params.regular_expressions.isEmpty()) {
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

    private static TargetClassifier createTitleRegexClassifier(ObjectMapper yaml,
                                                               JsonNode parameters)
                                                               throws JsonProcessingException {
        TitleRegexClassifierConfig params = yaml.treeToValue(parameters, TitleRegexClassifierConfig.class);
        
        if(params.regular_expression != null && !params.regular_expression.trim().isEmpty()) {
            return new TitleRegexTargetClassifier(params.regular_expression.trim());
        } else {
            return null;
        }
    }
    
    private static TargetClassifier createBodyRegexClassifier(ObjectMapper yaml,
            JsonNode parameters) throws JsonProcessingException {
        BodyRegexClassifierConfig params =
                yaml.treeToValue(parameters, BodyRegexClassifierConfig.class);
        if (params.regular_expressions != null) {
            return new BodyRegexTargetClassifier(params.regular_expressions);
        } else {
            return null;
        }
    }

    private static TargetClassifier createWekaClassifier(Path basePath,
                                                         ObjectMapper yaml,
                                                         JsonNode parameters)
                                                         throws JsonProcessingException,
                                                                IOException {
        
        WekaClassifierConfig params = yaml.treeToValue(parameters, WekaClassifierConfig.class);
        params.model_file = basePath.resolve(params.model_file).toFile().getAbsolutePath();
        params.features_file = basePath.resolve(params.features_file).toFile().getAbsolutePath();
        params.stopwords_file = basePath.resolve(params.stopwords_file).toFile().getAbsolutePath();
        
        return WekaTargetClassifier.create(params.model_file,
                                           params.features_file,
                                           params.relevanceThreshold,
                                           params.stopwords_file);
    }

}
