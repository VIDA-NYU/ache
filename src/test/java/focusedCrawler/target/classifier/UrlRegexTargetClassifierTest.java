package focusedCrawler.target.classifier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import focusedCrawler.util.Page;
import focusedCrawler.util.LinkFilter.LinkMatcher;

public class UrlRegexTargetClassifierTest {

    @Test
    public void shouldClassifyPagesBasedOnTheUrlRegexes() throws Exception {
        // given
        LinkMatcher matcher = new LinkMatcher(Arrays.asList(
            ".*/thread/.*",
            ".*/archive/index.php/t.*",
            "https?://www\\.mydomain\\.com.*",
            "https?://www\\.somedomain\\.com/forum/.*"
        ));
        UrlRegexTargetClassifier classifier = new UrlRegexTargetClassifier(matcher);
        
        List<String> urlsThatMatch = Arrays.asList(
            "http://some.domain.com/thread/something",
            "http://www.someforum.net/forum/archive/index.php/t-1234.html",
            "http://www.mydomain.com/asdf",
            "http://www.somedomain.com/forum/asdf.html"
        );
        
        List<String> urlsThatDoesntMatch = Arrays.asList(
            "http://some.domain.com/something",
            "http://www.otherforum.net/calgunforum/t-285330.html",
            "http://www.testdomain.com/asdf",
            "http://www.somedomain.com/somthingelse/asdf.html"
        );
        
        List<Page> pagesThatMatch = asPages(urlsThatMatch);
        List<Page> pagesThatDoesntMatch = asPages(urlsThatDoesntMatch);
        
        
        for (Page page : pagesThatMatch) {
            // when
            boolean isRelevant = classifier.classify(page);;
            // then
            assertThat(page.toString(), isRelevant, is(true));
            assertThat(page.toString(), classifier.distributionForInstance(page)[0], is(1d));
        }
        
        for (Page page : pagesThatDoesntMatch) {
            // when
            boolean isRelevant = classifier.classify(page);;
            // then
            assertThat(page.toString(), isRelevant, is(false));
            assertThat(page.toString(), classifier.distributionForInstance(page)[0], is(0d));
        }
    }

    private List<Page> asPages(List<String> urls) throws MalformedURLException {
        List<Page> pages = new  ArrayList<Page>();
        for (String url : urls) {
            Page page = new Page(new URL(url), "");
            pages.add(page);
        }
        return pages;
    }

}
