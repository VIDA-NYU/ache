package achecrawler.minhash;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.io.ByteStreams;

@RunWith(Parameterized.class)
public class DuplicatePageIndexerTest {
    
    @Rule
    // a new temp folder is created for each test case
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Parameter
    public boolean inMemory;

    private DuplicatePageIndexer deduper;
    
    /* 
     * This test runs multiple times for each of the following parameters,
     * to make sure that it works with all underlying storage implementations.
     */
    @Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList(true, false);
    }

    @Before
    public void setUp() throws IOException {
        if (inMemory) {
            deduper = new DuplicatePageIndexer();
        } else {
            deduper = new DuplicatePageIndexer(tempFolder.newFolder().toString());
        }
    }

    @Test
    public void shouldIndexPagesAndFindDuplicates() throws Exception {
        // given
        String url1 = "http://example.com/index.html";
        String content1 = readFileAsString("ache-docs.html");
        String content2 = readFileAsString("ache-docs-near-duplicate.html");
        String content3 = readFileAsString("ache-docs-install.html");

        // when
        deduper.insert(url1, content1);

        // then
        assertThat(deduper.isNearDuplicate(content2), is(true));

        Set<String> dups = deduper.findNearDuplicates(content2);
        assertThat(dups, is(notNullValue()));
        assertThat(dups.size(), is(1));
        assertThat(dups.iterator().next(), is(url1));

        assertThat(deduper.isNearDuplicate(content3), is(false));
    }

    private String readFileAsString(String filename) throws IOException {
        InputStream fileStream = DuplicatePageIndexerTest.class.getResourceAsStream(filename);
        return new String(ByteStreams.toByteArray(fileStream));
    }

}
