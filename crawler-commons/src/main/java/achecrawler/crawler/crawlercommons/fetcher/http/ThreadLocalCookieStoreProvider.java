package achecrawler.crawler.crawlercommons.fetcher.http;

import org.apache.http.client.CookieStore;

class ThreadLocalCookieStoreProvider extends ThreadLocal<CookieStore> implements CookieStoreProvider {

    @Override
    protected CookieStore initialValue() {
        return new LocalCookieStore();
    }

}