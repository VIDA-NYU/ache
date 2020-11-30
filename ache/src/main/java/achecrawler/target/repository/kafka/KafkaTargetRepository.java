package achecrawler.target.repository.kafka;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import achecrawler.memex.cdr.CDR31Document;
import achecrawler.target.model.Page;
import achecrawler.target.model.TargetModelElasticSearch;
import achecrawler.target.model.TargetModelJson;
import achecrawler.target.repository.TargetRepository;
import achecrawler.target.repository.kafka.KafkaConfig.Format;
import achecrawler.util.CloseableIterator;

public class KafkaTargetRepository implements TargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTargetRepository.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private Producer<String, String> producer;
    private String topicName;
    private KafkaConfig.Format format = KafkaConfig.Format.JSON;

    public KafkaTargetRepository(KafkaConfig config) {
        this(createKafkaClient(config), config.getTopicName(), config.getFormat());
    }

    public KafkaTargetRepository(Producer<String, String> producer, String topicName,
                                 Format format) {
        this.producer = producer;
        this.topicName = topicName;
        this.format = format;
    }

    private static Producer<String, String> createKafkaClient(KafkaConfig config) {
        Properties props = new Properties();
        
        // set default properties for ACHE
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 5);
        props.put("linger.ms", 100);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        if (config.getProperties() != null && !config.getProperties().isEmpty()) {
            logger.info("Configuring Kafka client with properties:");
            for (Entry<String, Object> entry : config.getProperties().entrySet()) {
                props.put(entry.getKey(), entry.getValue());
                logger.info(entry.getKey() + " = " + entry.getValue());
            }
        }
        return new KafkaProducer<>(props);
    }

    @Override
    public boolean insert(Page page) {
        String value;
        switch (format) {
            case CDR31:
                if (page.getContentType().startsWith("text/html")) {
                    CDR31Document obj = new CDR31Document.Builder()
                            .setUrl(page.getFinalUrl())
                            .setTimestampCrawl(new Date(page.getFetchTime()))
                            .setTimestampIndex(new Date())
                            .setContentType(page.getContentType())
                            .setResponseHeaders(page.getResponseHeaders())
                            .setRawContent(page.getContentAsString())
                            .setCrawler(page.getCrawlerId())
                            .build();
                    value = serializeAsJson(obj);
                } else {
                    // TODO: Handle image "objects"
                    return false;
                }
                break;
            case ELASTIC:
                value = serializeAsJson(new TargetModelElasticSearch(page));
                break;
            default:
                value = serializeAsJson(new TargetModelJson(page));
        }
        String key = encodeUrl(page.getURL().toString());

        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);
        try {
            producer.send(record).get();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to push data into kafka topic.", e);
        }
    }

    private String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to URL encode string: " + url, e);
        }
    }

    private String serializeAsJson(Object model) {
        String targetAsJson;
        try {
            targetAsJson = mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TargetModel to JSON.", e);
        }
        return targetAsJson;
    }

    @Override
    public void close() {
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close Kafka producer client", e);
        }
    }

    @Override
    public CloseableIterator<Page> pagesIterator() {
        throw new UnsupportedOperationException(
                "Iterator not supportted for KafkaTargetRepository yet");
    }

}
