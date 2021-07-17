package achecrawler.target.classifier;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.parser.PaginaURL;

public class BodyRegexTargetClassifierTest {

    @Test
    public void test() throws MalformedURLException, TargetClassifierException {
        // given
        URL url = new URL("http://example.com");
        String cont = "<html><div><a href=\"http://j6im4v42ur6dpic3.onion/\">TorProject Archive</a></div></html>";
        Page page1 = new Page(url, cont);
        page1.setParsedData(new ParsedData(new PaginaURL(page1)));
        
        URL url2 = new URL("http://example.com");
        String cont2 = "<html><div><a href=\"http://example.com/\">Garlic Project Archive</a></div></html>";
        Page page2 = new Page(url2, cont2);
        page2.setParsedData(new ParsedData(new PaginaURL(page2)));
        
        List<String> patterns = asList(".*[a-zA-Z0-9]*.onion.*");
        BodyRegexTargetClassifier classifier = new BodyRegexTargetClassifier(patterns);
        
        // when
        TargetRelevance relevance1 = classifier.classify(page1);
        TargetRelevance relevance2 = classifier.classify(page2);
        
        // then
        assertThat(relevance1.isRelevant(), is(true));
        assertThat(relevance2.isRelevant(), is(false));
    }
    
    @Test
    public void shouldMatchHtmlFileWithMultipleLines() throws Exception {
        // given
        Path file = Paths.get(getClass().getResource("body_regex_classifier/test-file.html").toURI());
        
        URL url = new URL("https://en.wikipedia.org/wiki/Ebola_virus_disease");
        String content = new String(Files.readAllBytes(file));

        Page page1 = new Page(url, content);
        page1.setParsedData(new ParsedData(new PaginaURL(page1)));
        
        List<String> patterns = asList(".*ebola.*");
        BodyRegexTargetClassifier classifier = new BodyRegexTargetClassifier(patterns);
        
        // when
        TargetRelevance relevance1 = classifier.classify(page1);
        
        // then
        assertThat(relevance1.isRelevant(), is(true));
    }
    
}
