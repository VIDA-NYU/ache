package achecrawler.memex.cdr;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.apache.tika.mime.MediaType;
import org.junit.Test;

import achecrawler.memex.cdr.TikaExtractor.ParsedData;

public class TikaExtractorTest {

	@Test
	public void testExtractMetadata() {
		// given
		String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
		InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);
		
		TikaExtractor parser = new TikaExtractor();
		// when
		ParsedData parsedData = parser.parse(fileStream);
		// then
		assertThat(parsedData.getMetadata().get("title"), is("Memex (Domain-Specific Search)"));
		assertThat(parsedData.getMetadata().get("Content-Type"), containsString(("text/html")));
		
		assertThat(parsedData.getPlainText(), is(notNullValue()));
		assertThat(parsedData.getPlainText(), containsString(("Memex")));
	}
	
	@Test
    public void testDetectMimeType() {
        // given
        String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
        InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);
        
        TikaExtractor parser = new TikaExtractor();

        // when
        MediaType type = parser.detect(fileStream, filename, null);
        
        // then
        assertThat(type.getBaseType(), is(MediaType.TEXT_HTML));
        assertThat(type.getBaseType().toString(), is("text/html"));
    }

}
