package focusedCrawler.target;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCWriter;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import focusedCrawler.target.classifier.TargetRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.repository.WarcTargetRepository;
import focusedCrawler.target.repository.WarcTargetRepository.RepositoryIterator;

public class WarcTargetRepositoryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

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
    public void shouldStoreAndIterageOverData() throws IOException {

        String folder = tempFolder.newFolder().toString();
        Page target = new Page(new URL(url), html, responseHeaders);
        target.setTargetRelevance(TargetRelevance.RELEVANT);
        target.setFetchTime(System.currentTimeMillis() / 1000);

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        repository.insert(target);
        repository.close();
        File testFolder = new File(folder);

        if (testFolder.isDirectory()) {
            File[] allFiles = testFolder.listFiles();
            assertTrue(allFiles[0].getName().startsWith("crawl_data"));
        }

        Iterator<WARCRecord> it = repository.iterator();
        assertThat(it.hasNext(), is(true));
        WARCRecord page = (WARCRecord) it.next();

        assertThat(page.getHeader().getUrl(), is(url));
        assertThat(page.getHeader().getHeaderValue("content-type"), is("[text/html]"));
        assertThat(page.getHeader().getHeaderValue("isRelevant"), is(TargetRelevance.RELEVANT.isRelevant() + ""));
        assertThat(page.getHeader().getHeaderValue("relevance"), is(TargetRelevance.RELEVANT.getRelevance() + ""));
    }

    @Test
    public void testReadingMultipleWarcRecords() throws Exception {
        String folder = tempFolder.newFolder().toString();

        String url1 = "http://a.com";
        String url2 = "http://b.com";

        Page target1 = new Page(new URL(url1), html);
        Page target2 = new Page(new URL(url2), html);

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        repository.insert(target1);
        repository.insert(target2);
        repository.close();

        WARCWriter writer = repository.getWriter();
        WARCReader reader = WARCReaderFactory.get(writer.getFile());

        // Get to second record. Get its offset for later use.
        boolean readFirst = false;
        boolean readSecond = false;
        for (final Iterator<ArchiveRecord> i = reader.iterator(); i.hasNext();) {
            WARCRecord ar = (WARCRecord) i.next();
            if (!readFirst) {
                readFirst = true;
                assertThat(ar.getHeader().getUrl(), is(url1));
                continue;
            }
            if (!readSecond) {
                url = ar.getHeader().getUrl();
                assertThat(ar.getHeader().getUrl(), is(url2));
                readSecond = true;
            }
        }
        reader.close();
    }

    @Test
    public void testReadingMultipleWarcRecordsUsingIterator() throws Exception {

        String folder = tempFolder.newFolder().toString();

        String url1 = "http://a.com";
        String url2 = "http://b.com";

        Page target1 = new Page(new URL(url1), html);
        Page target2 = new Page(new URL(url2), html);

        WarcTargetRepository repository = new WarcTargetRepository(folder);
        // when
        repository.insert(target1);
        repository.insert(target2);
        repository.close();

        RepositoryIterator respositoryIterator = repository.iterator();
        if (respositoryIterator.hasNext()) {
            WARCRecord ar = (WARCRecord) respositoryIterator.next();
            assertThat(ar.getHeader().getUrl(), is(url1));
        }

        if (respositoryIterator.hasNext()) {
            WARCRecord ar = (WARCRecord) respositoryIterator.next();
            assertThat(ar.getHeader().getUrl(), is(url2));
        }
    }


    @Test
    public void sholdIterateOverEmptyFolder() throws IOException {
        // given
        String folder = tempFolder.newFolder().toString();

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        Iterator<WARCRecord> it = repository.iterator();

        // then
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
    }

}