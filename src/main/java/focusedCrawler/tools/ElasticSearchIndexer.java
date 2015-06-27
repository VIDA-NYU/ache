package focusedCrawler.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.tika.Tika;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import focusedCrawler.target.TargetModel;
import focusedCrawler.target.elasticsearch.ElasticSearchClientFactory;
import focusedCrawler.target.elasticsearch.ElasticSearchConfig;
import focusedCrawler.target.elasticsearch.ElasticSearchPageModel;
import focusedCrawler.util.Page;

public class ElasticSearchIndexer {
    
    static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    static final ObjectMapper jsonMapper = new ObjectMapper();
    
    static final String format = "yyyy-MM-dd'T'HH:mm:ss";
    
    public static void main(String[] args) throws Exception {
        
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        
        options.addOption("if", "input-format", true, "Format of input data: {CBOR,FILE,ELASTICSEARCH}");
        options.addOption("id", "input-dir", true, "Input directory, if using CBOR or FILE");
        options.addOption("ii", "input-es-index", true, "Input ES index, if using ELASTICSEARCH");
        options.addOption("it", "input-es-type", true, "Input ES type, if using ELASTICSEARCH");
        options.addOption("ih", "input-es-hostname", true, "Input ES hostname, if using ELASTICSEARCH");
        options.addOption("ih", "input-es-cluster", true, "Input ES cluster name, if using ELASTICSEARCH");
        
        options.addOption("ou", "output-es-url", true, "ElasticSearch full HTTP URL address");
        options.addOption("oa", "output-es-auth", true, "User and password for ElasticSearch in format: 'user:pass'");
        options.addOption("oi", "output-es-index", true, "ElasticSearch index name");
        options.addOption("ot", "output-es-type", true, "ElasticSearch type name");
        options.addOption("obs", "output-es-bulk-size", true, "ElasticSearch bulk size");
        options.addOption("of", "output-format", true, "Format used for output data: {ACHE,CDR}");
        
        options.addOption("st", "start-date", true, "Indexes only data fetcher after this date");
        options.addOption("en", "end-date", true, "Indexes only data fetched before this date");

        CommandLine cmd = parser.parse(options, args);
        
        String outputIndex = getMandatoryOption(options, cmd, "output-es-index");
        String outputType = getMandatoryOption(options, cmd, "output-es-type");
        String outputFormat = cmd.getOptionValue("output-format", "ACHE");
        
        String inputFormat = getMandatoryOption(options, cmd, "input-format");
        
        SimpleBulkIndexer bulkIndexer = createBulkIndexer(cmd);
        if(inputFormat.equals("ELASTICSEARCH")) {
            indexFromElasticSearch(options, cmd, bulkIndexer, outputFormat, outputIndex, outputType);
        }
        else {
            String startStr = cmd.getOptionValue("start-date");
            Date startDate = startStr != null ? new SimpleDateFormat(format).parse(startStr) : null;
            
            String endStr = cmd.getOptionValue("end-date");
            Date endDate = endStr != null ? new SimpleDateFormat(format).parse(endStr) : null;    
            
            Path inputPath = Paths.get(getMandatoryOption(options, cmd, "input-dir"));
            indexFromFile(bulkIndexer, outputIndex, outputType, startDate, endDate,
                          inputPath, outputFormat, inputFormat);
        }
        bulkIndexer.close();
    }
    

    private static String getMandatoryOption(Options options, CommandLine cmd, String optionName) {
        String value = cmd.getOptionValue(optionName);
        if(value == null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(ElasticSearchIndexer.class.getName(), options, true);
            System.exit(0);
        }
        return value;
    }

    private static SimpleBulkIndexer createBulkIndexer(CommandLine cmd) {
        String elasticSearchServer = cmd.getOptionValue("es-server", "http://localhost:9200");
        int bulkSize = Integer.valueOf(cmd.getOptionValue("es-bulk-size", "5"));
        String userPass = cmd.getOptionValue("es-auth-header", null);
        
        SimpleBulkIndexer bulkIndexer = new SimpleBulkIndexer(elasticSearchServer, userPass, bulkSize);
        return bulkIndexer;
    }

    private static void indexFromFile(SimpleBulkIndexer bulkIndexer, String indexName,
                                      String typeName, Date startDate, Date endDate,
                                      Path inputPath, String outputFormat, String inputFormat)
                                      throws IOException {
        
        DirectoryStream<Path> fileStream = Files.newDirectoryStream(inputPath);
        for (Path p : fileStream) {
            File f = p.toFile();
            try {
                Date fileDate = new Date(f.lastModified());
                
                if(startDate != null && fileDate.before(startDate)) continue;
                if(endDate != null && fileDate.after(endDate)) continue;
    
                
                String id;
                Object doc;
                
                if (inputFormat.equals("CBOR")) {
                    
                    TargetModel input = cborMapper.readValue(f, focusedCrawler.target.TargetModel.class);
                    if (outputFormat.equals("ACHE")) {
                        id = input.url;
                        doc = new ElasticSearchPageModel(input);
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
                    
                    final byte[] bytes = Files.readAllBytes(p);
                    String fileAsString = new String(bytes);
                    String url = URLDecoder.decode(f.getName(), "UTF-8");
                    
                    if (outputFormat.equals("ACHE")) {
                        Page page = new Page(new URL(url), fileAsString);
                        // TODO: parse page
                        id = url;
                        doc = new ElasticSearchPageModel(page);
                    }
                    else if (outputFormat.equals("CDR")) {
                        Tika tika = new Tika();
                        String mediaType = tika.detect(bytes);
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

    private static void indexFromElasticSearch(Options options, CommandLine cmd,
                                               SimpleBulkIndexer bulkIndexer, String outputFormat,
                                               String outputIndex, String outputType)
                                               throws IOException {
        
        String inputIndex = cmd.getOptionValue("input-es-index");
        String inputType = cmd.getOptionValue("input-es-type");
        String inputHostname = cmd.getOptionValue("input-es-hostname");
        String inputClusterName = cmd.getOptionValue("input-es-cluster", "elasticsearch");
        int inputPort = Integer.valueOf(cmd.getOptionValue("input-es-port", "9300"));
        
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
                
                String id;
                Object doc;
                
                ElasticSearchPageModel pageModel;
                final String json = hit.getSourceAsString();
                try {
                    pageModel = jsonMapper.readValue(json, ElasticSearchPageModel.class);
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
                        pageModel.getText(), // FIXME this value should use raw HTML field
                        "text/html",
                        null
                    );
                }
                else {
                    throw new IllegalArgumentException("Invalid output format.");
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
