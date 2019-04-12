package focusedCrawler.link;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import focusedCrawler.link.frontier.LinkRelevance;

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
        
        
        // when add link l1 from domain ex1.com
        scheduler.addLink(l1);
        // then should return it (+some other state checks)
        assertThat(scheduler.hasLinksAvailable(), is(true));
        assertThat(scheduler.numberOfLinks(), is(1));

        assertThat(scheduler.nextLink(), is(l1));
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.canDownloadNow(l1), is(false));
        assertThat(scheduler.nextLink(), is(nullValue()));
        assertThat(scheduler.numberOfDownloadingLinks(), is(1));

        // after the download is finished, then the link is moved from a "downloading" list and
        // removed from the scheduler.
        scheduler.notifyDownloadFinished(l1);
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.canDownloadNow(l1), is(false));
        assertThat(scheduler.nextLink(), is(nullValue()));
        assertThat(scheduler.numberOfDownloadingLinks(), is(0));

        // and should remember domains from links recently chosen
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
        
        // wait 1ms just to make sure that domains will have
        // different access times (in case that test run too
        // fast they will have the same access times)
        Thread.sleep(1);
        
        // same thing when add link l2 from a different domain ex2.com...
        scheduler.addLink(l2);
        assertThat(scheduler.hasLinksAvailable(), is(true));
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.numberOfDownloadingLinks(), is(0));
        assertThat(scheduler.nextLink(), is(l2));
        assertThat(scheduler.numberOfDownloadingLinks(), is(1));
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.nextLink(), is(nullValue()));

        scheduler.notifyDownloadFinished(l2);
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.numberOfDownloadingLinks(), is(0));
        assertThat(scheduler.canDownloadNow(l2), is(false));
        assertThat(scheduler.nextLink(), is(nullValue()));

        // should remember domains from links recently chosen 
        assertThat(scheduler.numberOfNonExpiredDomains(), is(2));
        assertThat(scheduler.numberOfEmptyDomains(), is(2));
        
        Thread.sleep(1);
        
        // when add 3 links from 3 different domains...
        scheduler.addLink(l3);
        scheduler.addLink(l4);
        scheduler.addLink(l5);
        assertThat(scheduler.numberOfDownloadingLinks(), is(0));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(3));
        assertThat(scheduler.numberOfEmptyDomains(), is(0));
        
        // We assume that this test take less than 500ms to run.
        // Links l3 and l4 have higher priority, but they should be skipped
        // since other links from their domain has been chosen recently
        assertThat(scheduler.hasLinksAvailable(), is(true));
        assertThat(scheduler.nextLink(), is(l5));
        assertThat(scheduler.numberOfDownloadingLinks(), is(1));

        // at this moment, there should have no links available
        // because l3 and l4 are from domains that were just downloaded
        assertThat(scheduler.hasLinksAvailable(), is(false));
        assertThat(scheduler.nextLink(), is(nullValue()));
        assertThat(scheduler.numberOfLinks(), is(3));
        
        // after waiting the minimumAccessTime interval, the links can be returned
        Thread.sleep(minimumAccessTime+100);

        // now l3 and l4 can be downloaded
        assertThat(scheduler.nextLink(), is(l3));
        assertThat(scheduler.nextLink(), is(l4));
        assertThat(scheduler.numberOfDownloadingLinks(), is(3));

        // domains from links l3 and l4 should still be remembered until more than
        // minimumAccessTime has passed since their download time
        assertThat(scheduler.numberOfNonExpiredDomains(), is(3));
        // link l3 , l4, and l5 should still be in the scheduler until they are downloaded
        assertThat(scheduler.numberOfLinks(), is(3));

        // now we mark l3, l4, and l5 as downloaded
        scheduler.notifyDownloadFinished(l3);
        assertThat(scheduler.numberOfDownloadingLinks(), is(2));
        scheduler.notifyDownloadFinished(l4);
        assertThat(scheduler.numberOfDownloadingLinks(), is(1));
        scheduler.notifyDownloadFinished(l5);
        assertThat(scheduler.numberOfDownloadingLinks(), is(0));

        // now the links should have been removed
        assertThat(scheduler.numberOfLinks(), is(0));
        // but their domains should still be remembered for more minimumAccessTime milliseconds
        assertThat(scheduler.numberOfNonExpiredDomains(), is(3));


        // after minimumAccessTime has passed, the scheduler should forget domains that
        // don't have links chosen for longer then the minimumAccessTime
        Thread.sleep(minimumAccessTime+10);
        assertThat(scheduler.numberOfNonExpiredDomains(), is(0));
        
        // adding link again just to test that after removing old domain 
        // everything is still working fine
        scheduler.addLink(l1);
        assertThat(scheduler.nextLink(), is(l1));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
        assertThat(scheduler.numberOfEmptyDomains(), is(0));
        assertThat(scheduler.nextLink(), is(nullValue()));

        scheduler.notifyDownloadFinished(l1);
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
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

        LinkRelevance link = scheduler.nextLink();
        scheduler.notifyDownloadFinished(link);
        assertThat(link.getRelevance(), is(4d));

        link = scheduler.nextLink();
        assertThat(link.getRelevance(), is(3d));
        scheduler.notifyDownloadFinished(link);

        link = scheduler.nextLink();
        assertThat(link.getRelevance(), is(2d));
        scheduler.notifyDownloadFinished(link);

        link = scheduler.nextLink();
        assertThat(link.getRelevance(), is(1d));
        scheduler.notifyDownloadFinished(link);
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
    public void shouldNotConsiderToAddLinkThatWasAlreadyAdded() throws Exception {

        LinkRelevance l1 = new LinkRelevance("http://ex1.com/1", 1);

        int minimumAccessTime = 0;
        int maxLinksInScheduler = 100;

        PolitenessScheduler scheduler = new PolitenessScheduler(minimumAccessTime, maxLinksInScheduler);

        // when add links l1
        scheduler.addLink(l1);
        // should count them correctly
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.numberOfDownloadingLinks(), is(0));
        assertThat(scheduler.canInsertNow(l1), is(false));

        // even when select link l1 to be download
        scheduler.nextLink();
        // should not consider to be be included
        assertThat(scheduler.numberOfDownloadingLinks(), is(1));
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.canInsertNow(l1), is(false));

        // when download is completed
        scheduler.notifyDownloadFinished(l1);
        // then should be able to re-schedule (notethat the minimal delay is 0ms in this test)
        assertThat(scheduler.numberOfDownloadingLinks(), is(0));
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.canInsertNow(l1), is(true));
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
        LinkRelevance link = scheduler.nextLink();
        assertThat(link.getRelevance(), is(1d));
        assertThat(link.getURL().toString(), is(l1.getURL().toString()));

        // cannot be downloaded since it was just selected for download
        assertThat(scheduler.canDownloadNow(l2), is(false));
        scheduler.notifyDownloadFinished(l2);
        // still cannot be downloaded since minimum time between downloads has not passed
        assertThat(scheduler.canDownloadNow(l2), is(false));

        // no link from ex2.com was ever downloaded, so it can be downloaded
        assertThat(scheduler.canDownloadNow(l3), is(true));

        // now we wait a little longer than the minimum time between downloads
        Thread.sleep(minimumAccessTime+10);

        // now links both ex1.com and ex2.com can be downloaded
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

        // downloads 2 out of the 4 links added
        LinkRelevance link1 = scheduler.nextLink();
        scheduler.notifyDownloadFinished(link1);

        LinkRelevance link2= scheduler.nextLink();
        scheduler.notifyDownloadFinished(link2);

        scheduler.clearPendingQueue(); // should remove all links
        
        // then should have no links available anymore
        assertThat(scheduler.hasLinksAvailable(), is(false));
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.numberOfEmptyDomains(), is(4));
        // but should still remember download times from ex3.com and ex4.com
        assertThat(scheduler.numberOfNonExpiredDomains(), is(2));
        assertThat(scheduler.canDownloadNow(link1), is(false));
        assertThat(scheduler.canDownloadNow(link2), is(false));
        
        // make sure it remembers domains that were previously selected
        scheduler.addLink(l1);
        scheduler.addLink(l2);
        scheduler.addLink(l4);
        scheduler.addLink(l3);
        assertThat(scheduler.numberOfAvailableDomains(), is(2));
    }
    
}
