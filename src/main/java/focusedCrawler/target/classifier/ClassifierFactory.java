package focusedCrawler.target.classifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import focusedCrawler.target.detector.TitleRegexTargetClassifier;

public class ClassifierFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassifierFactory.class);

    static class WekaClassifierConfig {
        public String features_file = "pageclassifier.features";
        public String model_file = "pageclassifier.features";
        public String stopwords_file = "stoplist.txt";
    }
    
    static class UrlRegexClassifierConfig {
        public List<String> regular_expressions;
        public String whitelist_file;
        public String blacklist_file;
    }
    
    static class TitleRegexClassifierConfig {
        public String regular_expression;
    }

    public static TargetClassifier create(String modelPath) throws IOException {
        return create(modelPath, null);
    }
    
    public static TargetClassifier create(String modelPath, String stoplist) throws IOException {
        
        logger.info("Loading TargetClassifier...");
        
        Path basePath = Paths.get(modelPath);
        File configFile = Paths.get(modelPath, "pageclassifier.yml").toFile();
        
        if(configFile.exists() && configFile.canRead()) {
        
            ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
            JsonNode tree = yaml.readTree(configFile);
            String classifierType = tree.get("type").asText();
            JsonNode parameters = tree.get("parameters");
            
            logger.info("TargetClassifier: "+classifierType);
            
            if("url_regex".equals(classifierType)) {
                
                UrlRegexClassifierConfig params = yaml.treeToValue(parameters,
                                                                   UrlRegexClassifierConfig.class);
                
                if(params.regular_expressions != null && params.regular_expressions.size() > 0) {
                    return UrlRegexTargetClassifier.fromRegularExpressions(params.regular_expressions);
                }
                
                
                if(params.whitelist_file != null && params.blacklist_file != null) {
                    params.whitelist_file = basePath.resolve(params.whitelist_file).toString();
                    params.blacklist_file = basePath.resolve(params.blacklist_file).toString();
                    return UrlRegexTargetClassifier.fromWhitelistAndBlacklistFiles(
                        params.whitelist_file,
                        params.blacklist_file
                    );
                }
                
                if(params.whitelist_file != null && params.blacklist_file == null) {
                    params.whitelist_file = basePath.resolve(params.whitelist_file).toString();
                    return UrlRegexTargetClassifier.fromWhitelistFile(params.whitelist_file);
                }
                
                if(params.whitelist_file == null && params.blacklist_file != null) {
                    params.blacklist_file = basePath.resolve(params.blacklist_file).toString();
                    return UrlRegexTargetClassifier.fromBlacklistFile(params.blacklist_file);
                }
                
                throw new IllegalArgumentException("Config for url_regex classifier has "
                        + "missing or wrong values in file: "
                        + Paths.get(modelPath, "/pageclassifier.yml"));
            }
            
            if("title_regex".equals(classifierType)) {
                
                TitleRegexClassifierConfig params = yaml.treeToValue(parameters,
                        TitleRegexClassifierConfig.class);
                
                if(params.regular_expression != null && !params.regular_expression.trim().isEmpty()) {
                    return new TitleRegexTargetClassifier(params.regular_expression.trim());
                }
                
                throw new IllegalArgumentException("Config for title_regex classifier has "
                        + "missing or wrong values in file: "
                        + Paths.get(modelPath, "/pageclassifier.yml"));
            }
            
            if("weka".equals(classifierType)) {
                
                WekaClassifierConfig params = yaml.treeToValue(parameters, WekaClassifierConfig.class);
                params.model_file = basePath.resolve(params.model_file).toString();
                params.features_file = basePath.resolve(params.features_file).toString();
                params.stopwords_file = basePath.resolve(params.stopwords_file).toString();
                
                return WekaTargetClassifier.create(params.model_file,
                                                   params.features_file,
                                                   params.stopwords_file);
            }
            
            throw new IllegalArgumentException("Could not instantiate classifier using config: "
                                               + Paths.get(modelPath, "/pageclassifier.yml"));
        }
        
        // create classic weka classifer to maintain compatibility with older versions
        return WekaTargetClassifier.create(modelPath, stoplist);
    }

}
