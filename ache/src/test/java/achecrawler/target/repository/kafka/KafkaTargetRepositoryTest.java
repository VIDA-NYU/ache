package achecrawler.target.repository.kafka;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.TargetModelElasticSearch;
import achecrawler.target.model.TargetModelJson;

class KafkaTargetRepositoryTest {

    private final ObjectMapper mapper = new ObjectMapper();

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
    void shouldSendDataToKafka() throws IOException {
        // given
        Page target = new Page(new URL(url), html, responseHeaders);
        target.setCrawlerId("mycrawler");
        target.setTargetRelevance(TargetRelevance.RELEVANT);
        String topicName = "ache-data-topic";

        StringSerializer ss = new StringSerializer();
        MockProducer<String, String> producer = new MockProducer<>(true, ss, ss);

        KafkaConfig.Format format = KafkaConfig.Format.JSON;

        KafkaTargetRepository repository = new KafkaTargetRepository(producer, topicName, format);

        // when
        repository.insert(target);
        repository.close();

        // then
        List<ProducerRecord<String, String>> history = producer.history();

        TargetModelJson page = mapper.readValue(history.get(0).value(), TargetModelJson.class);
        assertThat(page.getContentAsString()).isEqualTo(html);
        assertThat(page.getUrl()).isEqualTo(url);
        assertThat(page.getResponseHeaders().get("content-type").get(0)).isEqualTo("text/html");
        assertThat(page.getRelevance().isRelevant()).isEqualTo(TargetRelevance.RELEVANT.isRelevant());
        assertThat(page.getRelevance().getRelevance()).isEqualTo(TargetRelevance.RELEVANT.getRelevance());
        assertThat(page.getCrawlerId()).isEqualTo("mycrawler");
    }

    @Test
    void shouldSendDataToKafkaUsingElasticsearchJsonFormat() throws IOException {
        // given
        Page target = new Page(new URL(url), html, responseHeaders);
        target.setCrawlerId("mycrawler");
        target.setTargetRelevance(TargetRelevance.RELEVANT);
        String topicName = "ache-data-topic";

        StringSerializer ss = new StringSerializer();
        MockProducer<String, String> producer = new MockProducer<>(true, ss, ss);

        KafkaConfig.Format format = KafkaConfig.Format.ELASTIC;

        KafkaTargetRepository repository = new KafkaTargetRepository(producer, topicName, format);

        // when
        repository.insert(target);
        repository.close();

        // then
        List<ProducerRecord<String, String>> history = producer.history();

        TargetModelElasticSearch page =
                mapper.readValue(history.get(0).value(), TargetModelElasticSearch.class);
        assertThat(page.getHtml()).isEqualTo(html);
        assertThat(page.getUrl()).isEqualTo(url);
        assertThat(page.getResponseHeaders().get("content-type").get(0)).isEqualTo("text/html");
        assertThat(page.getCrawlerId()).isEqualTo("mycrawler");
    }

}
