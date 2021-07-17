package achecrawler.target.repository;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.CloseableIterator;
import achecrawler.util.parser.PaginaURL;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class RocksDBTargetRepositoryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ObjectMapper mapper = new ObjectMapper();

    static String html;
    static String url;
    static Map<String, List<String>> responseHeaders;

    @BeforeClass
    static public void setUp() {
        url = "http://example.com";
        html = "<html><body>Hello World! Hello World! Hello World!</body></html>";
        responseHeaders = new HashMap<>();
        responseHeaders.put("content-type", asList("text/html"));
    }

    @Test
    public void shouldInsetGetAndIterate() throws IOException {
        // given
        String dataPath = tempFolder.newFolder().toString();

        Page target = new Page(new URL(url), html, responseHeaders);
        target.setCrawlerId("mycrawler");
        target.setTargetRelevance(TargetRelevance.RELEVANT);
        PaginaURL pageParser = new PaginaURL(target);
        target.setParsedData(new ParsedData(pageParser));

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
        assertThat(it.hasNext(), is(true));
        page = it.next();
        assertThat(page, is(notNullValue()));
        assertThatPagePropertiesAreTheSame(page);
        assertThat(it.hasNext(), is(false));
    }

    private void assertThatPagePropertiesAreTheSame(Page page) {
        assertThat(page.getContentAsString(), is(html));
        assertThat(page.getRequestedUrl(), is(url));
        assertThat(page.getResponseHeaders().get("content-type").get(0), is("text/html"));
        assertThat(page.getTargetRelevance().isRelevant(), is(TargetRelevance.RELEVANT.isRelevant()));
        assertThat(page.getTargetRelevance().getRelevance(), is(TargetRelevance.RELEVANT.getRelevance()));
        assertThat(page.getCrawlerId(), is("mycrawler"));
    }

}
