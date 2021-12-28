package achecrawler.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
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


public class CrawlerResource {

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    private static final Logger logger = LoggerFactory.getLogger(CrawlerResource.class);
    private final static ObjectMapper json = new ObjectMapper();

    private CrawlersManager crawlersManager;

    public CrawlerResource(CrawlersManager crawlManager) {
        crawlersManager = crawlManager;
    }

    public Handler getStatus = (Context ctx) -> {

        String crawlerId = ctx.pathParam("crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            ctx.status(HttpServletResponse.SC_NOT_FOUND);
            ctx.json(
                ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId)
            );
        } else {
            ctx.json(context);
        }
    };

    public Handler listCrawlers = (Context ctx) -> {
        Map<String, CrawlContext> crawlers = crawlersManager.getCrawls();
        ctx.json(ImmutableMap.of("crawlers", crawlers.values()));
    };

    public Handler metricsResourceJson = (Context ctx) -> {
        Object result = this.metricsResource(ctx);
        ctx.json(result);
    };

    public Handler metricsResourcePrometheus = (Context ctx) -> {
        Object result = this.metricsResource(ctx);
        if (result instanceof MetricRegistry) {
            ctx.contentType("text/plain");
            ctx.result(toPrometheusMetricsFormat((MetricRegistry) result));
        } else {
            ctx.json(result);
        }
    };

    public static String toPrometheusMetricsFormat(MetricRegistry registry) {
        StringBuilder sb = new StringBuilder();

        Map<String, Counter> counters = registry.getCounters();
        for (Map.Entry<String, Counter> c: counters.entrySet()) {
            sb.append(c.getKey().replace(".", "_"))
                    .append(" ")
                    .append(c.getValue().getCount())
                    .append("\n");
        }

        Map<String, Timer> timers = registry.getTimers();
        for (Map.Entry<String, Timer> t: timers.entrySet()) {
            sb.append(t.getKey().replace(".", "_"))
                    .append(" ")
                    .append(t.getValue().getCount())
                    .append("\n");
        }

        Map<String, Gauge> gauges = registry.getGauges();
        for (Map.Entry<String, Gauge> g: gauges.entrySet()) {
            sb.append(g.getKey().replace(".", "_"))
                    .append(" ")
                    .append(g.getValue().getValue())
                    .append("\n");
        }

        Map<String, Histogram> histograms = registry.getHistograms();
        for (Map.Entry<String, Histogram> h: histograms.entrySet()) {
            sb.append(h.getKey().replace(".", "_"))
                    .append(" ")
                    .append(h.getValue().getCount())
                    .append("\n");
        }

        return sb.toString();
    }

    public Object metricsResource(Context ctx) {
        String crawlerId = ctx.pathParam("crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            ctx.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
        }

        MetricsManager metricsManager = context.getCrawler().getMetricsManager();
        return (metricsManager != null) ? metricsManager.getMetricsRegistry() : null;
    }

    public Handler startCrawl = (Context ctx) -> {
        try {
            String crawlerId = ctx.pathParam("crawler_id");

            StartCrawlParams params = json.readValue(ctx.body(), StartCrawlParams.class);

            crawlersManager.createCrawler(crawlerId, params);
            crawlersManager.startCrawl(crawlerId);

            ctx.json(ImmutableMap.of(
                "message", "Crawler started successfully.",
                "crawler_id", crawlerId,
                "crawlerStarted", true));

        } catch (Exception e) {
            logger.error("Failed to start crawler.", e);
            ctx.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ctx.json(ImmutableMap.of(
                "message", "Failed to start crawler.",
                "crawlerStarted", false));
        }
    };

    public Handler stopCrawl = (Context ctx) -> {
        try {
            String crawlerId = ctx.pathParam("crawler_id");

            CrawlContext context = crawlersManager.getCrawl(crawlerId);
            if (context == null) {
                ctx.status(HttpServletResponse.SC_NOT_FOUND);
                ctx.json(ImmutableMap.of(
                        "message", "Crawler not found for crawler_id " + crawlerId,
                        "shutdownInitiated", false,
                        "crawlerStopped", false));
                return;
            }

            AsyncCrawler crawler = context.getCrawler();

            boolean awaitStopped = getParamAsBoolean("awaitStopped", ctx).orElse(false);
            crawler.stopAsync();
            if (awaitStopped) {
                crawler.awaitTerminated();
                ctx.json(ImmutableMap.of(
                        "message", "Crawler stopped successfully.",
                        "shutdownInitiated", true,
                        "crawlerStopped", true));
            } else {
                ctx.json(ImmutableMap.of(
                        "message", "Crawler shutdown initiated.",
                        "shutdownInitiated", true,
                        "crawlerStopped", false));
            }

        } catch (Exception e) {
            logger.error("Failed to stop crawler.", e);
            ctx.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ctx.json(ImmutableMap.of(
                "message", "Failed to stop crawler.",
                "shutdownInitiated", false,
                "crawlerStopped", false));
        }
    };

    public Handler addSeeds = (Context ctx) -> {
        try {
            String crawlerId = ctx.pathParam("crawler_id");

            CrawlContext context = crawlersManager.getCrawl(crawlerId);
            if (context == null) {
                ctx.status(HttpServletResponse.SC_NOT_FOUND);
                ctx.json(ImmutableMap.of(
                    "message", "Crawler not found for crawler_id " + crawlerId,
                    "addedSeeds", false));
                return;
            }

            AddSeedsParams params = json.readValue(ctx.body(), AddSeedsParams.class);
            if (params.seeds == null || params.seeds.isEmpty()) {
                ctx.status(HttpServletResponse.SC_BAD_REQUEST);
                ctx.json(ImmutableMap.of(
                    "message", "No seeds provided.",
                    "addedSeeds", false));
                return;
            }

            AsyncCrawler crawler = context.getCrawler();
            crawler.addSeeds(params.seeds);

            ctx.json(ImmutableMap.of(
                "message", "Seeds added successfully.",
                "addedSeeds", true));
        } catch (Exception e) {
            logger.error("Failed to add seeds.", e);
            ctx.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ctx.json(ImmutableMap.of(
                "message", "Failed to add seeds.",
                "addedSeeds", false));
        }
    };

    public Handler addCookies = (Context ctx) -> {
        String crawlerId = ctx.pathParam("crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            ctx.status(HttpServletResponse.SC_NOT_FOUND);
            ctx.json(ImmutableMap.of(
                    "message", "Crawler not found for crawler_id " + crawlerId,
                    "addedCookies", false));
            return;
        }

        try {
            HashMap<String, List<Cookie>> params = json.readValue(ctx.body(),
                    new TypeReference<HashMap<String, List<Cookie>>>() {});

            if (params == null || params.isEmpty()) {
                ctx.status(HttpServletResponse.SC_BAD_REQUEST);
                ctx.json(ImmutableMap.of(
                        "message", "No valid cookies provided.",
                        "addedCookies", false));
                return;
            }

            context.getCrawler().addCookies(params);

            ctx.json(ImmutableMap.of(
                    "message", "Cookies added successfully.",
                    "addedCookies", true));

        } catch (Exception e) {
            logger.error("Failed to add cookies.", e);
            ctx.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ctx.json(ImmutableMap.of(
                    "message", "Failed to add cookies to crawler " + crawlerId,
                    "addedCookies", false));
        }
    };
    
    private Optional<Boolean> getParamAsBoolean(String paramName, Context ctx) {
        try {
            Boolean valueOf = Boolean.valueOf(ctx.queryParam(paramName));
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
