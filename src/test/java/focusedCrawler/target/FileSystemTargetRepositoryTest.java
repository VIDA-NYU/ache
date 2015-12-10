package focusedCrawler.target;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import focusedCrawler.target.FileSystemTargetRepository.DataFormat;
import focusedCrawler.util.Page;


public class FileSystemTargetRepositoryTest {

	// a new temp folder is created for each test case
	@Rule public TemporaryFolder tempFolder = new TemporaryFolder();
	
	static String html;
	static String url;
	static Map<String, List<String>> responseHeaders;
	
	@BeforeClass
	static public void setUp() {
		url = "http://example.com";
		html = "<html><bodyHello World!></body></html>";
		responseHeaders = new HashMap<>();
		responseHeaders.put("content-type", asList("text/html"));
	}
	
	@Test
	public void shouldStoreContentAsRawFile() throws IOException {
		// given
		String folder = tempFolder.newFolder().toString(); 
		Page target = new Page(new URL(url), html);
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.HTML, false);
		
		// when
		repository.insert(target);
		
		// then
		Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
		assertThat(path.toFile().exists(), is(true));
		
		String content = new String(Files.readAllBytes(path));
		assertThat(content, is(html));
	}
	
	@Test
	public void shouldStoreContentAsJSON() throws IOException {
		// given
		String folder = tempFolder.newFolder().toString();
		Page target = new Page(new URL(url), html, responseHeaders);
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, false);
		
		// when
		repository.insert(target);
		
		// then
		Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
		
		assertThat(path.toFile().exists(), is(true));
		
		ObjectMapper mapper = new ObjectMapper();
		TargetModelJson value = mapper.readValue(path.toFile(), TargetModelJson.class);
		
		assertThat(value.getUrl(), is(url));
		assertThat(value.getResponseBody(), is(html));
	}
	
	@Test
	public void shouldStoreContentAsCBOR() throws IOException {
		// given
		String folder = tempFolder.newFolder().toString();
		Page target = new Page(new URL(url), html, responseHeaders);
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.CBOR, false);
		
		// when
		repository.insert(target);
		
		// then
		Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
		
		assertThat(path.toFile().exists(), is(true));
		
		ObjectMapper mapper = new ObjectMapper(new CBORFactory());
		TargetModel value = mapper.readValue(path.toFile(), TargetModel.class);
		
		assertThat(value.url, is(url));
		assertThat(value.response.get("body").toString(), is(html));
	}
	
	@Test
    public void shouldHashFilenameUsingSHA256Hash() throws IOException {
        // given
	    boolean hashFilename = true;
        String folder = tempFolder.newFolder().toString(); 
        Page target = new Page(new URL(url), html);
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.HTML, hashFilename);
        
        // when
        repository.insert(target);
        
        // then
        Path path = Paths.get(folder, "example.com", "f0e6a6a97042a4f1f1c87f5f7d44315b2d852c2df5c7991cc66241bf7072d1c4");
        assertThat(path.toFile().exists(), is(hashFilename));
        
        String content = new String(Files.readAllBytes(path));
        assertThat(content, is(html));
    }
	
	@Test
    public void sholdGetPageWasInserted() throws IOException {
        // given
        boolean hashFilename = true;
        String folder = tempFolder.newFolder().toString(); 
        
        String url1 = "http://example1.com";
        String url2 = "http://example2.com";
        
        Page target1 = new Page(new URL(url1), html);
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, hashFilename);
        
        // when
        repository.insert(target1);
        TargetModelJson page1 = repository.get(url1);
        TargetModelJson page2 = repository.get(url2);
        
        // then
        assertThat(page1, is(notNullValue()));
        assertThat(page1.getUrl(), is(url1));
        assertThat(page1.getResponseBody(), is(html));
        
        assertThat(page2, is(nullValue()));
    }
	
	@Test
    public void existsSholdReturnTrueOnlyWhenPageWasInserted() throws IOException {
        // given
        boolean hashFilename = true;
        String folder = tempFolder.newFolder().toString(); 
        
        String url1 = "http://example1.com";
        String url2 = "http://example2.com";
        
        Page target1 = new Page(new URL(url1), html);
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.HTML, hashFilename);
        
        // when
        repository.insert(target1);
        boolean url1exists = repository.exists(url1);
        boolean url2exists = repository.exists(url2);
        
        // then
        assertThat(url1exists, is(true));
        assertThat(url2exists, is(false));
    }

}