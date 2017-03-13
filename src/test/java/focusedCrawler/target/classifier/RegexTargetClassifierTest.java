package focusedCrawler.target.classifier;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.PaginaURL;

public class RegexTargetClassifierTest {

    @Test
    public void testRegexClassifierMatcheConfig1() throws TargetClassifierException, IOException {
        // given
        String path = ClassifierFactoryTest.class.getResource("regex_classifier_config/").getPath();
        
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
        assertThat(classifier.classify(page1).isRelevant(), is(false));
        assertThat(classifier.classify(page2).isRelevant(), is(true));
        assertThat(classifier.classify(page3).isRelevant(), is(true));
        assertThat(classifier.classify(page4).isRelevant(), is(false));
    }

    private Page createPage(String urlStr, String cont) throws MalformedURLException {
        URL url = new URL(urlStr);
        Page page1 = new Page(url, cont);
        page1.setParsedData(new ParsedData(new PaginaURL(page1)));
        return page1;
    }

}
