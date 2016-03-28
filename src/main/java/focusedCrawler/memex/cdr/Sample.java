package focusedCrawler.memex.cdr;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Sample {
    
    @SuppressWarnings("unused")
    public static void main(String[] args) throws JsonProcessingException {
        
        CDRDocumentBuilder builder = new CDRDocumentBuilder();
        
        builder.withUrl("http://www.darpa.mil/program/memex")
               .withRawContent("<html><head><title>Sample title</title></head><body>Original text</body></html>")
//               .withContentType("text/html")
               .withCrawler("memex-crawler")
               .withTeam("DARPA")
               .withTimestamp(new Date().getTime());
        
        // A object to acccess CDR document fields
        CDRDocument doc = builder.build();

        // A object already serialized in JSON format       
        String json = builder.buildAsJson();
        
        System.out.println(json);
        
    }
    
}
