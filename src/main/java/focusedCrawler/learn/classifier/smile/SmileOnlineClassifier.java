package focusedCrawler.learn.classifier.smile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import com.google.common.base.Preconditions;

import smile.classification.GradientTreeBoost;
import smile.classification.OnlineClassifier;
import smile.classification.RandomForest;
import smile.classification.SVM;
import smile.classification.SoftClassifier;
import smile.classification.SVM.Multiclass;
import smile.math.kernel.LinearKernel;

/**
 * This is a generic interface for implementing classifiers using the SMILE library. It provides an
 * abstraction layer of SMILE internals. Users of this class only need to provide an implementation
 * of a DoubleVectorizer interface that transforms any object into a double[] feature vector.
 * 
 * @author aeciosantos
 *
 * @param <T> an vectorizer that transforms any object into an double[] vector.
 */
public class SmileOnlineClassifier<T> {

    public enum Learner {
        SVM, RANDOM_FOREST, GRADIENT_BOOSTING
    }

    private final Learner classifierType;
    private final String[] attributes;
    private final DoubleVectorizer<T> vectorizer;
    private SoftClassifier<double[]> classifier;
    private int[] classValues;

    public SmileOnlineClassifier(Learner classifierType, String[] attributes,
            int[] classValues, DoubleVectorizer<T> vectorizer) {
        this.classifierType = classifierType;
        this.attributes = attributes;
        this.classValues = classValues;
        this.vectorizer = vectorizer;
    }

    public double[] classify(T x) {
        double[] result = new double[classValues.length];
        double[] instance = vectorizer.toDoubleVector(x);
        synchronized (classifier) {
            classifier.predict(instance, result);
        }
        return result;
    }

    public void updateModel(T x, int y) {
        double[] instance = vectorizer.toDoubleVector(x);
        Preconditions.checkArgument(instance.length == attributes.length,
                "Vectorized instance must have same number of attributes of this classifier");
        if (classifier instanceof OnlineClassifier) {
            @SuppressWarnings("unchecked")
            OnlineClassifier<double[]> updateable = (OnlineClassifier<double[]>) classifier;
            updateable.learn(instance, y);
        } else {
            throw new UnsupportedOperationException("This classifier does not support updates.");
        }
    }

    public void buildModel(List<T> trainingData, List<Integer> labels) {
        Preconditions.checkArgument(trainingData.size() == labels.size(),
                "trainingData and classes must have same size");
        double[][] x = createDataVectors(trainingData, vectorizer);
        int[] y = createLabelVector(labels);
        this.classifier = trainSmileClassifier(x, y, classifierType, this.classValues.length);
    }

    private SoftClassifier<double[]> trainSmileClassifier(double[][] x, int[] y,
            Learner classifierType, int numberOfClasses) {
        switch (classifierType) {
            case SVM:
                SVM<double[]> svm;
                double c = 0.1;
                if (numberOfClasses > 2) {
                    svm = new SVM<double[]>(new LinearKernel(), c, numberOfClasses,
                            Multiclass.ONE_VS_ALL);
                } else {
                    svm = new SVM<double[]>(new LinearKernel(), c);
                }
                svm = new SVM<>(new LinearKernel(), 0.01);
                svm.learn(x, y);
                svm.finish();
                svm.trainPlattScaling(x, y);
                return svm;
            case RANDOM_FOREST:
                RandomForest randomForest = new RandomForest(x, y, 100);
                return randomForest;
            case GRADIENT_BOOSTING:
                GradientTreeBoost gbt = new GradientTreeBoost(x, y, 300);
                return gbt;
            default:
                throw new IllegalArgumentException("Unknown learning algorithm: " + classifierType);
        }
    }

    private int[] createLabelVector(List<Integer> labels) {
        int[] y = new int[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            y[i] = labels.get(i);
        }
        return y;
    }

    private double[][] createDataVectors(List<T> trainingData, DoubleVectorizer<T> vectorizer) {
        double[][] x = new double[trainingData.size()][attributes.length];
        for (int i = 0; i < trainingData.size(); i++) {
            double[] instance = vectorizer.toDoubleVector(trainingData.get(i));
            Preconditions.checkArgument(instance.length == attributes.length,
                    "Vectorized instance must have same number of attributes of this classifier. instance_size: %s classifier: %s",
                    instance.length, attributes.length);
            x[i] = instance;
        }
        return x;
    }

    protected static SoftClassifier<double[]> loadClassifier(String modelFilePath) {
        try (InputStream is = new FileInputStream(modelFilePath)) {
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            @SuppressWarnings("unchecked")
            SoftClassifier<double[]> classifier =
                    (SoftClassifier<double[]>) objectInputStream.readObject();
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
