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

public class FrontierTest {

    final Path testPath = Paths.get("frontier_temp/");
    private Frontier frontier;
    
    @Before
    public void setUp() throws IOException {
        
        File file = testPath.toFile();
        if(file.exists()) {
            FileUtils.deleteDirectory(file);
        }
        
        frontier = new Frontier(testPath.toString(), 1000);
    }
    
    @After
    public void tearDown() throws IOException {
        frontier.close();
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
        assertThat(frontier.exist(link1), is(notNullValue()));
        assertThat(frontier.exist(link1), is(1));
        
        assertThat(frontier.exist(link2), is(notNullValue()));
        assertThat(frontier.exist(link2), is(2));
    }
    
    
    @Test
    public void shouldInsertAndDeleteUrl() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        
        // when
        frontier.insert(link1);
        // then
        assertThat(frontier.exist(link1), is(1));
        
        // when
        frontier.delete(link1);
        // then
        assertThat(frontier.exist(link1), is(-1));
    }

}
