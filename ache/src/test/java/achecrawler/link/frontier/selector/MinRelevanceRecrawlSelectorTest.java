package achecrawler.link.frontier.selector;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import achecrawler.link.frontier.LinkRelevance;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class MinRelevanceRecrawlSelectorTest {

    @Test
    public void shouldSelectNotSelectLinkWithRelevanceSmallerThanMinimum() throws Exception {
        // given
        long recrawlInterval = 100;
        MinRelevanceRecrawlSelector selector = new MinRelevanceRecrawlSelector(recrawlInterval,
                TimeUnit.MILLISECONDS, 100, false, false);
        List<LinkRelevance> frontier = asList(
                new LinkRelevance("http://localhost/001", -1),
                new LinkRelevance("http://localhost/099", -99),
                new LinkRelevance("http://localhost/199", -199),
                new LinkRelevance("http://localhost/299", -299)
        );

        // when
        selector.startSelection(3);
        for (LinkRelevance link : frontier) {
            selector.evaluateLink(link);
        }
        List<LinkRelevance> links = selector.getSelectedLinks();

        // then
        assertThat(links, is(notNullValue()));
        assertThat(links.size(), is(2));
        assertThat(Math.abs(links.get(0).getRelevance()) > 100d, is(true));
        assertThat(Math.abs(links.get(1).getRelevance()) > 100d, is(true));
    }

    @Test
    public void shouldNotSelectSitemapsAndRobotsWhenConfiguredNotTo() throws Exception {
        // given
        long recrawlInterval = 100;
        MinRelevanceRecrawlSelector selector = new MinRelevanceRecrawlSelector(recrawlInterval,
                TimeUnit.MILLISECONDS, 100, false, false);
        List<LinkRelevance> frontier = asList(
                LinkRelevance.createForward("http://example.com/001", -1d),
                LinkRelevance.createForward("http://example.com/099", -199d),
                LinkRelevance.createRobots("http://example.com/robots.txt", -299d),
                LinkRelevance.createSitemap("http://example.com/sitemap.xml", -299d)
        );

        // when
        selector.startSelection(3);
        for (LinkRelevance link : frontier) {
            selector.evaluateLink(link);
        }
        List<LinkRelevance> links = selector.getSelectedLinks();

        // then
        assertThat(links, is(notNullValue()));
        assertThat(links.size(), is(1));
        assertThat(Math.abs(links.get(0).getRelevance()), is(199d));
    }

}
