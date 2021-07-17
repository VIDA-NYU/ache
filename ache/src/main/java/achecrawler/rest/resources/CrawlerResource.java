package achecrawler.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import achecrawler.Main;
import achecrawler.crawler.CrawlersManager;
import achecrawler.crawler.CrawlersManager.CrawlContext;
import achecrawler.crawler.CrawlersManager.CrawlType;
import achecrawler.crawler.async.AsyncCrawler;
import achecrawler.crawler.cookies.Cookie;
import achecrawler.util.MetricsManager;
import spark.Request;
import spark.Route;

public class CrawlerResource {

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
