package achecrawler.seedfinder;

import java.io.FileOutputStream;
import java.io.PrintStream;

import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.fetcher.FetcherFactory;
import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import achecrawler.seedfinder.QueryProcessor.QueryResult;
import achecrawler.target.classifier.TargetClassifier;
import achecrawler.target.classifier.TargetClassifierFactory;
import achecrawler.target.model.Page;
import achecrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="seedFinder", description="Runs the SeedFinder tool")
public class SeedFinder extends CliTool {
    
    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    
    public enum SearchEngineType {
        GOOGLE, BING, BING_API, ALL
    }
    
    @Option(name="--maxPages", description="Maximum number of pages per query")
    private int maxPagesPerQuery = 2;

    @Option(name="--minPrecision", description="Stops query pagination after precision drops bellow this minimum precision threshold")
    private double minPrecision = 0.5;
    
    @Option(name="--maxQueries", description="Max number of generated queries")
    private int maxNumberOfQueries = 100;

    @Option(name="--initialQuery", description="The inital query to issue to the search engine", required=true)
    private String initialQuery;
    
    @Option(name="--csvPath", description="The path where to write a CSV file with stats")
    private String csvPath;

    @Option(name="--modelPath", description="The path to the page classifier model", required=true)
    private String modelPath;

    @Option(name="--seedsPath", description="The directory where the seeds generated should be saved")
    private String seedsPath = "";

    @Option(name="--searchEngine", description="The search engine to be used")
    private SearchEngineType searchEngine = SearchEngineType.ALL;
    
    public static void main(String[] args) {
        CliTool.run(args, new SeedFinder());
    }
    
    @Override
    public void execute() throws Exception {
        
        SearchEngineApi api = createSearchEngineApi(this.searchEngine);
        
        System.out.println("Search Engine: "+api.getClass().getSimpleName());
        
        TargetClassifier classifier = TargetClassifierFactory.create(modelPath);
        Query query = new Query(initialQuery);
        
        QueryGenerator queryGenerator = new QueryGenerator(minPrecision);
        QueryProcessor queryProcessor = new QueryProcessor(maxPagesPerQuery, minPrecision, classifier, api);

        String seedFileName = (seedsPath.length() == 0) ? "seeds_" + query.asString() + ".txt" : seedsPath+"/seeds_" + query.asString() + ".txt";
        PrintStream seedsFile = new PrintStream(new FileOutputStream(seedFileName), true);
        PrintStream csvFile = null;
        
        if(csvPath != null && !csvPath.isEmpty()) {
            csvFile = new PrintStream(csvPath);
        }
        
        try {
            int numberOfQueries = 0;
            while (numberOfQueries < maxNumberOfQueries) {
                
                System.out.println("\n---------------");
                System.out.println("Executing QUERY: "+query.asString());
                System.out.println("---------------\n");
                
                QueryResult result = queryProcessor.processQuery(query);
                
                if(csvFile != null) {
                    writeResultsToLog(csvFile, query, result);
                }
                
                for (Page page : result.positivePages) {
                    seedsFile.println(page.getURL().toExternalForm());
                }
                
                System.out.println("\nBuilding next query...");
                query = queryGenerator.buildNextQuery(query, result);
                numberOfQueries++;
            }
        } finally {
            queryProcessor.close();
            seedsFile.close();
            if(csvFile != null) {
                csvFile.close();
            }
        }
        
        System.out.println("\nSeeds file created at: "+seedFileName);
    }

    private void writeResultsToLog(PrintStream out, Query query, QueryResult result) {
        for(Page p : result.positivePages) {
            out.printf("%s, %s, %s\n", query.asString(), "relevant", p.getURL().toString());
        }
        for(Page p : result.negativePages) {
            out.printf("%s, %s, %s\n", query.asString(), "irrelevant", p.getURL().toString());
        }
    }

    private SearchEngineApi createSearchEngineApi(SearchEngineType searchEngine) {
        SimpleHttpFetcher fetcher = FetcherFactory.createSimpleHttpFetcher(new HttpDownloaderConfig());
        fetcher.setUserAgentString(userAgent);
        switch (searchEngine) {
        case GOOGLE:
            return new GoogleSearch(fetcher);
        case BING:
            return new BingSearch(fetcher);
        case BING_API:
            return new BingSearchAzureAPI();
        case ALL:
            return new SearchEnginePool(new BingSearch(fetcher), new GoogleSearch(fetcher));
        }
        return null;
    }

}

