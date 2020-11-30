package achecrawler.target.classifier;

import java.io.IOException;
import java.nio.file.Path;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.model.Page;
import achecrawler.util.ParameterFile;
import achecrawler.util.SmileUtil;
import achecrawler.util.string.StopList;
import achecrawler.util.string.StopListFile;
import achecrawler.util.vsm.VSMElement;
import achecrawler.util.vsm.VSMVector;
import smile.classification.SoftClassifier;


public class SmileTargetClassifier implements TargetClassifier {

    private final SoftClassifier<double[]> classifier;
    private final String[] attributes;
    private final StopList stoplist;
    private final Double relevanceThreshold;

    public SmileTargetClassifier(SoftClassifier<double[]> classifier, Double relevanceThreshold,
            String[] attributes, StopList stoplist) {
        this.classifier = classifier;
        this.relevanceThreshold = relevanceThreshold;
        this.attributes = attributes;
        this.stoplist = stoplist;
    }

    public TargetRelevance classify(Page page) throws TargetClassifierException {
        try {
            double[] features = getValues(page);
            synchronized (classifier) {
                double[] probs = new double[2];
                int predictedClass = classifier.predict(features, probs);
                double relevantProbability = probs[0];
                if (relevanceThreshold == null) {
                    // use actual SVM prediction output
                    if (predictedClass == 0) {
                        return new TargetRelevance(true, relevantProbability);
                    } else {
                        return new TargetRelevance(false, relevantProbability);
                    }
                } else {
                    // use probability score from platt's scaling
                    if (relevantProbability > relevanceThreshold) {
                        return new TargetRelevance(true, relevantProbability);
                    } else {
                        return new TargetRelevance(false, relevantProbability);
                    }
                }
            }

        } catch (Exception ex) {
            throw new TargetClassifierException(ex.getMessage(), ex);
        }
    }

    private double[] getValues(Page page) throws IOException, SAXException {
        VSMVector vsm = new VSMVector(page.getContentAsString(), stoplist, true);
        double[] values = new double[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            VSMElement elem = vsm.getElement(attributes[i]);
            if (elem == null) {
                values[i] = 0;
            } else {
                values[i] = elem.getWeight();
            }
        }
        return values;
    }

    public static TargetClassifier create(String modelPath,
            Double relevanceThreshold,
            StopListFile stopwordsFile)
            throws IOException {
        return create(modelPath + "/pageclassifier.model",
                modelPath + "/pageclassifier.features",
                relevanceThreshold,
                stopwordsFile);

    }

    public static TargetClassifier create(String modelFile,
            String featureFile,
            Double relevanceThreshold,
            String stopwordsFile)
            throws IOException {
        StopListFile stoplist;
        if (stopwordsFile != null && !stopwordsFile.isEmpty()) {
            stoplist = new StopListFile(stopwordsFile);
        } else {
            stoplist = StopListFile.DEFAULT;
        }
        return create(modelFile, featureFile, relevanceThreshold, stoplist);
    }

    public static TargetClassifier create(String modelFile,
            String featureFile,
            Double relevanceThreshold,
            StopList stoplist)
            throws IOException {
        ParameterFile featureConfig = new ParameterFile(featureFile);
        SoftClassifier<double[]> classifier = SmileUtil.loadSmileClassifier(modelFile);
        String[] attributes = featureConfig.getParam("ATTRIBUTES", " ");
        return new SmileTargetClassifier(classifier, relevanceThreshold, attributes, stoplist);
    }


    static class SmileClassifierConfig {
        public String features_file = "pageclassifier.features";
        public String model_file = "pageclassifier.features";
        public String stopwords_file = null;
        public Double relevance_threshold = null;
    }

    public static class Builder {

        public TargetClassifier build(Path basePath, ObjectMapper yaml, JsonNode parameters)
                throws IOException {

            SmileClassifierConfig params =
                    yaml.treeToValue(parameters, SmileClassifierConfig.class);
            params.model_file = resolveRelativePath(basePath, params.model_file);
            params.features_file = resolveRelativePath(basePath, params.features_file);
            params.stopwords_file = resolveRelativePath(basePath, params.stopwords_file);
            if (params.relevance_threshold != null) {
                if (params.relevance_threshold < 0.0 || params.relevance_threshold > 1.0) {
                    throw new IllegalArgumentException(
                            "The relevance threshold should be a number in the interval [0.0, 1.0]. Got value: "
                                    + params.relevance_threshold);
                }
            }
            return SmileTargetClassifier.create(
                    params.model_file,
                    params.features_file,
                    params.relevance_threshold,
                    params.stopwords_file);
        }

        private String resolveRelativePath(Path basePath, String relative) {
            if (relative == null) {
                return null;
            }
            return basePath.resolve(relative).toFile().getAbsolutePath();
        }

    }

}
