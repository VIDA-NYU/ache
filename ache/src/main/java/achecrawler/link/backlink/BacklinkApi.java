package achecrawler.link.backlink;

import java.io.IOException;

import achecrawler.util.parser.BackLinkNeighborhood;

public interface BacklinkApi {

    public BackLinkNeighborhood[] downloadBacklinks(String url) throws IOException;
}
