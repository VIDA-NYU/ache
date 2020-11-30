package achecrawler.link.classifier.builder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import achecrawler.util.Urls;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import achecrawler.config.Configuration;
import achecrawler.link.LinkStorageConfig.BackSurferConfig;
import achecrawler.link.backlink.BacklinkSurfer;
import achecrawler.util.parser.BackLinkNeighborhood;

public class BacklinkSurferTest {

    @Ignore
    @Test
    public void backlinksShouldBeDownloadedFromMozApi() throws IOException {
        // given
        Map<String, String> props = new HashMap<>();
        props.put("link_storage.backsurfer.moz.access_id", "mozscape-4a1d0827fc");
        props.put("link_storage.backsurfer.moz.secret_key", "d6ea0c3b253ab44425769e422624a0f");
        
        BackSurferConfig config = new Configuration(props).getLinkStorageConfig().getBackSurferConfig();
        BacklinkSurfer surfer = new BacklinkSurfer(config);
        
        URL url = new URL("http://www.bbc.co.uk/news/health-30577776");

        // when
        BackLinkNeighborhood[] backlinks = surfer.getLNBacklinks(url);
        
        // then
        assertThat(backlinks, is(notNullValue()));
        assertThat(backlinks.length>0, is(true));
        assertTrue(backLinkSetIsValid(backlinks));
    }

    @Ignore
    @Test
    public void backlinksShouldBeDownloadedFromGoogle() throws IOException {
        // given
        Map<String, String> props = new HashMap<>();
        BackSurferConfig config = new Configuration(props).getLinkStorageConfig().getBackSurferConfig();
        BacklinkSurfer surfer = new BacklinkSurfer(config);

        URL url = new URL("http://www.bbc.co.uk");

        // when
        BackLinkNeighborhood[] backlinks = surfer.getLNBacklinks(url);
        
        // then
        assertThat(backlinks, is(notNullValue()));
        assertThat(backlinks.length>0, is(true));
        assertTrue(backLinkSetIsValid(backlinks));

    }

    public boolean backLinkSetIsValid(BackLinkNeighborhood[] backlinks) {
        for (BackLinkNeighborhood backlink : backlinks) {
            if (Urls.isValid(backlink.getLink()))
                return true;
        }
        return false;
    }

}
