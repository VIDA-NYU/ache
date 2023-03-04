package achecrawler.crawler.cookies;

import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;
import org.apache.commons.lang.NullArgumentException;
import org.apache.http.client.CookieStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.fetcher.FetcherFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TestCookieUtils {

    Cookie cookie;

    @BeforeEach
    void setUp() {
        cookie = new Cookie("key1", "value1");
    }

    @Test
    void testApacheCookieNullInput() {
        assertThatExceptionOfType(NullArgumentException.class).isThrownBy(() -> {
            CookieUtils.asApacheCookie(null);
        });
    }

    @Test
    void testApacheCookielInput() {
        cookie.setDomain(".slides.com");
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);
        assertThat(resultCookie.getName()).isEqualTo("key1");
        assertThat(resultCookie.getValue()).isEqualTo("value1");
        assertThat(resultCookie.getDomain()).isEqualTo(".slides.com");
    }

    @Test
    void testOkHttpCookieNullInput() {
        assertThatExceptionOfType(NullArgumentException.class).isThrownBy(() -> {
            CookieUtils.asOkhttp3Cookie(null);
        });
    }

    @Test
    void testOkHttpCookielInput() {
        cookie.setDomain(".slides.com");
        okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
        assertThat(resultCookie.name()).isEqualTo("key1");
        assertThat(resultCookie.value()).isEqualTo("value1");
        assertThat(resultCookie.domain()).isEqualTo("slides.com");
    }

    @Test
    void testApacheCookielInputNullDomain() {
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);
        assertThat(resultCookie.getName()).isEqualTo("key1");
        assertThat(resultCookie.getValue()).isEqualTo("value1");
        assertThat(resultCookie.getDomain()).isNull();
    }


    @Test
    void testApacheCookielInputNullKey() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            cookie.setName(null);
            org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);

            assertThat(resultCookie.getName()).isNull();
            assertThat("value1").isEqualTo(resultCookie.getValue());
        });
    }

    @Test
    void testApacheCookielInputNullValue() {
        cookie.setValue(null);
        org.apache.http.cookie.Cookie resultCookie = CookieUtils.asApacheCookie(cookie);

        assertThat(resultCookie.getName()).isEqualTo("key1");
        assertThat(resultCookie.getValue() == null).isTrue();
    }

    @Test
    void testOkHttpCookielInputNullKey() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            cookie.setName(null);
            @SuppressWarnings("unused")
            okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
        });
    }

    @Test
    void testOkHttpInputNullValue() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            cookie.setValue(null);
            cookie.setName(null);
            @SuppressWarnings("unused")
            okhttp3.Cookie resultCookie = CookieUtils.asOkhttp3Cookie(cookie);
        });
    }

    @Test
    void testAddCookiesNullCookies() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
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
        assertThat(resultList.get(0).getName()).isEqualTo("key1");
        assertThat(resultList.get(0).getValue()).isEqualTo("value1");
        assertThat(resultList.get(0).getDomain()).isEqualTo("slides.com");
    }
}
