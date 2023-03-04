package achecrawler.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlsTest {

    /**
     * See issue https://github.com/VIDA-NYU/ache/issues/177
     */
    @Test
    void RecentTLDsShouldBeValid() {
        assertThat(Urls.isValid("http://registry.africa")).isTrue();
    }

    @Test
    void OnionLinksShouldBeValid() {
        assertThat(Urls.isValid("http://3g2upl4pq6kufc4m.onion/")).isTrue();
    }

}