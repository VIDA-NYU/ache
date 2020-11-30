package achecrawler.crawler.async.fetcher;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

public class GlobalCookieStore implements CookieStore, Serializable {

    private static final long serialVersionUID = -7581093305228232025L;

    private final ConcurrentHashMap<String, Cookie> cookies;

    public GlobalCookieStore() {
        super();
        this.cookies = new ConcurrentHashMap<String, Cookie>();
    }

    /**
     * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent
     * cookies. If the given ookie has already expired it will not be added, but
     * existing values will still be removed.
     * 
     * @param cookie
     *            the {@link Cookie cookie} to be added
     * 
     * @see #addCookies(Cookie[])
     * 
     */
    public void addCookie(Cookie cookie) {
        if (cookie != null) {
            // first remove any old cookie that is equivalent
            cookies.remove(cookie.getDomain()+"__"+cookie.getName()+"__"+cookie.getPath());
            if (!cookie.isExpired(new Date())) {
            	cookies.put(cookie.getDomain()+"__"+cookie.getName()+"__"+cookie.getPath(), cookie);
            }
        }
    }

    /**
     * Adds an array of {@link Cookie HTTP cookies}. Cookies are added
     * individually and in the given array order. If any of the given cookies
     * has already expired it will not be added, but existing values will still
     * be removed.
     * 
     * @param cookies
     *            the {@link Cookie cookies} to be added
     * 
     * @see #addCookie(Cookie)
     * 
     */
    public void addCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cooky : cookies) {
                this.addCookie(cooky);
            }
        }
    }

    /**
     * Returns an immutable array of {@link Cookie cookies} that this HTTP state
     * currently contains.
     * 
     * @return an array of {@link Cookie cookies}.
     */
    public List<Cookie> getCookies() {
        return (List<Cookie>) cookies.values();
    }

    /**
     * Removes all of {@link Cookie cookies} in this HTTP state that have
     * expired by the specified {@link java.util.Date date}.
     * 
     * @return true if any cookies were purged.
     * 
     * @see Cookie#isExpired(Date)
     */
    public boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        boolean removed = false;
        for(String key : cookies.keySet()) {
        	if(cookies.get(key).isExpired(date)) {
        		cookies.remove(key);
        		removed = true;
        	}
        }
        return removed;
    }

    /**
     * Clears all cookies.
     */
    public void clear() {
        cookies.clear();
    }

    @Override
    public String toString() {
        return cookies.toString();
    }

}

