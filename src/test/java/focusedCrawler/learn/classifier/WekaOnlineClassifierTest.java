package focusedCrawler.learn.classifier;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import focusedCrawler.learn.classifier.weka.WekaOnlineClassifier;
import focusedCrawler.learn.classifier.weka.WekaVectorizer;
import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;
import focusedCrawler.learn.vectorizer.SparseVector;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.SparseInstance;

public class WekaOnlineClassifierTest {

    @Test
    public void shouldTrainWekaClassifier() {

        List<String> trainingData = new ArrayList<>();
        List<String> classes = new ArrayList<>();

        trainingData.add("asdf qwer");
        classes.add("relevant");

        trainingData.add("asdf qwer");
        classes.add("relevant");

        trainingData.add("qwer zxcv");
        classes.add("irrelevant");

        trainingData.add("zxcv");
        classes.add("irrelevant");

        BinaryTextVectorizer textVectorizer = new BinaryTextVectorizer();
        for (String i : trainingData) {
            textVectorizer.partialFit(Arrays.asList(i.split(" ")));
        }

        String[] attributes = textVectorizer.getFeaturesAsArray();
        String[] classValues = {"relevant", "irrelevant"};

        WekaVectorizer<String> wekaVectorizer = new WekaVectorizer<String>() {
            @Override
            public Instance toInstance(String text) {
                SparseVector vector = textVectorizer.transform(text);
                SparseInstance instance = new SparseInstance(attributes.length + 1);
                for (int i = 0; i < textVectorizer.numberOfFeatures(); i++) {
                    instance.setValue(i, vector.get(i));
                    System.out.println(
                            i + " => " + vector.get(i) + "  " + textVectorizer.getFeature(i));
                }
                System.out.println();
                return instance;
            }
        };

        // when
        WekaOnlineClassifier<String> classifier = new WekaOnlineClassifier<>(
                new NaiveBayesUpdateable(), attributes, classValues, wekaVectorizer);
        classifier.buildModel(trainingData, classes);
        double[] result = classifier.classify("zxcv asdf");

        // then
        System.out.println(result[0] + " " + result[1]);
        assertTrue(result[0] < 0.5); // relevant
        assertTrue(result[1] > 0.5); // irrelevant
    }

}
