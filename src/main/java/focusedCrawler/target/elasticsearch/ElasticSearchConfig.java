package focusedCrawler.target.elasticsearch;

public class ElasticSearchConfig {

    private final String host;
    private final int port;
    private final String clusterName;

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