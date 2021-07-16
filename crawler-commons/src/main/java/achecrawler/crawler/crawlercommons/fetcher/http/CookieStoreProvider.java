package achecrawler.crawler.crawlercommons.fetcher.http;

import org.apache.http.client.CookieStore;

public interface CookieStoreProvider {
    CookieStore get();
}
