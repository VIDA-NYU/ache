package focusedCrawler.learn.classifier;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import focusedCrawler.learn.classifier.smile.DoubleVectorizer;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;
import focusedCrawler.learn.vectorizer.SparseVector;

public class SmileOnlineClassifierTest {
    
    private static final int RELEVANT = 0;
    private static final int IRELEVANT = 1;

    @Test
    public void shouldTrainWekaClassifier() {

        List<String> trainingData = new ArrayList<>();
        List<Integer> classes = new ArrayList<>();
        
        trainingData.add("asdf qwer");
        classes.add(RELEVANT);

        trainingData.add("asdf qwer");
        classes.add(RELEVANT);

        trainingData.add("qwer zxcv");
        classes.add(IRELEVANT);

        trainingData.add("zxcv");
        classes.add(IRELEVANT);

        BinaryTextVectorizer textVectorizer = new BinaryTextVectorizer();
        for (String i : trainingData) {
            textVectorizer.partialFit(Arrays.asList(i.split(" ")));
        }

        String[] attributes = textVectorizer.getFeaturesAsArray();
        int[] classValues = {RELEVANT, IRELEVANT};

        DoubleVectorizer<String> vectorizer = new DoubleVectorizer<String>() {
            @Override
            public double[] toInstance(String text) {
                SparseVector vector = textVectorizer.transform(text);
                double[] instance = vector.toDoubleVector(textVectorizer);
                for (int i = 0; i < instance.length; i++) {
                    System.out.println(i + " => " + vector.get(i) + "  " + textVectorizer.getFeature(i));
                }
                System.out.println();
                return instance;
            }
        };

        // when
        SmileOnlineClassifier<String> classifier = new SmileOnlineClassifier<>(Learner.SVM, attributes, classValues, vectorizer);
        classifier.buildModel(trainingData, classes);
        double[] result = classifier.classify("zxcv asdf");

        // then
        System.out.println(result[0] + " " + result[1]);
        assertTrue(result[0] < 0.5); // relevant
        assertTrue(result[1] > 0.5); // irrelevant
    }

}
