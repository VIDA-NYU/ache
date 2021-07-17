package achecrawler.crawler.async.fetcher;

import achecrawler.crawler.crawlercommons.fetcher.http.CookieStoreProvider;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.util.List;
import java.util.Map;

public class ConcurrentCookieStoreProvider implements CookieStoreProvider {

    private final CookieStore cookieStore;

    public ConcurrentCookieStoreProvider(ConcurrentCookieJar storeStore) {
        if (storeStore == null) {
            throw new IllegalArgumentException("Cookie store cannot be null");
        }
        this.cookieStore = storeStore;
    }

    @Override
    public CookieStore get() {
        return cookieStore;
    }

    /**
     * Update cookie store with a map of cookies.
     *  - key: domain name
     *  - value: List of cookies associated with that domain name
     * @param cookies the cookies to be stored
     * @throws NullPointerException if the cookies argument is null
     */
    public void updateCookieStore(Map<String, List<Cookie>> cookies) {
        if (cookies == null) {
            throw new NullPointerException("Cookies argument can not be null");
        }
        for (List<Cookie> listOfCookies : cookies.values()) {
            for(Cookie cookie: listOfCookies) {
                cookieStore.addCookie(cookie);
            }
        }
    }
}
