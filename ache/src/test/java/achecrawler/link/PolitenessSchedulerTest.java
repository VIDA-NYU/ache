package achecrawler.link;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import achecrawler.link.frontier.LinkRelevance;

public class PolitenessSchedulerTest {

    @Test
    public void shouldSelectLinksBasedOnPoliteness() throws Exception {
        
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/1", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex2.com/2", 2);
        
        LinkRelevance l3 = new LinkRelevance("http://ex1.com/3", 3);
        LinkRelevance l4 = new LinkRelevance("http://ex2.com/4", 4);
        LinkRelevance l5 = new LinkRelevance("http://ex3.com/5", 5);
                
        int minimumAccessTime = 500;
        int maxLinksInScheduler = 100;
        PolitenessScheduler scheduler = new PolitenessScheduler(minimumAccessTime, maxLinksInScheduler);
        
        
        // when add link l1
        scheduler.addLink(l1);
        // then should return it (+some other state checks)
        assertThat(scheduler.hasLinksAvailable(), is(true));
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.nextLink(), is(l1));
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.nextLink(), is(nullValue()));
        // and should remember domains from links recently chosen 
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
        
        // wait 1ms just to make sure that domains will have
        // different access times (in case that test run too
        // fast they will have the same access times)
        Thread.sleep(1);
        
        // same thing when add link l2...
        scheduler.addLink(l2);
        assertThat(scheduler.hasLinksAvailable(), is(true));
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.nextLink(), is(l2));
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.nextLink(), is(nullValue()));
        // should remember domains from links recently chosen 
        assertThat(scheduler.numberOfNonExpiredDomains(), is(2));
        assertThat(scheduler.numberOfEmptyDomains(), is(2));
        
        Thread.sleep(1);
        
        // when add 3 links from 3 different domains...
        scheduler.addLink(l3);
        scheduler.addLink(l4);
        scheduler.addLink(l5);
        
        assertThat(scheduler.numberOfNonExpiredDomains(), is(3));
        assertThat(scheduler.numberOfEmptyDomains(), is(0));
        
        // We assume that this test take less than 500ms to run.
        // Links l3 and l4 have higher priority, but they should be skipped
        // since other links from their domain has been chosen recently
        assertThat(scheduler.hasLinksAvailable(), is(true));
        assertThat(scheduler.nextLink(), is(l5));
        
        // at this moment, there should have no links available
        assertThat(scheduler.hasLinksAvailable(), is(false));
        assertThat(scheduler.nextLink(), is(nullValue()));
        
        // after waiting the minimumAccessTime interval, they links can be returned
        Thread.sleep(minimumAccessTime+100);
        
        assertThat(scheduler.nextLink(), is(l3));
        assertThat(scheduler.nextLink(), is(l4));
        
        // scheduler should also forget domains that don't have links chosen 
        // for longer then the minimumAccessTime
        Thread.sleep(minimumAccessTime+10);
        assertThat(scheduler.numberOfNonExpiredDomains(), is(0));
        
        // adding link again just to test that after removing old domain 
        // everything is still working fine
        scheduler.addLink(l1);
        
        assertThat(scheduler.nextLink(), is(l1));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
        assertThat(scheduler.nextLink(), is(nullValue()));
    }
    
    @Test
    public void addLinksShouldIgnoreLinkWhenMaxNumberOfLinksIsReached() throws Exception {
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex2.com/", 2);
                
        int minimumAccessTime = 100;
        int maxLinksInScheduler = 1;
        
        PolitenessScheduler scheduler = new PolitenessScheduler(minimumAccessTime, maxLinksInScheduler);
        
        scheduler.addLink(l1);
        assertThat(scheduler.numberOfLinks(), is(1));
        
        scheduler.addLink(l2);
        assertThat(scheduler.numberOfLinks(), is(1));
    }
    
    @Test
    public void shouldReturnLinksFromSameTLDsUsingRelevanceOrder() throws Exception {
        
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/1", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex1.com/2", 2);
        LinkRelevance l3 = new LinkRelevance("http://ex1.com/3", 3);
        LinkRelevance l4 = new LinkRelevance("http://ex1.com/4", 4);
                
        int minimumAccessTime = 0;
        int maxLinksInScheduler = 100;
        
        PolitenessScheduler scheduler = new PolitenessScheduler(minimumAccessTime, maxLinksInScheduler);
        
        scheduler.addLink(l1);
        scheduler.addLink(l2);
        scheduler.addLink(l3);
        scheduler.addLink(l4);
        
        assertThat(scheduler.nextLink().getRelevance(), is(4d));
        assertThat(scheduler.nextLink().getRelevance(), is(3d));
        assertThat(scheduler.nextLink().getRelevance(), is(2d));
        assertThat(scheduler.nextLink().getRelevance(), is(1d));
    }
    
    @Test
    public void shouldNotAddLinkMultipleTimes() throws Exception {
        
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/1", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex1.com/1", 1);
        LinkRelevance l3 = new LinkRelevance("http://ex1.com/1", 1);
                
        int minimumAccessTime = 0;
        int maxLinksInScheduler = 100;
        
        PolitenessScheduler scheduler = new PolitenessScheduler(minimumAccessTime, maxLinksInScheduler);
        
        scheduler.addLink(l1);
        scheduler.addLink(l1);
        scheduler.addLink(l2);
        scheduler.addLink(l3);
        
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.nextLink().getRelevance(), is(1d));
        assertThat(scheduler.nextLink(), is(nullValue()));
    }
    
    @Test
    public void shouldCheckIfLinkCanBeDownloadedAtCurrentTime() throws Exception {
        
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/1", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex1.com/2", 2);
        LinkRelevance l3 = new LinkRelevance("http://ex2.com/3", 3);
                
        int minimumAccessTime = 100;
        int maxLinksInScheduler = 100;
        
        PolitenessScheduler scheduler = new PolitenessScheduler(minimumAccessTime, maxLinksInScheduler);
        scheduler.addLink(l1);
        assertThat(scheduler.nextLink().getRelevance(), is(1d));
        
        assertThat(scheduler.canDownloadNow(l3), is(true));
        assertThat(scheduler.canDownloadNow(l2), is(false));
        Thread.sleep(minimumAccessTime+10);
        
        assertThat(scheduler.canDownloadNow(l2), is(true));
        assertThat(scheduler.canDownloadNow(l3), is(true));
    }
    
    @Test
    public void shouldBeAbleToClearListOfLinks() throws Exception {
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/1", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex2.com/2", 2);
        LinkRelevance l3 = new LinkRelevance("http://ex3.com/3", 3);
        LinkRelevance l4 = new LinkRelevance("http://ex4.com/4", 4);
        
        int minimumAccessTime = 100;
        int maxLinksInScheduler = 100;
        PolitenessScheduler scheduler = new PolitenessScheduler(minimumAccessTime, maxLinksInScheduler);
        
        scheduler.addLink(l1);
        scheduler.addLink(l2);
        scheduler.addLink(l4);
        scheduler.addLink(l3);
        
        assertThat(scheduler.numberOfLinks(), is(4));
        assertThat(scheduler.hasLinksAvailable(), is(true));
        
        // when
        scheduler.nextLink();
        scheduler.nextLink();
        scheduler.clear();
        
        // then
        assertThat(scheduler.hasLinksAvailable(), is(false));
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.numberOfEmptyDomains(), is(4));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(2));
        
        
        // make sure it remembers domains that were previously selected
        scheduler.addLink(l1);
        scheduler.addLink(l2);
        scheduler.addLink(l4);
        scheduler.addLink(l3);
        assertThat(scheduler.numberOfAvailableDomains(), is(2));
    }
    
}
