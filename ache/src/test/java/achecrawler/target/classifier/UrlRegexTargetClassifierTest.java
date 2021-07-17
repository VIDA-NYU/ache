package achecrawler.target.classifier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import achecrawler.target.model.Page;
import achecrawler.util.LinkFilter;

public class UrlRegexTargetClassifierTest {

    @Test
    public void shouldClassifyPagesBasedOnListOfUrlRegexes() throws Exception {
        // given
        List<String> urlPatterns = Arrays.asList(
            ".*/thread/.*",
            ".*/archive/index.php/t.*",
            "https?://www\\.mydomain\\.com.*",
            "https?://www\\.somedomain\\.com/forum/.*"
        );
        UrlRegexTargetClassifier classifier = UrlRegexTargetClassifier.fromRegularExpressions(urlPatterns);
        
        List<String> urlsThatMatch = Arrays.asList(
            "http://some.domain.com/thread/something",
            "http://www.someforum.net/forum/archive/index.php/t-1234.html",
            "http://www.mydomain.com/asdf",
            "http://www.somedomain.com/forum/asdf.html"
        );
        
        List<String> urlsThatDoesntMatch = Arrays.asList(
            "http://some.domain.com/something",
            "http://www.otherforum.net/someforum/t-285330.html",
            "http://www.testdomain.com/asdf",
            "http://www.somedomain.com/somthingelse/asdf.html"
        );
        
        List<Page> pagesThatMatch = asPages(urlsThatMatch);
        List<Page> pagesThatDoesntMatch = asPages(urlsThatDoesntMatch);
        
        
        for (Page page : pagesThatMatch) {
            // when
            TargetRelevance relevance = classifier.classify(page);
            // then
            assertThat(page.toString(), relevance.isRelevant(), is(true));
            assertThat(page.toString(), relevance.getRelevance(), is(1d));
        }
        
        for (Page page : pagesThatDoesntMatch) {
            // when
            TargetRelevance relevance = classifier.classify(page);
            // then
            assertThat(page.toString(), relevance.isRelevant(), is(false));
            assertThat(page.toString(), relevance.getRelevance(), is(0d));
        }
    }
    
    @Test
    public void shouldClassifyPagesBasedOnTheUrlWhiteListAndBlackLists() throws Exception {
        // given
        
        List<String> whitelistRegexes = Arrays.asList(
            "http[s]?://.*\\.?mydomain\\.com.*" // allow only links from mydomain.com
        );
        
        List<String> blacklistRegexes = Arrays.asList(
            ".*/new_reply\\.php.*", // disallow links with path "/new_reply.php"
            ".*/new_user\\.php.*"   // disallow links with path "/new_user.php"
        );
        
        LinkFilter linkfilter = new LinkFilter.Builder()
                .withWhitelistRegexes(whitelistRegexes)
                .withBlacklistRegexes(blacklistRegexes)
                .build();
        
        UrlRegexTargetClassifier classifier = new UrlRegexTargetClassifier(linkfilter);
        
        List<String> urlsThatMatch = Arrays.asList(
            "http://mydomain.com/show_thread.php?t=123",
            "http://www.mydomain.com/1234_some-url#qwer",
            "http://www.mydomain.com/yeah-yeah-yeah.1234.html",
            "http://www.mydomain.com/qwer.1234.html"
        );
        
        List<String> urlsThatDoesntMatch = Arrays.asList(
            "http://www.mydomain.com/new_reply.php?t=123&u=456",
            "http://www.mydomain.com/new_user.php?t=123&u=456",
            "http://www.otherdomain.com/calgunforum/t-285330.html",
            "http://www.someotherdomain.com/forum/t-285330.html"
        );
        
        
        List<Page> pagesThatMatch = asPages(urlsThatMatch);
        List<Page> pagesThatDoesntMatch = asPages(urlsThatDoesntMatch);
        
        
        for (Page page : pagesThatMatch) {
            // when
            TargetRelevance relevance = classifier.classify(page);
            // then
            assertThat(page.toString(), relevance.isRelevant(), is(true));
            assertThat(page.toString(), relevance.getRelevance(), is(1d));
        }
        
        for (Page page : pagesThatDoesntMatch) {
            // when
            TargetRelevance relevance = classifier.classify(page);
            // then
            assertThat(page.toString(), relevance.isRelevant(), is(false));
            assertThat(page.toString(), relevance.getRelevance(), is(0d));
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
