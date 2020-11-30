package achecrawler.link.frontier.selector;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import achecrawler.link.frontier.LinkRelevance;

public class TopkLinkSelectorTest {

    @Test
    public void shouldSelectTopkLinksOfHigherRelevance() throws Exception {
        // given
        TopkLinkSelector selector = new TopkLinkSelector();
        List<LinkRelevance> frontier = asList(
            new LinkRelevance("http://localhost/001", 1),
            new LinkRelevance("http://localhost/099", 99),
            new LinkRelevance("http://localhost/199", 199),
            new LinkRelevance("http://localhost/299", 299)
        );
        
        // when
        selector.startSelection(2);
        for(LinkRelevance link : frontier) {
            selector.evaluateLink(link); 
        }
        List<LinkRelevance> links = selector.getSelectedLinks();
        
        // then
        assertThat(links, is(notNullValue()));
        assertThat(links.size(), is(2));
        assertThat(links.get(0).getURL().toString(), is("http://localhost/299"));
        assertThat(links.get(1).getURL().toString(), is("http://localhost/199"));
    }
    
    @Test
    public void shouldNotSelectPagesWithNegativeRelevance() throws Exception {
        // given
        TopkLinkSelector selector = new TopkLinkSelector();
        List<LinkRelevance> frontier = asList(
            new LinkRelevance("http://localhost/001", -1),
            new LinkRelevance("http://localhost/099", 99),
            new LinkRelevance("http://localhost/199", -199),
            new LinkRelevance("http://localhost/299", -299)
        );
        
        // when
        selector.startSelection(2);
        for(LinkRelevance link : frontier) {
            selector.evaluateLink(link); 
        }
        List<LinkRelevance> links = selector.getSelectedLinks();
        
        // then
        assertThat(links, is(notNullValue()));
        assertThat(links.size(), is(1));
        assertThat(links.get(0).getURL().toString(), is("http://localhost/099"));
    }

}
