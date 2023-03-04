package achecrawler.link.frontier;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkRelevanceTest {

    @Test
    void shouldExtractCorrectTopLevelDomain() throws MalformedURLException {
        // given
        LinkRelevance link = new LinkRelevance("http://www.test.asdf.com/index.html", 299.00);
        // when
        String tld = link.getTopLevelDomainName();
        // then
        assertThat(tld).isEqualTo("asdf.com");
    }

    @Test
    void shouldExtractIPAsTLD2() throws MalformedURLException {
        // given
        LinkRelevance link = new LinkRelevance("http://127.0.0.1/index.html", 299.00);
        // when
        String tld = link.getTopLevelDomainName();
        // then
        assertThat(tld).isEqualTo("127.0.0.1");
    }

}
