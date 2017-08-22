package focusedCrawler.crawler.async.cookieHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.http.impl.cookie.BasicClientCookie;

import focusedCrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import focusedCrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import okhttp3.Cookie.Builder;

/**
 * Helper methods related to cookies
 *
 */

public class CookieUtils {

	/**
	 * Buils a new okhttp3 cookie from given cookie values
	 * @param cookie
	 * @return okhttp3 cookie
	 * @throws NullArgumentException if argument cookie is null;
	 */
	public static okhttp3.Cookie getOkkHttpCookie(Cookie cookie) {
		if(cookie == null) {
			throw new NullArgumentException("cookie");
		}
		Builder builder = new Builder();
		builder.name(cookie.getName());
		builder.value(cookie.getValue());
		builder.expiresAt(cookie.getExpiresAt());
		builder.domain(cookie.getDomain());
		builder.path(cookie.getPath());
		if (cookie.isSecure()) {
			builder.secure();
		}
		if (cookie.isHttpOnly()) {
			builder.httpOnly();
		}
		okhttp3.Cookie okkHttpCookie = builder.build();
		return okkHttpCookie;
	}

	/**
	 * Builds a new Apache cookie from given cookie values
	 * @param cookie
	 * @return apache http cookie
	 * @throws NullArgumentException if argument cookie is null;
	 */
	public static org.apache.http.cookie.Cookie getApacheCookie(Cookie cookie) {
		if(cookie == null) {
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
	 * @param cookies
	 * @param baseFetcher
	 * @throws NullPointerException if cookie is null
	 */
	public static void addCookies(HashMap<String, List<Cookie>> cookies, BaseFetcher baseFetcher) {
		if(cookies == null) {
			throw new NullPointerException("Cookies argument is null");
		}
		if(baseFetcher instanceof SimpleHttpFetcher) {
			HashMap<String, List<org.apache.http.cookie.Cookie>> tempCookies = new HashMap<>();
			for(String key: cookies.keySet()) {
				List<org.apache.http.cookie.Cookie> newCookieArrayList = new ArrayList<>();
				for(Cookie c: cookies.get(key)) {
					newCookieArrayList.add(CookieUtils.getApacheCookie(c));
				}
				tempCookies.put(key, newCookieArrayList);
			}
			SimpleHttpFetcher.updateCookieStore(tempCookies);
		}else {
			HashMap<String, List<okhttp3.Cookie>> tempCookies = new HashMap<>();
			for(String key: cookies.keySet()) {
				List<okhttp3.Cookie> newCookieArrayList = new ArrayList<>();
				for(Cookie c: cookies.get(key)) {
					newCookieArrayList.add(CookieUtils.getOkkHttpCookie(c));
				}
				tempCookies.put(key, newCookieArrayList);
			}
			OkHttpCookieJar.update(tempCookies);
		}
		
	}

}
