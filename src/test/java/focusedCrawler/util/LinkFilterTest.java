package focusedCrawler.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import focusedCrawler.util.LinkFilter.LinkBlackList;
import focusedCrawler.util.LinkFilter.LinkMatcher;
import focusedCrawler.util.LinkFilter.LinkWhiteList;

public class LinkFilterTest {

    @Test
    public void matchShoudReturnTrueIfStringMatchUrlPatterns() {
        // given
        List<String> urlRegexPatterns = Arrays.asList(
            ".*/thread/.*",
            ".*/archive/index.php/t.*",
            "www\\.mydomain\\.com.*",
            "www\\.somedomain\\.com/forum/.*"
        );
        LinkMatcher matcher = new LinkMatcher(urlRegexPatterns);
        
        List<String> urlsThatMatch = Arrays.asList(
            "http://some.domain.com/thread/something",
            "http://www.someforum.net/forum/archive/index.php/t-285330.html",
            "www.mydomain.com/asdf",
            "www.somedomain.com/forum/asdf.html"
        );
        
        List<String> urlsThatDoesntMatch = Arrays.asList(
            "http://some.domain.com/something",
            "http://www.otherforum.net/calgunforum/t-285330.html",
            "www.testdomain.com/asdf",
            "www.somedomain.com/somthingelse/asdf.html"
        );
        
        for (String url : urlsThatMatch) {
            // when
            boolean matched = matcher.matches(url);
            // then
            assertThat(url, matched, is(true));
        }
        
        for (String url : urlsThatDoesntMatch) {
            // when
            boolean matched = matcher.matches(url);
            // then
            assertThat(url, matched, is(false));
        }
        
    }
    
    @Test
    public void testComposedLinkFilter() {
        // given
        List<String> whitelistRegexes = Arrays.asList(
            "http[s]?://.*\\.?mydomain\\.com.*" // allow only links from mydomain.com
        );
        
        List<String> blacklistRegexes = Arrays.asList(
            ".*/new_reply\\.php.*", // disallow links with path "/new_reply.php"
            ".*/new_user\\.php.*"   // disallow links with path "/new_user.php"
        );
        
        LinkFilter linkfilter = new LinkFilter(new LinkWhiteList(whitelistRegexes),
                                               new LinkBlackList(blacklistRegexes));
        
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
        
        // when
        for (String url : urlsThatMatch) {
            // when
            boolean matched = linkfilter.accept(url);
            // then
            assertThat(url, matched, is(true));
        }
        
        for (String url : urlsThatDoesntMatch) {
            // when
            boolean matched = linkfilter.accept(url);
            // then
            assertThat(url, matched, is(false));
        }
    }
    
    
    @Test
    public void shouldAcceptAllUrlsIfBlackListAndWhiteListAreEmpty() {
        // given
        List<String> whitelistRegexes = Arrays.asList();
        List<String> blacklistRegexes = Arrays.asList();
        
        LinkFilter linkfilter = new LinkFilter(new LinkWhiteList(whitelistRegexes),
                                               new LinkBlackList(blacklistRegexes));
        
        List<String> urlsThatMatch = Arrays.asList(
            "http://mydomain.com/show_thread.php?t=123",
            "http://www.mydomain.com/1234_some-url#qwer",
            "http://www.mydomain.com/yeah-yeah-yeah.1234.html",
            "http://www.mydomain.com/qwer.1234.html"
        );
        
        // when
        for (String url : urlsThatMatch) {
            // when
            boolean matched = linkfilter.accept(url);
            // then
            assertThat(url, matched, is(true));
        }
    }

}
