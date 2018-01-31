package focusedCrawler.link.classifier;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.SmileUtil;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.string.StopList;
import smile.classification.SoftClassifier;

public class LNClassifier {

    private static final Logger logger = LoggerFactory.getLogger(LNClassifier.class);

    private final SoftClassifier<double[]> classifier;
    private final LinkNeighborhoodWrapper wrapper;
    private final String[] attributes;
    private String[] classValues;

    public LNClassifier(SoftClassifier<double[]> classifier, LinkNeighborhoodWrapper wrapper,
            String[] attributes, String[] classValues) {
        this.classifier = classifier;
        this.wrapper = wrapper;
        this.attributes = attributes;
        this.classValues = classValues;
    }

    public double[] classify(LinkNeighborhood ln) throws Exception {
        Instance instance = wrapper.extractToInstanceWithImageFeatures(ln, attributes);
        double[] values = instance.getValues();
        double[] prob = new double[classValues.length];
        synchronized (classifier) {
            classifier.predict(values, prob);
            return prob;
        }
    }

    public static LNClassifier create(String featureFilePath,
            String modelFilePath,
            StopList stoplist) {
        ParameterFile config = new ParameterFile(featureFilePath);
        String[] attributes = config.getParam("ATTRIBUTES", " ");
        String[] classValues = config.getParam("CLASS_VALUES", " ");
        return create(attributes, classValues, modelFilePath, stoplist);
    }

    public static LNClassifier create(String[] attributes, String[] classValues,
            String modelFilePath, StopList stoplist) {
        LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(attributes, stoplist);
        SoftClassifier<double[]> classifier = SmileUtil.loadSmileClassifier(modelFilePath);
        return new LNClassifier(classifier, wrapper, attributes, classValues);
    }

    public void writeToFolder(Path linkClassifierFolder) throws IOException {
        String featuresFile = linkClassifierFolder.resolve("link_classifier.features").toString();
        logger.info("Link Clasifier features file: " + featuresFile);
        writeFeaturesFile(featuresFile, attributes);

        String modelFile = linkClassifierFolder.resolve("link_classifier.model").toString();
        logger.info("Link Clasifier model file: " + modelFile);
        SmileUtil.writeSmileClassifier(modelFile, classifier);
    }

    private void writeFeaturesFile(String featuresFile, String[] features) throws IOException {
        FileWriter outputFile = new FileWriter(featuresFile, false);
        outputFile.write("CLASS_VALUES");
        for (int i = 0; i < classValues.length; i++) {
            outputFile.write(" " + classValues[i]);
        }
        outputFile.write("\nATTRIBUTES");
        for (int i = 0; i < features.length; i++) {
            outputFile.write(" " + features[i]);
        }
        outputFile.close();
    }

}
