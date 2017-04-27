package focusedCrawler.target.repository.elasticsearch;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchConfig {

    //
    // Elasticsearch Transport Client parameters
    //
    @JsonProperty("target_storage.data_format.elasticsearch.host")
    private String host = "localhost";
    
    @JsonProperty("target_storage.data_format.elasticsearch.port")
    private int port = 9300;
    
    @JsonProperty("target_storage.data_format.elasticsearch.cluster_name")
    private String clusterName = "elasticsearch";
    
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

    public ElasticSearchConfig() { }

    public ElasticSearchConfig(String hostname, int port, String clusterName) {
        this.host = hostname;
        this.port = port;
        this.clusterName = clusterName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getClusterName() {
        return clusterName;
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

}