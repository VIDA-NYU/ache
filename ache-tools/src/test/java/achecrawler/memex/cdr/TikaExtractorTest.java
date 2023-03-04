package achecrawler.memex.cdr;

import java.io.InputStream;

import org.apache.tika.mime.MediaType;

import org.junit.jupiter.api.Test;

import achecrawler.memex.cdr.TikaExtractor.ParsedData;

import static org.assertj.core.api.Assertions.assertThat;

class TikaExtractorTest {

	@Test
	void testExtractMetadata() {
		// given
		String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
		InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);

		TikaExtractor parser = new TikaExtractor();
		// when
		ParsedData parsedData = parser.parse(fileStream);
		// then
		assertThat(parsedData.getMetadata().get("title")).isEqualTo("Memex (Domain-Specific Search)");
		assertThat(parsedData.getMetadata().get("Content-Type")).contains(("text/html"));

		assertThat(parsedData.getPlainText()).isNotNull();
		assertThat(parsedData.getPlainText()).contains(("Memex"));
	}

	@Test
	void testDetectMimeType() {
		// given
		String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
		InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);

		TikaExtractor parser = new TikaExtractor();

		// when
		MediaType type = parser.detect(fileStream, filename, null);

		// then
		assertThat(type.getBaseType()).isEqualTo(MediaType.TEXT_HTML);
		assertThat(type.getBaseType().toString()).isEqualTo("text/html");
	}

}