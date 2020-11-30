package achecrawler.memex.cdr;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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

	@Test
    public void testSerializeToJsonCDRv31() throws Exception {

        String filename = "http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex";
        InputStream fileStream = CDRDocumentBuilderTest.class.getResourceAsStream(filename);
        String content = new String(ByteStreams.toByteArray(fileStream));
        Map<String, List<String>> responseHeaders = ImmutableMap.of(
            "name1", Arrays.asList("value1", "value2"),
            "name2", Arrays.asList("value3")
        );

        CDR31MediaObject obj = new CDR31MediaObject();
        obj.setContentType("image/png");
        obj.setTimestampCrawl(new Date());
        obj.setObjOriginalUrl("http://example.com/image.png");
        obj.setObjStoredUrl("com/example/ASDF435ASDFAFD");
        obj.setResponseHeaders(responseHeaders);

        Date date = new Date(1497763340874l);
        String json = new CDR31Document.Builder()
                .setUrl("http://www.darpa.mil/program/memex")
                .setRawContent(content)
                .setContentType("text/html")
                .setCrawler("memex-crawler")
                .setTeam("DARPA")
                .setObjects(asList(obj))
                .setResponseHeaders(responseHeaders)
                .setTimestampCrawl(date)
                .setTimestampIndex(date)
                .addExtraField("crawler_id", "mycrawler")
                .buildAsJson();

        JsonNode node = new ObjectMapper().readTree(json);

        assertThat(node.get("url"), is(notNullValue()));
        assertThat(node.get("url").asText(), is("http://www.darpa.mil/program/memex"));

        assertThat(node.get("raw_content"), is(notNullValue()));
        assertThat(node.get("raw_content").asText(), is(content));

        assertThat(node.get("response_headers"), is(notNullValue()));
        assertThat(node.get("response_headers").get("name1").asText(), is("value1,value2"));
        assertThat(node.get("response_headers").get("name2").asText(), is("value3"));

        assertThat(node.get("content_type"), is(notNullValue()));
        assertThat(node.get("content_type").asText(), containsString("text/html"));

        assertThat(node.get("crawler"), is(notNullValue()));
        assertThat(node.get("crawler").asText(), is("memex-crawler"));

        assertThat(node.get("team"), is(notNullValue()));
        assertThat(node.get("team").asText(), is("DARPA"));

        assertThat(node.get("timestamp_index").asText(), is("2017-06-18T05:22:20Z"));
        assertThat(node.get("timestamp_crawl").asText(), is("2017-06-18T05:22:20Z"));

        assertThat(node.get("objects"), is(notNullValue()));
        assertThat(node.get("objects").isArray(), is(true));

        assertThat(node.get("version").asDouble(), is(3.1d));

        JsonNode obj0 = node.get("objects").elements().next();

        assertThat(obj0.get("obj_original_url").asText(), is("http://example.com/image.png"));

        assertThat(obj0.get("response_headers"), is(notNullValue()));
        assertThat(obj0.get("response_headers").get("name1").asText(), is("value1,value2"));
        assertThat(obj0.get("response_headers").get("name2").asText(), is("value3"));

        assertThat(node.get("crawler_id"), is(notNullValue()));
        assertThat(node.get("crawler_id").asText(), is("mycrawler"));
    }

}
