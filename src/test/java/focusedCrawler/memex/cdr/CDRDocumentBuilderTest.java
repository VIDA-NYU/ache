package focusedCrawler.memex.cdr;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

public class CDRDocumentBuilderTest {

	@Test
	public void testSerializeToJsonCDRv2() throws Exception {
		
		String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
		InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);
		String content = new String(ByteStreams.toByteArray(fileStream));
		
		String json = new CDR2Document.Builder()
		        .setUrl("http://www.darpa.mil/program/memex")
		        .setRawContent(content)
		        .setContentType("text/html")
		        .setCrawler("memex-crawler")
		        .setTeam("DARPA")
		        .setVersion("2.0")
		        .setTimestamp(new Date().getTime())
		        .buildAsJson();
		
		JsonNode node = new ObjectMapper().readTree(json);
		
		assertThat(node.get("url"), is(notNullValue()));
		assertThat(node.get("url").asText(), is("http://www.darpa.mil/program/memex"));
		
		assertThat(node.get("raw_content"), is(notNullValue()));
		assertThat(node.get("raw_content").asText(), is(content));
		
		assertThat(node.get("content_type"), is(notNullValue()));
		assertThat(node.get("content_type").asText(), containsString("text/html"));
		
		assertThat(node.get("crawler"), is(notNullValue()));
		assertThat(node.get("crawler").asText(), is("memex-crawler"));
		
		assertThat(node.get("team"), is(notNullValue()));
		assertThat(node.get("team").asText(), is("DARPA"));
		
		assertThat(node.get("timestamp").asLong(), is(not(0L)));
		
		assertThat(node.get("extracted_text"), is(notNullValue()));
		
		assertThat(node.get("extracted_metadata"), is(notNullValue()));
	}
	
	@Test
    public void testSerializeToJsonCDRv3() throws Exception {
        
        String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
        InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);
        String content = new String(ByteStreams.toByteArray(fileStream));
        
        Date date = new Date();
        String json = new CDR3Document.Builder()
                .setUrl("http://www.darpa.mil/program/memex")
                .setRawContent(content)
                .setContentType("text/html")
                .setCrawler("memex-crawler")
                .setTeam("DARPA")
                .setTimestampCrawl(date)
                .setTimestampIndex(date)
                .buildAsJson();
        
        JsonNode node = new ObjectMapper().readTree(json);
        
        assertThat(node.get("url"), is(notNullValue()));
        assertThat(node.get("url").asText(), is("http://www.darpa.mil/program/memex"));
        
        assertThat(node.get("raw_content"), is(notNullValue()));
        assertThat(node.get("raw_content").asText(), is(content));
        
        assertThat(node.get("content_type"), is(notNullValue()));
        assertThat(node.get("content_type").asText(), containsString("text/html"));
        
        assertThat(node.get("crawler"), is(notNullValue()));
        assertThat(node.get("crawler").asText(), is("memex-crawler"));
        
        assertThat(node.get("team"), is(notNullValue()));
        assertThat(node.get("team").asText(), is("DARPA"));
        
        assertThat(node.get("timestamp_index").asText(), is(notNullValue()));
        assertThat(node.get("timestamp_crawl").asText(), is(notNullValue()));
        
        assertThat(node.get("objects"), is(notNullValue()));
        assertThat(node.get("objects").isArray(), is(true));
        
        assertThat(node.get("version").asDouble(), is(3.0d));
        
        
    }

}
