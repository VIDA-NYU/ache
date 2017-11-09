package focusedCrawler.learn.classifier.weka;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import com.google.common.base.Preconditions;

import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This is a generic interface for implementing classifiers using the Weka library. It provides an
 * abstraction layer of Weka internals. Users of this class only need to provide an implementation
 * of a WekaVectorizer interface that transforms any object into a Weka's Instance.
 * 
 * @author aeciosantos
 *
 * @param <T> an vectorizer that transforms any object into an Weka Instance.
 */
public class WekaOnlineClassifier<T> {

    protected Classifier classifier;
    protected Instances dataset;
    private WekaVectorizer<T> vectorizer;

    public WekaOnlineClassifier(Classifier classifier, String[] attributes,
            Object[] classValues, WekaVectorizer<T> vectorizer) {
        this.classifier = classifier;
        this.vectorizer = vectorizer;
        this.dataset = createWekaInstances(attributes, classValues);
        if (classifier instanceof UpdateableClassifier) {
            try {
                this.classifier.buildClassifier(this.dataset);
            } catch (Exception e) {
                throw new RuntimeException("Failed to build classifier.", e);
            }
        }
    }

    public double[] classify(T x) {
        Instance instance = vectorizer.toInstance(x);
        instance.setDataset(dataset);
        synchronized (classifier) {
            try {
                return classifier.distributionForInstance(instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to classify feature vector", e);
            }
        }
    }

    public void updateModel(T x, Object y) {
        Instance instance = vectorizer.toInstance(x);

        Preconditions.checkArgument(instance.numAttributes() == dataset.numAttributes(),
                "Instance and dataset must have same number of attributes: # of features + 1 (class)");

        instance.setDataset(dataset);
        if (y.getClass().equals(String.class)) {
            instance.setClassValue((String) y);
        } else {
            instance.setClassValue((Double) y);
        }
        synchronized (classifier) {
            UpdateableClassifier updateable = (UpdateableClassifier) classifier;
            try {
                updateable.updateClassifier(instance);
            } catch (Exception e) {
                throw new UnsupportedOperationException("Failed to update model using instance", e);
            }
        }
    }

    public void buildModel(List<T> trainingData, List<?> classes) {
        System.out.println("WekaOnlineClassifier.buildModel() " + trainingData.size() + " classes:"
                + classes.size());

        Preconditions.checkArgument(trainingData.size() == classes.size(),
                "trainingData and classes must have same size");

        for (int i = 0; i < trainingData.size(); i++) {
            Instance instance = vectorizer.toInstance(trainingData.get(i));

            Preconditions.checkArgument(instance.numAttributes() == dataset.numAttributes(),
                    "Instance and dataset must have same number of attributes: # of features + 1 (class)");

            instance.setDataset(dataset);
            if (classes.get(i).getClass().equals(String.class)) {
                instance.setClassValue((String) classes.get(i));
            } else {
                instance.setClassValue((Double) classes.get(i));
            }
            dataset.add(instance);
        }
        synchronized (classifier) {
            try {
                classifier.buildClassifier(dataset);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update model using instance", e);
            }
        }
    }

    private Instances createWekaInstances(String[] attributes, Object[] classValues) {
        // setup attributes
        FastVector vectorAtt = new FastVector();
        for (int i = 0; i < attributes.length; i++) {
            vectorAtt.addElement(new Attribute(attributes[i]));
        }

        // setup class attribute
        if (classValues.length > 0 && classValues[0].getClass().equals(String.class)) {

            FastVector classAtt = new FastVector();
            for (int i = 0; i < classValues.length; i++) {
                classAtt.addElement(classValues[i]);
            }
            vectorAtt.addElement(new weka.core.Attribute("__CLASS__", classAtt));
        } else {
            vectorAtt.addElement(new weka.core.Attribute("__CLASS__"));
        }

        // create instances object
        Instances instances = new Instances("__WEKA_CLASSIFIER__", vectorAtt, 1);
        instances.setClassIndex(attributes.length);
        return instances;
    }

    protected static Classifier loadClassifier(String modelFilePath) {
        try (InputStream is = new FileInputStream(modelFilePath)) {
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            Classifier classifier = (Classifier) objectInputStream.readObject();
            objectInputStream.close();
            return classifier;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to load classifier from file: " + modelFilePath, e);
        }
    }

    @Override
    public String toString() {
        return classifier.toString();
    }

}
