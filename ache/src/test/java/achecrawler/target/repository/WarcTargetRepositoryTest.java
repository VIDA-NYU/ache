package achecrawler.target.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.repository.WarcTargetRepository.RepositoryIterator;

public class WarcTargetRepositoryTest {

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
        responseHeaders.put("Content-Type", asList("text/html"));
        responseHeaders.put("Server", asList("Apache"));
    }

    @Test
    void shouldStoreAndIterateOverData() throws IOException {

        String folder = tempFolder.toString();

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
            assertThat(allFiles[0].getName().startsWith("crawl_data")).isTrue();
        }

        Iterator<WARCRecord> it = repository.iterator();

        // then
        assertThat(it.hasNext()).isTrue();
        WARCRecord page = it.next();
        assertThat(it.hasNext()).isFalse();

        assertThat(page.getHeader().getUrl()).isEqualTo(url);

        assertThat(page.getHeader().getHeaderValue("Content-Type")).isEqualTo(WARCConstants.HTTP_RESPONSE_MIMETYPE);

        assertThat(page.getHeader().getHeaderValue("ACHE-IsRelevant")).isEqualTo(target.getTargetRelevance().isRelevant() + "");

        assertThat(Double.valueOf(page.getHeader().getHeaderValue("ACHE-Relevance").toString())).isEqualTo(Double.valueOf(target.getTargetRelevance().getRelevance()));
    }

    @Test
    void shouldStoreAndIterateOverPages() throws IOException {

        String folder = tempFolder.toString();

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
            assertThat(allFiles[0].getName().startsWith("crawl_data")).isTrue();
        }

        Iterator<Page> it = repository.pagesIterator();

        // then
        assertThat(it.hasNext()).isTrue();
        Page page = it.next();
        assertThat(it.hasNext()).isFalse();
        assertThat(page.getRedirectedURL().toString()).isEqualTo(target.getRedirectedURL().toString());
        assertThat(page.getFinalUrl()).isEqualTo(target.getFinalUrl());
        assertThat(page.getFetchTime()).isEqualTo(target.getFetchTime());
        assertThat(page.getTargetRelevance().isRelevant()).isEqualTo(target.getTargetRelevance().isRelevant());
        assertThat(page.getTargetRelevance().getRelevance()).isEqualTo(target.getTargetRelevance().getRelevance());
        assertThat(page.getResponseHeaders().size()).isEqualTo(target.getResponseHeaders().size());
        assertThat(page.getResponseHeaders().get("Content-Type").get(0)).isEqualTo(target.getResponseHeaders().get("Content-Type").get(0));
        assertThat(page.getContentAsString()).isEqualTo(target.getContentAsString());
    }

    @Test
    void testReadingMultipleWarcRecords() throws Exception {
        String folder = tempFolder.toString();

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
                assertThat(ar.getHeader().getUrl()).isEqualTo(url1);
                continue;
            } else if (!readSecond) {
                url = ar.getHeader().getUrl();
                assertThat(ar.getHeader().getUrl()).isEqualTo(url2);
                readSecond = true;
            }
        }
        reader.close();
    }

    @Test
    void testReadingMultipleWarcRecordsUsingIterator() throws Exception {
        // given
        String folder = tempFolder.toString();
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
        assertThat(respositoryIterator.hasNext()).isTrue();
        WARCRecord record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl()).isEqualTo(url1);

        assertThat(respositoryIterator.hasNext()).isTrue();
        record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl()).isEqualTo(url2);

        assertThat(respositoryIterator.hasNext()).isFalse();
    }

    @Test
    void testShouldNotFailWhenThereAreNonASCIICharactersOnHeaders() throws Exception {
        // given
        String folder = tempFolder.toString();

        String url1 = "http://a.com";

        Map<String, List<String>> headers = new HashMap<>();
        char invalidChar = (char) 0x80;
        String headerValue = "inline; filename=\"Invalid_" + invalidChar + "\"";
        headers.put("Content-Disposition", asList(headerValue));

        Page target1 = new Page(new URL(url1), html, headers);

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        repository.insert(target1);
        repository.close();

        RepositoryIterator repositoryIterator = repository.iterator();

        // then
        assertThat(repositoryIterator.hasNext()).isTrue();
        WARCRecord record = repositoryIterator.next();
        assertThat(record.getHeader().getUrl()).isEqualTo(url1);
        String recordData = IOUtils.toString(record);
        assertThat(recordData).contains(html);
        assertThat(recordData).contains(headerValue);

        assertThat(repositoryIterator.hasNext()).isFalse();
    }

    @Test
    void shouldIterateOverEmptyFolder() throws IOException {
        // given
        String folder = tempFolder.toString();

        WarcTargetRepository repository = new WarcTargetRepository(folder);

        // when
        Iterator<WARCRecord> it = repository.iterator();

        // then
        assertThat(it.hasNext()).isFalse();
        assertThat(it.next()).isNull();
    }

    @Test
    void testWritingToAWarcFileWithMaxSize() throws Exception {
        // given
        String folder = tempFolder.toString();

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
        assertThat(allFiles[0].getName().startsWith("crawl_data")).isTrue();
        assertThat(allFiles.length).isEqualTo(2);
        assertThat(allFiles[1].getName().startsWith("crawl_data")).isTrue();

        List<String> allUrls = new ArrayList<>(asList(url1, url2));

        RepositoryIterator respositoryIterator = repository.iterator();

        assertThat(respositoryIterator.hasNext()).isTrue();
        WARCRecord record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl()).isIn(allUrls);

        allUrls.remove(record.getHeader().getUrl());

        assertThat(respositoryIterator.hasNext()).isTrue();
        record = respositoryIterator.next();
        assertThat(record.getHeader().getUrl()).isIn(allUrls);

        allUrls.remove(record.getHeader().getUrl());

        assertThat(allUrls).isEmpty();
    }
}