package focusedCrawler.rest;

import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.ipAddress;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.stop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.ThreadDump;
import com.google.common.collect.ImmutableMap;

import focusedCrawler.Main;
import spark.Route;

public class RestServer {
    
    public static final String VERSION = Main.class.getPackage().getImplementationVersion();
    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);

    private RestConfig restConfig;
    private MetricRegistry metricsRegistry;
    
    public RestServer(RestConfig restConfig, MetricRegistry metricsRegistry) {
        this.restConfig = restConfig;
        this.metricsRegistry = metricsRegistry;
    }

    public void start() {
        
        String host = restConfig.getHost();
        int port = restConfig.getPort();
        while(!portIsAvailable(port) && port < restConfig.getPort()+100) {
            logger.error("Port {} not available. Trying port {}.", port, port+1);
            port++;
        }
        
        port(port);
        ipAddress(host);
        
        if(restConfig.isEnableCors()) {
            enableCORS("*", "GET");
        }
        
        get("/",           Transformers.json(crawlerInfoResource));
        get("/metrics",    Transformers.json(metricsResource));
        get("/thread/dump", Transformers.text(threadDumpResource));
        
        awaitInitialization();
        
        logger.info("---------------------------------------------");
        logger.info("ACHE server available at http://{}:{}", host, port);
        logger.info("---------------------------------------------");
    }
    
    private Route crawlerInfoResource = (request, response) -> {
        Map<?, ?> crawlerInfo = ImmutableMap.of(
            "status", 200,
            "name", "ACHE Crawler",
            "version", VERSION
        );
        return crawlerInfo;
    };
    
//    private Route startCrawlResource = (request, response) -> {
//
//        String paramId = request.queryParams("id");
//
//        String id = paramId != null ? paramId : UUID.randomUUID().toString();
//        String status = "STARTING";
//        // TODO: start crawler
//        CrawlerState crawlerState = new CrawlerState(id, status);
//
//        return crawlerState;
//    };
//    
//    static class CrawlerState {
//        
//        public String id;
//        public String status;
//
//        public CrawlerState(String id, String status) {
//            this.id = id;
//            this.status = status;
//        }
//        
//    }
    
    private Route metricsResource = (request, response) -> {
        return metricsRegistry;
    };
    
    private ThreadDump threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
    
    private Route threadDumpResource = (request, response) -> {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        threadDump.dump(baos);
        return baos.toString();
    };
    
    public static boolean portIsAvailable(int port) {
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
        stop();
    }
    
    private static void enableCORS(final String origin, final String methods) {

        options("/*", (request, response) -> {

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

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
        });
        
    }
    
}
