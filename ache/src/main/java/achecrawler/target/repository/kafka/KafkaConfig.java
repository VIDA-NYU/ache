package achecrawler.target.repository.kafka;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KafkaConfig {

    enum Format {
        CDR31, JSON, ELASTIC
    }

    @JsonProperty("target_storage.data_format.kafka.topic_name")
    private String topicName;

    @JsonProperty("target_storage.data_format.kafka.properties")
    private Map<String, Object> properties;

    @JsonProperty("target_storage.data_format.kafka.format")
    private Format format = Format.JSON;

    public String getTopicName() {
        return topicName;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Format getFormat() {
        return format;
    }
}
