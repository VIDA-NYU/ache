package achecrawler.link.frontier;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import achecrawler.util.persistence.PersistentHashtable.DB;

public class HostManagerTest {
    
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    
    @Before
    public void setUp() throws Exception {}

    @Test
    public void shouldInsertAndPersistHostData() throws IOException {
        // given
        Path path = Paths.get(tmp.newFolder().getAbsolutePath());
        HostManager hosts = new HostManager(path, DB.ROCKSDB);
        String host = "www.example.com";
        String anotherHost = "www.another-example.com";

        // when
        hosts.insert(host);
        hosts.close();
        hosts = new HostManager(path, DB.ROCKSDB);
        
        assertThat(hosts.isKnown(host), is(true));
        assertThat(hosts.isKnown(anotherHost), is(false));
    }

}
