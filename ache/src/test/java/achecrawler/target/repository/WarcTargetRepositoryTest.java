package achecrawler.target.repository;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.IOUtils;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCWriter;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.repository.WarcTargetRepository.RepositoryIterator;

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
        responseHeaders.put("Content-Type", asList("text/html"));
        responseHeaders.put("Server", asList("Apache"));
    }

    @Test
    public void shouldStoreAndIterageOverData() throws IOException {

        String folder = tempFolder.newFolder().toString();

        Page target = new Page(new URL(url), html, responseHeaders);
        target.setTargetRelevance(TargetRelevance.RELEVANT);
        target.setFetchTime(System.currentTimeMillis());

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

        // then
        assertThat(it.hasNext(), is(true));
        WARCRecord page = it.next();
        assertThat(it.hasNext(), is(false));

        assertThat(page.getHeader().getUrl(), is(url));

        assertThat(page.getHeader().getHeaderValue("Content-Type"),
                is(WARCConstants.HTTP_RESPONSE_MIMETYPE));

        assertThat(page.getHeader().getHeaderValue("ACHE-IsRelevant"),
                is(target.getTargetRelevance().isRelevant() + ""));

        assertThat(Double.valueOf(page.getHeader().getHeaderValue("ACHE-Relevance").toString()),
                is(Double.valueOf(target.getTargetRelevance().getRelevance())));
    }

    @Test
    public void shouldStoreAndIterateOverPages() throws IOException {

        String folder = tempFolder.newFolder().toString();

        Page target = new Page(new URL(url), html.getBytes(), responseHeaders,
                new URL("http://example.com/site/index.php"));
        target.setTargetRelevance(new TargetRelevance(false, 0.4d));
        target.setFetchTime(Instant.parse("2017-10-19T18:03:17Z").toEpochMilli());
        WarcTargetRepository repository = new WarcTargetRepository(folder);
        // when
        repository.insert(target);
        repository.close();
        File testFolder = new File(folder);

        if (testFolder.isDirectory()) {
            File[] allFiles = testFolder.listFiles();
            assertTrue(allFiles[0].getName().startsWith("crawl_data"));
        }

        Iterator<Page> it = repository.pagesIterator();

        // then
        assertThat(it.hasNext(), is(true));
        Page page = it.next();
        assertThat(it.hasNext(), is(false));
        assertThat(page.getRedirectedURL().toString(), is(target.getRedirectedURL().toString()));
        assertThat(page.getFinalUrl(), is(target.getFinalUrl()));
        assertThat(page.getFetchTime(), is(target.getFetchTime()));
        assertThat(page.getTargetRelevance().isRelevant(),
                is(target.getTargetRelevance().isRelevant()));
        assertThat(page.getTargetRelevance().getRelevance(),
                is(target.getTargetRelevance().getRelevance()));
        assertThat(page.getResponseHeaders().size(), is(target.getResponseHeaders().size()));
        assertThat(page.getResponseHeaders().get("Content-Type").get(0),
                is(target.getResponseHeaders().get("Content-Type").get(0)));
        assertThat(page.getContentAsString(),
                is(target.getContentAsString()));
    }

    @Test
    public void testReadingMultipleWarcRecords() throws Exception {
        String folder = tempFolder.newFolder().toString();

        String url1 = "http://a.com";
        String url2 = "http://b.com";

        Page target1 = new Page(new URL(url1), html, responseHeaders);
        target1.setFetchTime(System.currentTimeMillis());

        Page target2 = new Page(new URL(url2), html, responseHeaders);
        target2.setFetchTime(System.currentTimeMillis());

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        repository.insert(target1);
        repository.insert(target2);
        repository.close();

        WARCWriter writer = repository.getWriter();
        WARCReader reader = WARCReaderFactory.get(writer.getFile());

        // Get to second record. Get its offset for later use.
        boolean readWarcInfoRecord = false;
        boolean readFirst = false;
        boolean readSecond = false;

        for (final Iterator<ArchiveRecord> i = reader.iterator(); i.hasNext();) {
            WARCRecord ar = (WARCRecord) i.next();
            if (!readWarcInfoRecord) {
                readWarcInfoRecord = true;
            } else if (!readFirst) {
                readFirst = true;
                assertThat(ar.getHeader().getUrl(), is(url1));
                continue;
            } else if (!readSecond) {
                url = ar.getHeader().getUrl();
                assertThat(ar.getHeader().getUrl(), is(url2));
                readSecond = true;
            }
        }
        reader.close();
    }

    @Test
    public void testReadingMultipleWarcRecordsUsingIterator() throws Exception {
        // given
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

        // then
        assertTrue(respositoryIterator.hasNext());
        WARCRecord record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl(), is(url1));

        assertTrue(respositoryIterator.hasNext());
        record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl(), is(url2));

        assertFalse(respositoryIterator.hasNext());
    }

    @Test
    public void testShouldNotFailWhenThereAreNonASCIICharactersOnHeaders() throws Exception {
        // given
        String folder = tempFolder.newFolder().toString();

        String url1 = "http://a.com";

        Map<String, List<String>> headers = new HashMap<>();
        Character invalidChar = new Character((char) 0x80);
        String headerValue = "inline; filename=\"Invalid_" + invalidChar + "\"";
        headers.put("Content-Disposition", asList(headerValue));

        Page target1 = new Page(new URL(url1), html, headers);

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        repository.insert(target1);
        repository.close();

        RepositoryIterator respositoryIterator = repository.iterator();

        // then
        assertTrue(respositoryIterator.hasNext());
        WARCRecord record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl(), is(url1));
        String recordData = IOUtils.toString(record);
        assertThat(recordData, containsString(html));
        assertThat(recordData, containsString(headerValue));

        assertFalse(respositoryIterator.hasNext());
    }

    @Test
    public void shouldIterateOverEmptyFolder() throws IOException {
        // given
        String folder = tempFolder.newFolder().toString();

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        Iterator<WARCRecord> it = repository.iterator();

        // then
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
    }
	
    @Test
    public void testWritingToAWarcFileWithMaxSize() throws Exception {
        // given
        String folder = tempFolder.newFolder().toString();

        String url1 = "http://a.com";
        String url2 = "http://b.com";

        Page target1 = new Page(new URL(url1), html, responseHeaders);
        Page target2 = new Page(new URL(url2), html);

        target1.setTargetRelevance(TargetRelevance.RELEVANT);
        target2.setTargetRelevance(TargetRelevance.IRRELEVANT);

        WarcTargetRepository repository = new WarcTargetRepository(folder, 400);

        // when
        repository.insert(target1);
        repository.insert(target2);
        repository.close();

        // then
        File[] allFiles = new File(folder).listFiles();
        assertTrue(allFiles[0].getName().startsWith("crawl_data"));
        assertThat(allFiles.length, is(2));
        assertTrue(allFiles[1].getName().startsWith("crawl_data"));

        List<String> allUrls = new ArrayList<>(asList(url1, url2));

        RepositoryIterator respositoryIterator = repository.iterator();

        assertTrue(respositoryIterator.hasNext());
        WARCRecord record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl(), isIn(allUrls));

        allUrls.remove(record.getHeader().getUrl());

        assertTrue(respositoryIterator.hasNext());
        record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl(), isIn(allUrls));

        allUrls.remove(record.getHeader().getUrl());

        assertThat(allUrls, empty());
    }

}