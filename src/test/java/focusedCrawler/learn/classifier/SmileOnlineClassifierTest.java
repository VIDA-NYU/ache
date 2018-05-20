package focusedCrawler.learn.classifier;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;

public class SmileOnlineClassifierTest {
    
    private static final int RELEVANT = 0;
    private static final int IRELEVANT = 1;

    @Test
    public void shouldTrainSmileClassifier() {

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
        textVectorizer.fit(trainingData);

        String[] attributes = textVectorizer.getFeaturesAsArray();
        int[] classValues = {RELEVANT, IRELEVANT};

        // when
        SmileOnlineClassifier<String> classifier = new SmileOnlineClassifier<>(Learner.SVM, attributes, classValues, textVectorizer);
        classifier.buildModel(trainingData, classes);
        double[] result = classifier.classify("zxcv asdf");

        // then
        assertTrue(result[0] < 0.5); // relevant
        assertTrue(result[1] > 0.5); // irrelevant
    }

}
