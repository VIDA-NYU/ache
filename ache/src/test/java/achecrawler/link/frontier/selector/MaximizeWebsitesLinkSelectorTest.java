package achecrawler.link.frontier.selector;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import achecrawler.link.frontier.LinkRelevance;

class MaximizeWebsitesLinkSelectorTest {

    @Test
    void shouldSelectLinksOfEachDomain() throws Exception {
        // given
        MaximizeWebsitesLinkSelector selector = new MaximizeWebsitesLinkSelector();
        List<LinkRelevance> frontier = new ArrayList<>(asList(
            new LinkRelevance("http://example1.com/1", 1d),
            new LinkRelevance("http://example1.com/2", 2d),
            new LinkRelevance("http://example2.com/1", 3d),
            new LinkRelevance("http://example2.com/2", 4d)
        ));
        // when
        selector.startSelection(2);
        for(LinkRelevance link : frontier) selector.evaluateLink(link);
        List<LinkRelevance> selectedLinks = selector.getSelectedLinks();
        // then
        assertThat(selectedLinks.size()).isEqualTo(2);
        assertThat(selectedLinks.get(0).getRelevance()).isEqualTo(4d);
        assertThat(selectedLinks.get(1).getRelevance()).isEqualTo(2d);
        
        // given
        frontier.removeAll(selectedLinks);
        // when
        selector.startSelection(2);
        for(LinkRelevance link : frontier) selector.evaluateLink(link);
        selectedLinks = selector.getSelectedLinks();
        // then
        assertThat(selectedLinks.size()).isEqualTo(2);
        assertThat(selectedLinks.get(0).getRelevance()).isEqualTo(3d);
        assertThat(selectedLinks.get(1).getRelevance()).isEqualTo(1d);
        
        // given
        frontier.removeAll(selectedLinks);
        // when
        selector.startSelection(2);
        selectedLinks = selector.getSelectedLinks();
        // then
        assertThat(selectedLinks.size()).isEqualTo(0);

    }

    @Test
    void shouldSelectTopkLinksOfHigherRelevance() throws Exception {
        // given
        MaximizeWebsitesLinkSelector selector = new MaximizeWebsitesLinkSelector();
        List<LinkRelevance> frontier = asList(
            new LinkRelevance("http://example1.com/1", 1),
            new LinkRelevance("http://example1.com/2", 2),
            new LinkRelevance("http://example1.com/3", 3),
        
            new LinkRelevance("http://example2.com/1", 1),
            new LinkRelevance("http://example2.com/2", 2),
            new LinkRelevance("http://example2.com/3", 3),
        
            new LinkRelevance("http://example3.com/1", 1),
            new LinkRelevance("http://example3.com/2", 2),
            new LinkRelevance("http://example3.com/3", 3)
        );
        
        // when
        selector.startSelection(15);
        for(LinkRelevance link : frontier) selector.evaluateLink(link);
        List<LinkRelevance> selectedLinks = selector.getSelectedLinks();
        
        // then
        assertThat(selectedLinks.size()).isEqualTo(9);
        assertThat(selectedLinks.get(0).getRelevance()).isEqualTo(3d);
        assertThat(selectedLinks.get(1).getRelevance()).isEqualTo(3d);
        assertThat(selectedLinks.get(2).getRelevance()).isEqualTo(3d);
        
        assertThat(selectedLinks.get(3).getRelevance()).isEqualTo(2d);
        assertThat(selectedLinks.get(4).getRelevance()).isEqualTo(2d);
        assertThat(selectedLinks.get(5).getRelevance()).isEqualTo(2d);
        
        assertThat(selectedLinks.get(6).getRelevance()).isEqualTo(1d);
        assertThat(selectedLinks.get(7).getRelevance()).isEqualTo(1d);
        assertThat(selectedLinks.get(8).getRelevance()).isEqualTo(1d);
    }
}
