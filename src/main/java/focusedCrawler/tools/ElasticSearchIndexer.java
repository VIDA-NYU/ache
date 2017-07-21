package focusedCrawler.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.tika.Tika;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.common.base.Preconditions;

import focusedCrawler.memex.cdr.CDR2Document;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.target.model.TargetModelCbor;
import focusedCrawler.target.model.TargetModelElasticSearch;
import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchClientFactory;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.CliTool;
import focusedCrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

//
// TODO: Refactor this class to something simpler and easily maintainable
//
@Command(name="ElasticSearchIndexer", description="Index crawled data in ElasticSearch")
public class ElasticSearchIndexer extends CliTool {
    
    static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    static final ObjectMapper jsonMapper = new ObjectMapper();
    
    static final String format = "yyyy-MM-dd'T'HH:mm:ss";
    
    // Output format option
    
    @Option(name={"-of", "--output-format"}, description="Format used for output data: {ACHE,CDR}")
    String outputFormat = "ACHE";
    
    // Elastic Search output options
    
    @Option(name={"-oi", "--output-es-index"}, required=true, description="ElasticSearch index name (output)")
    String outputIndex;
    
    @Option(name={"-ot", "--output-es-type"}, required=true, description="ElasticSearch index type (output)")
    String outputType;

    @Option(name={"-ou", "--output-es-url"}, description="ElasticSearch full HTTP URL address")
    String elasticSearchServer = "http://localhost:9200";
    
    @Option(name={"-oa", "--output-es-auth"},  description="User and password for ElasticSearch in format: user:pass")
    String userPass = null;
    
    @Option(name={"-obs", "--output-es-bulk-size"}, description="ElasticSearch bulk size")
    int bulkSize = 25;
    
    // Input options
    
    @Option(name={"-if", "--input-format"}, description="Format of input data: {CBOR,FILE,ELASTICSEARCH}")
    String inputFormat = "FILE";

    @Option(name={"-id", "--input-dir"}, description="Input directory, if using CBOR or FILE")
    String inputDirectory;
    
    // Elastic Search input options
    
    @Option(name={"-ii", "--input-es-index"}, description="Input ES index, if using ELASTICSEARCH")
    String inputIndex;
    
    @Option(name={"-it", "--input-es-type"}, description="Input ES type, if using ELASTICSEARCH")
    String inputType;
    
    @Option(name={"-ih", "--input-es-hostname"}, description="Input ES hostname, if using ELASTICSEARCH")
    String inputHostname = "localhost";

    @Option(name={"-ic", "--input-es-cluster"}, description="Input ES cluster name, if using ELASTICSEARCH")
    String inputClusterName = "elasticsearch";
    
    @Option(name={"-ip", "--input-es-port"}, description="Input ES port number, if using ELASTICSEARCH")
    int inputPort = 9300;
    
    // Filtering options
    
    @Option(name={"-sd", "--start-date"}, description="Only index data fetcher after this date")
    String startStr = null;

    @Option(name={"-en", "--end-date"}, description="Only index data fetched before this date")
    String endStr = null;
    
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new ElasticSearchIndexer());
    }
    
    public void execute() throws Exception {
        SimpleBulkIndexer bulkIndexer = new SimpleBulkIndexer(elasticSearchServer, userPass, bulkSize);
        if(inputFormat.equals("ELASTICSEARCH")) {
            indexFromElasticSearch(bulkIndexer, outputFormat, outputIndex, outputType);
        }
        else {
            Date startDate = startStr != null ? new SimpleDateFormat(format).parse(startStr) : null;
            Date endDate = endStr != null ? new SimpleDateFormat(format).parse(endStr) : null;    
            
            Preconditions.checkNotNull(inputDirectory, "Input directory option can't be null");
            Path inputPath = Paths.get(inputDirectory);
            indexFromFile(bulkIndexer, outputIndex, outputType, startDate, endDate,
                          inputPath, outputFormat, inputFormat);
        }
        bulkIndexer.close();
    }
    
    private void indexFromFile(SimpleBulkIndexer bulkIndexer, String indexName,
                               String typeName, Date startDate, Date endDate,
                               Path inputPath, String outputFormat, String inputFormat)
                               throws IOException {
        
        DirectoryStream<Path> fileStream = Files.newDirectoryStream(inputPath);
        for (Path filePath : fileStream) {
            File f = filePath.toFile();
            
            if(f.isDirectory()) {
                // recursivelly index files in the subfolder
                indexFromFile(bulkIndexer, indexName, typeName, startDate, endDate,
                              filePath, outputFormat, inputFormat);
                continue;
            }
            
            try {
                Date fileDate = new Date(f.lastModified());
                
                if(startDate != null && fileDate.before(startDate)) continue;
                if(endDate != null && fileDate.after(endDate)) continue;
    
                String id;
                Object doc;
                
                if (inputFormat.equals("CBOR")) {
                    
                    TargetModelCbor input = cborMapper.readValue(f, focusedCrawler.target.model.TargetModelCbor.class);
                    if (outputFormat.equals("ACHE")) {
                        id = input.url;
                        doc = new TargetModelElasticSearch(input);
                    }
                    else if (outputFormat.equals("CDR")) {
                        id = null;
                        doc = new MemexCrawlSchema(input);
                    }
                    else {
                        throw new IllegalArgumentException("Invalid output format = "+outputFormat);
                    }
                    
                }
                else if(inputFormat.equals("FILE")){
                    
                    final byte[] bytes = Files.readAllBytes(filePath);
                    String fileAsString = new String(bytes);
                    String url = URLDecoder.decode(f.getName(), "UTF-8");
                    
                    if (outputFormat.equals("ACHE")) {
                        Page page = new Page(new URL(url), fileAsString);
                        page.setParsedData(new ParsedData(new PaginaURL(page)));
                        
                        id = url;
                        doc = new TargetModelElasticSearch(page);
                    }
                    else if (outputFormat.equals("CDR")) {
                        Tika tika = new Tika();
                        String mediaType = tika.detect(bytes);
                        if(mediaType != null && !mediaType.startsWith("text")) {
                        	fileAsString =  Base64.encodeBase64String(bytes);
                        }
                        id = url; 
                        doc = new MemexCrawlSchema(
                            url,
                            f.lastModified(),
                            "NYU",
                            "ACHE-script",
                            fileAsString,
                            mediaType,
                            null
                        );
                    }
                    else {
                        throw new IllegalArgumentException("Invalid output schema");
                    }
                    
                }
                else if(inputFormat.equals("FILESYSTEM_JSON_ZIP")){
                    if (outputFormat.equals("CDR2")) {
                        
                        final byte[] bytes = Files.readAllBytes(filePath);
                        
                        TargetModelJson pageModel = null;
                        try(InputStream gzip = new InflaterInputStream(new ByteArrayInputStream(bytes))) {
                            pageModel = jsonMapper.readValue(gzip, TargetModelJson.class);
                        }
                        if(pageModel == null) {
                            continue;
                        }
                        
                        List<String> contentTypeHeader = pageModel.getResponseHeaders().get("Content-Type");
                        if(contentTypeHeader == null) {
                            contentTypeHeader = pageModel.getResponseHeaders().get("content-type");
                        }
                        
                        if(contentTypeHeader == null || contentTypeHeader.size() == 0) {
                            continue;
                        }
                        
                        if(!contentTypeHeader.iterator().next().contains("text/html")) {
                            continue;
                        }
                        
                        id = pageModel.getUrl();
                        
                        HashMap<String, Object> crawlData = new HashMap<>();
                        crawlData.put("response_headers", pageModel.getResponseHeaders());
                        
                        doc = new CDR2Document.Builder()
                                .setUrl(pageModel.getUrl())
                                .setTimestamp(pageModel.getFetchTime())
                                .setContentType("text/html")
                                .setTeam("NYU")
                                .setCrawler("ACHE")
                                .setRawContent(pageModel.getContentAsString())
                                .setCrawlData(crawlData)
                                .build();
                        
                    } else {
                        throw new IllegalArgumentException("Invalid output schema");
                    }
                }
                else {
                    throw new IllegalArgumentException("Invalid input format = "+inputFormat);
                }
                
            	bulkIndexer.addDocument(indexName, typeName, doc, id);
                    
            }
            catch(Exception e) {
                System.err.println("Problem while indexing file: "+f.getCanonicalPath());
                e.printStackTrace();
            }
        }
        fileStream.close();
    }

    private void indexFromElasticSearch(SimpleBulkIndexer bulkIndexer, String outputFormat,
                                        String outputIndex, String outputType)
                                        throws IOException {
        
        Preconditions.checkNotNull(inputIndex, "Input index can't be null");
        Preconditions.checkNotNull(inputType, "Input type can't be null");
        
        ElasticSearchConfig config = new ElasticSearchConfig(inputHostname, inputPort, inputClusterName);
        Client client = ElasticSearchClientFactory.createClient(config);
        
        SearchResponse scrollResp = client.prepareSearch(inputIndex)
            .setQuery(QueryBuilders.matchAllQuery())
            .setTypes(inputType)
            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
            .setScroll(new TimeValue(60000))
            .setSize(100)
            .execute().actionGet();
        
        while (true) {

            for (SearchHit hit : scrollResp.getHits().getHits()) {
                
                String id;
                Object doc;
                
                TargetModelElasticSearch pageModel;
                final String json = hit.getSourceAsString();
                try {
                    pageModel = jsonMapper.readValue(json, TargetModelElasticSearch.class);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to unserialize json object="+json);
                }
                
                if (outputFormat.equals("ACHE")) {
                    id = pageModel.getUrl();
                    doc = pageModel;
                }
                else if (outputFormat.equals("CDR")) {
                    id = null; 
                    doc = new MemexCrawlSchema(
                        pageModel.getUrl(),
                        pageModel.getRetrieved().getTime(),
                        "NYU",
                        "ACHE-script",
                        pageModel.getHtml(),
                        "text/html",
                        null
                    );
                }
                else {
                    throw new IllegalArgumentException("Invalid output format ("+outputFormat+")");
                }
                
                bulkIndexer.addDocument(outputIndex, outputType, doc, id);
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
