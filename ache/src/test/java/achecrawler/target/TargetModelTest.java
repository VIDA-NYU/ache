package achecrawler.target;

import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import achecrawler.target.model.TargetModelCbor;

import static org.assertj.core.api.Assertions.assertThat;

class TargetModelTest {

    @Test
    void shouldSerializeAndUnserializeFieldsCorrectly() throws Exception {
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
        assertThat(readValue).isNotNull();
        
        assertThat(readValue.key).isNotNull();
        assertThat(readValue.key).isEqualTo(writtenValue.key);
        
        assertThat(readValue.timestamp).isNotEqualTo(0L);
        assertThat(readValue.timestamp).isEqualTo(writtenValue.timestamp);
        
        assertThat(readValue.url).isEqualTo(writtenValue.url);
        assertThat(readValue.url).isEqualTo(url.toString());
        
        assertThat(readValue.imported).isEqualTo(writtenValue.imported);
        
        assertThat(readValue.request).isNotNull();
        assertThat(readValue.request).isEqualTo(writtenValue.request);
        
        assertThat(readValue.response).isNotNull();
        assertThat(readValue.response).isEqualTo(writtenValue.response);
        assertThat(((String) readValue.response.get("body"))).isEqualTo(body);
    }

}
