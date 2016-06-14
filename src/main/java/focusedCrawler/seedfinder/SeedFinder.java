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
    
    @Option(name="--searchEngine", usage="The search engine to be used (Google, Bing, BingAzureAPI)")
    private String searchEngine = "Google";
    
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
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println("ache seedFinder [options...]");
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();

            System.err.println("Example: ache seedFinder" + parser.printExample(OptionHandlerFilter.ALL));

            return;
        }
        
        SearchEngineApi api = null;
        if ("google".equals(searchEngine.toLowerCase())) {
            api = new GoogleSearch();
        } else if ("bing".equals(searchEngine.toLowerCase())) {
            api = new BingSearch();
        } else if ("bingazureapi".equals(searchEngine.toLowerCase())) {
            api = new BingSearchAzureAPI();
        }

        if (api == null) {
            System.err.println("Invalid search engine: " + searchEngine);
            System.exit(1);
        }
        
        System.out.println("Search Engine: "+searchEngine);
        
        TargetClassifier classifier = TargetClassifierFactory.create(modelPath);
        Query query = new Query(initialQuery);
        
        QueryGenerator queryGenerator = new QueryGenerator(minPrecision);
        QueryProcessor queryProcessor = new QueryProcessor(maxPagesPerQuery, minPrecision, classifier, api);

        PrintStream seedsFile = new PrintStream("seeds_" + query.asString() + ".txt");

        int numberOfQueries = 0;
        while (numberOfQueries < maxNumberOfQueries) {
            QueryResult result = queryProcessor.processQuery(query);

            for (Page page : result.positivePages) {
                seedsFile.println(page.getURL().toExternalForm());
            }

            query = queryGenerator.buildNextQuery(query, result);
            System.out.println("NextQuery: " + query.asString());
            numberOfQueries++;
        }
        queryProcessor.close();
        seedsFile.close();
    }

}

