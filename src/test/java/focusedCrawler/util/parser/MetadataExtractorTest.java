package focusedCrawler.util.parser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

public class MetadataExtractorTest {

    @Test
    public void extractorShouldWork() throws IOException {
        String path = MetadataExtractorTest.class.getResource("MetadataExtractor").getPath();
        String source = readFile(path + File.separator + "test_file", StandardCharsets.UTF_8);
        Map<String, String> extractedMetadata = MetadataExtractor.extractMetadata(source);
        HashMap<String, String> results = new HashMap<String, String>();
        getOriginalresults(results);
        assertEquals("Metadata Extractor not working properly !!", extractedMetadata, results);

    }

    public void getOriginalresults(HashMap<String, String> results) {

        results.put(
                "og:image",
                "https://scontent.fash1-1.fna.fbcdn.net/hprofile-xfp1/v/t1.0-1/p200x200/560870_427913377245296_792779965_n.jpg?oh=f15cbea0fecf9d76a9fcc99ef0f03506&oe=561C831A");
        results.put("X-Parsed-By", "org.apache.tika.parser.DefaultParser");
        results.put("og:type", "university");
        results.put("og:site_name", "Facebook");
        results.put("og:title", "Drexel University");
        results.put("refresh", "0; URL=/drexeluniv?_fb_noscript=1");
        results.put(
                "description",
                "Drexel University, Philadelphia, Pennsylvania. 59,834 likes · 3,206 talking about this. Drexel is an academically comprehensive and globally engaged...");
        results.put("title",
                "Drexel University - Philadelphia, Pennsylvania - College & University | Facebook");
        results.put(
                "og:description",
                "Drexel is an academically comprehensive and globally engaged urban research university, known for...");
        results.put("X-Frame-Options", "DENY");
        results.put("referrer", "default");
        results.put("dc:title",
                "Drexel University - Philadelphia, Pennsylvania - College & University | Facebook");
        results.put("Content-Encoding", "UTF-8");
        results.put("robots", "noodp,noydir");
        results.put("og:url", "https://www.facebook.com/drexeluniv");
        results.put("Content-Type", "text/html; charset=UTF-8");

    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
