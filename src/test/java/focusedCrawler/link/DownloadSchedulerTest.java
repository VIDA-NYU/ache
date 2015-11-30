package focusedCrawler.link;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Test;

import focusedCrawler.util.LinkRelevance;

public class DownloadSchedulerTest {

    @Test
    public void shouldSelectLinksBasedOnPolitenes() throws Exception {
        LinkRelevance l1 = new LinkRelevance("http://ex1.com/", 1);
        LinkRelevance l2 = new LinkRelevance("http://ex2.com/", 2);
        
        LinkRelevance l3 = new LinkRelevance("http://ex1.com/3", 3);
        LinkRelevance l4 = new LinkRelevance("http://ex2.com/4", 4);
        LinkRelevance l5 = new LinkRelevance("http://ex3.com/5", 5);
                
        int minimumAccessTime = 100;
        DownloadScheduler scheduler = new DownloadScheduler(minimumAccessTime);
        
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
        assertThat(scheduler.numberOfNonExpiredDomains(), is(3));
        Thread.sleep(minimumAccessTime*2);
        assertThat(scheduler.numberOfNonExpiredDomains(), is(0));
        
        scheduler.addLink(l1);
        
        assertThat(scheduler.nextLink(), is(l1));
        assertThat(scheduler.numberOfNonExpiredDomains(), is(1));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
        
        assertThat(scheduler.nextLink(), is(nullValue()));
        assertThat(scheduler.numberOfEmptyDomains(), is(1));
    }
    
}
