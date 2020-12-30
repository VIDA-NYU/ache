package achecrawler.memex.cdr;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Sample {
    
    @SuppressWarnings("unused")
    public static void main(String[] args) throws JsonProcessingException {
        
        CDR2Document.Builder builder = new CDR2Document.Builder();
        
        builder.setUrl("http://www.darpa.mil/program/memex")
               .setRawContent("<html><head><title>Sample title</title></head><body>Original text</body></html>")
//               .setContentType("text/html")
               .setCrawler("memex-crawler")
               .setTeam("DARPA")
               .setVersion("2.0")
               .setTimestamp(new Date().getTime());
        
        // A object to acccess CDR document fields
        CDR2Document doc = builder.build();

        // A object already serialized in JSON format       
        String json = builder.buildAsJson();
        
        System.out.println(json);
        
    }
    
}
