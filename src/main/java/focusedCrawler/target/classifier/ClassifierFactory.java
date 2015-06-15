package focusedCrawler.target.classifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ClassifierFactory {

    static class WekaClassifierConfig {
        public String features_file = "pageclassifier.features";
        public String model_file = "pageclassifier.features";
        public String stopwords_file = "stoplist.txt";
    }
    
    static class UrlRegexClassifierConfig {
        public List<String> regular_expressions;
        public String file;
    }

    public static TargetClassifier create(String modelPath) throws IOException {
        return create(modelPath, null);
    }
    
    public static TargetClassifier create(String modelPath, String stoplist) throws IOException {
        
        Path basePath = Paths.get(modelPath);
        File configFile = Paths.get(modelPath, "pageclassifier.yml").toFile();
        
        if(configFile.exists() && configFile.canRead()) {
        
            ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
            JsonNode tree = yaml.readTree(configFile);
            String classifierType = tree.get("type").asText();
            
            JsonNode parameters = tree.get("parameters");
            
            if("url_regex".equals(classifierType)) {
                
                UrlRegexClassifierConfig params = yaml.treeToValue(parameters,
                                                                   UrlRegexClassifierConfig.class);
                
                if(params.regular_expressions != null && params.regular_expressions.size() > 0) {
                    return UrlRegexTargetClassifier.fromRegularExpressions(params.regular_expressions);
                }
                
                if(params.file != null && !params.file.isEmpty()) {
                    return UrlRegexTargetClassifier.fromRegularExpressionsFile(params.file);
                }
                
                throw new IllegalArgumentException("Config file has missing values: "
                                                   + Paths.get(modelPath, "/pageclassifier.yml"));
            }
            
            if("weka".equals(classifierType)) {
                
                WekaClassifierConfig params = yaml.treeToValue(parameters, WekaClassifierConfig.class);
                params.model_file = basePath.resolve(params.model_file).toString();
                params.features_file = basePath.resolve(params.features_file).toString();
                params.stopwords_file = basePath.resolve(params.stopwords_file).toString();
                
                return TargetClassifierImpl.create(params.model_file,
                                                   params.features_file,
                                                   params.stopwords_file);
            }
            
            throw new IllegalArgumentException("Could not instantiate classifier using config: "
                                               + Paths.get(modelPath, "/pageclassifier.yml"));
        }
        
        // create classic weka classifer to maintain compatibility with older versions
        return TargetClassifierImpl.create(modelPath, stoplist);
    }

}
