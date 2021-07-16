package achecrawler.target.repository.elasticsearch;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchConfig {

    //
    // Elasticsearch REST API parameters
    //
    @JsonProperty("target_storage.data_format.elasticsearch.rest.hosts")
    private List<String> restApiHosts = null;

    @JsonProperty("target_storage.data_format.elasticsearch.rest.connect_timeout")
    private int restConnectTimeout = 30000;

    @JsonProperty("target_storage.data_format.elasticsearch.rest.socket_timeout")
    private int restSocketTimeout = 30000;

    @JsonProperty("target_storage.data_format.elasticsearch.rest.max_retry_timeout_millis")
    private int restMaxRetryTimeoutMillis = 60000;

    @JsonProperty("target_storage.data_format.elasticsearch.rest.initial_connection_timeout")
    private int restClientInitialConnectionTimeout = 30000;

    //
    // Index and type parameters
    //
    @JsonProperty("target_storage.data_format.elasticsearch.index_name")
    private String indexName = "ache-data";

    @JsonProperty("target_storage.data_format.elasticsearch.type_name")
    private String typeName = "page";

    public ElasticSearchConfig() {}

    public ElasticSearchConfig(List<String> restApiHosts) {
        this.restApiHosts = restApiHosts;
    }

    public List<String> getRestApiHosts() {
        return restApiHosts;
    }

    public int getRestConnectTimeout() {
        return restConnectTimeout;
    }

    public int getRestSocketTimeout() {
        return restSocketTimeout;
    }

    public int getRestMaxRetryTimeoutMillis() {
        return restMaxRetryTimeoutMillis;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getRestClientInitialConnectionTimeout() {
        return restClientInitialConnectionTimeout;
    }

    public void setRestClientInitialConnectionTimeout(int restClientInitialConnectionTimeout) {
        this.restClientInitialConnectionTimeout = restClientInitialConnectionTimeout;
    }
}
