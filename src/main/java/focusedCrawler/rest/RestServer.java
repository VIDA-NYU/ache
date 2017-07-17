package focusedCrawler.rest;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.Configuration;
import focusedCrawler.crawler.async.AsyncCrawler;
import focusedCrawler.rest.resources.CrawlerResource;
import focusedCrawler.rest.resources.ElasticsearchProxyResource;
import focusedCrawler.rest.resources.LabelsResource;
import focusedCrawler.rest.resources.ThreadsResource;
import focusedCrawler.target.TargetStorageConfig;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import spark.Service;

public class RestServer {
    
    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);
    
    private RestConfig restConfig;
    private Service server;

    private LabelsResource labelsResource;
    private CrawlerResource crawlerResource;
    private ThreadsResource threadsResource;
    private ElasticsearchProxyResource elasticsearchProxyResource;
    private boolean isSearchEnabled = false;

    private RestServer(String dataPath, RestConfig restConfig) {
        this(dataPath, restConfig, null, null, null);
    }
    
    private RestServer(String dataPath, RestConfig restConfig, String esIndexName,
                       String esTypeName, String esHostAddress) {
        this.restConfig = restConfig;
        if (esIndexName != null && esHostAddress != null) {
            this.isSearchEnabled = true;
            if (esTypeName == null || esTypeName.isEmpty()) {
                esTypeName = "page"; // default type name
            }
        }
        this.threadsResource = new ThreadsResource();
        this.labelsResource  = new LabelsResource(dataPath);
        this.crawlerResource = new CrawlerResource(dataPath, esIndexName, esTypeName);
        if (isSearchEnabled) {
            this.elasticsearchProxyResource = new ElasticsearchProxyResource(esHostAddress, esIndexName, esTypeName);
        }
    }

    public void start() {

        String host = restConfig.getHost();
        int port = restConfig.getPort();
        while (!portIsAvailable(port) && port < restConfig.getPort() + 100) {
            logger.error("Port {} not available. Trying port {}.", port, port + 1);
            port++;
        }

        server = Service.ignite();
        server.port(port);
        server.ipAddress(host);

        server.staticFiles.location("/public");

        if (restConfig.isEnableCors()) {
            enableCORS("*", "GET");
        }
        
        /*
         * API endpoints routes
         */
        server.get("/status", Transformers.json(crawlerResource.getStatus));
        server.get("/metrics", Transformers.json(crawlerResource.metricsResource));
        server.post("/startCrawl", "*/*", Transformers.json(crawlerResource.startCrawl));

        server.get("/thread/dump", Transformers.text(threadsResource.threadDump));

        if (isSearchEnabled) {
            /*
             * Elasticsearch proxy routes
             */
            server.get("/_search", "*/*", elasticsearchProxyResource.searchApi);
            server.post("/_search", "*/*", elasticsearchProxyResource.searchApi);
        }

        /*
         * Endpoints for labeling web pages
         */
        server.get( "/labels", Transformers.json(labelsResource.getLabels));
        server.put( "/labels", Transformers.json(labelsResource.addLabels));
        server.post("/labels", Transformers.json(labelsResource.addLabels));
        
        /*
         * Routes used by the static web application
         */
        server.get("/search",     StaticFileEngine.noopRouter, StaticFileEngine.engine);
        server.get("/monitoring", StaticFileEngine.noopRouter, StaticFileEngine.engine);
        server.get("/startCrawl", StaticFileEngine.noopRouter, StaticFileEngine.engine);
        
        server.awaitInitialization();
        
        logger.info("---------------------------------------------");
        logger.info("ACHE server available at http://{}:{}", host, port);
        logger.info("---------------------------------------------");
    }

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
        elasticsearchProxyResource.close();
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

    public static RestServer create(String dataPath, RestConfig restConfig) {
        return new RestServer(dataPath, restConfig);
    }

    public static RestServer create(String configPath, String dataPath,
                                    String esIndexName, String esTypeName) {
        requireNonNull(configPath, "A config path must be provided.");
        requireNonNull(dataPath, "A data path must be provided.");
        Configuration config = new Configuration(configPath);
        TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();
        ElasticSearchConfig esConfig = targetStorageConfig.getElasticSearchConfig();
        List<String> hosts = esConfig.getRestApiHosts();
        if (hosts != null && !hosts.isEmpty()) {
            requireNonNull(esIndexName, "Elasticsearch index name should be provided when using ELASTICSEARCH data format.");
            if(esTypeName == null || esTypeName.isEmpty()) {
                esTypeName = "page";
            }
            String esHostAddress = hosts.iterator().next();
            return new RestServer(dataPath, config.getRestConfig(), esIndexName, esTypeName, esHostAddress);
        } else {
            return new RestServer(dataPath, config.getRestConfig());
        }
    }

    public void setCrawler(AsyncCrawler crawler) {
        crawlerResource.setCrawler(crawler);
    }

}
