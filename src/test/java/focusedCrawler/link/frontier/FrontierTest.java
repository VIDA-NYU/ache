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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;

public class FrontierTest {

    final Path testPath = Paths.get("frontier_temp/");
    private Frontier frontier;
    
    @Before
    public void setUp() throws IOException {
        
        File file = testPath.toFile();
        if(file.exists()) {
            FileUtils.deleteDirectory(file);
        }
        
        PersistentHashtable urlRelevance = new PersistentHashtable(testPath.toString(), 1000);
        LinkSelectionStrategy linkSelector = new BaselineLinkSelector(urlRelevance);
        
        frontier = new Frontier(urlRelevance, linkSelector);
    }
    
    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(testPath.toFile());
    }

    @Test
    public void shouldInsertUrl() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 1);
        
        // when
        frontier.insert(link1);
        
        // then
        assertThat(frontier.exist(link1), is(1));
        assertThat(frontier.exist(link2), is(nullValue()));
    }
    
    @Test
    public void shouldInsertUrlsAndSelectGivenNumberOfUrls() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        
        // when
        frontier.insert(link1);
        frontier.insert(link2);
        
        // then
        LinkRelevance[] urls = frontier.select(1);
        assertThat(urls, is(notNullValue()));
        assertThat(urls.length, is(1));
    }
    
    
    @Test
    public void shouldInsertAndDeleteUrl() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        
        // when
        frontier.insert(link1);
        // then
        assertThat(frontier.exist(link1), is(1));
        assertThat(frontier.select(1).length, is(1));
        
        // when
        frontier.delete(link1);
        // then
        assertThat(frontier.exist(link1), is(-1));
        assertThat(frontier.select(1).length, is(0));
    }

}
