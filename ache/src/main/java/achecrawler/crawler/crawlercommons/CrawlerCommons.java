package achecrawler.crawler.crawlercommons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CrawlerCommons {

    public static String getVersion() {
        String path = "/version.prop";
        InputStream stream = CrawlerCommons.class.getResourceAsStream(path);
        if (stream == null) {
            return "Unknown Version";
        }

        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "Unknown Version";
        }
    }
}
