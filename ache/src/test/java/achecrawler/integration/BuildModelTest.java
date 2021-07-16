package achecrawler.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import achecrawler.Main;
import achecrawler.target.classifier.TargetClassifier;
import achecrawler.target.classifier.TargetClassifierFactory;
import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;

public class BuildModelTest {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void smileFeaturesFileShouldBeGeneratedInTheProperFormat() throws Exception {
        
        String trainingPath = BuildModelTest.class.getResource("build_model_test").getFile();
        String modelPath    = tempFolder.newFolder().toString();
        
        // Train a page classifier model
        String[] args = {"buildModel", "-t", trainingPath, "-o", modelPath};
        Main.main(args);

        // Load model trained
        TargetClassifier tc = TargetClassifierFactory.create(modelPath);
        
        // Classify one example from training data just for sanity check
        Page samplePositivePage = readOnePageFromFolder(trainingPath + "/positive");
        Page sampleNegativePage = readOnePageFromFolder(trainingPath + "/negative");

        TargetRelevance positive = tc.classify(samplePositivePage);
        assertThat(positive.isRelevant(), is(true));
        assertThat(positive.getRelevance(), greaterThan(0.5));

        TargetRelevance negative = tc.classify(sampleNegativePage);
        assertThat(negative.isRelevant(), is(false));
        assertThat(negative.getRelevance(), lessThan(0.5));
    }

    private Page readOnePageFromFolder(String positiveFolder) throws Exception {
        File[] allPositivePages = (new File(positiveFolder)).listFiles();
        assertThat(allPositivePages.length, is(6));
        String positiveFileName = allPositivePages[0].getName();
        String fileContent = new String(Files.readAllBytes(Paths.get(allPositivePages[0].getAbsolutePath())));
        Page samplePositivePage = new Page(new URL(URLDecoder.decode(positiveFileName, "UTF-8")), fileContent);
        return samplePositivePage;
    }

}
