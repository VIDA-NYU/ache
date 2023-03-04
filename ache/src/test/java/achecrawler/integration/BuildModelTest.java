package achecrawler.integration;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import achecrawler.Main;
import achecrawler.target.classifier.TargetClassifier;
import achecrawler.target.classifier.TargetClassifierFactory;
import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildModelTest {

    @TempDir
    public File tempFolder;

    @Test
    void smileFeaturesFileShouldBeGeneratedInTheProperFormat() throws Exception {
        
        String trainingPath = BuildModelTest.class.getResource("build_model_test").getFile();
        String modelPath    = tempFolder.toString();
        
        // Train a page classifier model
        String[] args = {"buildModel", "-t", trainingPath, "-o", modelPath};
        Main.main(args);

        // Load model trained
        TargetClassifier tc = TargetClassifierFactory.create(modelPath);
        
        // Classify one example from training data just for sanity check
        Page samplePositivePage = readOnePageFromFolder(trainingPath + "/positive");
        Page sampleNegativePage = readOnePageFromFolder(trainingPath + "/negative");

        TargetRelevance positive = tc.classify(samplePositivePage);
        assertThat(positive.isRelevant()).isTrue();
        assertThat(positive.getRelevance()).isGreaterThan(0.5);

        TargetRelevance negative = tc.classify(sampleNegativePage);
        assertThat(negative.isRelevant()).isFalse();
        assertThat(negative.getRelevance()).isLessThan(0.5);
    }

    private Page readOnePageFromFolder(String positiveFolder) throws Exception {
        File[] allPositivePages = (new File(positiveFolder)).listFiles();
        assertThat(allPositivePages.length).isEqualTo(6);
        String positiveFileName = allPositivePages[0].getName();
        String fileContent = new String(Files.readAllBytes(Paths.get(allPositivePages[0].getAbsolutePath())));
        Page samplePositivePage = new Page(new URL(URLDecoder.decode(positiveFileName, "UTF-8")), fileContent);
        return samplePositivePage;
    }

}
