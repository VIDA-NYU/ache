package achecrawler.rest.resources;

import achecrawler.crawler.CrawlersManager;
import achecrawler.crawler.CrawlersManager.CrawlContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LabelsResource {

    private static final Logger logger = LoggerFactory.getLogger(LabelsResource.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private CrawlersManager crawlersManager;
    private final Map<String, Map<String, Boolean>> crawlersLabelsCache = new HashMap<>();

    public LabelsResource(CrawlersManager crawlersManager) {
        this.crawlersManager = crawlersManager;
    }

    public final Handler addLabels = (Context ctx) -> {
        String crawlerId = ctx.pathParam("crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            ctx.status(HttpServletResponse.SC_NOT_FOUND);
            ctx.json(ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId));
            return;
        }

        Map<String, Boolean> newLabels = deserializeMap(ctx.body());
        Map<String, Boolean> labelsCache = saveNewLabels(crawlerId, newLabels);
        ctx.status(HttpServletResponse.SC_CREATED);
        ctx.json(labelsCache);
    };

    private Map<String, Boolean> saveNewLabels(String crawlerId, Map<String, Boolean> newLabels)
            throws IOException {

        Map<String, Boolean> labelsCache = getLabelsCache(crawlerId);
        labelsCache.putAll(newLabels);

        Path filePath = getLabelsFilename(crawlerId);
        mapper.writeValue(filePath.toFile(), labelsCache);

        return labelsCache;
    }

    public final Handler getLabels = (Context ctx) -> {
        String crawlerId = ctx.pathParam("crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            ctx.status(HttpServletResponse.SC_NOT_FOUND);
            ctx.json(ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId));
            return;
        }

        ctx.json(getLabelsCache(crawlerId));
    };

    private Map<String, Boolean> getLabelsCache(String crawlerId) {
        Map<String, Boolean> labelsCache = crawlersLabelsCache.get(crawlerId);
        if (labelsCache == null) {
            labelsCache = loadLabelsFile(crawlerId);
            if (labelsCache == null) {
                labelsCache = new HashMap<>();
            }
            crawlersLabelsCache.put(crawlerId, labelsCache);
        }
        return labelsCache;
    }

    private Map<String, Boolean> loadLabelsFile(String crawlerId) {
        Path filePath = getLabelsFilename(crawlerId);
        try {
            if (Files.exists(filePath)) {
                String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                return deserializeMap(json);
            }
        } catch (IOException e) {
            String filename = filePath != null ? filePath.toString() : null;
            logger.error("Failed to load labels from file: " + filename, e);
        }
        return null;
    }

    private Path getLabelsFilename(String crawlerId) {
        CrawlContext crawlContext = crawlersManager.getCrawl(crawlerId);
        return Paths.get(crawlContext.dataPath, "labels.json");
    }

    private static Map<String, Boolean> deserializeMap(String body)
            throws IOException {
        TypeReference<HashMap<String, Boolean>> typeRef =
                new TypeReference<HashMap<String, Boolean>>() {};
        return LabelsResource.mapper.readValue(body, typeRef);
    }

}
