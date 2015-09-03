package focusedCrawler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.util.Page;

public class MainTest {

    @Test
    public void wekaFeaturesFileShouldBeGeneratedInTheProperFormat() throws Exception {

        String stopList = "config/sample_config/stoplist.txt";
        String trainingPath = "config/sample_training_data";
        String mainOutput = "config/test_model_output_main";
        
        new File(mainOutput).mkdir();

        String[] args = { "buildModel", "-c", stopList, "-t", trainingPath, "-o", mainOutput };
        Main.main(args);

        File[] allPositivePages = (new File("config/sample_training_data/positive")).listFiles();
        File[] allNegativePages = (new File("config/sample_training_data/negative")).listFiles();

        if (allPositivePages.length > 0 && allNegativePages.length > 0) {
            
            String positiveFileName = allPositivePages[0].getName();
            String negativeFileName = allNegativePages[0].getName();
            
            Page sampleNegativePage = readPageFromFile(allNegativePages[0], negativeFileName);
            Page samplePositivePage = readPageFromFile(allPositivePages[0], positiveFileName);

            String modelPath = mainOutput;
            String stopWordsFile = "config/sample_config/stoplist.txt";

            TargetClassifier tc = TargetClassifierFactory.create(modelPath, 0.9, stopWordsFile);
            
            assertThat(tc.classify(samplePositivePage).isRelevant(), is(true));
            assertThat(tc.classify(sampleNegativePage).isRelevant(), is(false));
            
            deleteTestFiles(mainOutput);
        }
    }

    private Page readPageFromFile(File file, String encodedUrl) throws Exception {
        String fileContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        return new Page(new URL(URLDecoder.decode(encodedUrl, "UTF-8")), fileContent);
    }

    private void deleteTestFiles(String mainOutput) {
        (new File(mainOutput + File.separator + "pageclassifier.features")).delete();
        (new File(mainOutput + File.separator + "pageclassifier.model")).delete();
        (new File(mainOutput)).delete();

    }

}
