package focusedCrawler.link.classifier;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import focusedCrawler.config.Configuration;
import focusedCrawler.dedup.DupDetector;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.link.classifier.online.DeduplicationOnlineLearning;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.minhash.DupDetectorFactory;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.PaginaURL;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LinkClassifierDeduplicationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Parameter
    public Configuration config;

    /*
     * This test runs multiple times, one for each of the items in the returned list.
     * The test case must pass with all given configurations.
     */
    @Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList(
            new Configuration(ImmutableMap.of(
                "target_storage.near_duplicate_detection.enabled", true,
                "target_storage.near_duplicate_detection.type", "EXACT_DUP",
                "link_storage.online_learning.dedup.type", "CLASSIFIER",
                "link_storage.online_learning.dedup.classifier.learner", "SVM"
            )),
            new Configuration(ImmutableMap.of(
                "target_storage.near_duplicate_detection.enabled", true,
                "target_storage.near_duplicate_detection.type", "NEAR_DUP",
                "target_storage.near_duplicate_detection.similarity", 0.95,
                "link_storage.online_learning.dedup.type", "CLASSIFIER",
                "link_storage.online_learning.dedup.classifier.learner", "SVM"
            )),
            new Configuration(ImmutableMap.of(
                "target_storage.near_duplicate_detection.enabled", true,
                "target_storage.near_duplicate_detection.type", "EXACT_DUP",
                "link_storage.online_learning.dedup.type", "RULES"
            ))
            // FIXME
//            new Configuration(ImmutableMap.of(
//                "target_storage.near_duplicate_detection.enabled", true,
//                "target_storage.near_duplicate_detection.type", "PROBABILISTIC_EXACT_DUP"
//            ))
        );
    }

    @Test
    public void shouldTrainClassifierOnline() throws Exception {

        String dataPath =  tempFolder.newFolder().toString();
        DeduplicationOnlineLearning.LearningType type = this.config.getLinkStorageConfig().getOnlineLearningDedupLearningType();
        Learner learner = this.config.getLinkStorageConfig().getOnlineLearningClassifierLearner();

        DupDetector dupDetector = DupDetectorFactory.create(this.config, dataPath);

        DeduplicationOnlineLearning linkClassifierBuilder = new DeduplicationOnlineLearning(10,
                false, null, dupDetector, type, learner);

        Page page1 = createPage("http://example.com", createTestPage("example"));
        Page pageL1 = createPage("http://example.com/comment/1", createTestPage("login"));
        Page pageL2 = createPage("http://example.com/comment/2", createTestPage("login"));
        Page pageL3 = createPage("http://example.com/comment/3", createTestPage("login"));
        Page pageL4 = createPage("http://example.com/comment/4", createTestPage("login"));
        Page page2 = createPage("http://example2.com", pageWithLink("http://example.com/comment/4"));
        Page page3 = createPage("http://example3.com", pageWithLink("http://example.com/asdf"));

        dupDetector.detectAndIndex(page1.getRequestedUrl(), page1.getContentAsString());
        dupDetector.detectAndIndex(pageL1.getRequestedUrl(), pageL1.getContentAsString());
        dupDetector.detectAndIndex(pageL2.getRequestedUrl(), pageL2.getContentAsString());
        dupDetector.detectAndIndex(pageL3.getRequestedUrl(), pageL3.getContentAsString());
        dupDetector.detectAndIndex(pageL4.getRequestedUrl(), pageL4.getContentAsString());

        LinkClassifier dedup = linkClassifierBuilder.buildModel();

        LinkRelevance[] ln2 = dedup.classify(page2);
        LinkRelevance[] ln3 = dedup.classify(page3);

        assertTrue(ln3[0].getRelevance() > ln2[0].getRelevance());
    }

    private Page createPage(String url, String p1) throws MalformedURLException {
        Page page = new Page(new URL(url), p1);
        page.setParsedData(new ParsedData(new PaginaURL(page)));
        return page;
    }

    private String createTestPage(String content) {
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>");
        testPage.append(content);
        testPage.append("</h1>");
        testPage.append("</body>");
        testPage.append("</html>");
        return testPage.toString();
    }

    private String pageWithLink(String link) {
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>My First Heading</h1>");
        testPage.append("<a href = \"");
        testPage.append(link);
        testPage.append("\">My first paragraph.</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        return testPage.toString();
    }

}
