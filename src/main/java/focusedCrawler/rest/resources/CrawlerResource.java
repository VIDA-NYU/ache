package focusedCrawler.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.core.JsonParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import focusedCrawler.Main;
import focusedCrawler.crawler.CrawlersManager;
import focusedCrawler.crawler.CrawlersManager.CrawlContext;
import focusedCrawler.crawler.CrawlersManager.CrawlType;
import focusedCrawler.crawler.async.AsyncCrawler;
import focusedCrawler.crawler.cookies.Cookie;
import focusedCrawler.util.MetricsManager;
import spark.Request;
import spark.Route;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;
import org.apache.commons.io.FileUtils;
import java.util.concurrent.TimeUnit ;

public class CrawlerResource {
    private Map<String, CrawlContext> crawlers = new HashMap<>();

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    private static final Logger logger = LoggerFactory.getLogger(CrawlerResource.class);
    private final static ObjectMapper json = new ObjectMapper();

    private CrawlersManager crawlersManager;

    public CrawlerResource(CrawlersManager crawlManager) {
        crawlersManager = crawlManager;
    }

    public Route getStatus = (request, response) -> {

        String crawlerId = request.params(":crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            response.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
        }

        return context;
    };

    public Route listCrawlers = (request, response) -> {
        Map<String, CrawlContext> crawlers = crawlersManager.getCrawls();
        return ImmutableMap.of("crawlers", crawlers.values());
    };
    public Route helloWorld = (request, response) -> {
        System.out.println("helloWorld");
        return ImmutableMap.of("crawlers", "hello this is working");
    };

    public Route metricsResource = (request, response) -> {
        String crawlerId = request.params(":crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            response.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
        }

        MetricsManager metricsManager = context.getCrawler().getMetricsManager();
        return metricsManager != null ? metricsManager.getMetricsRegistry() : null;
    };

    public Route addurl = (request, response) -> {
          try {
            String crawlerId = request.params(":crawler_id");
            CrawlContext context = crawlersManager.getCrawl(crawlerId);  
            System.out.println(context);        
            System.out.print(request.body());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(request.body());
            String yaml = new YAMLMapper().writeValueAsString(jsonNode);
            String path;
            String directory;
            if (context == null) {
                response.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
            }
               path = context.seedPath;
               File f = new File(path);
               directory = f.getParent();
               String link_filters_path = directory + File.separator + "link_filters.yaml";
               System.out.println(link_filters_path);
               File link_filter_file = new File(link_filters_path);
               link_filter_file.delete();
           //create new file with content 
               link_filter_file.createNewFile();
            
            FileWriter myWriter = new FileWriter(link_filter_file);
            //FileWriter myWriter = new FileWriter(link_filters_path);
            myWriter.write(yaml);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
               return ImmutableMap.of("output", yaml);
            
            } catch (Exception e) {
                logger.error("failed.", e);
                response.status(HttpServletResponse.SC_NOT_ACCEPTABLE);
                System.out.println("hello");
                return ImmutableMap.of("message", false);

            }
          };

    public Route startCrawl = (request, response) -> {
        try {
            String crawlerId = request.params(":crawler_id");
            StartCrawlParams params = json.readValue(request.body(), StartCrawlParams.class);

            crawlersManager.createCrawler(crawlerId, params);
            crawlersManager.startCrawl(crawlerId);

            return ImmutableMap.of(
                "message", "Crawler started successfully.",
                "crawlerStarted", true);

        } catch (Exception e) {
            logger.error("Failed to start crawler.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                "message", "Failed to start crawler.",
                "crawlerStarted", false);
        }
    };
    public Route restartCrawl = (request, response) -> {
        try {
            String crawlerId = request.params(":crawler_id");
            StartCrawlParams params = json.readValue(request.body(), StartCrawlParams.class);
            CrawlContext context = crawlersManager.getCrawl(crawlerId);          
            System.out.println(context);
            String path;
            String directory;
            if (context == null) {
                response.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
            }

            path = context.dataPath;
            File f = new File(path);
            directory = f.getParent();
            String crawler_path = directory + File.separator + crawlerId;
            File crawler_directory = new File(crawler_path);
            
            AsyncCrawler crawler = context.getCrawler();
            crawler.stopAsync();
                crawler.awaitTerminated();
                if (crawler_directory.exists()) {
                FileUtils.cleanDirectory(crawler_directory); 
                FileUtils.forceDelete(crawler_directory);
                System.out.println("folder deleted");
            }
                crawlersManager.createCrawler(crawlerId, params);
                crawlersManager.startCrawl(crawlerId);
                System.out.println("Crawler stopped successfully");
             
            return ImmutableMap.of(
                "message", "Crawler restarted successfully.",
                "crawlerreStarted", true);

        } catch (Exception e) {
            logger.error("Failed to restart crawler.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                "message", "Failed to restart crawler.",
                "crawlerreStarted", false);
        }
    };

    public Route stopCrawl = (request, response) -> {
        try {
            String crawlerId = request.params(":crawler_id");

            CrawlContext context = crawlersManager.getCrawl(crawlerId);
            if (context == null) {
                response.status(HttpServletResponse.SC_NOT_FOUND);
                return ImmutableMap.of(
                        "message", "Crawler not found for crawler_id " + crawlerId,
                        "shutdownInitiated", false,
                        "crawlerStopped", false);
            }

            AsyncCrawler crawler = context.getCrawler();

            boolean awaitStopped = getParamAsBoolean("awaitStopped", request).orElse(false);
            crawler.stopAsync();
            if (awaitStopped) {
                crawler.awaitTerminated();
                return ImmutableMap.of(
                        "message", "Crawler stopped successfully.",
                        "shutdownInitiated", true,
                        "crawlerStopped", true);
            } else {
                return ImmutableMap.of(
                        "message", "Crawler shutdown initiated.",
                        "shutdownInitiated", true,
                        "crawlerStopped", false);
            }

        } catch (Exception e) {
            logger.error("Failed to stop crawler.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                "message", "Failed to stop crawler.",
                "shutdownInitiated", false,
                "crawlerStopped", false);
        }
    };

    public Route addSeeds = (request, response) -> {
        try {
            String crawlerId = request.params(":crawler_id");

            CrawlContext context = crawlersManager.getCrawl(crawlerId);
            if (context == null) {
                response.status(HttpServletResponse.SC_NOT_FOUND);
                return ImmutableMap.of(
                    "message", "Crawler not found for crawler_id " + crawlerId,
                    "addedSeeds", false);
            }

            AddSeedsParams params = json.readValue(request.body(), AddSeedsParams.class);
            if (params.seeds == null || params.seeds.isEmpty()) {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                return ImmutableMap.of(
                    "message", "No seeds provided.",
                    "addedSeeds", false);
            }

            AsyncCrawler crawler = context.getCrawler();
            crawler.addSeeds(params.seeds);

            return ImmutableMap.of(
                "message", "Seeds added successfully.",
                "addedSeeds", true);

        } catch (Exception e) {
            logger.error("Failed to add seeds.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                "message", "Failed to add seeds.",
                "addedSeeds", false);
        }
    };

    public Route addCookies = (request, response) -> {
        String crawlerId = request.params(":crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            response.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of(
                    "message", "Crawler not found for crawler_id " + crawlerId,
                    "addedCookies", false);
        }

        try {
            HashMap<String, List<Cookie>> params = json.readValue(request.body(),
                    new TypeReference<HashMap<String, List<Cookie>>>() {});

            if (params == null || params.isEmpty()) {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                return ImmutableMap.of(
                        "message", "No valid cookies provided.",
                        "addedCookies", false);
            }

            context.getCrawler().addCookies(params);

            return ImmutableMap.of(
                    "message", "cookies added successfully.",
                    "addedCookies", true);

        } catch (Exception e) {
            logger.error("Failed to add cookies.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                    "message", "Failed to add cookies to crawler " + crawlerId,
                    "addedCookies", false);
        }
    };
    
    private Optional<Boolean> getParamAsBoolean(String paramName, Request request) {
        try {
            Boolean valueOf = Boolean.valueOf(request.queryParams(paramName));
            return Optional.of(valueOf);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static class StartCrawlParams {
        public CrawlType crawlType;
        public List<String> seeds;
        public byte[] model;
        public String esTypeName;
        public String esIndexName;
    }

    public static class AddSeedsParams {
        public List<String> seeds;
    }
    

}
