package achecrawler.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import achecrawler.target.repository.elasticsearch.ElasticSearchClientFactory;
import achecrawler.target.repository.elasticsearch.ElasticSearchConfig;
import achecrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

@Command(name = "DumpDataFromElasticsearch")
public class DumpDataFromElasticsearch extends CliTool {

    private static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Option(name = {"-i", "--es-index"}, description = "The Elasticsearch index name")
    public String indexName = "ache-data";

    @Option(name = {"-t", "--es-type"}, description = "The Elasticsearch type name")
    public String typeName = "page";

    @Option(name = {"-a", "--es-host-address"},
            description = "The Elasticsearch REST API host address")
    public String hostAddress = "http://localhost:9200";

    @Option(name = {"--es-timeout"},
            description = "The Elasticsearch scroll query timeout in minutes")
    public int esTimeout = 10;

    @Option(name = {"-q", "--es-query-string"},
            description = "An Elasticsearch query string to filter data")
    public String query = null;

    @Option(name = {"-o", "--output-file"}, required = true,
            description = "Path for output file")
    public String outputFile = null;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new DumpDataFromElasticsearch());
    }

    @Override
    public void execute() throws Exception {

        FileWriter fw = new FileWriter(new File(this.outputFile));

        ElasticSearchConfig config = new ElasticSearchConfig(Arrays.asList(hostAddress));

        RestClient client = ElasticSearchClientFactory.createClient(config);

        Map<String, ?> esQuery;
        if (this.query == null || this.query.isEmpty()) {
            esQuery = ImmutableMap.of(
                    "match_all", EMPTY_MAP
            );
        } else {
            esQuery = ImmutableMap.of(
                    "query_string", ImmutableMap.of(
                            "default_field", "_all",
                            "query", this.query
                    )
            );
        }

        Map<String, ?> body = ImmutableMap.of(
                "query", esQuery
        );

        AbstractHttpEntity entity = createJsonEntity(serializeAsJson(body));

        String searchEndpoint = String
                .format("/%s/%s/_search?scroll=%dm", indexName, typeName, esTimeout);
        Response scrollResponse = client.performRequest("POST", searchEndpoint, EMPTY_MAP, entity);

        JsonNode jsonResponse = mapper.readTree(scrollResponse.getEntity().getContent());
        List<String> hits = getHits(jsonResponse);
        String scrollId = getScrollId(jsonResponse);
        int i = 0;
        while (hits.size() > 0) {
            System.out.printf("Processing scroll: %d hits: %d\n", ++i, hits.size());
            for (String hit : hits) {
                fw.write(hit);
                fw.write('\n');
            }
            fw.flush();

            // Execute next scroll search request
            Map<String, ?> scrollBody = ImmutableMap.of(
                    "scroll", (this.esTimeout + "m"),
                    "scroll_id", scrollId
            );
            entity = createJsonEntity(serializeAsJson(scrollBody));

            String scrollEndpoint = "/_search/scroll";
            scrollResponse = client.performRequest("POST", scrollEndpoint, EMPTY_MAP, entity);

            jsonResponse = mapper.readTree(scrollResponse.getEntity().getContent());
            hits = getHits(jsonResponse);
            scrollId = getScrollId(jsonResponse);
        }
        client.close();
        fw.close();
        System.out.println("Done.");
    }

    private String getScrollId(JsonNode jsonResponse) throws IOException {
        return jsonResponse.get("_scroll_id").asText();
    }

    private List<String> getHits(JsonNode jsonResponse) throws IOException {
        Iterator<JsonNode> it = jsonResponse.get("hits").get("hits").elements();
        List<String> hits = new ArrayList<>();
        while (it.hasNext()) {
            JsonNode source = it.next();
            hits.add(mapper.writeValueAsString(source));
        }
        return hits;
    }

    private AbstractHttpEntity createJsonEntity(String mapping) {
        return new NStringEntity(mapping, ContentType.APPLICATION_JSON);
    }

    private String serializeAsJson(Object model) {
        String targetAsJson;
        try {
            targetAsJson = mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TargetModel to JSON.", e);
        }
        return targetAsJson;
    }

}
