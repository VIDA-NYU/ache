package achecrawler.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UrlsTest {

    /**
     * See issue https://github.com/VIDA-NYU/ache/issues/177
     */
    @Test
    void RecentTLDsShouldBeValid() {
        assertTrue(Urls.isValid("http://registry.africa"));
    }

    @Test
    void OnionLinksShouldBeValid() {
        assertTrue(Urls.isValid("http://3g2upl4pq6kufc4m.onion/"));
    }

}