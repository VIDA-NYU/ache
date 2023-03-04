package achecrawler.target.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.CloseableIterator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RocksDBTargetRepositoryTest {

    @TempDir
    public File tempFolder;
    static String html;
    static String url;
    static Map<String, List<String>> responseHeaders;

    @BeforeAll
    static void setUp() {
        url = "http://example.com";
        html = "<html><body>Hello World! Hello World! Hello World!</body></html>";
        responseHeaders = new HashMap<>();
        responseHeaders.put("content-type", asList("text/html"));
    }

    @Test
    void shouldInsetGetAndIterate() throws IOException {
        // given
        String dataPath = tempFolder.toString();

        Page target = new Page(new URL(url), html, responseHeaders);
        target.setCrawlerId("mycrawler");
        target.setTargetRelevance(TargetRelevance.RELEVANT);

        RocksDBTargetRepository repository = new RocksDBTargetRepository(dataPath);

        // when
        repository.insert(target);
        repository.close();
        repository = new RocksDBTargetRepository(dataPath);

        // then: should get inserted pages
        Page page = repository.get(url);
        assertThatPagePropertiesAreTheSame(page);

        // then: should iterate over inserted pages
        CloseableIterator<Page> it = repository.pagesIterator();
        assertThat(it.hasNext()).isTrue();
        page = it.next();
        assertThat(page).isNotNull();
        assertThatPagePropertiesAreTheSame(page);
        assertThat(it.hasNext()).isFalse();
    }

    private void assertThatPagePropertiesAreTheSame(Page page) {
        assertThat(page.getContentAsString()).isEqualTo(html);
        assertThat(page.getRequestedUrl()).isEqualTo(url);
        assertThat(page.getResponseHeaders().get("content-type").get(0)).isEqualTo("text/html");
        assertThat(page.getTargetRelevance().isRelevant()).isEqualTo(TargetRelevance.RELEVANT.isRelevant());
        assertThat(page.getTargetRelevance().getRelevance()).isEqualTo(TargetRelevance.RELEVANT.getRelevance());
        assertThat(page.getCrawlerId()).isEqualTo("mycrawler");
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }

}
