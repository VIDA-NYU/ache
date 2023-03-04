package achecrawler.target.classifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.io.IOUtils;

import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.parser.PaginaURL;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexTargetClassifierTest {

    @Test
    void testRegexClassifierMatchConfig1() throws TargetClassifierException, IOException {
        // given
        String path = ClassifierFactoryTest.class.getResource("regex_classifier_config/").getPath();
        System.out.println(path);
        
        String url1 = "http://example.com/foo";
        String con1 = "<html><div><a href=\"http://j6im4v42ur6dpic3.onion/\">Page 1, foo</a></div></html>";
        Page  page1 = createPage(url1, con1);
        
        String url2 = "http://example.com/?category=1";
        String con2 = "<html><div><a href=\"http://example.com/\">Page 2, foo</a></div></html>";
        Page  page2 = createPage(url2, con2);
        
        String url3 = "http://example.com/?post=1";
        String con3 = "<html><div><a href=\"http://example.com/\">Page 2, bar</a></div></html>";
        Page  page3 = createPage(url3, con3);
        
        String url4 = "http://example.com/?post=1";
        String con4 = "<html><div><a href=\"http://example.com/\">Page 2, asdf</a></div></html>";
        Page  page4 = createPage(url4, con4);
        
        RegexTargetClassifier classifier = (RegexTargetClassifier) TargetClassifierFactory.create(path);
        
        // then
        assertThat(classifier.classify(page1).isRelevant()).isFalse();
        assertThat(classifier.classify(page2).isRelevant()).isTrue();
        assertThat(classifier.classify(page3).isRelevant()).isTrue();
        assertThat(classifier.classify(page4).isRelevant()).isFalse();
    }

    @Test
    void testRegexClassifierMatchConfigContentTypeCSV() throws TargetClassifierException, IOException {
        // given
        String path = ClassifierFactoryTest.class.getResource("regex_classifier/config_content_type_csv/").getPath();

        String url = "http://example.com/foo";
        String content = "<html><div><a href=\"http://j6im4v42ur6dpic3.onion/\">Page 1, foo</a></div></html>";
        Page  page1 = createPage(url, content);
        page1.setContentType("text/html");
        Page  page2 = createPage(url, content);
        page2.setContentType("text/csv");

        RegexTargetClassifier classifier = (RegexTargetClassifier) TargetClassifierFactory.create(path);

        // then
        assertThat(classifier.classify(page1).isRelevant()).isFalse();
        assertThat(classifier.classify(page2).isRelevant()).isTrue();
    }

    @Test
    void testRegexClassifierMatchConfig2() throws TargetClassifierException, IOException {
        // given
        String config = ClassifierFactoryTest.class.getResource("regex_classifier/config_jobs/").getPath();
        String pageFile = "https%3A%2F%2Fmarkettrack.com%2Fcareers%2Fjob-openings";
        InputStream fileInput = ClassifierFactoryTest.class.getResourceAsStream("regex_classifier/"+pageFile);
        
        String url = URLDecoder.decode(pageFile, "UTF-8");
        String content = IOUtils.toString(fileInput, "UTF-8");
        Page page = createPage(url, content);
        
        RegexTargetClassifier classifier = (RegexTargetClassifier) TargetClassifierFactory.create(config);
        
        // then
        assertThat(classifier.classify(page).isRelevant()).isTrue();
    }
    
    private Page createPage(String urlStr, String cont) throws MalformedURLException {
        URL url = new URL(urlStr);
        Page page1 = new Page(url, cont);
        page1.setParsedData(new ParsedData(new PaginaURL(page1)));
        return page1;
    }

}
