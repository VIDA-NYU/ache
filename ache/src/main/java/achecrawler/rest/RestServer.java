package achecrawler.rest;

import static achecrawler.rest.Transformers.json;
import static achecrawler.rest.Transformers.promethize;
import static achecrawler.rest.Transformers.text;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.crawler.CrawlersManager;
import achecrawler.rest.resources.CrawlerResource;
import achecrawler.rest.resources.ElasticsearchProxyResource;
import achecrawler.rest.resources.LabelsResource;
import achecrawler.rest.resources.ThreadsResource;
import spark.Service;

public class RestServer {
    
    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);
    
    private Service server;
    private RestConfig restConfig;
    private LabelsResource labelsResource;
    private CrawlerResource crawlerResource;
    private ThreadsResource threadsResource;
    private ElasticsearchProxyResource elasticsearchProxyResource;

    private RestServer(RestConfig restConfig, CrawlersManager crawlManager) {
        this.restConfig = restConfig;
        this.threadsResource = new ThreadsResource();
        this.labelsResource  = new LabelsResource(crawlManager);
        this.elasticsearchProxyResource = new ElasticsearchProxyResource(crawlManager);
        this.crawlerResource = new CrawlerResource(crawlManager);
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
            "/search/*",
            "/monitoring",
            "/monitoring/*",
            "/start",
            "/start?*"
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
        server.get( "/crawls",                        json(crawlerResource.listCrawlers));
        server.get( "/crawls/:crawler_id",            json(crawlerResource.getStatus));
        server.get( "/crawls/:crawler_id/status",     json(crawlerResource.getStatus));
        server.post("/crawls/:crawler_id",            json(crawlerResource.startCrawl));
        server.post("/crawls/:crawler_id/startCrawl", json(crawlerResource.startCrawl));
        server.get( "/crawls/:crawler_id/metrics",    json(crawlerResource.metricsResource));
        server.get( "/crawls/:crawler_id/prometheus", promethize(crawlerResource.metricsResource));
        server.get( "/crawls/:crawler_id/stopCrawl",  json(crawlerResource.stopCrawl));
        server.post("/crawls/:crawler_id/seeds",      json(crawlerResource.addSeeds));
        server.post("/crawls/:crawler_id/cookies",    json(crawlerResource.addCookies));
        server.get( "/crawls/:crawler_id/labels",     json(labelsResource.getLabels));
        server.put( "/crawls/:crawler_id/labels",     json(labelsResource.addLabels));
        server.post("/crawls/:crawler_id/labels",     json(labelsResource.addLabels));
        server.get( "/crawls/:crawler_id/_search",    elasticsearchProxyResource.searchApi);
        server.post("/crawls/:crawler_id/_search",    elasticsearchProxyResource.searchApi);

        /*
         * Thread management routes
         */
        server.get("/thread/dump", text(threadsResource.threadDump));
        
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

    public static RestServer create(RestConfig restConfig, CrawlersManager crawlManager) {
        return new RestServer(restConfig, crawlManager);
    }

}
