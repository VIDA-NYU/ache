package focusedCrawler.memex.cdr;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.core.JsonProcessingException;

import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FileSystemTargetRepository.FileContentIterator;
import focusedCrawler.util.CliTool;

public class AcheToCdrFileExporter extends CliTool {
    
    @Option(name="--input-path", usage="Path to ACHE data target folder", required=true)
    private String inputPath;
    
    @Option(name="--output-file", usage="Gziped output file containing data formmated as per CDR 2.0 schema", required=true)
    private String outputFile;
    
    @Option(name="--hashed-filename", usage="Wheter ACHE repository files names are hashed")
    private boolean hashFilename = false;
    
    @Option(name="--compressed-data", usage="Wheter ACHE repository files is compressed")
    private boolean compressData = false;
    
    private DataFormat dataFormat = DataFormat.JSON;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new AcheToCdrFileExporter());
    }
    
    @Override
    public void execute() throws Exception {
        
        System.out.println("Reading ACHE data from: "+inputPath);
        System.out.println("Generating CDR file at: "+outputFile);
        System.out.println(" Compressed repository: "+compressData);
        System.out.println("      Hashed file name: "+hashFilename);
        
        FileSystemTargetRepository repository =
                new FileSystemTargetRepository(inputPath, dataFormat, hashFilename, compressData);

        PrintWriter out =
                new PrintWriter(new GZIPOutputStream(new FileOutputStream(outputFile)), true);

        int processedPages = 0;
        FileContentIterator<TargetModelJson> it = repository.iterator();
        while (it.hasNext()) {
            
            TargetModelJson pageModel = it.next();
            
            String contentType = pageModel.getContentType();

            if (contentType == null || contentType.isEmpty()) {
                System.err.println("Ignoring URL with no content-type: "+pageModel.getUrl());
                continue;
            }

            if (!contentType.startsWith("text/")) {
                // TODO: Deal with media documents, create parent/child 
                continue;
            }

            String json = createCDRDocumentJson(pageModel);
            
            if(json != null) {
                out.println(json);
            }
            processedPages++;
            if(processedPages % 100 == 0) {
                System.out.printf("Processed %d pages\n", processedPages);
            }
        }
        it.close();
        out.close();
        System.out.println("done.");
    }

    public static String createCDRDocumentJson(TargetModelJson pageModel) {
        HashMap<String, Object> crawlData = new HashMap<>();
        crawlData.put("response_headers", pageModel.getResponseHeaders());
        
        CDRDocument.Builder builder = new CDRDocument.Builder()
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

}
