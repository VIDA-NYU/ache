package focusedCrawler.target;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;


import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

public class TargetModelTest {

    @Test
    public void shouldSerializeAndUnserializeFieldsCorrectly() throws Exception {
        // given
        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);

        TargetModel model = new TargetModel();
        model.setContent("asdf");
        model.setUrl("http://example.org/index.html");
        model.setKey("http://example.org/index.html", "example.org");
        model.resetTimestamp();
        
        // when
        byte[] data = mapper.writeValueAsBytes(model);
        TargetModel readValue = mapper.readValue(data, TargetModel.class);

        // then
        assertThat(readValue, is(notNullValue()));
        assertThat(readValue.key, is(model.key));
        assertThat(readValue.timestamp, is(model.timestamp));
        assertThat(readValue.url, is(model.url));
        assertThat(readValue.imported, is(model.imported));
        assertThat(readValue.request, is(model.request));
        assertThat(readValue.response, is(model.response));
    }

}
