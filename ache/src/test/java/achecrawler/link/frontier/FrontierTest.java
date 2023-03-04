package achecrawler.link.frontier;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import achecrawler.util.persistence.PersistentHashtable.DB;

import static org.assertj.core.api.Assertions.assertThat;

public class FrontierTest {

    @TempDir
    public File tempFolder;

    public Path testPath;
    
    private Frontier frontier;

    @BeforeEach
    void setUp() {
        testPath = Paths.get(tempFolder.toString());
        frontier = new Frontier(testPath.toString(), 1000, DB.ROCKSDB);
    }

    @AfterEach
    void tearDown() throws IOException {
        frontier.close();
        FileUtils.deleteDirectory(testPath.toFile());
    }

    @Test
    void shouldInsertUrl() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 1);
        
        // when
        frontier.insert(link1);
        
        // then
        assertThat(frontier.exist(link1)).isEqualTo(1d);
        assertThat(frontier.exist(link2)).isNull();
    }

    @Test
    void shouldInsertUrlsAndSelectGivenNumberOfUrls() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        
        // when
        frontier.insert(link1);
        frontier.insert(link2);
        
        // then
        assertThat(frontier.exist(link1)).isNotNull();
        assertThat(frontier.exist(link1)).isEqualTo(1d);
        
        assertThat(frontier.exist(link2)).isNotNull();
        assertThat(frontier.exist(link2)).isEqualTo(2d);
    }


    @Test
    void shouldInsertAndDeleteUrl() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        
        // when
        frontier.insert(link1);
        // then
        assertThat(frontier.exist(link1)).isEqualTo(1d);
        
        // when
        frontier.delete(link1);
        // then
        assertThat(frontier.exist(link1)).isEqualTo(-1d);
    }
}
