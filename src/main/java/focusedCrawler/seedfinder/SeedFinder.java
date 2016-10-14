package focusedCrawler.seedfinder;

import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.ParserProperties;

import focusedCrawler.seedfinder.QueryProcessor.QueryResult;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.model.Page;

public class SeedFinder {
    
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
    
    @Option(name="--searchEngine", usage="The search engine to be used (Google, Bing, BingAzureAPI, All)")
    private String searchEngine = "All";
    
    public static void main(String[] args) {
        try {
            new SeedFinder().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void run(String[] args) throws Exception {
        ParserProperties properties = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(this, properties);
        SearchEngineApi api = null;
        try {
            parser.parseArgument(args);
            api = createSearchEngineApi(this.searchEngine);
            if (api == null) {
                String msg = "Invalid search engine: " + this.searchEngine;
                Exception cause = new IllegalArgumentException(msg);
                this.searchEngine = "All";
                throw new CmdLineException(parser, msg, cause);
            }
            
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println("ache seedFinder [options...]");
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();

            System.err.println("Example: ache seedFinder" + parser.printExample(OptionHandlerFilter.ALL));
            System.exit(1);
        }
        
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

    private SearchEngineApi createSearchEngineApi(String searchEngine) {
        switch (searchEngine.toLowerCase()) {
        case "google":
            return new GoogleSearch();
        case "bing":
            return new BingSearch();
        case "bingazureapi":
            return new BingSearchAzureAPI();
        case "all":
            return new SearchEnginePool(new BingSearch(), new GoogleSearch());
        }
        return null;
    }

}

