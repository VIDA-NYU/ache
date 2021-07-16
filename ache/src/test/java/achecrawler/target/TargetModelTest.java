package achecrawler.target;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import achecrawler.target.model.TargetModelCbor;

public class TargetModelTest {

    @Test
    public void shouldSerializeAndUnserializeFieldsCorrectly() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper(new CBORFactory());

        final String name = "Name";
        final String email = "email@example.com";
        final String body = "Lorem ipsum dolor sit amet";
        final URL url = new URL("http://example.org/index.html");
        
        TargetModelCbor writtenValue = new TargetModelCbor(name, email, url, body);
        
        // when
        byte[] data = mapper.writeValueAsBytes(writtenValue);
        TargetModelCbor readValue = mapper.readValue(data, TargetModelCbor.class);

        // then
        assertThat(readValue, is(notNullValue()));
        
        assertThat(readValue.key, is(notNullValue()));
        assertThat(readValue.key, is(writtenValue.key));
        
        assertThat(readValue.timestamp, is(not(0L)));
        assertThat(readValue.timestamp, is(writtenValue.timestamp));
        
        assertThat(readValue.url, is(writtenValue.url));
        assertThat(readValue.url, is(url.toString()));
        
        assertThat(readValue.imported, is(writtenValue.imported));
        
        assertThat(readValue.request, is(notNullValue()));
        assertThat(readValue.request, is(writtenValue.request));
        
        assertThat(readValue.response, is(notNullValue()));
        assertThat(readValue.response, is(writtenValue.response));
        assertThat(((String)readValue.response.get("body")), is(body));
    }

}
