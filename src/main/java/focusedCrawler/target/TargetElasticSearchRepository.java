package focusedCrawler.target;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import focusedCrawler.target.elasticsearch.ElasticSearchClientFactory;
import focusedCrawler.target.elasticsearch.ElasticSearchConfig;
import focusedCrawler.target.elasticsearch.ElasticSearchPageModel;
import focusedCrawler.util.Target;

public class TargetElasticSearchRepository implements TargetRepository {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private Client client;
    private String typeName;
    private String indexName;
    
    public TargetElasticSearchRepository(ElasticSearchConfig config,
                                         String indexName,
                                         String typeName) {
        this.client = ElasticSearchClientFactory.createClient(config);
        this.indexName = indexName;
        this.typeName = typeName;
        this.createIndexMapping(indexName);
    }

    private void createIndexMapping(String indexName) {
        
        boolean exists = client.admin().indices().prepareExists(indexName)
                .execute().actionGet().isExists();
        
        if(!exists) {
            String targetMapping = "{\"properties\": {"
                + "\"domain\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"words\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"wordsMeta\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"retrieved\": {\"format\": \"dateOptionalTime\",\"type\": \"date\"},"
                + "\"text\": {\"type\": \"string\"},\"title\": {\"type\": \"string\"},"
                + "\"url\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"topPrivateDomain\": {\"type\": \"string\",\"index\": \"not_analyzed\"}"
                + "}}";
            
            client.admin().indices().prepareCreate(indexName)
                .addMapping("target", targetMapping)
                .addMapping("negative", targetMapping)
                .execute()
                .actionGet();
        }
    }

    public boolean insert(Target target, int counter) {
        return index(target);
    }

    public boolean insert(Target target) {
        return index(target);
    }

    private boolean index(Target target) {

        ElasticSearchPageModel data = new ElasticSearchPageModel(target);

        String docId = target.getIdentifier();
        IndexResponse response = client.prepareIndex(indexName, typeName, docId)
                .setSource(serializeAsJson(data))
                .execute()
                .actionGet();

        return response.isCreated();
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
