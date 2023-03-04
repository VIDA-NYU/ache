package achecrawler.link.frontier;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import achecrawler.util.persistence.PersistentHashtable.DB;

import static org.assertj.core.api.Assertions.assertThat;

public class HostManagerTest {

    @TempDir
    public File tmp;

    @BeforeEach
    void setUp() {}

    @Test
    void shouldInsertAndPersistHostData() {
        // given
        Path path = Paths.get(tmp.getAbsolutePath());
        HostManager hosts = new HostManager(path, DB.ROCKSDB);
        String host = "www.example.com";
        String anotherHost = "www.another-example.com";

        // when
        hosts.insert(host);
        hosts.close();
        hosts = new HostManager(path, DB.ROCKSDB);
        
        assertThat(hosts.isKnown(host)).isTrue();
        assertThat(hosts.isKnown(anotherHost)).isFalse();
    }
}
