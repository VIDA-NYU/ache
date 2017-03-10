package focusedCrawler.memex.cdr;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;

import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="AcheToCdrFileExporter", description="Exports crawled data to CDR format")
public class AcheToCdrFileExporter extends CliTool {
    
    @Option(name = "--input-path", description="Path to ACHE data target folder", required=true)
    private String inputPath;
    
    @Option(name="--output-file", description="Gziped output file containing data formmated as per CDR schema", required=true)
    private String outputFile;
    
    @Option(name="--hashed-filename", description="Whether ACHE repository files names are hashed")
    private boolean hashFilename = false;
    
    @Option(name="--compressed-data", description="Whether ACHE repository files is compressed")
    private boolean compressData = false;
    
    @Option(name="--cdr-version", description="Which CDR version should be used")
    private CDRVersion cdrVersion = CDRVersion.CDRv2;
    
    public enum CDRVersion {
        CDRv2, CDRv3
    }

    @Option(name={"-r", "--repository-type"}, description="Which repository type should be used")
    private RepositoryType repositoryType;
    
    enum RepositoryType { 
        FILES, FILESYSTEM_JSON;
    }
    
    //
    // Runtime variables
    //
    int processedPages = 0;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new AcheToCdrFileExporter());
    }
    
    @Override
    public void execute() throws Exception {
        
        System.out.println("Reading ACHE data from: "+inputPath);
        System.out.println("Generating CDR file at: "+outputFile);
        System.out.println(" Compressed repository: "+compressData);
        System.out.println("      Hashed file name: "+hashFilename);
        
        GZIPOutputStream gzipStream = new GZIPOutputStream(new FileOutputStream(outputFile));
        PrintWriter out = new PrintWriter(gzipStream, true);

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
            processRecord(out, pageModel);
        }
        
        //it.close();
        out.close();
        System.out.println("done.");
    }

    private void processRecord(PrintWriter out, TargetModelJson pageModel) {
        String contentType = pageModel.getContentType();

        if (contentType == null || contentType.isEmpty()) {
            System.err.println("Ignoring URL with no content-type: "+pageModel.getUrl());
            return;
        }

        if (!contentType.startsWith("text/")) {
            return;
        }

        String json;
        if(cdrVersion == CDRVersion.CDRv2) {
            json = createCDR2DocumentJson(pageModel);
        } else {
            json = createCDR3DocumentJson(pageModel);
        }
        
        if(json != null) {
            out.println(json);
        }
        
        processedPages++;
        if(processedPages % 100 == 0) {
            System.out.printf("Processed %d pages\n", processedPages);
        }
    }

    public static String createCDR2DocumentJson(TargetModelJson pageModel) {
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

        try {
            return builder.buildAsJson();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String createCDR3DocumentJson(TargetModelJson pageModel) {
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

        try {
            return builder.buildAsJson();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
