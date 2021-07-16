package achecrawler.link.frontier;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;

import org.junit.Test;

public class LinkRelevanceTest {

    @Test
    public void shouldExtractCorrectTopLevelDomain() throws MalformedURLException {
        // given
        LinkRelevance link = new LinkRelevance("http://www.test.asdf.com/index.html", 299.00);
        // when
        String tld = link.getTopLevelDomainName();
        // then
        assertThat(tld,  is("asdf.com"));
    }
    
    @Test
    public void shouldExtractIPAsTLD2() throws MalformedURLException {
        // given
        LinkRelevance link = new LinkRelevance("http://127.0.0.1/index.html", 299.00);
        // when
        String tld = link.getTopLevelDomainName();
        // then
        assertThat(tld,  is("127.0.0.1"));
    }

}
