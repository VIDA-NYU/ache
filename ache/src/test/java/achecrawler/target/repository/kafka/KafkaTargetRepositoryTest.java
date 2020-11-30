package achecrawler.target.repository.kafka;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.memex.cdr.CDR31Document;
import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.TargetModelElasticSearch;
import achecrawler.target.model.TargetModelJson;

public class KafkaTargetRepositoryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ObjectMapper mapper = new ObjectMapper();

    static String html;
    static String url;
    static Map<String, List<String>> responseHeaders;

    @BeforeClass
    static public void setUp() {
        url = "http://example.com";
        html = "<html><body>Hello World! Hello World! Hello World!</body></html>";
        responseHeaders = new HashMap<>();
        responseHeaders.put("content-type", asList("text/html"));
    }

    @Test
    public void shouldSendDataToKafka() throws IOException {
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
        assertThat(page.getContentAsString(), is(html));
        assertThat(page.getUrl(), is(url));
        assertThat(page.getResponseHeaders().get("content-type").get(0), is("text/html"));
        assertThat(page.getRelevance().isRelevant(), is(TargetRelevance.RELEVANT.isRelevant()));
        assertThat(page.getRelevance().getRelevance(), is(TargetRelevance.RELEVANT.getRelevance()));
        assertThat(page.getCrawlerId(), is("mycrawler"));
    }

    @Test
    public void shouldSendDataToKafkaUsingCDR31() throws IOException {
        // given
        Page target = new Page(new URL(url), html, responseHeaders);
        target.setCrawlerId("mycrawler");
        target.setTargetRelevance(TargetRelevance.RELEVANT);
        String topicName = "ache-data-topic";

        StringSerializer ss = new StringSerializer();
        MockProducer<String, String> producer = new MockProducer<>(true, ss, ss);

        KafkaConfig.Format format = KafkaConfig.Format.CDR31;

        KafkaTargetRepository repository = new KafkaTargetRepository(producer, topicName, format);

        // when
        repository.insert(target);
        repository.close();

        // then
        List<ProducerRecord<String, String>> history = producer.history();

        CDR31Document page = mapper.readValue(history.get(0).value(), CDR31Document.class);
        assertThat(page.getRawContent(), is(html));
        assertThat(page.getUrl(), is(url));
        assertThat(page.getResponseHeaders().get("content-type"), is("text/html"));
        assertThat(page.getCrawler(), is("mycrawler"));
    }

    @Test
    public void shouldSendDataToKafkaUsingElasticsearchJsonFormat() throws IOException {
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
        assertThat(page.getHtml(), is(html));
        assertThat(page.getUrl(), is(url));
        assertThat(page.getResponseHeaders().get("content-type").get(0), is("text/html"));
        assertThat(page.getCrawlerId(), is("mycrawler"));
    }

}
