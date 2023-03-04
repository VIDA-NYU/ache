package achecrawler.target.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat(it.hasNext()).isTrue();
		Page page = it.next();
        assertThat(page.getContentAsString()).isEqualTo(html);
        assertThat(page.getFinalUrl()).isEqualTo(url);
        assertThat(page.getResponseHeaders().get("content-type").get(0)).isEqualTo("text/html");
        assertThat(page.getTargetRelevance().isRelevant()).isEqualTo(TargetRelevance.RELEVANT.isRelevant());
        assertThat(page.getTargetRelevance().getRelevance()).isEqualTo(TargetRelevance.RELEVANT.getRelevance());
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
        
        assertThat(it.hasNext()).isTrue();
        page = it.next();
        
        assertThat(page).isNotNull();
        assertThat(page.getContentAsString()).isEqualTo(html);
        
        assertThat(it.hasNext()).isTrue();
        page = it.next();
        
        assertThat(page).isNotNull();
        assertThat(page.getContentAsString()).isEqualTo(html);
        
        assertThat(it.hasNext()).isFalse();
        assertThat(it.next()).isNull();
        
        assertThat(it.hasNext()).isFalse();
        assertThat(it.next()).isNull();
        
        File[] files = new File(folder).listFiles();
        assertThat(files.length).isEqualTo(2);
        assertThat(files[0].length()).isLessThan(maxFileSize);
        assertThat(files[1].length()).isLessThan(maxFileSize);
    }

    @Test
    void shouldIterateOverEmptyFolder() {
        // given
        String folder = tempFolder.toString();
        
        FilesTargetRepository repository = new FilesTargetRepository(folder);
        
        // when
        CloseableIterator<Page> it = repository.pagesIterator();
        
        // then
        assertThat(it.hasNext()).isFalse();
        assertThat(it.next()).isNull();
    }
}