package achecrawler.link.frontier.selector;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import achecrawler.link.frontier.LinkRelevance;

import org.junit.jupiter.api.Test;

class TopkLinkSelectorTest {

    @Test
    void shouldSelectTopkLinksOfHigherRelevance() throws Exception {
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
        assertThat(links).isNotNull();
        assertThat(links.size()).isEqualTo(2);
        assertThat(links.get(0).getURL().toString()).isEqualTo("http://localhost/299");
        assertThat(links.get(1).getURL().toString()).isEqualTo("http://localhost/199");
    }

    @Test
    void shouldNotSelectPagesWithNegativeRelevance() throws Exception {
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
        assertThat(links).isNotNull();
        assertThat(links.size()).isEqualTo(1);
        assertThat(links.get(0).getURL().toString()).isEqualTo("http://localhost/099");
    }

}
