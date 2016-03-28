package focusedCrawler.memex.cdr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class TikaExtractor {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Logger logger = LoggerFactory.getLogger(CDRDocumentBuilder.class);

    private String plainText;
    private Map<String, String> metadata;

    public TikaExtractor(String content) {
        this(new ByteArrayInputStream(content.getBytes(UTF8)));
    }

    public TikaExtractor(InputStream stream) {
        BodyContentHandler handler = new BodyContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        try {
            Metadata metadata = new Metadata();
            parser.parse(stream, handler, metadata);

            Map<String, String> metadataMap = new HashMap<String, String>();
            for (String propertyName : metadata.names()) {
                metadataMap.put(propertyName, metadata.get(propertyName));
            }

            this.metadata = metadataMap;
            this.plainText = handler.toString();

        } catch (IOException | SAXException | TikaException e) {
            logger.error("Failed to extract metadata using Tika.", e);
        }
    }

    public String getPlainText() {
        return plainText;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

}
