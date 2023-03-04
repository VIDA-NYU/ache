package achecrawler.minhash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.io.ByteStreams;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicatePageIndexerTest {

    @TempDir
    public File tempFolder;
    
    /* 
     * This test runs multiple times for each of the following parameters,
     * to make sure that it works with all underlying storage implementations.
     */
    public static Iterable<? extends Object> data() {
        return Arrays.asList(true, false);
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldIndexPagesAndFindDuplicates(boolean inMemory) throws Exception {
        DuplicatePageIndexer deduper = initDuplicatePageIndexer(inMemory);
        // given
        String url1 = "http://example.com/index.html";
        String content1 = readFileAsString("ache-docs.html");
        String content2 = readFileAsString("ache-docs-near-duplicate.html");
        String content3 = readFileAsString("ache-docs-install.html");

        // when
        deduper.insert(url1, content1);

        // then
        assertThat(deduper.isNearDuplicate(content2)).isTrue();

        Set<String> dups = deduper.findNearDuplicates(content2);
        assertThat(dups).isNotNull();
        assertThat(dups.size()).isEqualTo(1);
        assertThat(dups.iterator().next()).isEqualTo(url1);

        assertThat(deduper.isNearDuplicate(content3)).isFalse();
    }

    DuplicatePageIndexer initDuplicatePageIndexer(boolean inMemory) {
        if (inMemory) {
            return new DuplicatePageIndexer();
        } else {
            return new DuplicatePageIndexer(tempFolder.toString());
        }
    }

    private String readFileAsString(String filename) throws IOException {
        InputStream fileStream = DuplicatePageIndexerTest.class.getResourceAsStream(filename);
        return new String(ByteStreams.toByteArray(fileStream));
    }

}
