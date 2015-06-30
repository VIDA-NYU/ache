package focusedCrawler.util.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.LinkContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class MetadataExtractor {
    
    public static final Logger logger = LoggerFactory.getLogger(MetadataExtractor.class);

    public static Map<String,String> extractMetadata(String source) throws IOException{
        
        AutoDetectParser myAutoDetectParser = new AutoDetectParser();
        LinkContentHandler myContentHandler = new LinkContentHandler();
        Metadata inputMetadata = new Metadata();
        HashMap<String,String> metaDataMap = new HashMap<String,String>();
        try {
            myAutoDetectParser.parse(new ByteArrayInputStream(source.getBytes()), myContentHandler, inputMetadata);
            String[] propertyNames = inputMetadata.names();
            for(String propertyName:propertyNames) 
                metaDataMap.put(propertyName, inputMetadata.get(propertyName));
        } catch (IOException e) {
            logger.error("I/O Exception while extracting metadata from source",e);
        } catch (SAXException e) {
            logger.error("SAX Exception while extracting metadata from source",e);
        } catch (TikaException e) {
            logger.error("Tika Exception while extracting metadata from source",e);
        }
        
        return metaDataMap;
    }
    
    
}
