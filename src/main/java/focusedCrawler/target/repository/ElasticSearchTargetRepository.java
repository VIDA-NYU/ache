package focusedCrawler.target.repository;

import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

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

	public ElasticSearchTargetRepository(ElasticSearchConfig config, String indexName, String typeName) {
		this.client = ElasticSearchClientFactory.createClient(config);
		this.indexName = indexName;
		this.typeName = typeName;
//		this.createIndexMapping(indexName);
	}

//	private void createIndexMapping(String indexName) {
//
//		boolean exists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
//
//		if (!exists) {
//			String targetMapping = "" + "{" + " \"properties\": {"
//					+ "  \"domain\":           {\"type\": \"string\",\"index\": \"not_analyzed\"},"
//					+ "  \"words\":            {\"type\": \"string\",\"index\": \"not_analyzed\"},"
//					+ "  \"wordsMeta\":        {\"type\": \"string\",\"index\": \"not_analyzed\"},"
//					+ "  \"retrieved\":        {\"type\": \"date\",\"format\": \"dateOptionalTime\"},"
//					+ "  \"text\":             {\"type\": \"string\"},"
//					+ "  \"title\":            {\"type\": \"string\"},"
//					+ "  \"url\":              {\"type\": \"string\",\"index\": \"not_analyzed\"},"
//					+ "  \"topPrivateDomain\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
//					+ "  \"isRelevant\":       {\"type\": \"string\",\"index\": \"not_analyzed\"},"
//					+ "  \"relevance\":        {\"type\": \"double\"}" + " }" + "}";
//
//			client.admin().indices().prepareCreate(indexName).addMapping(typeName, targetMapping, XContentType.JSON)
//					.execute().actionGet();
//		}
//	}

	@Override
	public boolean insert(Page page) {

		TargetModelElasticSearch data = new TargetModelElasticSearch(page);
		String docId = page.getURL().toString();

		// We use upsert to avoid overriding existing fields in previously
		// indexed documents
		UpdateResponse response = client.prepareUpdate(indexName, typeName, docId)
				.setDoc(serializeAsJson(data), XContentType.JSON).setDocAsUpsert(true).execute().actionGet();

		boolean isCreated = false;

		isCreated = response.status().equals(RestStatus.OK) ? true : false;

		return isCreated;
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

	@Override
	public void close() {
		client.close();
	}

}
