package focusedCrawler.crawler.async.cookieHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class OkHttpCookieJar implements CookieJar, CookieHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(OkHttpCookieJar.class);
	private static ConcurrentHashMap<HttpUrl, List<Cookie>> cookieJar = new ConcurrentHashMap<>();

	public OkHttpCookieJar() {
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		cookieJar.put(url, cookies);
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		List<Cookie> validCookies = new ArrayList<>();

		List<Cookie> cooky = cookieJar.get(url);
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
	 * Updates the global cookie store currently being used by the fetcher
	 * 
	 * @param map
	 */
	public static void update(HashMap<String, List<Cookie>> map) {
		for (String s : map.keySet()) {
			HttpUrl url = HttpUrl.parse(s);
			List<Cookie> cookiesList = map.get(s);
			if (url == null) {
				LOGGER.debug("Unable to parse url " + s + " and build a HttpUrl object");
			}
			if (cookiesList != null && url != null) {
				cookieJar.put(url, map.get(s));
			}
		}

	}
}