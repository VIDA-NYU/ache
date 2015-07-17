package focusedCrawler.util.parser;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MetadataExtractorTest {

    @Test
    public void extractorShouldExtractHtmlMetatags() throws IOException {
    	// given
        String path = MetadataExtractorTest.class.getResource("MetadataExtractor").getPath();
        String source = readFile(path + File.separator + "test_file", StandardCharsets.UTF_8);
        
        Map<String, String> expectedMetadata = new HashMap<String, String>();
        expectedMetadata.put("X-Parsed-By", "org.apache.tika.parser.DefaultParser");
        expectedMetadata.put("og:image", "https://scontent.fash1-1.fna.fbcdn.net/hprofile-xfp1/v/t1.0-1/p200x200/560870_427913377245296_792779965_n.jpg?oh=f15cbea0fecf9d76a9fcc99ef0f03506&oe=561C831A");
        expectedMetadata.put("og:type", "university");
        expectedMetadata.put("og:site_name", "Facebook");
        expectedMetadata.put("og:title", "Drexel University");
        expectedMetadata.put("og:description", "Drexel is an academically comprehensive and globally engaged urban research university, known for...");
        expectedMetadata.put("og:url", "https://www.facebook.com/drexeluniv");
        
        expectedMetadata.put("dc:title", "Drexel University - Philadelphia, Pennsylvania - College & University | Facebook");
        
        expectedMetadata.put("title", "Drexel University - Philadelphia, Pennsylvania - College & University | Facebook");
        expectedMetadata.put("description", "Drexel University, Philadelphia, Pennsylvania. 59,834 likes · 3,206 talking about this. Drexel is an academically comprehensive and globally engaged...");
        expectedMetadata.put("refresh", "0; URL=/drexeluniv?_fb_noscript=1");
        expectedMetadata.put("referrer", "default");
        
        expectedMetadata.put("X-Frame-Options", "DENY");
        expectedMetadata.put("robots", "noodp,noydir");
        expectedMetadata.put("Content-Encoding", "UTF-8");
        expectedMetadata.put("Content-Type", "text/html; charset=UTF-8");
        
        // when
        Map<String, String> extractedMetadata = MetadataExtractor.extractMetadata(source);
        
        // then
        assertThat(extractedMetadata, is(expectedMetadata));
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
