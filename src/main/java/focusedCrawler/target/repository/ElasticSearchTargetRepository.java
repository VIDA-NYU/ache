package focusedCrawler.target.repository;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.TargetModelElasticSearch;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchClientFactory;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;

public class ElasticSearchTargetRepository implements TargetRepository {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private Client client;
    private String typeName;
    private String indexName;
    
    public ElasticSearchTargetRepository(ElasticSearchConfig config,
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

    public boolean insert(Page target, int counter) {
        return index(target);
    }

    public boolean insert(Page target) {
        return index(target);
    }

    private boolean index(Page page) {

        TargetModelElasticSearch data = new TargetModelElasticSearch(page);

        String docId = page.getIdentifier();
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
