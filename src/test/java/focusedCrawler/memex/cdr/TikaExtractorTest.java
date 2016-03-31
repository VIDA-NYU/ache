package focusedCrawler.memex.cdr;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

import focusedCrawler.memex.cdr.TikaExtractor;

public class TikaExtractorTest {

	@Test
	public void testExtractMetadata() {
		// given
		String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
		InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);
		
		// when
		TikaExtractor extractor = new TikaExtractor(fileStream);
		
		// then
		assertThat(extractor.getMetadata().get("title"), is("Memex (Domain-Specific Search)"));
		assertThat(extractor.getMetadata().get("Content-Type"), containsString(("text/html")));
		
		assertThat(extractor.getPlainText(), is(notNullValue()));
		assertThat(extractor.getPlainText(), containsString(("Memex")));
	}

}
