package achecrawler.crawler.cookies;

import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import java.util.List;
import org.apache.commons.lang.NullArgumentException;
import org.apache.http.client.CookieStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.fetcher.FetcherFactory;

class TestCookieUtils {

    Cookie cookie;

    @BeforeEach
    void setUp() {
        cookie = new Cookie("key1", "value1");
    }

    @Test
    void testApacheCookieNullInput() {
        assertThrows(NullArgumentException.class, () -> {
            CookieUtils.asApacheCookie(null);
        });
    }

    @Test
    void testApacheCookielInput() {
        cookie.setDomain(".slides.com");
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);
        assertEquals("key1", resultCookie.getName());
        assertEquals("value1", resultCookie.getValue());
        assertEquals(".slides.com", resultCookie.getDomain());
    }

    @Test
    void testOkHttpCookieNullInput() {
        assertThrows(NullArgumentException.class, () -> {
            CookieUtils.asOkhttp3Cookie(null);
        });
    }

    @Test
    void testOkHttpCookielInput() {
        cookie.setDomain(".slides.com");
        okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
        assertEquals("key1", resultCookie.name());
        assertEquals("value1", resultCookie.value());
        assertEquals("slides.com", resultCookie.domain());
    }

    @Test
    void testApacheCookielInputNullDomain() {
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);
        assertEquals("key1", resultCookie.getName());
        assertEquals("value1", resultCookie.getValue());
        assertTrue(resultCookie.getDomain() == null);
    }


    @Test
    void testApacheCookielInputNullKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            cookie.setName(null);
            org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);

            assertTrue(resultCookie.getName() == null);
            assertEquals("value1", resultCookie.getValue());
        });
    }

    @Test
    void testApacheCookielInputNullValue() {
        cookie.setValue(null);
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);

        assertEquals("key1", resultCookie.getName());
        assertTrue(resultCookie.getValue() == null);
    }

    @Test
    void testOkHttpCookielInputNullKey() {
        assertThrows(NullPointerException.class, () -> {
            cookie.setName(null);
            @SuppressWarnings("unused")
            okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
        });
    }

    @Test
    void testOkHttpInputNullValue() {
        assertThrows(NullPointerException.class, () -> {
            cookie.setValue(null);
            cookie.setName(null);
            @SuppressWarnings("unused")
            okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
        });
    }

    @Test
    void testAddCookiesNullCookies() {
        assertThrows(NullPointerException.class, () -> {
            CookieUtils.addCookies(null,
                    FetcherFactory.createFetcher(new HttpDownloaderConfig("okHttp")));
        });
    }

    @Test
    void testAddCookiesNullFetcher() {
        CookieUtils.addCookies(new HashMap<>(), null);
    }

    @Test
    void testCookieStore() {
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
        assertEquals("key1", resultList.get(0).getName());
        assertEquals("value1", resultList.get(0).getValue());
        assertEquals("slides.com", resultList.get(0).getDomain());
    }
}
