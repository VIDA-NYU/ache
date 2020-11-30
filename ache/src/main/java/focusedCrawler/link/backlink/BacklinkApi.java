package focusedCrawler.link.backlink;

import java.io.IOException;

import focusedCrawler.util.parser.BackLinkNeighborhood;

public interface BacklinkApi {

    public BackLinkNeighborhood[] downloadBacklinks(String url) throws IOException;
}
