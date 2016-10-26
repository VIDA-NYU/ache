package focusedCrawler.seedfinder;

import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import focusedCrawler.seedfinder.QueryProcessor.QueryResult;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.CliTool;

public class SeedFinder extends CliTool {
    
    enum SearchEngineType {
        GOOGLE, BING, BING_API, ALL
    }
    
    @Option(name="--maxPages", usage="Maximum number of pages per query")
    private int maxPagesPerQuery = 2;

    @Option(name="--minPrecision", usage="Stops query pagination after precision drops bellow this minimum precision threshold")
    private double minPrecision = 0.5;
    
    @Option(name="--maxQueries", usage="Max number of generated queries")
    private int maxNumberOfQueries = 100;

    @Option(name="--initialQuery", usage="The inital query to issue to the search engine", required=true)
    private String initialQuery;

    @Option(name="--modelPath", usage="The inital query to issue to the search engine", required=true)
    private String modelPath;
    
    @Option(name="--searchEngine", usage="The search engine to be used")
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

        String seedFileName = "seeds_" + query.asString() + ".txt";
        PrintStream seedsFile = new PrintStream(seedFileName);
        try {
            int numberOfQueries = 0;
            while (numberOfQueries < maxNumberOfQueries) {
                
                System.out.println("\n---------------");
                System.out.println("Executing QUERY: "+query.asString());
                System.out.println("---------------\n");
                
                QueryResult result = queryProcessor.processQuery(query);
    
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
        }
        
        System.out.println("\nSeeds file created at: "+seedFileName);
    }

    private SearchEngineApi createSearchEngineApi(SearchEngineType searchEngine) {
        switch (searchEngine) {
        case GOOGLE:
            return new GoogleSearch();
        case BING:
            return new BingSearch();
        case BING_API:
            return new BingSearchAzureAPI();
        case ALL:
            return new SearchEnginePool(new BingSearch(), new GoogleSearch());
        }
        return null;
    }

}

