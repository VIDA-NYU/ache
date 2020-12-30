package achecrawler.memex.cdr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;

public class TikaExtractor {
    
    public static class ParsedData {
        
        private String plainText;
        private Map<String, String> metadata;
        
        public ParsedData(String plainText, Map<String, String> metadata) {
            this.plainText = plainText;
            this.metadata = metadata;
        }
        
        public String getPlainText() {
            return plainText;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }
        
    }

    private static final Logger logger = LoggerFactory.getLogger(TikaExtractor.class);
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int MAX_CHARACTERS = 25 * 1000 * 1000;
    private static final TikaConfig CONFIG = TikaConfig.getDefaultConfig();

    private final Parser parser = new AutoDetectParser(CONFIG);
    private final Detector mimeTypeDetector = CONFIG.getDetector();
    
    public ParsedData parse(String content) {
        return parse(new ByteArrayInputStream(content.getBytes(UTF8)), null, null);
    }
    
    public ParsedData parse(InputStream stream) {
        return parse(stream, null, null);
    }

    public ParsedData parse(String content, String fileName, String contentType) {
        return parse(new ByteArrayInputStream(content.getBytes(UTF8)), fileName, contentType);
    }
    
    public ParsedData parse(InputStream stream, String fileName, String contentType) {
        BodyContentHandler handler = new BodyContentHandler(MAX_CHARACTERS);
        BoilerpipeContentHandler textHandler = new BoilerpipeContentHandler(handler, KeepEverythingExtractor.INSTANCE);
        Metadata metadata = createMetadata(fileName, contentType);
        ParseContext context = new ParseContext();
        try {
            parser.parse(stream, textHandler, metadata, context);
            
            Map<String, String> metadataMap = new HashMap<String, String>();
            for (String propertyName : metadata.names()) {
                metadataMap.put(propertyName, metadata.get(propertyName));
            }
            
            return new ParsedData(handler.toString(), metadataMap);
            
        } catch (IOException | SAXException | TikaException e) {
            logger.error("Failed to extract metadata using Tika.", e);
            return null;
        }
    }
    
    public MediaType detect(String content) {
        return detect(new ByteArrayInputStream(content.getBytes(UTF8)), null, null);
    }

    public MediaType detect(String content, String fileName, String contentType) {
        return detect(new ByteArrayInputStream(content.getBytes(UTF8)), fileName, contentType);
    }
    
    public MediaType detect(InputStream fileStream, String fileName, String contentType) {
        Metadata metadata = createMetadata(fileName, contentType);
        try {
            return mimeTypeDetector.detect(fileStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input stream", e);
        }
    }

    private Metadata createMetadata(String fileName, String contentType) {
        Metadata metadata = new Metadata();
        if(fileName != null) {
            metadata.add(Metadata.RESOURCE_NAME_KEY, fileName);
        }
        if(contentType != null) {
            metadata.add(Metadata.CONTENT_TYPE, contentType);
        }
        return metadata;
    }

}
