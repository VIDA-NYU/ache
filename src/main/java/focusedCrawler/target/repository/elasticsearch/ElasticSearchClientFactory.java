package focusedCrawler.target.repository.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchClientFactory.class);

    private static Node clientNode;
    private static Client client;

    @SuppressWarnings("resource")
    public static Client createClient(ElasticSearchConfig config) {

        if (client != null) {
            return client;
        }

        String elasticSearchHost = config.getHost();
        int elasticSearchPort = config.getPort();
        String clusterName = config.getClusterName();

        Builder settingsBuilder = ImmutableSettings.settingsBuilder();
        if (clusterName != null) {
            settingsBuilder.put("cluster.name", clusterName);
        }
        Settings settings = settingsBuilder.build();

        if (elasticSearchHost != null) {
            logger.info("Creating a ElasticSearch TransportClient for address: {}:{}",
                    elasticSearchHost, elasticSearchPort);
            int port = elasticSearchPort != 0 ? elasticSearchPort : 9300;
            InetSocketTransportAddress socketAddress = new InetSocketTransportAddress(
                    elasticSearchHost, port);
            client = new TransportClient(settings).addTransportAddress(socketAddress);
            return client;
        } else {
            logger.info("Creating a ElasticSearch Node Client for address: %s:%s",
                    elasticSearchHost, elasticSearchPort);
            clientNode = NodeBuilder.nodeBuilder().client(true).settings(settings).node();
            client = clientNode.client();
            return client;
        }
    }

    public void closeClient() {
        if (client != null) {
            client.close();
        }
        if (clientNode != null) {
            clientNode.close();
        }
    }

}