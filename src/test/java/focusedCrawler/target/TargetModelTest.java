package focusedCrawler.target;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

public class TargetModelTest {

    @Test
    public void shouldSerializeAndUnserializeFieldsCorrectly() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper(new CBORFactory());

        final String name = "Name";
        final String email = "email@example.com";
        final String body = "Lorem ipsum dolor sit amet";
        final URL url = new URL("http://example.org/index.html");
        
        TargetModel writtenValue = new TargetModel(name, email, url, body);
        
        // when
        byte[] data = mapper.writeValueAsBytes(writtenValue);
        TargetModel readValue = mapper.readValue(data, TargetModel.class);

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
        assertThat(readValue.response.get("body"), is(body));
    }

}
