package focusedCrawler.rest;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.ThreadDump;
import com.google.common.collect.ImmutableMap;

import focusedCrawler.Main;
import focusedCrawler.config.ConfigService;
import focusedCrawler.target.TargetStorageConfig;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import spark.Route;
import spark.Service;

public class RestServer {
    
    public static final String VERSION = Main.class.getPackage().getImplementationVersion();
    
    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);

    private RestConfig restConfig;
    private MetricRegistry metricsRegistry;
    private String indexName;
    private String hostAddress;
    private boolean isSearchEnabled = false;
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private Service server;
    
    private RestServer(RestConfig restConfig, MetricRegistry metricsRegistry) {
        this(restConfig, metricsRegistry, null, null);
    }
    
    private RestServer(RestConfig restConfig, MetricRegistry metricsRegistry, String indexName, String hostAddress) {
        this.restConfig = restConfig;
        this.metricsRegistry = metricsRegistry;
        this.indexName = indexName;
        this.hostAddress = hostAddress;
        if(indexName != null && hostAddress != null) {
            isSearchEnabled = true;
        }
    }
 
    public void start() {
        
        String host = restConfig.getHost();
        int port = restConfig.getPort();
        while(!portIsAvailable(port) && port < restConfig.getPort()+100) {
            logger.error("Port {} not available. Trying port {}.", port, port+1);
            port++;
        }
        
        server = Service.ignite();
        server.port(port);
        server.ipAddress(host);
        
        if(restConfig.isEnableCors()) {
            enableCORS("*", "GET");
        }
        
        server.get("/",            Transformers.json(crawlerInfoResource));
        server.get("/metrics",     Transformers.json(metricsResource));
        server.get("/thread/dump", Transformers.text(threadDumpResource));
        
        if(isSearchEnabled) {
            server.get("/_search", "*/*", searchApiProxy);
            server.post("/_search", "*/*", searchApiProxy);
        }
        
        server.awaitInitialization();
        
        logger.info("---------------------------------------------");
        logger.info("ACHE server available at http://{}:{}", host, port);
        logger.info("---------------------------------------------");
    }

    public void stop() {
        try {
            httpclient.close();
        } catch (IOException e) {
            logger.error("Failed to close http client.", e);
        }
    }
    
    private Route crawlerInfoResource = (request, response) -> {
        Map<?, ?> crawlerInfo = ImmutableMap.of(
            "status", 200,
            "name", "ACHE Crawler",
            "version", VERSION,
            "searchEnabled", isSearchEnabled
        );
        return crawlerInfo;
    };

    
    private Route searchApiProxy = (request, response) -> {
        try {
            String query = "";
            for (String param : request.queryParams()) {
                query += param + "=" + request.queryParams(param);
            }
            String url = String.format("%s/%s/_search", hostAddress, indexName);
            if (!query.isEmpty()) {
                url += "?" + query;
            }
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(request.body(), "UTF-8"));
            CloseableHttpResponse apiResponse = httpclient.execute(post);
            try {
                HttpEntity entity = apiResponse.getEntity();
                Header[] headers = apiResponse.getAllHeaders();
                for (Header header : headers) {
                    response.header(header.getName(), header.getValue());
                }
                String body = EntityUtils.toString(entity);
                response.body(body);
                return body;
            } finally {
                apiResponse.close();
            }
        } catch (Exception e) {
            logger.error("Failed to forward request to ElasticSearch.", e);
            throw e;
        }
    };

    private Route metricsResource = (request, response) -> {
        return metricsRegistry;
    };
    
    private ThreadDump threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
    
    private Route threadDumpResource = (request, response) -> {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        threadDump.dump(baos);
        return baos.toString();
    };

    private boolean portIsAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (ds != null)  ds.close();
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void shutdown() {
        server.stop();
        try {
            this.httpclient.close();
        } catch (IOException e) {
            logger.error("Failed to close http client.", e);
        }
    }
    
    private void enableCORS(final String origin, final String methods) {

        server.options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        server.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
        });
        
    }

    public static RestServer create(RestConfig restConfig, MetricRegistry metricsRegistry) {
        return new RestServer(restConfig, metricsRegistry);
    }
    
    public static RestServer create(MetricRegistry metricsRegistry, ConfigService config, String indexName) {
        requireNonNull(metricsRegistry, "A metrics registry must be provided.");
        requireNonNull(config, "A configuration must be provided.");
        TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();
        if("ELASTICSEARCH".equals(targetStorageConfig.getDataFormat())) {
            ElasticSearchConfig esConfig = targetStorageConfig.getElasticSearchConfig();
            List<String> hosts = esConfig.getRestApiHosts();
            if(hosts == null || hosts.isEmpty()) {
                throw new IllegalArgumentException("Elasticsearch host addresses (REST API) can not be empty");
            }
            requireNonNull(indexName, "Elasticsearch index name should be provided when using ELASTICSEARCH data format.");
            String esHostAddress = hosts.iterator().next();
            return new RestServer(config.getRestConfig(), metricsRegistry, indexName, esHostAddress);
        } else {
            return new RestServer(config.getRestConfig(), metricsRegistry);
        }
    }
    
}
