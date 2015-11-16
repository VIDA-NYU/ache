package focusedCrawler.target.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchConfig {

    @JsonProperty("host")
    private final String host;
    @JsonProperty("port")
    private final int port;
    @JsonProperty("cluster_name")
    private final String clusterName;
    
    public ElasticSearchConfig() {
        this("localhost", 9300, "elasticsearch");
    }

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