package achecrawler.seedfinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import achecrawler.util.parser.BackLinkNeighborhood;

public class SearchEnginePool implements SearchEngineApi {
    
    private SearchEngineApi[] apis;
    private ExecutorService threadPool;

    public SearchEnginePool(SearchEngineApi... apis) {
        this.apis = apis;
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public List<BackLinkNeighborhood> submitQuery(final String query, final int page) throws IOException {
        
        List<Future<List<BackLinkNeighborhood>>> futures = new ArrayList<>();
        for (int i = 0; i < apis.length; i++) {
            final SearchEngineApi searchEngineApi = apis[i];
            futures.add(threadPool.submit(new Callable<List<BackLinkNeighborhood>>() {
                @Override
                public List<BackLinkNeighborhood> call() throws Exception {
                    return searchEngineApi.submitQuery(query, page);
                }
            }));
        }
        
        Map<String, BackLinkNeighborhood> links = new HashMap<>();
        for (Future<List<BackLinkNeighborhood>> f : futures) {
            try {
                for(BackLinkNeighborhood b: f.get()) {
                    links.put(b.getLink(), b);
                }
            } catch (InterruptedException | ExecutionException e) {
               System.err.println("Failed to get search results");
            }
        }
        
        List<BackLinkNeighborhood> result = new ArrayList<>(links.values());
        System.out.println(getClass().getSimpleName()+" hits: "+result.size());
        
        return result;
    }

}
