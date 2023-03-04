package achecrawler.link.frontier.selector;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import achecrawler.link.frontier.LinkRelevance;

import org.junit.jupiter.api.Test;

class NonRandomLinkSelectorTest {

    @Test
    void shouldSelectLinks() throws Exception {
        // given
        NonRandomLinkSelector selector = new NonRandomLinkSelector();
        List<LinkRelevance> frontier = asList(
            new LinkRelevance("http://localhost/299/1", 299),
            new LinkRelevance("http://localhost/299/2", 299),
            new LinkRelevance("http://localhost/199", 199),
            new LinkRelevance("http://localhost/099", 99),
            new LinkRelevance("http://localhost/001", 1)
        );
        int numberOfLinks = 2;
        
        // when
        selector.startSelection(numberOfLinks);
        for(LinkRelevance link : frontier) {
            selector.evaluateLink(link); 
        }
        List<LinkRelevance> links = selector.getSelectedLinks();
        
        // then
        assertThat(links).isNotNull();
        assertThat(links.size()).isEqualTo(2);
        assertThat(links.get(0).getRelevance()).isEqualTo(299d);
        assertThat(links.get(0).getRelevance()).isEqualTo(299d);
    }
    
}
