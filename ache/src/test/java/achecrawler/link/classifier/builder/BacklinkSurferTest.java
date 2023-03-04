package achecrawler.link.classifier.builder;

import achecrawler.util.Urls;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import achecrawler.config.Configuration;
import achecrawler.link.LinkStorageConfig.BackSurferConfig;
import achecrawler.link.backlink.BacklinkSurfer;
import achecrawler.util.parser.BackLinkNeighborhood;

import static org.assertj.core.api.Assertions.assertThat;

public class BacklinkSurferTest {

    @Disabled
    @Test
    void backlinksShouldBeDownloadedFromMozApi() throws IOException {
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
        assertThat(backlinks).isNotNull();
        assertThat(backlinks.length > 0).isTrue();
        assertThat(backLinkSetIsValid(backlinks)).isTrue();
    }

    @Disabled
    @Test
    void backlinksShouldBeDownloadedFromGoogle() throws IOException {
        // given
        Map<String, String> props = new HashMap<>();
        BackSurferConfig config = new Configuration(props).getLinkStorageConfig().getBackSurferConfig();
        BacklinkSurfer surfer = new BacklinkSurfer(config);

        URL url = new URL("http://www.bbc.co.uk");

        // when
        BackLinkNeighborhood[] backlinks = surfer.getLNBacklinks(url);
        
        // then
        assertThat(backlinks).isNotNull();
        assertThat(backlinks.length > 0).isTrue();
        assertThat(backLinkSetIsValid(backlinks)).isTrue();

    }

    public boolean backLinkSetIsValid(BackLinkNeighborhood[] backlinks) {
        for (BackLinkNeighborhood backlink : backlinks) {
            if (Urls.isValid(backlink.getLink()))
                return true;
        }
        return false;
    }

}
