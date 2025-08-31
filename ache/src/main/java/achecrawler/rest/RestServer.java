package achecrawler.rest;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.util.JavalinLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.crawler.CrawlersManager;
import achecrawler.rest.resources.CrawlerResource;
import achecrawler.rest.resources.ElasticsearchProxyResource;
import achecrawler.rest.resources.LabelsResource;
import achecrawler.rest.resources.ThreadsResource;


public class RestServer {
    
    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);
    
    private Javalin server;
    private final RestConfig restConfig;
    private final LabelsResource labelsResource;
    private final CrawlerResource crawlerResource;
    private final ThreadsResource threadsResource;
    private final ElasticsearchProxyResource elasticsearchProxyResource;

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

        SinglePageAppHandler singlePageAppHandler = new SinglePageAppHandler(restConfig.getBasePath());

        JavalinLogger.startupInfo = false; // suppress Javalin startup log messages

        server = Javalin.create(config -> {

            config.showJavalinBanner = false;

            /*
             * Enable HTTP CORS (Cross-Origin Resource Sharing)
             */
            if (restConfig.isEnableCors()) {
                config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            }

            /*
             * Configure single-page handler to serve the React App index.html file
             * to paths that do not match any other handler.
             */
            config.spaRoot.addHandler("/", singlePageAppHandler);

            /*
             * Configure handler for serving static files under /public.
             */
            config.staticFiles.add(staticFiles -> {
                staticFiles.location = Location.CLASSPATH;
                staticFiles.directory = "/public";
                staticFiles.hostedPath = "/";
            });

            /*
             * Configure a custom JSON mapper.
             */
            final TimeUnit rateUnit = TimeUnit.SECONDS;
            final TimeUnit durationUnit = TimeUnit.MILLISECONDS;
            final boolean showSamples = false;
            ObjectMapper jsonMapper = new ObjectMapper()
                    .registerModule(new MetricsModule(rateUnit, durationUnit, showSamples));
            config.jsonMapper(new JavalinJackson(jsonMapper, false));
        });

        /*
         * Configure Basic authorization
         */
        String user = restConfig.getBasicUser();
        String passwd = restConfig.getBasicPassword();
        if (user != null && !user.isEmpty() && passwd != null && !passwd.isEmpty()) {
            server.before("/*", new BasicAuthenticationFilter(user, passwd));
        }


        /*
         * Single page app routers. These routes are required to override the default
         * static index.html that would be served by the static file handler.
         */
        server.get("/", singlePageAppHandler);
        server.get("/index.html", singlePageAppHandler);
        
        /*
         * Crawl API routes
         */
        server.get( "/crawls",                         crawlerResource.listCrawlers);
        server.get( "/crawls/{crawler_id}",            crawlerResource.getStatus);
        server.get( "/crawls/{crawler_id}/status",     crawlerResource.getStatus);
        server.post("/crawls/{crawler_id}",            crawlerResource.startCrawl);
        server.post("/crawls/{crawler_id}/startCrawl", crawlerResource.startCrawl);
        server.get( "/crawls/{crawler_id}/metrics",    crawlerResource.metricsResourceJson);
        server.get( "/crawls/{crawler_id}/prometheus", crawlerResource.metricsResourcePrometheus);
        server.get( "/crawls/{crawler_id}/stopCrawl",  crawlerResource.stopCrawl);
        server.post("/crawls/{crawler_id}/seeds",      crawlerResource.addSeeds);
        server.post("/crawls/{crawler_id}/cookies",    crawlerResource.addCookies);
        server.get( "/crawls/{crawler_id}/labels",     labelsResource.getLabels);
        server.put( "/crawls/{crawler_id}/labels",     labelsResource.addLabels);
        server.post("/crawls/{crawler_id}/labels",     labelsResource.addLabels);
        server.get( "/crawls/{crawler_id}/_msearch",    elasticsearchProxyResource.searchApi);
        server.post("/crawls/{crawler_id}/_msearch",    elasticsearchProxyResource.searchApi);

        /*
         * Thread management routes
         */
        server.get("/thread/dump", threadsResource.threadDump);

        server.exception(UnauthorizedException.class, BasicAuthenticationFilter.exceptionHandler);
        
        server.start(host, port);

        logger.info("---------------------------------------------");
        logger.info("ACHE server available at http://{}:{}", host, port);
        logger.info("---------------------------------------------");
    }

    private static boolean portIsAvailable(int port) {
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
                    logger.error("Failed to close server socket while detecting available port.");
                }
            }
        }
    }

    public void shutdown() {
        server.stop();
        elasticsearchProxyResource.close();
    }

    public static RestServer create(RestConfig restConfig, CrawlersManager crawlManager) {
        return new RestServer(restConfig, crawlManager);
    }

}
