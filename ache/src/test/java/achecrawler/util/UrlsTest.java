package achecrawler.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UrlsTest {

    /**
     * See issue https://github.com/VIDA-NYU/ache/issues/177
     */
    @Test
    public void RecentTLDsShouldBeValid() {
        assertTrue(Urls.isValid("http://registry.africa"));
    }

    @Test
    public void OnionLinksShouldBeValid() {
        assertTrue(Urls.isValid("http://3g2upl4pq6kufc4m.onion/"));
    }

}