package achecrawler.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.memex.cdr.CDR2Document;
import achecrawler.target.model.TargetModelJson;

public class CrawlerEvalFile {
    
    static final ObjectMapper jsonMapper = new ObjectMapper();
    
    public static void main(String[] args) throws IOException {
        
        Path path = Paths.get("/data/memex/crawleval/onion");
        DirectoryStream<Path> basePathStream = Files.newDirectoryStream(path);
        for (Path outputPath : basePathStream) {
            Path dataPath = outputPath.resolve("data_target");
            for(Path site : Files.newDirectoryStream(dataPath)) {
                System.out.println(outputPath.getFileName() + " - "+ site.getFileName());
                indexFolder(site, site.getFileName()+"_NYU.json");
            }
        }

    }
    

    private static void indexFolder(Path inputPath, String filename) throws IOException {
        PrintStream fileWriter = new PrintStream(filename);
        
        DirectoryStream<Path> fileStream = Files.newDirectoryStream(inputPath);
        for (Path filePath : fileStream) {
            
            final byte[] bytes = Files.readAllBytes(filePath);
            
            TargetModelJson pageModel = jsonMapper.readValue(bytes, TargetModelJson.class);
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
            
            String contentType = contentTypeHeader.iterator().next();
//            if(!contentType.contains("text/html")) {
//                System.out.println("Discarding "+ pageModel.getUrl()+ " due to content-type: "+contentType);
//                continue;
//            }
            
            
            HashMap<String, Object> crawlData = new HashMap<>();
            crawlData.put("response_headers", pageModel.getResponseHeaders());
            
            String doc = new CDR2Document.Builder()
                    .setUrl(pageModel.getUrl())
                    .setTimestamp(pageModel.getFetchTime())
                    .setContentType(contentType)
                    .setTeam("NYU")
                    .setCrawler("ACHE")
                    .setRawContent(pageModel.getContentAsString())
                    .setCrawlData(crawlData)
                    .buildAsJson();
            
            
            fileWriter.println(doc);
            
        }
        fileWriter.close();
    }

}
