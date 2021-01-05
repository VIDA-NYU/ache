package achecrawler.crawler.cookies;

import static org.junit.Assert.*;

import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import org.apache.commons.lang.NullArgumentException;
import org.apache.http.client.CookieStore;
import org.junit.Before;
import org.junit.Test;

import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.fetcher.FetcherFactory;

public class TestCookieUtils {

    Cookie cookie;

    @Before
    public void setUp() {
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

    @Test
    public void testCookieStore() {
        Cookie cookie = new Cookie("key1", "value1");
        cookie.setDomain(".slides.com");

        HashMap<String, List<Cookie>> map = new HashMap<>();
        List<Cookie> listOfCookies = new ArrayList<>();
        listOfCookies.add(cookie);
        map.put("www.slides.com", listOfCookies);

        SimpleHttpFetcher baseFetcher =
            FetcherFactory.createSimpleHttpFetcher(new HttpDownloaderConfig());
        CookieUtils.addCookies(map, baseFetcher);

        CookieStore globalCookieStore = baseFetcher.getCookieStoreProvider().get();
        List<org.apache.http.cookie.Cookie> resultList = globalCookieStore.getCookies();
        assertTrue(resultList.get(0).getName().equals("key1"));
        assertTrue(resultList.get(0).getValue().equals("value1"));
        assertTrue(resultList.get(0).getDomain().equals("slides.com"));
    }
}
