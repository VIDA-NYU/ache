package achecrawler.crawler.cookies;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.Cookie;
import okhttp3.HttpUrl;


class OkHttpCookieJarTest {

    private OkHttpCookieJar cookieJar;
    private HttpUrl url;
    private Cookie cookieRegular;
    private Cookie cookieExpired;
    private List<Cookie> cookies;

    @BeforeEach
    void createCookieJar() {
        cookieJar = new OkHttpCookieJar();
        url = HttpUrl.parse("https://domain.com/");
        cookieRegular = TestCookieCreator.createPersistentCookie(false);
        cookieExpired = TestCookieCreator.createExpiredCookie();
        cookies = asList(cookieRegular, cookieExpired);
    }

    @Test
    void saveFromResponse() throws Exception {
        // when
        cookieJar.saveFromResponse(url, cookies);
        // then
        assertThat(cookies).isEqualTo(cookieJar.getCookieJar().get(url.host()));
        cookieJar.clear();
    }

    @Test
    void loadOnlyNonExpiredCookiesForRequest() throws Exception {
        // when
        cookieJar.saveFromResponse(url, asList(cookieRegular, cookieExpired));
        List<Cookie> loadedCookies = cookieJar.loadForRequest(url);
        // then
        assertThat(loadedCookies).isEqualTo(asList(cookieRegular));
    }

    @Test
    void shouldLoadCookieForDifferentUrlFromSameDomain() throws Exception {
        // given
        HttpUrl url1 = HttpUrl.parse("https://domain.com/");
        HttpUrl url2 = HttpUrl.parse("https://domain.com/about");
        HttpUrl url3 = HttpUrl.parse("https://another-domain.com/");

        // when
        cookieJar.saveFromResponse(url1, asList(cookieRegular, cookieExpired));
        List<Cookie> cookiesFor2 = cookieJar.loadForRequest(url2);
        List<Cookie> cookiesFor3 = cookieJar.loadForRequest(url3);

        // then
        assertThat(cookiesFor2).isEqualTo(asList(cookieRegular));
        assertThat(cookiesFor3).isEmpty();
    }

    @Test
    void shouldLoadTopLevelCookieForSubdomains() throws Exception {
        // given
        HttpUrl topDomainUrl = HttpUrl.parse("https://domain.com/");
        HttpUrl subDomainUrl = HttpUrl.parse("https://sub.domain.com/");
        Cookie topLevelCookie = new Cookie.Builder()
                .domain("domain.com")
                .path("/")
                .name("name")
                .value("value")
                .expiresAt(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
                .httpOnly()
                .secure()
                .build();

        // when
        cookieJar.saveFromResponse(topDomainUrl, asList(topLevelCookie));

        List<Cookie> cookiesForTop = cookieJar.loadForRequest(topDomainUrl);
        List<Cookie> cookiesForSub = cookieJar.loadForRequest(subDomainUrl);

        // then
        assertThat(cookiesForSub).isEqualTo(asList(topLevelCookie));
        assertThat(cookiesForTop).isEqualTo(asList(topLevelCookie));
    }


    @Test
    void shouldNotLoadSubDomainCookieForTopLevelDomain() throws Exception {
        // given
        HttpUrl topDomainUrl = HttpUrl.parse("https://domain.com/");
        HttpUrl subDomainUrl = HttpUrl.parse("https://sub.domain.com/");
        Cookie topLevelCookie = new Cookie.Builder()
                .domain("sub.domain.com")
                .path("/")
                .name("name")
                .value("value")
                .expiresAt(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
                .httpOnly()
                .secure()
                .build();

        // when
        cookieJar.saveFromResponse(subDomainUrl, asList(topLevelCookie));

        List<Cookie> cookiesForTop = cookieJar.loadForRequest(topDomainUrl);
        List<Cookie> cookiesForSub = cookieJar.loadForRequest(subDomainUrl);

        // then
        assertThat(cookiesForTop).isEmpty();
        assertThat(cookiesForSub).isEqualTo(asList(topLevelCookie));
    }

    @Test
    void shouldNotLoadSubdomainCookiesForHigherLevelDomain() throws Exception {
        // given
        HttpUrl url1 = HttpUrl.parse("https://login.domain.com/");
        Cookie subdomainCookie = new Cookie.Builder()
                .domain("login.domain.com")
                .path("/")
                .name("name")
                .value("value")
                .expiresAt(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
                .httpOnly()
                .secure()
                .build();

        HttpUrl url2 = HttpUrl.parse("https://domain.com/");
        HttpUrl url3 = HttpUrl.parse("https://www.domain.com/");

        cookieJar.saveFromResponse(url1, asList(subdomainCookie));

        // when
        List<Cookie> cookiesFor2 = cookieJar.loadForRequest(url2);
        // then
        assertThat(cookiesFor2).isEmpty();

        // when
        List<Cookie> cookiesFor3 = cookieJar.loadForRequest(url3);
        // then
        assertThat(cookiesFor3).isEmpty();
    }

    @Test
    void update() throws Exception {
        // given
        String domain = "https://domain.com/";
        HttpUrl newUrl = HttpUrl.parse(domain);
        Cookie newCookie = TestCookieCreator.createPersistentCookie(false);
        Map<String, List<Cookie>> cookieHashMap = new HashMap<>();
        cookieHashMap.put(domain, asList(newCookie));

        // when
        cookieJar.update(cookieHashMap);

        // then
        assertThat(cookieJar.getCookieJar().get(newUrl.host())).isEqualTo(asList(newCookie));
    }

}
