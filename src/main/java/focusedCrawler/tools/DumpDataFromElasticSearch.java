package focusedCrawler.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import focusedCrawler.target.repository.elasticsearch.ElasticSearchClientFactory;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;

public class DumpDataFromElasticSearch {
    
    static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    static final ObjectMapper jsonMapper = new ObjectMapper();

    private DumpDataFromElasticSearch() {
    }

    static class DDTDocument {
        public String url;
        public String html;
        public String text;
        public int length;
        public String query;
        public Date retrieved;
        public String tag;
    }
    
    public static void main(String[] args) throws Exception {
        
        String inputIndex = "patent_trolls";
        String inputType = "page";
        String inputHostname = "localhost";
        String inputClusterName = "elasticsearch";
        int inputPort = 9300;
        final String basePath = "/data/classifiers/patent_trolls/training_data";
        
        ElasticSearchConfig config = new ElasticSearchConfig(inputHostname, inputPort, inputClusterName);
        Client client = ElasticSearchClientFactory.createClient(config);
        
        SearchResponse scrollResp = client.prepareSearch(inputIndex)
            .setQuery(QueryBuilders.matchAllQuery())
            .setTypes(inputType)
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(60000))
            .setSize(100)
            .execute().actionGet();
        
        while (true) {

            for (SearchHit hit : scrollResp.getHits().getHits()) {
                
                DDTDocument doc;
                final String json = hit.getSourceAsString();
                try {
                    doc = jsonMapper.readValue(json, DDTDocument.class);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to unserialize json object="+json);
                }
                
                
                Path folderPath;
                if(doc.tag != null && doc.tag.equals("Relevant")) {
                    folderPath = Paths.get(basePath, "positive");
                } else if(doc.tag != null && doc.tag.equals("Irrelevant")) {
                    folderPath = Paths.get(basePath, "negative");
                } else {
                    System.err.println("Found unlabeled document.");
                    continue;
                }
                
                final File folder = folderPath.toFile();
                if(!folder.exists()) folder.mkdirs();
                
                
                final String filename = URLEncoder.encode(doc.url, "UTF-8");
                FileWriter fw = new FileWriter(folderPath.resolve(filename).toFile());
                fw.write(doc.html);
                fw.close();
            }
            
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(600000))
                    .execute().actionGet();
            
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
    }

}
