package achecrawler.link.classifier.builder;

import achecrawler.link.classifier.LNClassifier;
import achecrawler.link.classifier.builder.LinkClassifierFeatureSelector.Features;
import achecrawler.util.Sampler;
import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.string.Stopwords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.classification.SVM;
import smile.classification.SVM.Multiclass;
import smile.classification.SoftClassifier;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.data.NominalAttribute;
import smile.data.NumericAttribute;
import smile.math.kernel.LinearKernel;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class LinkClassifierTrainer {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkClassifierTrainer.class);
    
    private final LinkClassifierFeatureSelector featureSelector;
    private final Stopwords stopwords;
    
    public LinkClassifierTrainer(Stopwords stopwords) {
        this.stopwords = stopwords;
        this.featureSelector = new LinkClassifierFeatureSelector(stopwords);
    }
    
    public LNClassifier trainLNClassifier(List<Sampler<LinkNeighborhood>> instances, List<String> classValues) throws Exception {
        Features bestFeatures = selectFeatures(instances, false);
        return createLNClassifier(instances, classValues, bestFeatures);
    }
    
    public LNClassifier trainLNClassifierBacklink(List<Sampler<LinkNeighborhood>> instances, List<String> classValues) throws Exception {
        Features bestFeatures = selectFeatures(instances, true);
        return createLNClassifier(instances, classValues, bestFeatures);
    }

    private LNClassifier createLNClassifier(List<Sampler<LinkNeighborhood>> instances,
            List<String> classValues, Features bestFeatures) {
        AttributeDataset inputDataset = createSmileInput(instances, bestFeatures);
        SoftClassifier<double[]> classifier = trainClassifier(inputDataset, instances.size());
        String[] classValuesArray = classValues.toArray(new String[classValues.size()]);
        LinkNeighborhoodWrapper wrapper =
                new LinkNeighborhoodWrapper(bestFeatures.features, stopwords);
        return new LNClassifier(classifier, wrapper, bestFeatures.features, classValuesArray);
    }

    private SoftClassifier<double[]> trainClassifier(AttributeDataset data, int numberOfClasses) {
        final double c = 1.0;
        SVM<double[]> classifier;
        if(numberOfClasses > 2) {
            classifier = new SVM<>(new LinearKernel(), c, numberOfClasses, Multiclass.ONE_VS_ALL);
        } else {
            classifier = new SVM<>(new LinearKernel(), c);
        }
        
        int[] y = data.labels();
        double[][] x = data.x();
        classifier.learn(x, y);
        classifier.finish();
        classifier.trainPlattScaling(x, y);
        return classifier;
    }

    private AttributeDataset createSmileInput(List<Sampler<LinkNeighborhood>> instances,
            Features bestFeatures) {

        LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(this.stopwords);
        wrapper.setFeatures(bestFeatures.fieldWords);

        List<String> classValues = new ArrayList<>();
        for (int i = 0; i < instances.size(); i++) {
            classValues.add(String.valueOf(i));
        }

        return createDataset(instances, bestFeatures.features, classValues, wrapper);
    }

    private Features selectFeatures(List<Sampler<LinkNeighborhood>> instances, boolean backlink)
            throws MalformedURLException {
        List<LinkNeighborhood> allInstances = new ArrayList<>();
        for (Sampler<LinkNeighborhood> sampler : instances) {
            for (LinkNeighborhood ln : sampler.getSamples()) {
                allInstances.add(ln);
            }
        }

        return featureSelector.selectBestFeatures(allInstances, backlink);
    }

    /**
     * Converts the input instances into an AttributeDataset object that can be used to train a
     * SMILE classifier.
     *
     */
    private AttributeDataset createDataset(List<Sampler<LinkNeighborhood>> instances,
            String[] features, List<String> classValues, LinkNeighborhoodWrapper wrapper) {
        
        List<Attribute> attributes = new ArrayList<>();
        for(String featureName : features) {
            NumericAttribute attribute = new NumericAttribute(featureName);
            attributes.add(attribute);
        }

        Attribute[] attributesArray = attributes.toArray(new Attribute[attributes.size()]);
        String[] classValuesArray = classValues.toArray(new String[classValues.size()]);
        String description = "If link leads to relevant page or not.";
        Attribute response = new NominalAttribute("y", description, classValuesArray);
        AttributeDataset dataset = new AttributeDataset("link_classifier", attributesArray, response);

        for (int level = 0; level < instances.size(); level++) {
            Sampler<LinkNeighborhood> levelSamples = instances.get(level);
            for (LinkNeighborhood ln : levelSamples.getSamples()) {
                Instance instance;
                try {
                    instance = wrapper.extractToInstance(ln, features);
                } catch (MalformedURLException e) {
                    logger.warn("Failed to process instance: "+ln.getLink().toString(), e);
                    continue;
                }
                double[] values = instance.getValues(); // the instance's feature vector
                int y = level; // the class we're trying to predict
                dataset.add(values, y);
            }
        }
        return dataset;
    }

}
