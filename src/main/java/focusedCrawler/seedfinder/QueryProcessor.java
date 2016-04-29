package focusedCrawler.seedfinder;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import focusedCrawler.crawler.async.HttpDownloader;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifier.TargetRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

public class QueryProcessor {
    
    int maxNumberOfIterations = 10;
    double minimumPrecision = 0.25;
    TargetClassifier classifier;
    HttpDownloader downloader = new HttpDownloader();
    
    private BingSearch searchEngine = new BingSearch();
//    private GoogleSearch searchEngine = new GoogleSearch();
    
    Set<String> usedUrls = new HashSet<>();
    
    public QueryProcessor(int maxNumberOfIterations,
                          double minimumPrecision,
                          TargetClassifier classifier) {
        this.maxNumberOfIterations = maxNumberOfIterations;
        this.classifier = classifier;
    }
    
    public QueryResult processQuery(Query query) throws Exception {
        
        
        
        QueryResult searchResult = new QueryResult(0d);
        for (int i = 0; i < maxNumberOfIterations; i++) {
            System.out.println("Search page "+i);
            
            QueryResult result = processSingleQuery(query, i);
            
            searchResult.positivePages.addAll(result.positivePages);
            searchResult.negativePages.addAll(result.negativePages);
            
            if(result.precision() < minimumPrecision || result.getTotalNumOfDocs() == 0) {
                break;
            }
        }
        return searchResult;
    }
    
    
    public QueryResult processSingleQuery(Query query, int searchPage) throws Exception {
        
        BackLinkNeighborhood[] searchResults = searchEngine.submitQuery(query.asString(), searchPage);
        List<BackLinkNeighborhood> newSearchResults = filterUsedUrls(searchResults);
        List<FetchedResult> fetchedPages = fetchPages(newSearchResults);
        
        if(fetchedPages == null || fetchedPages.size() == 0) {
            return new QueryResult(0d);
        }
        
        
        QueryResult result = new QueryResult();
        if(searchResults.length != 0)
            result.percentNewResults = newSearchResults.size() / searchResults.length;
        else
            result.percentNewResults = 0;
        
        for(FetchedResult fetchedResult : fetchedPages) {
            
            if(fetchedResult == null) {
                continue;
            }
            
            URL url = new URL(fetchedResult.getBaseUrl());
            String contentAsString = new String(fetchedResult.getContent());
            
            Page page = new Page(url, contentAsString);
            page.setPageURL(new PaginaURL(page.getURL(), page.getContent()));
            
            TargetRelevance relevance = classifier.classify(page);
            if(relevance.isRelevant()) {
                result.positivePages.add(page);
            } else {
                result.negativePages.add(page);
            }
            System.out.println(relevance.isRelevant() + " -> " + url);
        }
        
        return result;
    }

    private List<FetchedResult> fetchPages(List<BackLinkNeighborhood> newSearchResults)
                                           throws InterruptedException,
                                                  ExecutionException {
        if(newSearchResults.size() == 0) {
            return new ArrayList<FetchedResult>();
        }
        
        List<Future<FetchedResult>> futures = new ArrayList<Future<FetchedResult>>();
        for(BackLinkNeighborhood result : newSearchResults) {
            futures.add(downloader.dipatchDownload(result.getLink()));
        }
        
        List<FetchedResult> fetchedPages = new ArrayList<FetchedResult>();
        for (Future<FetchedResult> future : futures) {
            fetchedPages.add(future.get());
        }
        
        return fetchedPages;
    }
    
    private List<BackLinkNeighborhood> filterUsedUrls(BackLinkNeighborhood[] searchResults) {
        List<BackLinkNeighborhood> filteredResult = new ArrayList<BackLinkNeighborhood>();
        for (BackLinkNeighborhood link : searchResults) {
            if(!usedUrls.contains(link.getLink())) {
                filteredResult.add(link);
                usedUrls.add(link.getLink());
            }
        }
        return filteredResult;
    }

    public void close() {
        downloader.close();
    }
    
    public static class QueryResult {
        
        List<Page> positivePages = new ArrayList<>();
        List<Page> negativePages = new ArrayList<>();
        double percentNewResults;
        
        public QueryResult() {
        }
        
        public QueryResult(double percentNewResults) {
            this.percentNewResults = percentNewResults;
        }

        int getTotalNumOfDocs() {
            return positivePages.size() + negativePages.size();
        }
        
        double precision() {
            if(getTotalNumOfDocs()==0)
                return 0d;
            else
                return positivePages.size() / (double) getTotalNumOfDocs();
        }
        
    }

}