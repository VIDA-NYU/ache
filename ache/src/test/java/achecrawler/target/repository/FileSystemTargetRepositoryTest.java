package achecrawler.target.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    void shouldStoreContentAsRawFile() throws IOException {
		// given
	    String folder = tempFolder.toString();
		Page target = new Page(new URL(url), html);
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.HTML, false);
		
		// when
		repository.insert(target);
		
		// then
		Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
		assertThat(path.toFile().exists()).isTrue();
		
		String content = new String(Files.readAllBytes(path));
		assertThat(content).isEqualTo(html);
	}

    @Test
    void shouldStoreContentCompressed() throws IOException {
        // given
	    boolean compressData = true;
	    String folder = tempFolder.toString();
        Page target = new Page(new URL(url), html);
        FileSystemTargetRepository repository = new FileSystemTargetRepository(Paths.get(folder), DataFormat.HTML, false, compressData);
        
        // when
        repository.insert(target);
        
        // then
        Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
        assertThat(path.toFile().exists()).isTrue();
        
        byte[] fileBytes = Files.readAllBytes(path);
        assertThat(fileBytes).isNotNull();
        assertThat(fileBytes.length < html.getBytes().length).isTrue();
        
        InputStream gzip = new InflaterInputStream(new ByteArrayInputStream(fileBytes));
        byte[] uncompressedBytes = IOUtils.toByteArray(gzip);
        String content = new String(uncompressedBytes);
        assertThat(content).isEqualTo(html);
    }


    @Test
    void shouldStoreAndReadCompressedContent() throws IOException {
        // given
        boolean compressData = true;
        String folder = tempFolder.toString();
        Page target = new Page(new URL(url), html);
        FileSystemTargetRepository repository = new FileSystemTargetRepository(Paths.get(folder), DataFormat.JSON, false, compressData);
        
        // when
        repository.insert(target);
        TargetModelJson jsonModel = repository.get(url);
        
        // then
        assertThat(jsonModel).isNotNull();
        assertThat(jsonModel.getUrl()).isEqualTo(url);
        assertThat(jsonModel.getContentAsString()).isEqualTo(html);
    }

    @Test
    void shouldStoreContentAsJSON() throws IOException {
		// given
		String folder = tempFolder.toString();
		Page target = new Page(new URL(url), html, responseHeaders);
		target.setTargetRelevance(TargetRelevance.IRRELEVANT);
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, false);
		
		// when
		repository.insert(target);
		
		// then
		Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
		
		assertThat(path.toFile().exists()).isTrue();
		
		ObjectMapper mapper = new ObjectMapper();
		TargetModelJson value = mapper.readValue(path.toFile(), TargetModelJson.class);
		
		assertThat(value.getUrl()).isEqualTo(url);
		assertThat(value.getContentAsString()).isEqualTo(html);
		assertThat(value.getRelevance().isRelevant()).isEqualTo(TargetRelevance.IRRELEVANT.isRelevant());
		assertThat(value.getRelevance().getRelevance()).isEqualTo(TargetRelevance.IRRELEVANT.getRelevance());
	}

    @Test
    void shouldStoreContentAsCBOR() throws IOException {
		// given
		String folder = tempFolder.toString();
		Page target = new Page(new URL(url), html, responseHeaders);
		FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.CBOR, false);
		
		// when
		repository.insert(target);
		
		// then
		Path path = Paths.get(folder, "example.com", "http%3A%2F%2Fexample.com");
		
		assertThat(path.toFile().exists()).isTrue();
		
		ObjectMapper mapper = new ObjectMapper(new CBORFactory());
		TargetModelCbor value = mapper.readValue(path.toFile(), TargetModelCbor.class);
		
		assertThat(value.url).isEqualTo(url);
		assertThat(value.response.get("body").toString()).isEqualTo(html);
	}

    @Test
    void shouldHashFilenameUsingSHA256Hash() throws IOException {
        // given
	    boolean hashFilename = true;
        String folder = tempFolder.toString();
        Page target = new Page(new URL(url), html);
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.HTML, hashFilename);
        
        // when
        repository.insert(target);
        
        // then
        Path path = Paths.get(folder, "example.com", "f0e6a6a97042a4f1f1c87f5f7d44315b2d852c2df5c7991cc66241bf7072d1c4");
        assertThat(path.toFile().exists()).isEqualTo(hashFilename);
        
        String content = new String(Files.readAllBytes(path));
        assertThat(content).isEqualTo(html);
    }

    @Test
    void shouldGetPageThatWasInserted() throws IOException {
        // given
        boolean hashFilename = true;
        String folder = tempFolder.toString();
        
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
        assertThat(page1).isNotNull();
        assertThat(page1.getUrl()).isEqualTo(url1);
        assertThat(page1.getContentAsString()).isEqualTo(html);
        assertThat(page1.getRelevance().isRelevant()).isEqualTo(TargetRelevance.IRRELEVANT.isRelevant());
        assertThat(page1.getRelevance().getRelevance()).isEqualTo(TargetRelevance.IRRELEVANT.getRelevance());
        
        assertThat(page2).isNull();
    }

    @Test
    void shouldIterateOverInsertedPages() throws IOException {
        // given
        boolean hashFilename = true;
        boolean compressData = true;
        
        String folder = tempFolder.toString();
        
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
    }

    @Test
    void shouldIterateOverEmptyFolder() {
        // given
        boolean hashFilename = true;
        String folder = tempFolder.toString();
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.JSON, hashFilename);
        
        // when
        CloseableIterator<Page> it = repository.pagesIterator();
        
        // then
        assertThat(it.hasNext()).isFalse();
        assertThat(it.next()).isNull();
    }

    @Test
    void shouldIterateOverFilePaths() throws IOException {
     // given
        boolean hashFilename = false;
        String folder = tempFolder.toString();
        
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
        
        assertThat(it.hasNext()).isTrue();
        pagePath = it.next();
        
        assertThat(pagePath).isNotNull();
        assertThat(pagePath.toString()).satisfiesAnyOf(
            path -> path.endsWith("1.com"),
            path -> path.endsWith("2.com")
        );
        
        assertThat(it.hasNext()).isTrue();
        pagePath = it.next();
        
        assertThat(pagePath).isNotNull();
        assertThat(pagePath.toString()).satisfiesAnyOf(
            path -> path.endsWith("1.com"),
            path -> path.endsWith("2.com")
        );
        
        assertThat(it.hasNext()).isFalse();
        assertThat(it.next()).isNull();
        
        assertThat(it.hasNext()).isFalse();
        assertThat(it.next()).isNull();
    }

    @Test
    void existsShouldReturnTrueOnlyWhenPageWasInserted() throws IOException {
        // given
        boolean hashFilename = true;
        String folder = tempFolder.toString();
        
        String url1 = "http://example1.com";
        String url2 = "http://example2.com";
        
        Page target1 = new Page(new URL(url1), html);
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(folder, DataFormat.HTML, hashFilename);
        
        // when
        repository.insert(target1);
        boolean url1exists = repository.exists(url1);
        boolean url2exists = repository.exists(url2);
        
        // then
        assertThat(url1exists).isTrue();
        assertThat(url2exists).isFalse();
    }
}