package focusedCrawler.rest;

import static java.util.Arrays.asList;
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

    private RestServer(String dataPath, Configuration config) {
        this.restConfig = config.getRestConfig();
        this.elasticsearchProxyResource = new ElasticsearchProxyResource(config);
        this.threadsResource = new ThreadsResource();
        this.labelsResource  = new LabelsResource(dataPath);
        this.crawlerResource = new CrawlerResource(config, dataPath, elasticsearchProxyResource);
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

        /*
         * Configure Basic authorization
         */
        String user = restConfig.getBasicUser();
        String passwd = restConfig.getBasicPassword();
        if (user != null && !user.isEmpty() && passwd != null && !passwd.isEmpty()) {
            server.before("/*", new BasicAuthenticationFilter(server, user, passwd));
        }

        /*
         * Configure static files handler location and renders index.html file. New paths added to
         * the React static web application, should also be added here, otherwise page refreshes in
         * the browser will not work.
         */
        List<String> indexes = asList(
            "/",
            "/index.html",
            "/search",
            "/monitoring"
        );
        server.before("/*", new StaticFileHandlerFilter(indexes, restConfig.getBasePath()));

        /*
         * Enable HTTP CORS (Cross-origin Resource Sharing)
         */
        if (restConfig.isEnableCors()) {
            enableCORS("*", "GET");
        }
        
        /*
         * Crawl routes
         */
        server.get("/status", Transformers.json(crawlerResource.getStatus));
        server.get("/metrics", Transformers.json(crawlerResource.metricsResource));
        server.get("/prometheus",Transformers.promethize(crawlerResource.metricsResource));
        server.post("/startCrawl", "*/*", Transformers.json(crawlerResource.startCrawl));
        server.get("/stopCrawl", Transformers.json(crawlerResource.stopCrawl));
        server.post("/seeds", "*/*", Transformers.json(crawlerResource.addSeeds));
        server.post("/cookies", "*/*", Transformers.json(crawlerResource.addCookies));

        /*
         * Thread management routes
         */
        server.get("/thread/dump", Transformers.text(threadsResource.threadDump));

        /*
         * Elasticsearch proxy routes
         */
        server.get("/_search", "*/*", elasticsearchProxyResource.searchApi);
        server.post("/_search", "*/*", elasticsearchProxyResource.searchApi);

        /*
         * Page labeling routes
         */
        server.get( "/labels", Transformers.json(labelsResource.getLabels));
        server.put( "/labels", Transformers.json(labelsResource.addLabels));
        server.post("/labels", Transformers.json(labelsResource.addLabels));

        
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
        if(elasticsearchProxyResource != null) {
            elasticsearchProxyResource.close();
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

    public static RestServer create(String configPath, String dataPath,
                                    String esIndexName, String esTypeName) {

        requireNonNull(dataPath, "A data path must be provided.");

        Configuration config = configPath == null ? new Configuration() : new Configuration(configPath);
        TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();

        if (targetStorageConfig.isElasticsearchRestEnabled()) {

            ElasticSearchConfig esConfig = targetStorageConfig.getElasticSearchConfig();

            if (esIndexName != null && !esIndexName.isEmpty()) {
                esConfig.setIndexName(esIndexName);
            }
            if (esTypeName != null && !esTypeName.isEmpty()) {
                esConfig.setTypeName(esTypeName);
            }

            logger.info("Starting server with Elasticsearch: "
                + esConfig.getRestApiHosts().iterator().next() + "/"
                + esConfig.getIndexName() + "/"
                + esConfig.getTypeName());
        }

        return new RestServer(dataPath, config);
    }

    public void setCrawler(AsyncCrawler crawler) {
        crawlerResource.setCrawler(crawler);
        elasticsearchProxyResource.updateConfig(crawler.getConfig());
    }

}
