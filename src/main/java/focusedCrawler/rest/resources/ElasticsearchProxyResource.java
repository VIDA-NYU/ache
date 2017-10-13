package focusedCrawler.rest.resources;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import focusedCrawler.config.Configuration;
import focusedCrawler.rest.Transformers;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import spark.Route;

public class ElasticsearchProxyResource {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchProxyResource.class);

    private CloseableHttpClient httpclient;

    private ElasticSearchConfig esConfig;
    private boolean isElasticsearchEnabled = false;
    private String esHostAddress;
    private String esIndexName;
    private String esTypeName;

    public ElasticsearchProxyResource(Configuration config) {
        this.httpclient = HttpClients.createDefault();
        this.updateConfig(config);
    }

    public Route searchApi = (request, response) -> {

        if (isElasticsearchEnabled) {
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            response.header("Content-Type", "application/json");
            return Transformers.json.render(ImmutableMap.of(
                "message", "No Elasticsearch index configured"));
        }

        try {
            String query = "";
            for (String param : request.queryParams()) {
                query += param + "=" + request.queryParams(param);
            }
            String url = String.format("%s/%s/%s/_search", esHostAddress, esIndexName, esTypeName);
            if (!query.isEmpty()) {
                url += "?" + query;
            }
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(request.body(), "UTF-8"));
            CloseableHttpResponse apiResponse = httpclient.execute(post);
            try {
                HttpEntity entity = apiResponse.getEntity();
                Header[] headers = apiResponse.getAllHeaders();
                for (Header header : headers) {
                    response.header(header.getName(), header.getValue());
                }
                String body = EntityUtils.toString(entity);
                response.body(body);
                return body;
            } finally {
                apiResponse.close();
            }
        } catch (Exception e) {
            logger.error("Failed to forward request to ElasticSearch.", e);
            throw e;
        }
    };

    public void close() {
        try {
            if (this.httpclient != null) {
                this.httpclient.close();
            }
        } catch (IOException e) {
            logger.error("Failed to close http client.", e);
        }
    }

    public void updateConfig(Configuration config) {
        if (config != null && config.getTargetStorageConfig().isElasticsearchRestEnabled()) {
            this.isElasticsearchEnabled = true;
            this.esConfig = config.getTargetStorageConfig().getElasticSearchConfig();
        } else {
            this.isElasticsearchEnabled = false;
            this.esConfig = null;
        }
    }

    public String getIndexName() {
        return esIndexName;
    }

    public String getTypeName() {
        return esTypeName;
    }

    public boolean isElasticsearchEnabled() {
        return esConfig != null && isElasticsearchEnabled;
    }

}
