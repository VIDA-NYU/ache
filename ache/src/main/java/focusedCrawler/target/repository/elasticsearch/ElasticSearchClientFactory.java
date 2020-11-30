package focusedCrawler.target.repository.elasticsearch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchClientFactory.class);

    public static RestClient createClient(ElasticSearchConfig config) {

        HttpHost[] httpHosts = parseHostAddresses(config.getRestApiHosts());

        RestClient client = RestClient.builder(httpHosts)
                .setRequestConfigCallback(new RequestConfigCallback() {
                    @Override
                    public Builder customizeRequestConfig(
                            Builder requestConfigBuilder) {
                        return requestConfigBuilder
                                .setConnectTimeout(config.getRestConnectTimeout())
                                .setSocketTimeout(config.getRestSocketTimeout());
                    }
                })
                .setMaxRetryTimeoutMillis(config.getRestMaxRetryTimeoutMillis())
                .build();

        logger.info("Initialized Elasticsearch REST client for: " + Arrays.toString(httpHosts));
        return client;
    }

    private static HttpHost[] parseHostAddresses(List<String> esHosts) {
        List<HttpHost> hosts = new ArrayList<>();
        for (String host : esHosts) {
            try {
                URL url = new URL(host);
                int port = url.getPort() == -1 ? 9200 : url.getPort();
                hosts.add(new HttpHost(url.getHost(), port, url.getProtocol()));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to initialize Elasticsearch REST client. "
                        + "Invalid host: " + host, e);
            }
        }

        return (HttpHost[]) hosts.toArray(new HttpHost[hosts.size()]);
    }

}