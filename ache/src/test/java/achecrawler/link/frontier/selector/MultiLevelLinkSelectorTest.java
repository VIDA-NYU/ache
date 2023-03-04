package achecrawler.link.frontier.selector;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import achecrawler.link.frontier.LinkRelevance;

import org.junit.jupiter.api.Test;

class MultiLevelLinkSelectorTest {

    @Test
    void shouldSelectLinks() throws Exception {
        // given
        MultiLevelLinkSelector selector = new MultiLevelLinkSelector();
        List<LinkRelevance> frontier = asList(
            new LinkRelevance("http://localhost/299", 299),
            new LinkRelevance("http://localhost/199", 199),
            new LinkRelevance("http://localhost/099", 99),
            new LinkRelevance("http://localhost/001", 1)
        );
        int numberOfLinks = 3;
        
        // when
        selector.startSelection(numberOfLinks);
        for(LinkRelevance link : frontier) {
            selector.evaluateLink(link); 
        }
        List<LinkRelevance> links = selector.getSelectedLinks();
        
        // then
        assertThat(links, is(notNullValue()));
        assertThat(links.size(), is(numberOfLinks));
        assertThat(links.get(0).getRelevance(), is(299d));
    }
    
}
