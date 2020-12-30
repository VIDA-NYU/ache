package achecrawler.memex.cdr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="CountCdrTlds", description="Counts the number of unique TLDs in a CDR file")
public class CountTlds extends CliTool {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Option(name="--input-path", required=true,
            description="Path to folder with multiple CDR files")
    private String inputPath;
    
    @Option(name="--output-file", required=true,
            description="Text file with TLD counts")
    private String outputFile;
    
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new CountTlds());
    }
    
    @Override
    public void execute() throws Exception {
        
        System.out.println("Reading CDR files from: "+inputPath);
        System.out.println("Generating CDR file at: "+outputFile);
        
        File inputFile = new File(inputPath);
        List<File> files;
        if(inputFile.isDirectory()) {
            files = Arrays.asList(inputFile.listFiles());
        } else {
            files = Arrays.asList(inputFile);
        }

        int processedPages = 0;
        Map<String,Integer> tldCounts = new HashMap<String, Integer>();
        for(File file : files) {
            System.out.printf("Processing file: %s\n", file.getCanonicalPath());
            try(BufferedReader in = openGzipFile(file)) {
                String line;
                while((line = in.readLine()) != null) {
                    
                    CDR2Document doc = mapper.readValue(line, CDR2Document.class);
                    
                    LinkRelevance link = new LinkRelevance(doc.getUrl(), 0);
                    String tld = link.getTopLevelDomainName();
                    
                    Integer tldCount = tldCounts.get(tld);
                    if(tldCount == null) {
                        tldCount = new Integer(0);
                    }
                    
                    tldCount++;
                    
                    tldCounts.put(tld, tldCount);
                    
                    processedPages++;
                    if(processedPages % 1000 == 0) {
                        System.out.printf("Counted %s pages...\n", processedPages);
                    }
                }
            }
        }
        
        try(PrintWriter out = new PrintWriter(new FileOutputStream(outputFile), true)) {
            for (Entry<String, Integer> count : tldCounts.entrySet()) {
                out.printf("%s, %d\n", count.getKey(), count.getValue());
            }
        }
        
        System.out.printf("Finished processing %d pages.\n", processedPages);
    }
    
    private BufferedReader openGzipFile(File file) throws IOException, FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file), 512*4096)));
    }

}
