package achecrawler.crawler.cookies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class OkHttpCookieJar implements CookieJar {

    private static Logger logger = LoggerFactory.getLogger(OkHttpCookieJar.class);

    private ConcurrentHashMap<String, List<Cookie>> cookieJar = new ConcurrentHashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieJar.put(url.host(), new ArrayList<>(cookies));
    }

    private List<Cookie> loadValidCookies(HttpUrl url) {
        List<Cookie> validCookies = new ArrayList<>();
        List<Cookie> cooky = cookieJar.get(url.host());
        if (cooky != null) {
            Iterator<Cookie> it = cooky.iterator();
            while (it.hasNext()) {
                Cookie currentCookie = it.next();
                if (isCookieExpired(currentCookie)) {
                    it.remove();
                } else if (currentCookie.matches(url)) {
                    validCookies.add(currentCookie);
                }
            }
        }
        return validCookies;
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> allCookies = new ArrayList<>();

        String host = url.host();
        String topPrivateDomain = url.topPrivateDomain();

        if (!host.equals(topPrivateDomain) && !host.equals("www." + topPrivateDomain)) {
            for (int i = 0; i < host.length(); i++) {
                if (host.charAt(i) == '.') {
                    HttpUrl subDomainUrl = HttpUrl.parse("https://" + host.substring(i + 1));
                    allCookies.addAll(loadValidCookies(subDomainUrl));
                    if (host.substring(i + 1).equals(topPrivateDomain)) {
                        break;
                    }
                }
            }
        }

        allCookies.addAll(loadValidCookies(url));
        return allCookies;
    }

    public void addCookieForDomain(String domain, Cookie cookie) {
        List<Cookie> cookieList = cookieJar.get(domain);
        if (cookieList == null) {
            cookieList = new ArrayList<>();
        }
        cookieList.add(cookie);
        cookieJar.put(domain, cookieList);
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    /**
     * Clears cookie store
     */
    public void clear() {
        cookieJar.clear();
    }

    /**
     * Updates this cookie store
     * 
     * @param map
     */
    public void update(Map<String, List<Cookie>> map) {
        for (String urlString : map.keySet()) {
            HttpUrl url = HttpUrl.parse(urlString);
            List<Cookie> cookiesList = map.get(urlString);
            if (url == null) {
                logger.debug("Unable to parse url " + urlString + " and build a HttpUrl object");
            }
            if (cookiesList != null && url != null) {
                cookieJar.put(url.host(), map.get(urlString));
            }
        }
    }

    public ConcurrentHashMap<String, List<Cookie>> getCookieJar() {
        return cookieJar;
    }

}
