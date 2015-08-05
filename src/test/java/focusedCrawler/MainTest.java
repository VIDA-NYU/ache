package focusedCrawler;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifierException;
import focusedCrawler.util.Page;

public class MainTest {

    @Test
    public void wekaFeaturesFileShouldBeGeneratedInTheProperFormat() throws IOException,
            TargetClassifierException {

        String stopList = "config/sample_config/stoplist.txt";
        String trainingPath = "config/sample_training_data";
        String mainOutput = "config/test_model_output_main";
        (new File(mainOutput)).mkdir();

        String[] args = { "buildModel", "-c", stopList, "-t", trainingPath, "-o", mainOutput };
        Main.main(args);

        File[] allPositivePages = (new File("config/sample_training_data/positive")).listFiles();
        File[] allNegativePages = (new File("config/sample_training_data/negative")).listFiles();

        if (allPositivePages.length > 0 && allNegativePages.length > 0) {
            String positiveFileName = allPositivePages[0].getName();
            String negativeFileName = allNegativePages[0].getName();
            String positiveFileContent = new String(Files.readAllBytes(Paths
                    .get(allPositivePages[0].getAbsolutePath())));
            String negativeFileContent = new String(Files.readAllBytes(Paths
                    .get(allNegativePages[0].getAbsolutePath())));
            Page samplePositivePage = new Page(
                    new URL(URLDecoder.decode(positiveFileName, "UTF-8")), positiveFileContent);
            Page sampleNegativePage = new Page(
                    new URL(URLDecoder.decode(negativeFileName, "UTF-8")), negativeFileContent);

            String modelPath = mainOutput;
            String stopWordsFile = "config/sample_config/stoplist.txt";

            TargetClassifier tc = TargetClassifierFactory.create(modelPath, stopWordsFile);
            assertEquals("Unable to classify a positive page using the model! ", true,
                    (tc.classify(samplePositivePage)));
            assertEquals("Unable to classify a positive page using the model! ", false,
                    (tc.classify(sampleNegativePage)));
            deleteTestFiles(mainOutput);
        }
    }

    private void deleteTestFiles(String mainOutput) {
        (new File(mainOutput + File.separator + "pageclassifier.features")).delete();
        (new File(mainOutput + File.separator + "pageclassifier.model")).delete();
        (new File(mainOutput)).delete();

    }

}
