package achecrawler.rest.resources;

import achecrawler.crawler.CrawlersManager;
import achecrawler.crawler.CrawlersManager.CrawlContext;
import achecrawler.target.repository.elasticsearch.ElasticSearchConfig;
import com.google.common.collect.ImmutableMap;
import io.javalin.http.Context;
import io.javalin.http.Handler;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class ElasticsearchProxyResource {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchProxyResource.class);

    private CloseableHttpClient httpclient;
    private CrawlersManager crawlersManager;

    public Handler searchApi = (Context ctx) -> {

        String crawlerId = ctx.pathParam("crawler_id");

        CrawlContext context = crawlersManager.getCrawl(crawlerId);
        if (context == null) {
            ctx.status(HttpServletResponse.SC_NOT_FOUND);
            ctx.header("Content-Type", "application/json");
            ctx.json(ImmutableMap.of("message", "Crawler not found for crawler_id " + crawlerId));
            return;
        }

        if (!context.isSearchEnabled()) {
            ctx.status(HttpServletResponse.SC_BAD_REQUEST);
            ctx.json(ImmutableMap.of(
                    "message", "No Elasticsearch index configured"));
            return;
        }

        ElasticSearchConfig esConfig = context.getEsConfig();
        try {
            StringBuilder query = new StringBuilder();
            for (String param : ctx.queryParamMap().keySet()) {
                for (String paramValue : ctx.queryParams(param)) {
                    query.append(param).append("=").append(paramValue);
                }
            }
            String url = String.format("%s/%s/_msearch", esConfig.getRestApiHosts().get(0),
                    esConfig.getIndexName());
            if (query.length() > 0) {
                url += "?" + query;
            }
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(ctx.body(), "UTF-8"));
            post.addHeader("Content-Type", "application/json"); // mandatory since ES 6.x
            try (CloseableHttpResponse apiResponse = httpclient.execute(post)) {
                HttpEntity entity = apiResponse.getEntity();
                Header[] headers = apiResponse.getAllHeaders();
                for (Header header : headers) {
                    ctx.header(header.getName(), header.getValue());
                }
                String body = EntityUtils.toString(entity);
                ctx.result(body);
            }
        } catch (Exception e) {
            logger.error("Failed to forward request to ElasticSearch.", e);
            throw e;
        }
    };

    public ElasticsearchProxyResource(CrawlersManager crawlerManager) {
        this.crawlersManager = crawlerManager;
        this.httpclient = HttpClients.createDefault();
    }

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
