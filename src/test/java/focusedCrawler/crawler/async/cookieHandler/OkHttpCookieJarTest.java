package focusedCrawler.crawler.async.cookieHandler;

import okhttp3.*;
import okhttp3.Cookie;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;


public class OkHttpCookieJarTest {

    private static OkHttpCookieJar cookieJar;
    private HttpUrl url;
    private Cookie cookie_regular;
    private Cookie cookie_expired;
    private List<Cookie> cookies = new ArrayList<>();

    @Before
    public void createCookieJar(){
         cookieJar = new OkHttpCookieJar();
         url =  HttpUrl.parse("https://domain.com/");
         cookie_regular =  TestCookieCreator.createPersistentCookie(false);
         cookie_expired = TestCookieCreator.createExpiredCookie();
         cookies.add(cookie_regular);
         cookies.add(cookie_expired);
    }

    @Test
    public void saveFromResponse() throws Exception{
        cookieJar.saveFromResponse(url,cookies);
        assertEquals(OkHttpCookieJar.getCookieJar().get(url), cookies);
        cookieJar.clear();
    }

    @Test
    public void loadForRequest() throws Exception {
        cookieJar.saveFromResponse(url,cookies);
        List<Cookie> response = cookieJar.loadForRequest(url);
        cookies.remove(cookie_expired);
        assertEquals(response, cookies);
        cookieJar.clear();
    }

    @Test
    public void update() throws Exception {
        String domain = "https://domain.com/";
        HttpUrl newUrl = HttpUrl.parse(domain);
        Cookie newCookie = TestCookieCreator.createPersistentCookie(false);
        List<Cookie> newCookieList = Arrays.asList(newCookie);

        HashMap<String,List<Cookie>> cookieHashMap = new HashMap<>();
        cookieHashMap.put(domain, newCookieList);
        OkHttpCookieJar.update(cookieHashMap);

        assertEquals(newCookieList, OkHttpCookieJar.getCookieJar().get(newUrl));
    }

}