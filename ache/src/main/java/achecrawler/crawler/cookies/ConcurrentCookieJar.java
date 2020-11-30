package achecrawler.crawler.cookies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.concurrent.GuardedBy;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

@SuppressWarnings("serial")
public class ConcurrentCookieJar implements CookieStore, Serializable {

    @GuardedBy("this")
    private final ConcurrentHashMap<String, Cookie> cookies;

    public ConcurrentCookieJar() {
        super();
        this.cookies = new ConcurrentHashMap<>();
    }

    /**
     * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent cookies. If the given
     * cookie has already expired it will not be added, but existing values will still be removed.
     *
     * @param cookie the {@link Cookie cookie} to be added
     *
     * @see #addCookies(HashMap)
     *
     */
    @Override
    public void addCookie(final Cookie cookie) {
        if (cookie != null) {
            // first remove any old cookie that is equivalent
            cookies.remove(cookie.getDomain() + cookie.getName() + cookie.getPath());
            if (!cookie.isExpired(new Date())) {
                cookies.put(cookie.getDomain() + cookie.getName() + cookie.getPath(), cookie);
            }
        }
    }

    /**
     * Adds an array of {@link Cookie HTTP cookies}. Cookies are added individually and in the given
     * array order. If any of the given cookies has already expired it will not be added, but
     * existing values will still be removed.
     *
     * @param cookies the {@link Cookie cookies} to be added
     *
     * @see #addCookie(Cookie)
     *
     */
    public void addCookies(final HashMap<String, Cookie> cookies) {
        if (cookies != null) {
            for (final Map.Entry<String, Cookie> cooky : cookies.entrySet()) {
                this.addCookie(cooky.getValue());
            }
        }
    }

    public void addCookies(final Cookie[] cookies) {
        if (cookies != null) {
            for (final Cookie cooky : cookies) {
                this.addCookie(cooky);
            }
        }
    }

    /**
     * Returns an immutable array of {@link Cookie cookies} that this HTTP state currently contains.
     *
     * @return an array of {@link Cookie cookies}.
     */
    @Override
    public List<Cookie> getCookies() {
        // create defensive copy so it won't be concurrently modified
        return new ArrayList<Cookie>(cookies.values());
    }

    /**
     * Removes all of {@link Cookie cookies} in this HTTP state that have expired by the specified
     * {@link java.util.Date date}.
     *
     * @return true if any cookies were purged.
     *
     * @see Cookie#isExpired(Date)
     */

    @Override
    public boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        boolean removed = false;
        Iterator<Cookie> it = cookies.values().iterator();
        while (it.hasNext()) {
            Cookie cooky = it.next();
            if (cooky.isExpired(date)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Clears all cookies.
     */
    @Override
    public void clear() {
        cookies.clear();
    }

    @Override
    public String toString() {
        return cookies.toString();
    }

}
