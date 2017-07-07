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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCRecord;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import focusedCrawler.target.classifier.TargetRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.model.TargetModelWarcRecord;
//import focusedCrawler.target.model.TargetModelWarcRecord;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.target.repository.FilesTargetRepository.RepositoryIterator;
//import focusedCrawler.target.repository.WarcTargetRepository;
import focusedCrawler.target.repository.WarcTargetRepository;


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
		// given
	    String folder = tempFolder.newFolder().toString(); 
		Page target = new Page(new URL(url), html, responseHeaders);
		target.setTargetRelevance(TargetRelevance.RELEVANT);
		
		WarcTargetRepository repository = new WarcTargetRepository(folder);
		
		// when
		repository.insert(target);
		repository.close();
		File testFolder = new File(folder);
		if(testFolder.isDirectory()){
		    File[] allFiles = testFolder.listFiles();
		    assertTrue(allFiles[0].getName().startsWith("crawl_data"));
		}
		
		Iterator<WARCRecord> it = repository.iterator();
		assertThat(it.hasNext(), is(true));
		WARCRecord page = (WARCRecord) it.next();
        //assertThat(page.getHeader().getContentType(), is(html));
        assertThat(page.getHeader().getUrl(), is(url));
//        assertThat(page.getResponseHeaders().get("content-type").get(0), is("text/html"));
//        assertThat(page.getRelevance().isRelevant(), is(TargetRelevance.RELEVANT.isRelevant()));
//        assertThat(page.getRelevance().getRelevance(), is(TargetRelevance.RELEVANT.getRelevance()));
	}
	
//	@Test
//    public void shoudNotCreateFilesLargerThanMaximumSize() throws IOException {
//        // given
//        String folder = tempFolder.newFolder().toString(); 
//        
//        String url1 = "http://a.com";
//        String url2 = "http://b.com";
//        
//        Page target1 = new Page(new URL(url1), html);
//        Page target2 = new Page(new URL(url2), html);
//        
//        long maxFileSize = 250;
//        WarcTargetRepository repository = new WarcTargetRepository(folder, maxFileSize);
//        
//        // when
//        repository.insert(target1);
//        repository.insert(target2);
//        repository.close();
//        
//        Iterator<TargetModelWarcRecord> it = repository.iterator();
//        
//        // then
//        TargetModelJson page;
//        
//        assertThat(it.hasNext(), is(true));
//        page = it.next();
//        
//        assertThat(page, is(notNullValue()));
//        assertThat(page.getContentAsString(), is(html));
//        
//        assertThat(it.hasNext(), is(true));
//        page = it.next();
//        
//        assertThat(page, is(notNullValue()));
//        assertThat(page.getContentAsString(), is(html));
//        
//        assertThat(it.hasNext(), is(false));
//        assertThat(it.next(), is(nullValue()));
//        
//        assertThat(it.hasNext(), is(false));
//        assertThat(it.next(), is(nullValue()));
//        
//        File[] files = new File(folder).listFiles();
//        assertThat(files.length, is(2));
//        assertThat(files[0].length(), is(lessThan(maxFileSize)));
//        assertThat(files[1].length(), is(lessThan(maxFileSize)));
//    }
	
//	@Test
//    public void sholdIterateOverEmptyFolder() throws IOException {
//        // given
//        String folder = tempFolder.newFolder().toString(); 
//        
//        FilesTargetRepository repository = new FilesTargetRepository(folder);
//        
//        // when
//        Iterator<TargetModelJson> it = repository.iterator();
//        
//        // then
//        assertThat(it.hasNext(), is(false));
//        assertThat(it.next(), is(nullValue()));
//    }

}