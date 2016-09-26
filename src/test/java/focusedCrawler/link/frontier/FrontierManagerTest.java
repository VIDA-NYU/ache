package focusedCrawler.link.frontier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
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
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkFilter;

public class FrontierManagerTest {

    @Rule
    // a new temp folder is created for each test case
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private LinkFilter emptyLinkFilter = new LinkFilter(new ArrayList<String>());

    private Frontier frontier;
    private String dataPath;
    private boolean downloadRobots = false;

    private int minimumAccessTimeInterval = 0;
    
    @Before
    public void setUp() throws Exception {
        frontier = new Frontier(tempFolder.newFolder().toString(), 1000);
        dataPath = tempFolder.newFolder().toString();
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
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, downloadRobots,
                2, 2, minimumAccessTimeInterval, linkSelector, new LinkFilter(new ArrayList<String>()));
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        DataNotFoundException notFoundException = null;
        try {
            frontierManager.nextURL();
        } catch(DataNotFoundException e) {
            notFoundException = e;
        }
        
        
        // then
        assertThat(selectedLink1, is(notNullValue()));
        assertThat(selectedLink1.getURL(), is(notNullValue()));
        assertThat(selectedLink1.getURL(), is(link1.getURL()));
        
        assertThat(notFoundException, is(notNullValue()));
        assertThat(notFoundException.ranOutOfLinks(), is(true));
        frontierManager.close();
    }
    

    @Test
    public void shouldInsertUrl() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, downloadRobots,
                2, 2, minimumAccessTimeInterval, linkSelector, emptyLinkFilter);
        
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
    public void shouldSelectUrlsInsertedAfterFirstSelect() throws Exception {
        // given
        int minimumAccessTimeInterval = 500;
        int linksToLoad = 2;
        int schedulerMaxLinks = 10;
        LinkSelector linkSelector = new TopkLinkSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, downloadRobots,
                linksToLoad, schedulerMaxLinks, minimumAccessTimeInterval, linkSelector, emptyLinkFilter);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index1.html"), 1, LinkRelevance.Type.FORWARD);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example1.com/index2.html"), 1, LinkRelevance.Type.FORWARD);
        
        LinkRelevance link3 = new LinkRelevance(new URL("http://www.example2.com/index2.html"), 1, LinkRelevance.Type.FORWARD);
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        LinkRelevance nextUrl1 = frontierManager.nextURL();
        
        frontierManager.insert(link3);
        
        // at this point, should not return link2, but it should return link3
        // because it is from another TLD
        LinkRelevance nextUrl3 = frontierManager.nextURL();
            
        
        // then
        assertThat(nextUrl1, is(notNullValue()));
        assertThat(nextUrl1.getURL(), is(notNullValue()));
        assertThat(nextUrl1.getURL(), is(link1.getURL()));
        assertThat(nextUrl1.getRelevance(), is(link1.getRelevance()));
        assertThat(nextUrl1.getType(), is(link1.getType()));
        
        assertThat(nextUrl3, is(notNullValue()));
        assertThat(nextUrl3.getURL(), is(notNullValue()));
        assertThat(nextUrl3.getURL(), is(link3.getURL()));
        assertThat(nextUrl3.getRelevance(), is(link3.getRelevance()));
        assertThat(nextUrl3.getType(), is(link3.getType()));
        
        frontierManager.close();
    }
    
    @Test
    public void shouldInsertRobotsLinkWhenAddDomainForTheFirstTime() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        boolean downloadRobots = true;
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, downloadRobots,
                2, 2, minimumAccessTimeInterval, linkSelector, emptyLinkFilter);
        
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
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, downloadRobots,
                2, 2, minimumAccessTimeInterval, linkSelector, emptyLinkFilter);
        
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
        DataNotFoundException notFoundException = null;
        try {
            frontierManager.nextURL();
        } catch(DataNotFoundException e) {
            notFoundException = e;
        }
        
        // then
        
        // should return only 3 inserted links, 4th should be null 
        assertThat(selectedLink1, is(notNullValue()));
        assertThat(selectedLink2, is(notNullValue()));
        assertThat(selectedLink3, is(notNullValue()));
        assertThat(notFoundException, is(notNullValue()));
        assertThat(notFoundException.ranOutOfLinks(), is(true));
        
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
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, downloadRobots,
                2, 2, minimumAccessTimeInterval , linkSelector, emptyLinkFilter);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        LinkRelevance selectedLink2 = frontierManager.nextURL();
        DataNotFoundException notFoundException1 = null;
        try {
            frontierManager.nextURL();
        } catch(DataNotFoundException e) {
            notFoundException1 = e;
        }
        
        frontierManager.insert(link1); // insert link 1 again, should not be returned
        
        DataNotFoundException notFoundException2 = null;
        try {
            frontierManager.nextURL();
        } catch(DataNotFoundException e) {
            notFoundException2 = e;
        }
        
        // then
        assertThat(selectedLink1, is(notNullValue()));
        assertThat(selectedLink2, is(notNullValue()));
        
        assertThat(notFoundException1, is(notNullValue()));
        assertThat(notFoundException1.ranOutOfLinks(), is(true));
        
        assertThat(notFoundException2, is(notNullValue()));
        assertThat(notFoundException2.ranOutOfLinks(), is(true));
        
        frontierManager.close();
        
    }
    
    @Test
    public void shouldNotReturnLinkReturnedWithinMinimumTimeInterval() throws Exception {
        // given
        int minimumAccessTimeInterval = 500;
        LinkSelector linkSelector = new TopkLinkSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, downloadRobots,
                2, 2, minimumAccessTimeInterval , linkSelector, emptyLinkFilter);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index1.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example1.com/index2.html"), 2);
        
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        
        // when
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        
        DataNotFoundException notFoundException1 = null;
        try {
            frontierManager.nextURL();
        } catch(DataNotFoundException e) {
            notFoundException1 = e;
        }
        
        // should return after minimum time interval
        Thread.sleep(minimumAccessTimeInterval+10);
        LinkRelevance selectedLink2 = frontierManager.nextURL();        
        
        // then
        assertThat(selectedLink1, is(notNullValue()));
        assertThat(selectedLink1.getURL().toString(), is(link2.getURL().toString()));
        
        assertThat(notFoundException1, is(notNullValue()));
        assertThat(notFoundException1.ranOutOfLinks(), is(false));
        
        assertThat(selectedLink2, is(notNullValue()));
        assertThat(selectedLink2, is(notNullValue()));
        
        frontierManager.close();
        
    }

}
