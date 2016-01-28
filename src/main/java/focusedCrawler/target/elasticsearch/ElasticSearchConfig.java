package focusedCrawler.target.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchConfig {

    @JsonProperty("target_storage.data_format.elasticsearch.host")
    private String host = "localhost";
    
    @JsonProperty("target_storage.data_format.elasticsearch.port")
    private int port = 9300;
    
    @JsonProperty("target_storage.data_format.elasticsearch.cluster_name")
    private String clusterName = "elasticsearch";
    
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
}