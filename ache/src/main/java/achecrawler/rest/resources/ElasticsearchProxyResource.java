package achecrawler.rest.resources;

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

import achecrawler.crawler.CrawlersManager;
import achecrawler.crawler.CrawlersManager.CrawlContext;
import achecrawler.rest.Transformers;
import achecrawler.target.repository.elasticsearch.ElasticSearchConfig;
import spark.Route;

public class ElasticsearchProxyResource {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchProxyResource.class);

    private CloseableHttpClient httpclient;
    private CrawlersManager crawlersManager;

    public ElasticsearchProxyResource(CrawlersManager crawlerManager) {
        this.crawlersManager = crawlerManager;
        this.httpclient = HttpClients.createDefault();
    }

    public Route searchApi = (request, response) -> {

        String crawlerId = request.params(":crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            response.status(HttpServletResponse.SC_NOT_FOUND);
            response.header("Content-Type", "application/json");
            return ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId);
        }

        if (!context.isSearchEnabled()) {
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            response.header("Content-Type", "application/json");
            return Transformers.json.render(ImmutableMap.of(
                "message", "No Elasticsearch index configured"));
        }

        ElasticSearchConfig esConfig = context.getEsConfig();
        try {
            String query = "";
            for (String param : request.queryParams()) {
                query += param + "=" + request.queryParams(param);
            }
            String url = String.format("%s/%s/%s/_search", esConfig.getRestApiHosts().get(0),
                esConfig.getIndexName(), esConfig.getTypeName());
            if (!query.isEmpty()) {
                url += "?" + query;
            }
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(request.body(), "UTF-8"));
            post.addHeader("Content-Type", "application/json"); // mandatory since ES 6.x
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
