package focusedCrawler.seedfinder;

import java.io.IOException;
import java.util.List;

import focusedCrawler.util.parser.BackLinkNeighborhood;

public interface SearchEngineApi {
    
    public List<BackLinkNeighborhood> submitQuery(String query, int page) throws IOException;

}