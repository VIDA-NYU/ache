package focusedCrawler.link;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import focusedCrawler.link.frontier.LinkRelevance;

public class DownloadSchedulerTest {

    @Test
    public void shouldSelectLinksBasedOnPolitenes() throws Exception {
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex2.com/", 2);
        
        LinkRelevance l3 = new LinkRelevance("http://ex1.com/3", 3);
        LinkRelevance l4 = new LinkRelevance("http://ex2.com/4", 4);
        LinkRelevance l5 = new LinkRelevance("http://ex3.com/5", 5);
                
        int minimumAccessTime = 500;
        int maxLinksInScheduler = 100;
        DownloadScheduler scheduler = new DownloadScheduler(minimumAccessTime, maxLinksInScheduler);
        
        scheduler.addLink(l1);
        
        assertThat(scheduler.numberOfLinks(), is(1));
        assertThat(scheduler.nextLink(), is(l1));
        
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.nextLink(), is(nullValue()));
        
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
        
        scheduler.addLink(l2);
        
        assertThat(scheduler.numberOfLinks(), is(1));
        
        assertThat(scheduler.nextLink(), is(l2));
        
        assertThat(scheduler.numberOfLinks(), is(0));
        assertThat(scheduler.nextLink(), is(nullValue()));
        
        assertThat(scheduler.numberOfNonExpiredDomains(), is(2));
        assertThat(scheduler.numberOfEmptyDomains(), is(2));
        
        scheduler.addLink(l3);
        scheduler.addLink(l4);
        scheduler.addLink(l5);
        
        assertThat(scheduler.numberOfEmptyDomains(), is(0));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(3));
        
        assertThat(scheduler.nextLink(), is(l5));
        assertThat(scheduler.nextLink(), is(l3));
        assertThat(scheduler.nextLink(), is(l4));
        assertThat(scheduler.numberOfEmptyDomains(), is(3));
        
        // should remove expired domains automatically
        Thread.sleep(minimumAccessTime+10);
        assertThat(scheduler.numberOfNonExpiredDomains(), is(0));
        
        scheduler.addLink(l1);
        
        assertThat(scheduler.nextLink(), is(l1));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
        
        assertThat(scheduler.nextLink(), is(nullValue()));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
    }
    
    @Test
    public void addLinksShouldBlockWhenMaxNumberOfLinksIsReached() throws Exception {
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex2.com/", 2);
                
        int minimumAccessTime = 100;
        int maxLinksInScheduler = 1;
        
        final DownloadScheduler scheduler = new DownloadScheduler(minimumAccessTime, maxLinksInScheduler);
        final int removeAfterTime = 500;
        
        Thread removeThread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(removeAfterTime);
                    scheduler.nextLink();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Test interrupted.");
                }
            };
        };
        
        long t0 = System.currentTimeMillis();
        scheduler.addLink(l1);
        long t1 = System.currentTimeMillis();
        removeThread.start();
        scheduler.addLink(l2);
        long t2 = System.currentTimeMillis();
        
        boolean addTime1 = t1 - t0 < removeAfterTime;
        assertThat(addTime1, is(true));
        
        boolean addTime2 = t2 - t1 > removeAfterTime;
        assertThat(addTime2, is(true));
    }
    
    @Test
    public void nextLinksShouldWaitMinimumAccessTimeOfDomain() throws Exception {
        // given
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/1", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex1.com/2", 2);
                
        int minimumAccessTime = 500;
        int maxLinksInScheduler = 10;
        
        final DownloadScheduler scheduler = new DownloadScheduler(minimumAccessTime, maxLinksInScheduler);
        scheduler.addLink(l1);
        scheduler.addLink(l2);
        
        // when
        long t0 = System.currentTimeMillis();
        scheduler.nextLink();
        long t1 = System.currentTimeMillis();
        scheduler.nextLink();
        long t2 = System.currentTimeMillis();
        
        // then
        boolean nextLinkTime1 = t1 - t0 < minimumAccessTime;
        assertThat(nextLinkTime1, is(true));
        
        int recordTimeErrorMargin = 5; // sometimes the recorded times outside the scheduler
                                       // can be slightly different from actual ones
        boolean nextLinkTime2 = t2 - t1 > (minimumAccessTime - recordTimeErrorMargin);
        assertThat(nextLinkTime2, is(true));
    }
}
