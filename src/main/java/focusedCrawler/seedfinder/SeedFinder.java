package focusedCrawler.seedfinder;

import static java.util.Arrays.asList;

import java.io.PrintStream;
import java.util.List;

import focusedCrawler.seedfinder.QueryProcessor.QueryResult;
import focusedCrawler.target.classifier.BodyRegexTargetClassifier;
import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.model.Page;

public class SeedFinder {
    
    public static void main(String[] args) throws Exception {

        int iterations = 10;
        double minimumPrecision = 0.5;
        int numberOfQuerys = 0;

        Query query = new Query("deep", "web", "onion", "links");
        List<String> patterns = asList(".*[a-zA-Z0-9]{8,32}.onion.*");
        
        TargetClassifier classifier = new BodyRegexTargetClassifier(patterns);

        QueryBuilder queryBuilder = new QueryBuilder(minimumPrecision);
        QueryProcessor queryProcessor =
                new QueryProcessor(iterations, minimumPrecision, classifier);

        PrintStream seedsFile = new PrintStream("seeds_" + query.asString() + ".txt");

        while (numberOfQuerys < 15) {
            QueryResult result = queryProcessor.processQuery(query);

            for (Page page : result.positivePages) {
                seedsFile.println(page.getURL().toExternalForm());
            }

            query = queryBuilder.buildNextQuery(query, result);
            System.out.println("NextQuery: " + query.asString());
            numberOfQuerys++;
        }
        queryProcessor.close();
        seedsFile.close();
    }

}

