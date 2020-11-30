package achecrawler.crawler.cookies;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.apache.commons.lang.NullArgumentException;
import org.junit.Before;
import org.junit.Test;

import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.fetcher.FetcherFactory;

public class TestCookieUtils {

    Cookie cookie;

    @Before
    public void setUp() throws Exception {
        cookie = new Cookie("key1", "value1");
    }

    @Test(expected = NullArgumentException.class)
    public void testApacheCookieNullInput() {
        CookieUtils.asApacheCookie(null);
    }

    @Test
    public void testApacheCookielInput() {
        cookie.setDomain(".slides.com");
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);
        assertTrue(resultCookie.getName().equals("key1"));
        assertTrue(resultCookie.getValue().equals("value1"));
        assertTrue(resultCookie.getDomain().equals(".slides.com"));
    }

    @Test(expected = NullArgumentException.class)
    public void testOkHttpCookieNullInput() {
        CookieUtils.asOkhttp3Cookie(null);
    }

    @Test
    public void testOkHttpCookielInput() {
        cookie.setDomain(".slides.com");
        okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
        assertTrue(resultCookie.name().equals("key1"));
        assertTrue(resultCookie.value().equals("value1"));
        assertTrue(resultCookie.domain().equals("slides.com"));
    }

    @Test
    public void testApacheCookielInputNullDomain() {
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);
        assertTrue(resultCookie.getName().equals("key1"));
        assertTrue(resultCookie.getValue().equals("value1"));
        assertTrue(resultCookie.getDomain() == null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testApacheCookielInputNullKey() {
        cookie.setName(null);
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);

        assertTrue(resultCookie.getName() == null);
        assertTrue(resultCookie.getValue().equals("value1"));
    }

    @Test
    public void testApacheCookielInputNullValue() {
        cookie.setValue(null);
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);

        assertTrue(resultCookie.getName().equals("key1"));
        assertTrue(resultCookie.getValue() == null);
    }

    @Test(expected = NullPointerException.class)
    public void testOkHttpCookielInputNullKey() {
        cookie.setName(null);
        @SuppressWarnings("unused")
        okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
    }

    @Test(expected = NullPointerException.class)
    public void testOkHttpInputNullValue() {
        cookie.setValue(null);
        cookie.setName(null);
        @SuppressWarnings("unused")
        okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
    }

    @Test(expected = NullPointerException.class)
    public void testAddCookiesNullCookies() {
        CookieUtils.addCookies(null,
                FetcherFactory.createFetcher(new HttpDownloaderConfig("okHttp")));
    }

    @Test
    public void testAddCookiesNullFetcher() {
        CookieUtils.addCookies(new HashMap<>(), null);
    }
}
