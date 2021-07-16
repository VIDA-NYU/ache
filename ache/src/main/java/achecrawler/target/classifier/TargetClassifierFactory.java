package achecrawler.target.classifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class TargetClassifierFactory {

    private static final Logger logger = LoggerFactory.getLogger(TargetClassifierFactory.class);

    public static TargetClassifier create(String modelPath) throws IOException {

        logger.info("Loading TargetClassifier...");

        Path basePath = Paths.get(modelPath);
        Path configPath = Paths.get(modelPath, "/pageclassifier.yml");
        File configFile = Paths.get(modelPath, "pageclassifier.yml").toFile();

        if (configFile.exists() && configFile.canRead()) {

            ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

            JsonNode tree = yaml.readTree(configFile);
            String classifierType = tree.get("type").asText();
            JsonNode parameters = tree.get("parameters");

            logger.info("TARGET_CLASSIFIER: " + classifierType);

            TargetClassifier classifier = null;

            if ("url_regex".equals(classifierType)) {
                classifier =
                        new UrlRegexTargetClassifier.Builder().build(basePath, yaml, parameters);
            }

            if ("title_regex".equals(classifierType)) {
                classifier =
                        new TitleRegexTargetClassifier.Builder().build(basePath, yaml, parameters);
            }

            if ("body_regex".equals(classifierType)) {
                classifier =
                        new BodyRegexTargetClassifier.Builder().build(basePath, yaml, parameters);
            }

            if ("regex".equals(classifierType)) {
                classifier = new RegexTargetClassifier.Builder().build(basePath, yaml, parameters);
            }

            if ("keep_link_relevance".equals(classifierType)) {
                classifier = new KeepLinkRelevanceTargetClassifier.Builder().build(basePath, yaml,
                        parameters);
            }

            if ("smile".equals(classifierType)) {
                classifier = new SmileTargetClassifier.Builder().build(basePath, yaml, parameters);
            }

            if ("weka".equals(classifierType)) {
                throw new IllegalArgumentException("The 'weka' classifier is not supported anymore"
                        + " and was replaced by the 'smile' classifier. Please rebuild your model "
                        + "using ACHE's buildModel command of an up-to-date version of ACHE.");
            }

            if (classifier != null) {
                return classifier;
            } else {
                String errorMsg = "Could not instantiate classifier using config: " + configPath;
                throw new IllegalArgumentException(errorMsg);
            }
        }

        throw new IllegalArgumentException("Failed to load target classifier model. "
                + "Please make sure that:"
                + " 1) you provided the correct path to the model"
                + " 2) your model was build using an ACHE version compatible with this version"
                + " 3) your model path contains a pageclassifier.yml file");
    }

}
