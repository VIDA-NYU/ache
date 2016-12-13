package focusedCrawler.dedup;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;
import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;

public class ComputeDuplications {
    
    static final ObjectMapper jsonMapper = new ObjectMapper();
    
    public static void main(String[] args) throws Exception {

        Path outputPath = Paths.get("/data/memex/crawleval/surface/data_target/");
        
        FileSystemTargetRepository repository = new FileSystemTargetRepository(outputPath, DataFormat.JSON, true, false);
        
        Map<String, Set<String>> contentHashes = computeDups(repository);
        
        printDups(contentHashes, "data/" + outputPath.getFileName() + "_dups.txt");
    }
    

    private static void printDups(Map<String, Set<String>> contentHashes, String filename)
            throws Exception {

        PrintStream fileWriter = new PrintStream(filename);
        int pages = 0;
        int duplicates = 0;
        for (Entry<String, Set<String>> entry : contentHashes.entrySet()) {
            pages += entry.getValue().size();
            if (entry.getValue().size() > 1) {
                duplicates += entry.getValue().size() - 1; // one of them is the canonical
            }
            fileWriter.print(entry.getKey());
            for (String url : entry.getValue()) {
                fileWriter.print(' ');
                fileWriter.print(url);
            }
            fileWriter.print('\n');
        }

        System.out.println("    pages: " + pages);
        System.out.println("     dups: " + duplicates);
        System.out.println("dup_ratio: " + String.format("%.2f", 100 * (duplicates / ((double) pages))) + "%");

        fileWriter.close();
    }


    private static Map<String, Set<String>> computeDups(FileSystemTargetRepository repository) throws Exception {
        
        Map<String, Set<String>> contentHashMap = new HashMap<>();
        Iterator<TargetModelJson> iterator = repository.iterator();
        
        while(iterator.hasNext()) {
            TargetModelJson page = iterator.next();
            
            List<String> contentTypeHeader = page.getResponseHeaders().get("Content-Type");
            if(contentTypeHeader == null) {
                contentTypeHeader = page.getResponseHeaders().get("content-type");
            }
            
            if(contentTypeHeader == null || contentTypeHeader.size() == 0) {
                continue;
            }
            
            String contentType = contentTypeHeader.iterator().next();
            if(!contentType.contains("text/html")) {
                continue;
            }
            
            HashMap<String, Object> crawlData = new HashMap<>();
            crawlData.put("response_headers", page.getResponseHeaders());
            
            String text = KeepEverythingExtractor.INSTANCE.getText(page.getContentAsString());
            String contentHash = DigestUtils.md5Hex(text);
            
            Set<String> dups = contentHashMap.get(contentHash); 
            if(dups == null) {
                dups = new HashSet<>();
                contentHashMap.put(contentHash, dups);
            }
            
            dups.add(page.getUrl());
        }
        return contentHashMap;
    }

}
