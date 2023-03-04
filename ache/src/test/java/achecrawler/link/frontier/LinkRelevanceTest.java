package achecrawler.link.frontier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

class LinkRelevanceTest {

    @Test
    void shouldExtractCorrectTopLevelDomain() throws MalformedURLException {
        // given
        LinkRelevance link = new LinkRelevance("http://www.test.asdf.com/index.html", 299.00);
        // when
        String tld = link.getTopLevelDomainName();
        // then
        assertThat(tld,  is("asdf.com"));
    }

    @Test
    void shouldExtractIPAsTLD2() throws MalformedURLException {
        // given
        LinkRelevance link = new LinkRelevance("http://127.0.0.1/index.html", 299.00);
        // when
        String tld = link.getTopLevelDomainName();
        // then
        assertThat(tld,  is("127.0.0.1"));
    }

}
