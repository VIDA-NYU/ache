package focusedCrawler.link.classifier.builder;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.validator.routines.UrlValidator;
import org.junit.Test;

import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.parser.SimpleWrapper;

public class BacklinkSurferTest {

    @Test
    public void backlinksShouldBeDownloaded() throws MalformedURLException, IOException {

        // of no use since we are using JSON for parsing. pattern taken from
        // config file.
        SimpleWrapper wrapper = new SimpleWrapper(",\"uu\":\"", "\"}");

        BacklinkSurfer surfer = new BacklinkSurfer(wrapper);

        String[] testURLs = { "http://www.bbc.co.uk/news/health-30577776" };

        for (String url : testURLs) {
            BackLinkNeighborhood[] backlinks = surfer.getLNBacklinks(new URL(url));
            assertEquals("Backlink extraction not working! ", true, isBackLinkSetValid(backlinks));
        }

    }

    public boolean isBackLinkSetValid(BackLinkNeighborhood[] backlinks) {
        UrlValidator validator = new UrlValidator();
        for (BackLinkNeighborhood backlink : backlinks) {
            if (validator.isValid(backlink.getLink()))
                return true;
        }
        return false;
    }

}
