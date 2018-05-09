package focusedCrawler.target.repository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.TargetModelElasticSearch;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.CloseableIterator;

public class ElasticSearchRestTargetRepository implements TargetRepository {
    
    private static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchRestTargetRepository.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    static {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private RestClient client;
    private String typeName;
    private String indexName;
    
    public ElasticSearchRestTargetRepository(ElasticSearchConfig config) {
        this.indexName = config.getIndexName();
        this.typeName = config.getTypeName();
        this.client = createRestClient(config);
        this.createIndexMapping(indexName);
    }

    private void createIndexMapping(String indexName) {

        String indexEndpoint = "/" + indexName;
        boolean exists = false;
        String esVersion = "5.x.x";
        try {
            Response existsResponse = client.performRequest("HEAD", indexEndpoint);
            exists = (existsResponse.getStatusLine().getStatusCode() == 200);
            
            Response rootResponse = client.performRequest("GET", "/");
            String json = EntityUtils.toString(rootResponse.getEntity());
            String versionNumber = mapper.readTree(json).path("version").path("number").asText();
            if (versionNumber != null && !versionNumber.isEmpty()) {
                esVersion = versionNumber;
            }
            logger.info("Elasticsearch version: {}", esVersion);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to check whether index already exists in Elasticsearch.", e);
        }

        if (!exists) {
            final String targetMapping1x = ""
                + "{"
                + "  \"properties\": {"
                + "    \"domain\":           {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "    \"words\":            {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "    \"wordsMeta\":        {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "    \"retrieved\":        {\"type\": \"date\",\"format\": \"dateOptionalTime\"},"
                + "    \"text\":             {\"type\": \"string\"},"
                + "    \"title\":            {\"type\": \"string\"},"
                + "    \"url\":              {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "    \"topPrivateDomain\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "    \"isRelevant\":       {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "    \"crawlerId\":        {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "    \"relevance\":        {\"type\": \"double\"}"
                + "  }"
                + "}";
            
            final String pageMapping5x =""
                + "{"
                + "  \"properties\": {"
                + "    \"domain\":           {\"type\": \"keyword\",\"index\": true},"
                + "    \"words\":            {\"type\": \"keyword\",\"index\": true},"
                + "    \"wordsMeta\":        {\"type\": \"keyword\",\"index\": true},"
                + "    \"retrieved\":        {\"type\": \"date\",\"format\": \"dateOptionalTime\"},"
                + "    \"text\":             {\"type\": \"text\"},"
                + "    \"title\":            {\"type\": \"text\"},"
                + "    \"url\":              {\"type\": \"keyword\",\"index\": true},"
                + "    \"topPrivateDomain\": {\"type\": \"keyword\",\"index\": true},"
                + "    \"isRelevant\":       {\"type\": \"keyword\",\"index\": true},"
                + "    \"crawlerId\":        {\"type\": \"keyword\",\"index\": true},"
                + "    \"relevance\":        {\"type\": \"double\"}"
                + "  }"
                + "}";
            
            String pageProperties = esVersion.startsWith("5.") ? pageMapping5x : targetMapping1x;
            
            String mapping =
                     "{"
                   + "  \"mappings\": {"
                   + "    \"" + typeName + "\": " + pageProperties
                   + "  }"
                   + "}";
            
            try {
                AbstractHttpEntity entity = createJsonEntity(mapping);
                Response response = client.performRequest("PUT", indexEndpoint, EMPTY_MAP, entity);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException(
                        "Failed to create index in Elasticsearch." + response.toString());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create index in Elasticsearch.", e);
            }
        }
    }

    private AbstractHttpEntity createJsonEntity(String mapping) {
        return new NStringEntity(mapping, ContentType.APPLICATION_JSON);
    }

    @Override
    public boolean insert(Page page) {
        TargetModelElasticSearch document = new TargetModelElasticSearch(page);
        String docId = encodeUrl(page.getURL().toString());
        // We use upsert to avoid overriding existing fields in previously indexed documents
        String endpoint = String.format("/%s/%s/%s/_update", indexName, typeName, docId);
        Map<String, ?> body = ImmutableMap.of(
            "doc", document,
            "doc_as_upsert", true
        );
        AbstractHttpEntity entity = createJsonEntity(serializeAsJson(body));
        try {
            Response response = client.performRequest("POST", endpoint, EMPTY_MAP, entity);
            return response.getStatusLine().getStatusCode() == 201;
        } catch (IOException e) {
            throw new RuntimeException("Failed to index page.", e);
        }
    }

    private String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to URL encode string: "+url, e);
        }
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

    public RestClient createRestClient(ElasticSearchConfig config) {

        List<String> esHosts = config.getRestApiHosts();
        List<HttpHost> hosts = new ArrayList<>();
        for (String host : esHosts) {
            try {
                URL url = new URL(host);
                hosts.add(new HttpHost(url.getHost(), url.getPort()));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to initialize Elasticsearch REST client. "
                        + "Invalid host: " + host, e);
            }
        }

        HttpHost[] httpHostsArray = (HttpHost[]) hosts.toArray(new HttpHost[hosts.size()]);
        
        client = RestClient.builder(httpHostsArray)
            .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                @Override
                public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                    return requestConfigBuilder
                            .setConnectTimeout(config.getRestConnectTimeout())
                            .setSocketTimeout(config.getRestSocketTimeout());
                }
            })
            .setMaxRetryTimeoutMillis(config.getRestMaxRetryTimeoutMillis())
            .build();
        
        logger.info("Initialized Elasticsearch REST client for hosts: "+Arrays.toString(httpHostsArray));
        return client;
    }

    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close Elasticsearch REST client", e);
        }
    }

    @Override
    public CloseableIterator<Page> pagesIterator() {
        throw new UnsupportedOperationException(
                "Iterator not supportted for ElasticSearchRestTargetRepository yet");
    }

}
