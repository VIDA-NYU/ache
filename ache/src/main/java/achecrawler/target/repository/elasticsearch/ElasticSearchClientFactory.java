package achecrawler.target.repository.elasticsearch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchClientFactory.class);

    public static RestClient createClient(ElasticSearchConfig config) {

        HttpHost[] httpHosts = parseHostAddresses(config.getRestApiHosts());

        final RestClientBuilder builder = RestClient.builder(httpHosts)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(config.getRestConnectTimeout())
                        .setSocketTimeout(config.getRestSocketTimeout())
                )
                .setMaxRetryTimeoutMillis(config.getRestMaxRetryTimeoutMillis());

        final int connectionTimeout = config.getRestClientInitialConnectionTimeout();
        final long start = System.currentTimeMillis();
        int attempts = 0;
        RestClient client = null;
        while(true) {
            try {
                attempts++;
                if (client == null) {
                    client = builder.build();
                    logger.info("Initialized Elasticsearch REST client for hosts: " + Arrays.toString(httpHosts));
                }
                if (client != null) {
                    checkRestApi(client);
                }
                return client;
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > connectionTimeout) {
                    String msg = String.format("Failed to connect to Elasticsearch server after %d retries", attempts);
                    throw new IllegalStateException(msg, e);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException("Interrupted while trying to connect to Elasticsearch server.", ex);
                }
                if (attempts % 10 == 0) {
                    logger.info("Failed to connect to Elasticsearch server (failed attempts: {}). Retrying...", attempts);
                }
            }
        }
    }

    private static void checkRestApi(RestClient client) {
        String rootEndpoint = "/";
        try {
            Response response = client.performRequest("GET", rootEndpoint);
            final int statusCode = response.getStatusLine().getStatusCode();
            logger.info(response.getEntity().toString());
            if(statusCode != 200) {
                throw new IllegalStateException("Cluster returned non-OK status code: " + statusCode);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to issue request to Elasticsearch REST API.", e);
        }
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