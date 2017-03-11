package focusedCrawler.memex.cdr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.tools.SimpleBulkIndexer;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="AcheToCdrExporter", description="Exports crawled data to CDR format")
public class AcheToCdrExporter extends CliTool {
    
    //
    // Input data options
    //
    
    @Option(name = "--input-path", description="Path to ACHE data target folder", required=true)
    private String inputPath;

    @Option(name={"--repository-type", "-rt"}, description="Which repository type should be used", required=true)
    private RepositoryType repositoryType;
    
    public enum RepositoryType { 
        FILES, FILESYSTEM_JSON;
    }

    @Option(name="--fs-hashed", description="Whether ACHE filesystem repository files names are hashed")
    private boolean hashFilename = false;
    
    @Option(name="--fs-compressed", description="Whether ACHE filesystem repository files is compressed")
    private boolean compressData = false;
    
    //
    // Options for output data format
    //
    
    @Option(name="--cdr-version", description="Which CDR version should be used")
    private CDRVersion cdrVersion = CDRVersion.CDRv2;
    
    public enum CDRVersion {
        CDRv2, CDRv3
    }
    
    @Option(name="--output-file", description="Gziped output file containing data formmated as per CDR schema")
    private String outputFile;
    
    // Elastic Search output options
    
    @Option(name={"--output-es-index", "-oi"}, description="ElasticSearch index name (output)")
    String outputIndex;
    
    @Option(name={"--output-es-type", "-ot"}, description="ElasticSearch index type (output)")
    String outputType;

    @Option(name={"--output-es-url", "-ou"}, description="ElasticSearch full HTTP URL address")
    String elasticSearchServer = "http://localhost:9200";
    
    @Option(name={"--output-es-auth", "-oa"}, description="User and password for ElasticSearch in format: user:pass")
    String userPass = null;
    
    @Option(name={"--output-es-bulk-size", "-obs"}, description="ElasticSearch bulk size")
    int bulkSize = 25;

    
    //
    // Runtime variables
    //
    private int processedPages = 0;
    private PrintWriter out;
    private SimpleBulkIndexer bulkIndexer;
    private String id;
    private Object doc;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new AcheToCdrExporter());
    }
    
    @Override
    public void execute() throws Exception {
        
        System.out.println("Reading ACHE data from: "+inputPath);
        System.out.println("Generating CDR file at: "+outputFile);
        System.out.println(" Compressed repository: "+compressData);
        System.out.println("      Hashed file name: "+hashFilename);
        
        if(outputFile != null) {
            GZIPOutputStream gzipStream = new GZIPOutputStream(new FileOutputStream(outputFile));
            out = new PrintWriter(gzipStream, true);
        }

        if(elasticSearchServer != null) {
            bulkIndexer = new SimpleBulkIndexer(elasticSearchServer, userPass, bulkSize);
        }
        
        Iterator<TargetModelJson> it;
        if(repositoryType == RepositoryType.FILESYSTEM_JSON) {
            FileSystemTargetRepository repository = new FileSystemTargetRepository(inputPath,
                    DataFormat.JSON, hashFilename, compressData);
            it = repository.iterator();
        } else {
            FilesTargetRepository repository = new FilesTargetRepository(inputPath);
            it = repository.iterator();
        }
        
        while (it.hasNext()) {
            TargetModelJson pageModel = it.next();
            processRecord(pageModel);
            processedPages++;
            if(processedPages % 100 == 0) {
                System.out.printf("Processed %d pages\n", processedPages);
            }
        }
        System.out.printf("Processed %d pages\n", processedPages);
        
        //it.close();
        
        if(out != null) out.close();
        if(bulkIndexer!= null) bulkIndexer.close();
        
        System.out.println("done.");
    }

    private void processRecord(TargetModelJson pageModel) throws IOException {
        String contentType = pageModel.getContentType();

        if (contentType == null || contentType.isEmpty()) {
            System.err.println("Ignoring URL with no content-type: "+pageModel.getUrl());
            return;
        }

        if (!contentType.startsWith("text/")) {
            return;
        }

        if(cdrVersion == CDRVersion.CDRv2) {
            createCDR2DocumentJson(pageModel);
        } else {
            createCDR3DocumentJson(pageModel);
        }
        
        if(doc != null&& out != null) {
            out.println(doc);
        }
        
        if(bulkIndexer != null) {
            bulkIndexer.addDocument(outputIndex, outputType, doc, id);
        }
        
        
    }

    public void createCDR2DocumentJson(TargetModelJson pageModel) {
        HashMap<String, Object> crawlData = new HashMap<>();
        crawlData.put("response_headers", pageModel.getResponseHeaders());
        
        CDR2Document.Builder builder = new CDR2Document.Builder()
                .setUrl(pageModel.getUrl())
                .setTimestamp(pageModel.getFetchTime())
                .setContentType(pageModel.getContentType())
                .setVersion("2.0")
                .setTeam("NYU")
                .setCrawler("ACHE")
                .setRawContent(pageModel.getContentAsString())
                .setCrawlData(crawlData);

        CDR2Document doc = builder.build();
        this.id = doc.getId();
        this.doc = doc;
    }
    
    public void createCDR3DocumentJson(TargetModelJson pageModel) {
        HashMap<String, Object> crawlData = new HashMap<>();
        crawlData.put("response_headers", pageModel.getResponseHeaders());
        
        CDR3Document.Builder builder = new CDR3Document.Builder()
                .setUrl(pageModel.getUrl())
                .setTimestampCrawl(new Date(pageModel.getFetchTime()))
                .setTimestampIndex(new Date())
                .setContentType(pageModel.getContentType())
                .setTeam("NYU")
                .setCrawler("ACHE")
                .setRawContent(pageModel.getContentAsString());

        CDR3Document doc = builder.build();
        this.id = doc.getId();
        this.doc = doc;
    }

}
