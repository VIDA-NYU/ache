package achecrawler.rest.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import achecrawler.crawler.CrawlersManager;
import achecrawler.crawler.CrawlersManager.CrawlContext;
import spark.Route;

public class LabelsResource {

    private static final Logger logger = LoggerFactory.getLogger(LabelsResource.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private CrawlersManager crawlersManager;
    private Map<String, Map<String, Boolean>> crawlersLabelsCache = new HashMap<>();

    public LabelsResource(CrawlersManager crawlersManager) {
        this.crawlersManager = crawlersManager;
    }

    public final Route addLabels = (request, response) -> {
        String crawlerId = request.params(":crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            response.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
        }

        Map<String, Boolean> newLabels = deserializeMap(request.body(), mapper);
        Map<String, Boolean> labelsCache = saveNewLabels(crawlerId, newLabels);
        response.status(HttpServletResponse.SC_CREATED);
        return labelsCache;
    };

    private Map<String, Boolean> saveNewLabels(String crawlerId, Map<String, Boolean> newLabels)
            throws IOException {

        Map<String, Boolean> labelsCache = getLabelsCache(crawlerId);
        for (Entry<String, Boolean> label : newLabels.entrySet()) {
            labelsCache.put(label.getKey(), label.getValue());
        }

        Path filePath = getLabelsFilename(crawlerId);
        mapper.writeValue(filePath.toFile(), labelsCache);

        return labelsCache;
    }

    public final Route getLabels = (request, response) -> {
        String crawlerId = request.params(":crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            response.status(HttpServletResponse.SC_NOT_FOUND);
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
        }

        return getLabelsCache(crawlerId);
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
                String json = new String(Files.readAllBytes(filePath), "UTF-8");
                return deserializeMap(json, mapper);
            }
        } catch (IOException e) {
            String filename = filePath != null ? filePath.toString() : null;
            logger.error("Failed to load labels from file: " + filename, e);
        }
        return null;
    }

    private Path getLabelsFilename(String crawlerId) {
        CrawlContext crawlContext = crawlersManager.getCrawl(crawlerId);
        Path filePath = Paths.get(crawlContext.dataPath, "labels.json");
        return filePath;
    }

    private static Map<String, Boolean> deserializeMap(String body, ObjectMapper mapper)
            throws IOException, JsonParseException, JsonMappingException {
        TypeReference<HashMap<String, Boolean>> typeRef =
                new TypeReference<HashMap<String, Boolean>>() {};
        Map<String, Boolean> labels = mapper.readValue(body, typeRef);
        return labels;
    }

}
