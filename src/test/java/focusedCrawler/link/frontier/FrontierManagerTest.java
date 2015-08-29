package focusedCrawler.link.frontier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;

public class FrontierManagerTest {

    final Path testPath = Paths.get("frontiermanager_test/");
    private Frontier frontier;
    private FrontierManager frontierManager;
    
    @Before
    public void setUp() throws Exception {
        
        File file = testPath.toFile();
        if(file.exists()) {
            FileUtils.deleteDirectory(file);
        }
        
        PersistentHashtable urlRelevance = new PersistentHashtable(testPath.toString(), 1000);
        LinkSelectionStrategy linkSelector = new BaselineLinkSelector(urlRelevance);
        frontier = new Frontier(urlRelevance, linkSelector);
        frontierManager = new FrontierManager(frontier, 2, 2, new LinkFilter(new ArrayList<String>()));
    }
    
    @After
    public void tearDown() throws IOException {
        frontierManager.close();
        FileUtils.deleteDirectory(testPath.toFile());
    }

    @Test
    public void shouldInsertUrl() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        
        // when
        frontierManager.insert(link1);
        
        LinkRelevance nextURL = frontierManager.nextURL();
        
        // then
        assertThat(nextURL.getURL(), is(notNullValue()));
        assertThat(nextURL.getURL(), is(link1.getURL()));
        assertThat(nextURL.getRelevance(), is(link1.getRelevance()));
    }
    
    @Test
    public void shouldInsertUrlsAndSelectUrlsInSortedByRelevance() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        LinkRelevance link3 = new LinkRelevance(new URL("http://www.example3.com/index.html"), 3);
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        frontierManager.insert(link3);
        
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        LinkRelevance selectedLink2 = frontierManager.nextURL();
        LinkRelevance selectedLink3 = frontierManager.nextURL();
        LinkRelevance selectedLink4 = frontierManager.nextURL();
        
        // then
        
        // should return only 3 inserted links, 4th should be null 
        assertThat(selectedLink1, is(notNullValue()));
        assertThat(selectedLink2, is(notNullValue()));
        assertThat(selectedLink3, is(notNullValue()));
        assertThat(selectedLink4, is(nullValue()));
        
        // should return bigger relevance values first
        assertThat(selectedLink1.getURL(), is(link3.getURL()));
        assertThat(selectedLink2.getURL(), is(link2.getURL()));
        assertThat(selectedLink3.getURL(), is(link1.getURL()));
    }
    
    
    @Test
    public void shouldNotReturnAgainALinkThatWasAlreadyReturned() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        LinkRelevance selectedLink2 = frontierManager.nextURL();
        
        LinkRelevance selectedLink3 = frontierManager.nextURL();
        
        frontierManager.insert(link1); // insert link 1 again, should not be returned
        LinkRelevance selectedLink4 = frontierManager.nextURL();
        
        // then
        assertThat(selectedLink1, is(notNullValue()));
        assertThat(selectedLink2, is(notNullValue()));
        
        assertThat(selectedLink3, is(nullValue()));
        
        assertThat(selectedLink4, is(nullValue()));
        
    }

}
