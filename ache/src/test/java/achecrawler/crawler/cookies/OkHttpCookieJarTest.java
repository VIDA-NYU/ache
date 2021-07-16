package achecrawler.crawler.cookies;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Cookie;
import okhttp3.HttpUrl;


public class OkHttpCookieJarTest {

    private OkHttpCookieJar cookieJar;
    private HttpUrl url;
    private Cookie cookieRegular;
    private Cookie cookieExpired;
    private List<Cookie> cookies;

    @Before
    public void createCookieJar() {
        cookieJar = new OkHttpCookieJar();
        url = HttpUrl.parse("https://domain.com/");
        cookieRegular = TestCookieCreator.createPersistentCookie(false);
        cookieExpired = TestCookieCreator.createExpiredCookie();
        cookies = asList(cookieRegular, cookieExpired);
    }

    @Test
    public void saveFromResponse() throws Exception {
        // when
        cookieJar.saveFromResponse(url, cookies);
        // then
        assertEquals(cookieJar.getCookieJar().get(url.host()), cookies);
        cookieJar.clear();
    }

    @Test
    public void loadOnlyNonExpiredCookiesForRequest() throws Exception {
        // when
        cookieJar.saveFromResponse(url, asList(cookieRegular, cookieExpired));
        List<Cookie> loadedCookies = cookieJar.loadForRequest(url);
        // then
        assertThat(loadedCookies, is(asList(cookieRegular)));
    }

    @Test
    public void shouldLoadCookieForDifferentUrlFromSameDomain() throws Exception {
        // given
        HttpUrl url1 = HttpUrl.parse("https://domain.com/");
        HttpUrl url2 = HttpUrl.parse("https://domain.com/about");
        HttpUrl url3 = HttpUrl.parse("https://another-domain.com/");

        // when
        cookieJar.saveFromResponse(url1, asList(cookieRegular, cookieExpired));
        List<Cookie> cookiesFor2 = cookieJar.loadForRequest(url2);
        List<Cookie> cookiesFor3 = cookieJar.loadForRequest(url3);

        // then
        assertThat(cookiesFor2, is(asList(cookieRegular)));
        assertThat(cookiesFor3, is(empty()));
    }

    @Test
    public void shouldLoadTopLevelCookieForSubdomains() throws Exception {
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
        assertThat(cookiesForSub, is(asList(topLevelCookie)));
        assertThat(cookiesForTop, is(asList(topLevelCookie)));
    }


    @Test
    public void shouldNotLoadSubDomainCookieForTopLevelDomain() throws Exception {
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
        assertThat(cookiesForTop, is(empty()));
        assertThat(cookiesForSub, is(asList(topLevelCookie)));
    }

    @Test
    public void shouldNotLoadSubdomainCookiesForHigherLevelDomain() throws Exception {
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
        assertThat(cookiesFor2, is(empty()));

        // when
        List<Cookie> cookiesFor3 = cookieJar.loadForRequest(url3);
        // then
        assertThat(cookiesFor3, is(empty()));
    }

    @Test
    public void update() throws Exception {
        // given
        String domain = "https://domain.com/";
        HttpUrl newUrl = HttpUrl.parse(domain);
        Cookie newCookie = TestCookieCreator.createPersistentCookie(false);
        Map<String, List<Cookie>> cookieHashMap = new HashMap<>();
        cookieHashMap.put(domain, asList(newCookie));

        // when
        cookieJar.update(cookieHashMap);

        // then
        assertEquals(asList(newCookie), cookieJar.getCookieJar().get(newUrl.host()));
    }

}
