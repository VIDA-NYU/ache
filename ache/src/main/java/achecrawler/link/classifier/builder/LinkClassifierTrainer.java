package achecrawler.link.classifier.builder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.link.classifier.LNClassifier;
import achecrawler.link.classifier.builder.LinkClassifierFeatureSelector.Features;
import achecrawler.util.Sampler;
import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.string.StopList;
import smile.classification.SVM;
import smile.classification.SVM.Multiclass;
import smile.classification.SoftClassifier;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.data.NominalAttribute;
import smile.data.NumericAttribute;
import smile.math.kernel.LinearKernel;

public class LinkClassifierTrainer {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkClassifierTrainer.class);
    
    private LinkClassifierFeatureSelector featureSelector;
    private StopList stoplist;
    
    public LinkClassifierTrainer(StopList stoplist) {
        this.stoplist = stoplist;
        this.featureSelector = new LinkClassifierFeatureSelector(stoplist);
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
        String[] classValuesArray = (String[]) classValues.toArray(new String[classValues.size()]);
        LinkNeighborhoodWrapper wrapper =
                new LinkNeighborhoodWrapper(bestFeatures.features, stoplist);
        return new LNClassifier(classifier, wrapper, bestFeatures.features, classValuesArray);
    }

    private SoftClassifier<double[]> trainClassifier(AttributeDataset data, int numberOfClasses) {
        final double c = 1.0;
        SVM<double[]> classifier;
        if(numberOfClasses > 2) {
            classifier = new SVM<double[]>(new LinearKernel(), c, numberOfClasses, Multiclass.ONE_VS_ALL);
        } else {
            classifier = new SVM<double[]>(new LinearKernel(), c);
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

        LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(this.stoplist);
        wrapper.setFeatures(bestFeatures.fieldWords);

        List<String> classValues = new ArrayList<>();
        for (int i = 0; i < instances.size(); i++) {
            classValues.add(String.valueOf(i));
        }

        return createDataset(instances, bestFeatures.features, classValues, wrapper);
    }

    private Features selectFeatures(List<Sampler<LinkNeighborhood>> instances, boolean backlink)
            throws MalformedURLException {
        List<LinkNeighborhood> allInstances = new ArrayList<LinkNeighborhood>();
        for (int i = 0; i < instances.size(); i++) {
            Sampler<LinkNeighborhood> sampler = instances.get(i);
            for (LinkNeighborhood ln : sampler.getSamples()) {
                allInstances.add(ln);
            }
        }

        Features bestFeatures = featureSelector.selectBestFeatures(allInstances, backlink);
        return bestFeatures;
    }

    /**
     * Converts the input instances into an AttributeDataset object that can be used to train a
     * SMILE classifier.
     * 
     * @param attributes
     * @param instances
     * @param wrapper
     * @param dataset
     * @throws IOException
     */
    private AttributeDataset createDataset(List<Sampler<LinkNeighborhood>> instances,
            String[] features, List<String> classValues, LinkNeighborhoodWrapper wrapper) {
        
        List<Attribute> attributes = new ArrayList<>();
        for(String featureName : features) {
            NumericAttribute attribute = new NumericAttribute(featureName);
            attributes.add(attribute);
        }

        Attribute[] attributesArray = (Attribute[]) attributes.toArray(new Attribute[attributes.size()]);
        String[] classValuesArray = (String[]) classValues.toArray(new String[classValues.size()]);
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
                    logger.warn("Failed to process intance: "+ln.getLink().toString(), e);
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
