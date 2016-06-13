package focusedCrawler.seedfinder;

import java.io.IOException;

import focusedCrawler.util.parser.BackLinkNeighborhood;

public interface SearchEngineApi {
    
    public BackLinkNeighborhood[] submitQuery(String query, int page) throws IOException;

}