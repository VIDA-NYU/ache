package focusedCrawler.tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SimpleBulkIndexer {

    static final ObjectMapper jsonMapper = new ObjectMapper();
    static {
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    private StringBuilder bulkData = new StringBuilder();
    private int bulkSize = 0;
    private int maxBulkSize;
    
    private String elasticSearchAddress;
    private BasicHeader authHeader;
    private static CloseableHttpClient httpclient = HttpClients.createDefault();
    
    public SimpleBulkIndexer(String elasticSearchAddress) {
        this(elasticSearchAddress, null, 25);
    }
    
    public SimpleBulkIndexer(String elasticSearchAddress, int maxBulkSize) {
        this(elasticSearchAddress, null, maxBulkSize);
    }
    
    public SimpleBulkIndexer(String elasticSearchAddress, String authHeader) {
        this(elasticSearchAddress, authHeader, 25);
    }
    
    public SimpleBulkIndexer(String elasticSearchAddress,
                             String userAndPassword,
                             int maxBulkSize) {
        
        this.elasticSearchAddress = elasticSearchAddress;
        this.maxBulkSize = maxBulkSize;
        
        if(userAndPassword != null) {
            String headerName = "Authorization";
            String headerValue = "Basic " +  Base64.encodeBase64String(userAndPassword.getBytes());
            System.out.println(headerName);
            System.out.println(headerValue);
            this.authHeader = new BasicHeader(headerName, headerValue);
        }
    }


    void addDocument(String indexName, String typeName, Object obj, String id) throws IOException {
        
        String command;
        if(id == null) {
            command = "{ \"index\" : { \"_index\" : \""+indexName+"\", \"_type\" : \""+typeName+"\"} }";
        } else {
            command = "{ \"index\" : { \"_index\" : \""+indexName+"\", \"_type\" : \""+typeName+"\", \"_id\":\""+id+"\"} }";
        }
        
        final String json;
        try {
            json = jsonMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON object. ", e);
        }
        
        bulkData.append(command+"\n");
        bulkData.append(json+"\n");
        bulkSize++;
        
        if(bulkSize > maxBulkSize) {
            flushBulk();
        }
    }

    public void flushBulk() throws IOException {
        if(bulkSize > 0) {
            executeBulkRequest(bulkData.toString());
            bulkSize = 0;
            bulkData = new StringBuilder();
        }
    }
    
    public void bulkIndexDocuments(String indexName,
                                   String typeName,
                                   List<String> sources) throws IOException {
        
        StringBuilder builder = new StringBuilder();
        for (String source : sources) {
            String command = "{ \"index\" : { \"_index\" : \""+indexName+"\", \"_type\" : \""+typeName+"\"} }";
            builder.append(command+"\n");
            builder.append(source+"\n");
        }
        executeBulkRequest(builder.toString());
    }
    
    public void bulkIndexDocumentsWithId(String indexName,
                                         String typeName,
                                         Map<String, String> sources) throws IOException {
        
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> source : sources.entrySet()) {
            String command = "{ \"index\" : { \"_index\" : \""+indexName+"\", \"_type\" : \""+typeName+"\", \"_id\":\""+source.getKey()+"\"} }";
            builder.append(command+"\n");
            builder.append(source.getValue()+"\n");
        }
        executeBulkRequest(builder.toString());
    }
    
    private void executeBulkRequest(final String requestBody) throws IOException {
        
        HttpPost httpPost = new HttpPost(elasticSearchAddress +"/_bulk");
        
        if(authHeader != null) {
            httpPost.addHeader(authHeader);
        }
        
        httpPost.setEntity(new StringEntity(
            requestBody, 
            ContentType.create(URLEncodedUtils.CONTENT_TYPE, Charset.forName("UTF-8"))
        ));
        
        CloseableHttpResponse response = httpclient.execute(httpPost);
        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
//            EntityUtils.consume(entity);
            System.out.println(EntityUtils.toString(entity));
        } finally {
            response.close();
        }
    }
    
    public void close() throws IOException {
        flushBulk();
        httpclient.close();
    }
    
}