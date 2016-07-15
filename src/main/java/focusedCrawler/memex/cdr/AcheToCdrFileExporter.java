package focusedCrawler.memex.cdr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import com.fasterxml.jackson.core.JsonProcessingException;

import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FileSystemTargetRepository.FileContentIterator;

public class AcheToCdrFileExporter {
    
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
        new AcheToCdrFileExporter().run(args);
    }
    
    public void run(String[] args) throws Exception {
        ParserProperties properties = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(this, properties);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }
        generateCdrFile();
    }

    private void generateCdrFile()
            throws IOException, FileNotFoundException, JsonProcessingException {
        
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

            HashMap<String, Object> crawlData = new HashMap<>();
            crawlData.put("response_headers", pageModel.getResponseHeaders());

            CDRDocumentBuilder builder = new CDRDocumentBuilder()
                    .withUrl(pageModel.getUrl())
                    .withTimestamp(pageModel.getFetchTime())
                    .withContentType(contentType)
                    .withVersion("2.0")
                    .withTeam("NYU")
                    .withCrawler("ACHE")
                    .withRawContent(pageModel.getResponseBody())
                    .withCrawlData(crawlData);

            String json = builder.buildAsJson();
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

}
