package achecrawler.target.repository;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.CloseableIterator;


public class FilesTargetRepositoryTest {

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
    void shouldStoreAndIterateOverData() throws IOException {
		// given
	    String folder = tempFolder.toString();
		Page target = new Page(new URL(url), html, responseHeaders);
		target.setTargetRelevance(TargetRelevance.RELEVANT);
		
		FilesTargetRepository repository = new FilesTargetRepository(folder);
		
		// when
		repository.insert(target);
		repository.close();
		// then
		CloseableIterator<Page> it = repository.pagesIterator();
		assertThat(it.hasNext(), is(true));
		Page page = it.next();
        assertThat(page.getContentAsString(), is(html));
        assertThat(page.getFinalUrl(), is(url));
        assertThat(page.getResponseHeaders().get("content-type").get(0), is("text/html"));
        assertThat(page.getTargetRelevance().isRelevant(), is(TargetRelevance.RELEVANT.isRelevant()));
        assertThat(page.getTargetRelevance().getRelevance(), is(TargetRelevance.RELEVANT.getRelevance()));
	}

    @Test
    void shouldNotCreateFilesLargerThanMaximumSize() throws IOException {
        // given
        String folder = tempFolder.toString();
        
        String url1 = "http://a.com";
        String url2 = "http://b.com";
        
        Page target1 = new Page(new URL(url1), html);
        Page target2 = new Page(new URL(url2), html);
        
        long maxFileSize = 250;
        FilesTargetRepository repository = new FilesTargetRepository(folder, maxFileSize);
        
        // when
        repository.insert(target1);
        repository.insert(target2);
        repository.close();
        
        CloseableIterator<Page> it = repository.pagesIterator();
        
        // then
        Page page;
        
        assertThat(it.hasNext(), is(true));
        page = it.next();
        
        assertThat(page, is(notNullValue()));
        assertThat(page.getContentAsString(), is(html));
        
        assertThat(it.hasNext(), is(true));
        page = it.next();
        
        assertThat(page, is(notNullValue()));
        assertThat(page.getContentAsString(), is(html));
        
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
        
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
        
        File[] files = new File(folder).listFiles();
        assertThat(files.length, is(2));
        assertThat(files[0].length(), is(lessThan(maxFileSize)));
        assertThat(files[1].length(), is(lessThan(maxFileSize)));
    }

    @Test
    void shouldIterateOverEmptyFolder() {
        // given
        String folder = tempFolder.toString();
        
        FilesTargetRepository repository = new FilesTargetRepository(folder);
        
        // when
        CloseableIterator<Page> it = repository.pagesIterator();
        
        // then
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
    }
}