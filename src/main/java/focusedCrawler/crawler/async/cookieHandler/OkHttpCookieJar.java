package focusedCrawler.crawler.async.cookieHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class OkHttpCookieJar implements CookieJar,CookieHandler {

    private ConcurrentHashMap<HttpUrl,List<Cookie>> cookieJar;

    public OkHttpCookieJar() {
        this.cookieJar = new ConcurrentHashMap();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieJar.put(url,cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> validCookies = new ArrayList<>();

        for (Iterator<Cookie> it = cookieJar.get(url).iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();
            if (isCookieExpired(currentCookie)) {
                it.remove();
            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

        return validCookies;
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    public void clear() {
        cookieJar.clear();
    }
}