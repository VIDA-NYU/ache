package focusedCrawler.target.repository.elasticsearch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
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

		Builder settingsBuilder = Settings.builder();

		if (clusterName != null) {
			settingsBuilder.put("cluster.name", clusterName);
		}
		Settings settings = settingsBuilder.build();

		if (elasticSearchHost != null) {
			logger.info("Creating a ElasticSearch TransportClient for address: {}:{}", elasticSearchHost,
					elasticSearchPort);
			int port = elasticSearchPort != 0 ? elasticSearchPort : 9300;

			TransportClient client = new PreBuiltTransportClient(settingsBuilder.build());

			try {
				client.addTransportAddress(
						new InetSocketTransportAddress(InetAddress.getByName(elasticSearchHost), port));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return client;
		} else {
			logger.info("Creating a ElasticSearch Node Client for address: %s:%s", elasticSearchHost,
					elasticSearchPort);
			clientNode = new Node(settings);
			client = clientNode.client();
			return client;
		}
	}

	public void closeClient() {
		if (client != null) {
			client.close();
		}
		if (clientNode != null) {
			try {
				clientNode.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}