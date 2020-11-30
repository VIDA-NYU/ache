package achecrawler.crawler.cookies;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.http.impl.cookie.BasicClientCookie;

import achecrawler.crawler.async.fetcher.OkHttpFetcher;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import okhttp3.Cookie.Builder;

/**
 * Helper methods related to cookies
 *
 */
public class CookieUtils {

    /**
     * Builds a new okhttp3 cookie from given cookie values
     * 
     * @param cookie
     * @return okhttp3 cookie
     * @throws NullArgumentException if argument cookie is null;
     */
    public static okhttp3.Cookie asOkhttp3Cookie(Cookie cookie) {
        if (cookie == null) {
            throw new NullArgumentException("cookie can't be null");
        }
        Builder builder = new Builder();
        builder.name(cookie.getName());
        builder.value(cookie.getValue());
        builder.expiresAt(cookie.getExpiresAt());
        if (cookie.getDomain() != null && cookie.getDomain().startsWith(".")) {
            cookie.setDomain(cookie.getDomain().replaceFirst(".", ""));
        }
        if (cookie.getDomain() != null) {
            builder.domain(cookie.getDomain());
        } else {
            builder.domain("");
        }

        builder.path(cookie.getPath());
        if (cookie.isSecure()) {
            builder.secure();
        }
        if (cookie.isHttpOnly()) {
            builder.httpOnly();
        }
        return builder.build();
    }

    /**
     * Builds a new Apache cookie from given cookie values
     * 
     * @param cookie
     * @return apache http cookie
     * @throws NullArgumentException if argument cookie is null;
     */
    public static org.apache.http.cookie.Cookie asApacheCookie(Cookie cookie) {
        if (cookie == null) {
            throw new NullArgumentException("cookie");
        }
        BasicClientCookie apacheCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        apacheCookie.setDomain(cookie.getDomain());
        apacheCookie.setExpiryDate(new Date(cookie.getExpiresAt()));
        apacheCookie.setPath(cookie.getPath());
        apacheCookie.setSecure(cookie.isSecure());
        return apacheCookie;
    }

    /**
     * Adds cookies to the respective fetcher
     * 
     * @param cookies
     * @param baseFetcher
     * @throws NullPointerException if cookie is null
     */
    public static void addCookies(Map<String, List<Cookie>> cookies, BaseFetcher baseFetcher) {
        if (cookies == null) {
            throw new NullPointerException("Cookies argument is null");
        }

        Map<String, List<Cookie>> validCookies = cleanCookies(cookies);
        if (baseFetcher instanceof SimpleHttpFetcher) {
            ((SimpleHttpFetcher) baseFetcher).updateCookieStore(asApacheCookies(validCookies));
        } else if (baseFetcher instanceof OkHttpFetcher) {
            ((OkHttpFetcher) baseFetcher).updateCookies(asOkhttp3Cookies(cookies));
        }
    }

    private static Map<String, List<okhttp3.Cookie>> asOkhttp3Cookies(
            Map<String, List<Cookie>> cookies) {
        Map<String, List<okhttp3.Cookie>> tempCookies = new HashMap<>();
        for (String key : cookies.keySet()) {
            List<okhttp3.Cookie> newCookies = new ArrayList<>();
            for (Cookie c : cookies.get(key)) {
                newCookies.add(CookieUtils.asOkhttp3Cookie(c));
            }
            tempCookies.put(key, newCookies);
        }
        return tempCookies;
    }

    private static Map<String, List<org.apache.http.cookie.Cookie>> asApacheCookies(
            Map<String, List<Cookie>> cookies) {
        Map<String, List<org.apache.http.cookie.Cookie>> tempCookies = new HashMap<>();
        for (String key : cookies.keySet()) {
            List<org.apache.http.cookie.Cookie> newCookies = new ArrayList<>();
            for (Cookie c : cookies.get(key)) {
                newCookies.add(asApacheCookie(c));
            }
            tempCookies.put(key, newCookies);
        }
        return tempCookies;
    }

    private static Map<String, List<Cookie>> cleanCookies(Map<String, List<Cookie>> cookies) {
        Map<String, List<Cookie>> validCookiesMap = new HashMap<>();
        for (String url : cookies.keySet()) {
            List<Cookie> validCookies = new ArrayList<>();
            for (Cookie c : cookies.get(url)) {
                String cookieDomain = c.getDomain();
                if (cookieDomain != null) {
                    if (cookieDomain.startsWith(".")) {
                        c.setDomain(cookieDomain.replaceFirst(".", ""));
                    }
                    validCookies.add(c);
                }
            }
            validCookiesMap.put(url, validCookies);
        }
        return validCookiesMap;
    }

}
