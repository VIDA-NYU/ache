package achecrawler.target.repository;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.TargetModelCbor;
import achecrawler.target.model.TargetModelJson;
import achecrawler.target.repository.FileSystemTargetRepository.DataFormat;
import achecrawler.util.CloseableIterator;


public class FileSystemTargetRepositoryTest {

	// a new temp folder is created for each test case
	@Rule public TemporaryFolder tempFolder = new TemporaryFolder();
	
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
    public void shouldStoreContentCompressed() throws IOException {
        // given
	    boolean compressData = true;
	    String folder = tempFolder.newFolder().toString();
        Page target = new Page(new URL(url), html);
        FileSystemTargetRepository repository = new FileSystemTargetRepository(Paths.get(folder), DataFormat.HTML, false, compressData);
        
        // when
        repository.insert(target);
        
        // then
        Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
        assertThat(path.toFile().exists(), is(true));
        
        byte[] fileBytes = Files.readAllBytes(path);
        assertThat(fileBytes, is(notNullValue()));
        assertThat(fileBytes.length < html.getBytes().length, is(true));
        
        InputStream gzip = new InflaterInputStream(new ByteArrayInputStream(fileBytes));
        byte[] uncompressedBytes = IOUtils.toByteArray(gzip);
        String content = new String(uncompressedBytes);
        assertThat(content, is(html));
    }
	
	
	@Test
    public void shouldStoreAndReadCompressedContent() throws IOException {
        // given
        boolean compressData = true;
        String folder = tempFolder.newFolder().toString();
        Page target = new Page(new URL(url), html);
        FileSystemTargetRepository repository = new FileSystemTargetRepository(Paths.get(folder), DataFormat.JSON, false, compressData);
        
        // when
        repository.insert(target);
        TargetModelJson jsonModel = repository.get(url);
        
        // then
        assertThat(jsonModel, is(notNullValue()));
        assertThat(jsonModel.getUrl(), is(url));
        assertThat(jsonModel.getContentAsString(), is(html));
    }
	
	@Test
	public void shouldStoreContentAsJSON() throws IOException {
		// given
		String folder = tempFolder.newFolder().toString();
		Page target = new Page(new URL(url), html, responseHeaders);
		target.setTargetRelevance(TargetRelevance.IRRELEVANT);
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, false);
		
		// when
		repository.insert(target);
		
		// then
		Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
		
		assertThat(path.toFile().exists(), is(true));
		
		ObjectMapper mapper = new ObjectMapper();
		TargetModelJson value = mapper.readValue(path.toFile(), TargetModelJson.class);
		
		assertThat(value.getUrl(), is(url));
		assertThat(value.getContentAsString(), is(html));
		assertThat(value.getRelevance().isRelevant(), is(TargetRelevance.IRRELEVANT.isRelevant()));
		assertThat(value.getRelevance().getRelevance(), is(TargetRelevance.IRRELEVANT.getRelevance()));
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
		TargetModelCbor value = mapper.readValue(path.toFile(), TargetModelCbor.class);
		
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
    public void sholdGetPageThatWasInserted() throws IOException {
        // given
        boolean hashFilename = true;
        String folder = tempFolder.newFolder().toString(); 
        
        String url1 = "http://example1.com";
        String url2 = "http://example2.com";
        
        Page target1 = new Page(new URL(url1), html);
        target1.setTargetRelevance(TargetRelevance.IRRELEVANT);
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, hashFilename);
        
        // when
        repository.insert(target1);
        TargetModelJson page1 = repository.get(url1);
        TargetModelJson page2 = repository.get(url2);
        
        // then
        assertThat(page1, is(notNullValue()));
        assertThat(page1.getUrl(), is(url1));
        assertThat(page1.getContentAsString(), is(html));
        assertThat(page1.getRelevance().isRelevant(), is(TargetRelevance.IRRELEVANT.isRelevant()));
        assertThat(page1.getRelevance().getRelevance(), is(TargetRelevance.IRRELEVANT.getRelevance()));
        
        assertThat(page2, is(nullValue()));
    }
	
	@Test
    public void sholdIterateOverInsertedPages() throws IOException {
        // given
        boolean hashFilename = true;
        boolean compressData = true;
        
        String folder = tempFolder.newFolder().toString(); 
        
        String url1 = "http://a.com";
        String url2 = "http://b.com";
        
        Page target1 = new Page(new URL(url1), html);
        Page target2 = new Page(new URL(url2), html);
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, hashFilename, compressData);
        
        // when
        repository.insert(target1);
        repository.insert(target2);
        
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
    }
	
	@Test
    public void sholdIterateOverEmptyFolder() throws IOException {
        // given
        boolean hashFilename = true;
        String folder = tempFolder.newFolder().toString(); 
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, hashFilename);
        
        // when
        CloseableIterator<Page> it = repository.pagesIterator();
        
        // then
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
    }
    
    @Test
    public void sholdIterateOverFilePaths() throws IOException {
     // given
        boolean hashFilename = false;
        String folder = tempFolder.newFolder().toString(); 
        
        String url1 = "http://1.com";
        String url2 = "http://2.com";
        
        Page target1 = new Page(new URL(url1), html);
        Page target2 = new Page(new URL(url2), html);
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, hashFilename);
        
        // when
        repository.insert(target1);
        repository.insert(target2);
        
        Iterator<Path> it = repository.filesIterator();
        
        // then
        Path pagePath;
        
        assertThat(it.hasNext(), is(true));
        pagePath = it.next();
        
        assertThat(pagePath, is(notNullValue()));
        assertThat(pagePath.toString(), either(endsWith("1.com")).or(endsWith("2.com")));
        
        assertThat(it.hasNext(), is(true));
        pagePath = it.next();
        
        assertThat(pagePath, is(notNullValue()));
        assertThat(pagePath.toString(), either(endsWith("1.com")).or(endsWith("2.com")));
        
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
        
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
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