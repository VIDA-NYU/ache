package achecrawler.util;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkFilterTest {

    @Test
    void regexMatcherTestCase1() {
        // given
        List<String> urlRegexPatterns = Arrays.asList(
            ".*/thread/.*",
            ".*/archive/index.php/t.*",
            "www\\.mydomain\\.com.*",
            "www\\.somedomain\\.com/forum/.*"
        );
        RegexMatcher matcher = RegexMatcher.fromWhitelist(urlRegexPatterns);
        RegexMatcher reverseMatcher = RegexMatcher.fromBlacklist(urlRegexPatterns);

        List<String> urlsThatMatch = Arrays.asList(
            "http://some.domain.com/thread/something",
            "http://www.someforum.net/forum/archive/index.php/t-285330.html",
            "www.mydomain.com/asdf",
            "www.somedomain.com/forum/asdf.html"
        );

        List<String> urlsThatDontMatch = Arrays.asList(
            "http://some.domain.com/something",
            "http://www.otherforum.net/calgunforum/t-285330.html",
            "www.testdomain.com/asdf",
            "www.somedomain.com/somthingelse/asdf.html"
        );

        // then
        for (String url : urlsThatMatch) {
            assertThat(matcher.matches(url)).as(url).isTrue();
            assertThat(reverseMatcher.matches(url)).as(url).isFalse();
        }

        for (String url : urlsThatDontMatch) {
            assertThat(matcher.matches(url)).as(url).isFalse();
            assertThat(reverseMatcher.matches(url)).as(url).isTrue();
        }
    }

    @Test
    void wildcardMatherTestCase1() {
        // given
        List<String> urlRegexPatterns = Arrays.asList(
            "*/thread/*",
            "*/archive/index.php/t*",
            "www.mydomain.com*",
            "www.somedomain.com/forum/*"
        );
        TextMatcher matcher = WildcardMatcher.fromWhitelist(urlRegexPatterns);
        TextMatcher reverseMatcher = WildcardMatcher.fromBlacklist(urlRegexPatterns);

        List<String> urlsThatMatch = Arrays.asList(
            "http://some.domain.com/thread/something",
            "http://www.someforum.net/forum/archive/index.php/t-285330.html",
            "www.mydomain.com/asdf",
            "www.somedomain.com/forum/asdf.html"
        );

        List<String> urlsThatDontMatch = Arrays.asList(
            "http://some.domain.com/something",
            "http://www.otherforum.net/calgunforum/t-285330.html",
            "www.testdomain.com/asdf",
            "www.somedomain.com/somthingelse/asdf.html"
        );

        for (String url : urlsThatMatch) {
            assertThat(matcher.matches(url)).as(url).isTrue();
            assertThat(reverseMatcher.matches(url)).as(url).isFalse();
        }

        for (String url : urlsThatDontMatch) {
            assertThat(matcher.matches(url)).as(url).isFalse();
            assertThat(reverseMatcher.matches(url)).as(url).isTrue();
        }
    }

    @Test
    void wildcardMatherTestCase2() {
        // given
        List<String> urlRegexPatterns = Arrays.asList(
            "http://a.com/*c"
        );
        TextMatcher matcher = WildcardMatcher.fromWhitelist(urlRegexPatterns);
        TextMatcher reverseMatcher = WildcardMatcher.fromBlacklist(urlRegexPatterns);

        List<String> urlsThatMatch = Arrays.asList(
            "http://a.com/c",
            "http://a.com/cc",
            "http://a.com/ccc"
        );

        List<String> urlsThatDontMatch = Arrays.asList(
            "http://a.com/",
            "http://a.com/cd",
            "http://a.com/ccd"
        );

        for (String url : urlsThatMatch) {
            assertThat(matcher.matches(url)).as(url).isTrue();
            assertThat(reverseMatcher.matches(url)).as(url).isFalse();
        }

        for (String url : urlsThatDontMatch) {
            assertThat(matcher.matches(url)).as(url).isFalse();
            assertThat(reverseMatcher.matches(url)).as(url).isTrue();
        }
    }

    @Test
    void testComposedLinkFilter() {
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

        // then
        shouldMatchAll(linkfilter, urlsThatMatch);
        shouldMatchNone(linkfilter, urlsThatDoesntMatch);
    }


    @Test
    void shouldAcceptAllUrlsIfBlackListAndWhiteListAreEmpty() {
        // given
        List<String> whitelistRegexes = Arrays.asList();
        List<String> blacklistRegexes = Arrays.asList();

        LinkFilter linkfilter = new LinkFilter.Builder()
                .withWhitelistRegexes(whitelistRegexes)
                .withBlacklistRegexes(blacklistRegexes)
                .build();

        List<String> urlsThatMatch = Arrays.asList(
            "http://mydomain.com/show_thread.php?t=123",
            "http://www.mydomain.com/1234_some-url#qwer",
            "http://www.mydomain.com/yeah-yeah-yeah.1234.html",
            "http://www.mydomain.com/qwer.1234.html"
        );

        // then
        shouldMatchAll(linkfilter, urlsThatMatch);
    }


    @Test
    void shouldLoadFiltersFromYamlFile() {
        // given
        String path = LinkFilterTest.class.getResource("link_filters.yml").getPath();

        LinkFilter linkfilter = new LinkFilter.Builder()
                .fromYamlFile(path)
                .build();

        List<String> urlsThatMatch = Arrays.asList(
            "http://www.example1.com/allowed001.html",
            "http://www.example1.com/allowed-test.html",
            "http://www.example2.com/allowed002.html"
        );

        List<String> urlsThatDoesntMatch = Arrays.asList(
            "http://www.example1.com/disallowed001.html",
            "http://www.example1.com/disallowed002test.html",
            "http://www.example2.com/disallowed002.html",
            "http://www.example2.com/allowed-but-not.html"
        );

        // then
        shouldMatchAll(linkfilter, urlsThatMatch);
        shouldMatchNone(linkfilter, urlsThatDoesntMatch);
    }

    @Test
    void shouldLoadFiltersFromYamlFileBackpage() {
        // given
        String path = LinkFilterTest.class.getResource("backpage_link_filters.yml").getPath();
        LinkFilter linkfilter = new LinkFilter.Builder()
                .fromYamlFile(path)
                .build();

        List<String> urlsThatMatch = Arrays.asList(
            "http://milwaukee.backpage.com/",
            "http://bowlinggreen.backpage.com/SportsEquipForSale/",
            "http://newyork.backpage.com/SportsEquipForSale/",
            "http://bowlinggreen.backpage.com/SportsEquipForSale/something/1234"
        );

        List<String> urlsThatDoesntMatch = Arrays.asList(
            "http://milwaukee.backpage.com/ComputerJobs/",
            "http://arizona.backpage.com/buy-sell-trade"
        );

        // then
        shouldMatchAll(linkfilter, urlsThatMatch);
        shouldMatchNone(linkfilter, urlsThatDoesntMatch);
    }

    private void shouldMatchNone(LinkFilter linkfilter, List<String> urlsThatDoesntMatch) {
        for (String url : urlsThatDoesntMatch) {
            // when
            boolean matched = linkfilter.accept(url);
            // then
            assertThat(matched).as(url).isFalse();
        }
    }

    private void shouldMatchAll(LinkFilter linkfilter, List<String> urlsThatMatch) {
        for (String url : urlsThatMatch) {
            // when
            boolean matched = linkfilter.accept(url);
            // then
            assertThat(matched).as(url).isTrue();
        }
    }

}
