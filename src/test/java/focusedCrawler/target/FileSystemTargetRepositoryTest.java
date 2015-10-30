package focusedCrawler.target;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
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
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.FILE);
		
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
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON);
		
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
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.CBOR);
		
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

}
