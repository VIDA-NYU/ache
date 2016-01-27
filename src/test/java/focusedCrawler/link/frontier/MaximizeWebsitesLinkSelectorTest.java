package focusedCrawler.link.frontier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import focusedCrawler.util.LinkRelevance;

public class MaximizeWebsitesLinkSelectorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Test
    public void shouldSelectLinksOfEachDomain() throws Exception {
        // given
        MaximizeWebsitesLinkSelector selector = new MaximizeWebsitesLinkSelector();
        Frontier frontier = new Frontier(tempFolder.newFolder().toString(), 100);
        
        frontier.insert(new LinkRelevance("http://example1.com/1", 1));
        frontier.insert(new LinkRelevance("http://example1.com/2", 2));
        
        frontier.insert(new LinkRelevance("http://example2.com/1", 4));
        frontier.insert(new LinkRelevance("http://example2.com/2", 5));
        
        frontier.commit();
        
        Set<String> domainsSet = new HashSet<>(
            Arrays.asList(
                "example1.com",
                "example2.com"
            )
        );
        
        // when
        LinkRelevance[] links = selector.select(frontier, 2);
        removeAll(frontier, links);
        // then
        assertThat(links.length, is(2));
        assertThat(containsAll(links, domainsSet), is(true));
        
        // when
        links = selector.select(frontier, 2);
        removeAll(frontier, links);
        // then
        assertThat(links.length, is(2));
        assertThat(containsAll(links, domainsSet), is(true));
        
        // when
        links = selector.select(frontier, 2);
        // then
        assertThat(links.length, is(0));

    }
    
    @Test
    public void shouldSelectTopkLinksOfHigherRelevance() throws Exception {
        // given
        MaximizeWebsitesLinkSelector selector = new MaximizeWebsitesLinkSelector();
        Frontier frontier = new Frontier(tempFolder.newFolder().toString(), 100);
        
        frontier.insert(new LinkRelevance("http://example1.com/1", 1));
        frontier.insert(new LinkRelevance("http://example1.com/2", 2));
        frontier.insert(new LinkRelevance("http://example1.com/3", 3));
        
        frontier.insert(new LinkRelevance("http://example2.com/1", 1));
        frontier.insert(new LinkRelevance("http://example2.com/2", 2));
        frontier.insert(new LinkRelevance("http://example2.com/3", 3));
        
        frontier.insert(new LinkRelevance("http://example3.com/1", 1));
        frontier.insert(new LinkRelevance("http://example3.com/2", 2));
        frontier.insert(new LinkRelevance("http://example3.com/3", 3));
        
        frontier.commit();
        
        // when
        LinkRelevance[] links = selector.select(frontier, 15);
        
        // then
        assertThat(links.length, is(9));
        assertThat(links[0].getRelevance(), is(3d));
        assertThat(links[1].getRelevance(), is(3d));
        assertThat(links[2].getRelevance(), is(3d));
        
        assertThat(links[3].getRelevance(), is(2d));
        assertThat(links[4].getRelevance(), is(2d));
        assertThat(links[5].getRelevance(), is(2d));
        
        assertThat(links[6].getRelevance(), is(1d));
        assertThat(links[7].getRelevance(), is(1d));
        assertThat(links[8].getRelevance(), is(1d));

    }


    private void removeAll(Frontier frontier, LinkRelevance[] links) throws FrontierPersistentException {
        for (LinkRelevance l : links) {
            frontier.delete(l);
        }
        frontier.commit();
    }

    private boolean containsAll(LinkRelevance[] links,  Set<String> domainsSet) {
        for (LinkRelevance link : links) {
            String domainName = link.getTopLevelDomainName();
            if(!domainsSet.contains(domainName)) {
                return false;
            }
        }
        return true;
    }
    
}
