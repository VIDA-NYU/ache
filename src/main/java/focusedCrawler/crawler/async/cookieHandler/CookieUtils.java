package focusedCrawler.crawler.async.cookieHandler;

import java.util.Date;

import org.apache.http.impl.cookie.BasicClientCookie;

import okhttp3.Cookie.Builder;

public class CookieUtils {

	public static okhttp3.Cookie getOkkHttpCookie(Cookie cookie) {
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

	public static org.apache.http.cookie.Cookie getApacheCookie(Cookie cookie) {
		BasicClientCookie apacheCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
		apacheCookie.setDomain(cookie.getDomain());
		apacheCookie.setExpiryDate(new Date(cookie.getExpiresAt()));
		apacheCookie.setPath(cookie.getPath());
		apacheCookie.setSecure(cookie.isSecure());
		return apacheCookie;
	}

}
