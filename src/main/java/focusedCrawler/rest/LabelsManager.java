package focusedCrawler.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Route;

public class LabelsManager {

    private static final ObjectMapper mapper = new ObjectMapper();

    private Path filePath;
    private Map<String, Boolean> labelsCache;

    public LabelsManager(String dataPath) {
        this.labelsCache = new HashMap<>();
        try {
            filePath = Paths.get(dataPath, "labels.json");
            if (Files.exists(filePath)) {
                String json = new String(Files.readAllBytes(filePath), "UTF-8");
                this.labelsCache = deserializeMap(json, mapper);
            }
        } catch (IOException e) {
            String filename = filePath != null ? filePath.toString() : null;
            throw new RuntimeException("Failed to load labels from file: " + filename, e);
        }
    }

    public Route addLabelsResource = (request, response) -> {

        Map<String, Boolean> labels = deserializeMap(request.body(), mapper);
        for (Entry<String, Boolean> entry : labels.entrySet()) {
            labelsCache.put(entry.getKey(), entry.getValue());
        }

        synchronized (this) {
            mapper.writeValue(filePath.toFile(), labelsCache);
        }

        response.status(HttpServletResponse.SC_CREATED);
        return labelsCache;
    };

    public Route getLabelsResource = (request, response) -> {
        return labelsCache;
    };

    private static Map<String, Boolean> deserializeMap(String body, ObjectMapper mapper)
            throws IOException, JsonParseException, JsonMappingException {
        TypeReference<HashMap<String, Boolean>> typeRef =
                new TypeReference<HashMap<String, Boolean>>() {};
        Map<String, Boolean> labels = mapper.readValue(body, typeRef);
        return labels;
    }

}
