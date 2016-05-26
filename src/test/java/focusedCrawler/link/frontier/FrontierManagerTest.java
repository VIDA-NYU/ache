package focusedCrawler.link.frontier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.link.frontier.selector.RandomLinkSelector;
import focusedCrawler.link.frontier.selector.TopkLinkSelector;
import focusedCrawler.util.LinkFilter;

public class FrontierManagerTest {

    @Rule
    // a new temp folder is created for each test case
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private LinkFilter emptyLinkFilter = new LinkFilter(new ArrayList<String>());

    private Frontier frontier;
    private HostManager hostManager;
    private boolean downloadRobots = false;
    
    @Before
    public void setUp() throws Exception {
        frontier = new Frontier(tempFolder.newFolder().toString(), 1000);
        hostManager = new HostManager(Paths.get(tempFolder.newFolder().toString()));
    }
    
    @After
    public void tearDown() throws IOException {
    }
    
    @Test
    public void shouldNotInsertLinkOutOfScope() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        
        Map<String, Integer> scope = new HashMap<String, Integer>();
        scope.put("www.example1.com", -1);
        
        
        LinkSelector linkSelector = new RandomLinkSelector();
        Frontier frontier = new Frontier(tempFolder.newFolder().toString(), 1000, scope);
        FrontierManager frontierManager = new FrontierManager(frontier, hostManager, downloadRobots,
                2, 2, linkSelector, new LinkFilter(new ArrayList<String>()));
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        LinkRelevance selectedLink2 = frontierManager.nextURL();
        
        // then
        assertThat(selectedLink1, is(notNullValue()));
        assertThat(selectedLink1.getURL(), is(notNullValue()));
        assertThat(selectedLink1.getURL(), is(link1.getURL()));
        
        assertThat(selectedLink2, is(nullValue()));
        
        frontierManager.close();
    }
    

    @Test
    public void shouldInsertUrl() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, hostManager, downloadRobots,
                2, 2, linkSelector, emptyLinkFilter);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1, LinkRelevance.Type.FORWARD);
        
        // when
        frontierManager.insert(link1);
        
        LinkRelevance nextURL = frontierManager.nextURL();
        
        // then
        assertThat(nextURL, is(notNullValue()));
        assertThat(nextURL.getURL(), is(notNullValue()));
        assertThat(nextURL.getURL(), is(link1.getURL()));
        assertThat(nextURL.getRelevance(), is(link1.getRelevance()));
        assertThat(nextURL.getType(), is(link1.getType()));
        
        frontierManager.close();
    }
    
    @Test
    public void shouldInsertRobotsLinkWhenAddDomainForTheFirstTime() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        boolean downloadRobots = true;
        FrontierManager frontierManager = new FrontierManager(frontier, hostManager, downloadRobots,
                2, 2, linkSelector, emptyLinkFilter);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/sitemap.xml"), 1, LinkRelevance.Type.FORWARD);
        
        // when
        frontierManager.insert(link1);
        
        
        // then
        LinkRelevance nextURL;
        
        nextURL = frontierManager.nextURL();
        assertThat(nextURL, is(notNullValue()));
        assertThat(nextURL.getURL(), is(notNullValue()));
        assertThat(nextURL.getURL().toString(), is("http://www.example1.com/robots.txt"));
        assertThat(nextURL.getType(), is(LinkRelevance.Type.ROBOTS));
        
        nextURL = frontierManager.nextURL();
        assertThat(nextURL, is(notNullValue()));
        assertThat(nextURL.getURL(), is(notNullValue()));
        assertThat(nextURL.getURL(), is(link1.getURL()));
        assertThat(nextURL.getRelevance(), is(link1.getRelevance()));
        assertThat(nextURL.getType(), is(link1.getType()));
        
        frontierManager.close();
    }
    
    @Test
    public void shouldInsertUrlsAndSelectUrlsInSortedByRelevance() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, hostManager, downloadRobots,
                2, 2, linkSelector, emptyLinkFilter);
        
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
        
        frontierManager.close();
    }
    
    
    @Test
    public void shouldNotReturnAgainALinkThatWasAlreadyReturned() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, hostManager, downloadRobots,
                2, 2, linkSelector, emptyLinkFilter);
        
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
        
        frontierManager.close();
        
    }

}
