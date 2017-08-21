package focusedCrawler.rest.resources;

import java.io.IOException;

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

import spark.Route;

public class ElasticsearchProxyResource {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchProxyResource.class);

    private CloseableHttpClient httpclient;

    private String esHostAddress;
    private String esIndexName;
    private String esTypeName;

    public ElasticsearchProxyResource(String esHostAddress, String esIndexName, String esTypeName) {
        this.esHostAddress = esHostAddress;
        this.esIndexName = esIndexName;
        this.esTypeName = esTypeName;
        this.httpclient = HttpClients.createDefault();
    }

    public Route searchApi = (request, response) -> {
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

}
