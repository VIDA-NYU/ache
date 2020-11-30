package achecrawler;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.config.Configuration;
import achecrawler.crawler.CrawlersManager;
import achecrawler.crawler.CrawlersManager.CrawlContext;
import achecrawler.crawler.async.AsyncCrawler;
import achecrawler.link.frontier.FrontierManager;
import achecrawler.link.frontier.FrontierManagerFactory;
import achecrawler.rest.RestServer;
import achecrawler.seedfinder.SeedFinder;
import achecrawler.target.classifier.TargetClassifierBuilder;
import achecrawler.tools.StartRestServer;
import io.airlift.airline.Arguments;
import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;
import io.airlift.airline.ParseException;

/**
 * <p>
 * Description: This is the main entry point for working with the components of ACHE
 * </p>
 */
public class Main {
    
	public static final String VERSION = Main.class.getPackage().getImplementationVersion();
    
	public static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String... args) {
    	printVersion();
    	
    	@SuppressWarnings("unchecked")
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("ache")
            .withDescription("ACHE Focused Crawler")
            .withDefaultCommand(AcheHelp.class)
            .withCommands(
                AcheHelp.class,
                StartCrawl.class,
                StartRestServer.class,
                BuildModel.class,
                AddSeeds.class,
                SeedFinder.class,
                RunCliTool.class
            );

        Cli<Runnable> acheParser = builder.build();
        try {
            acheParser.parse(args).run();
        }
        catch(ParseException e) {
            System.out.println("Unable to parse the input. "+e.getMessage()+"\n");
            Help.help(acheParser.getMetadata(), Arrays.asList());
            System.exit(1);
        } 
        catch (Exception e) {
            System.err.println("Failed to execute command.");
            e.printStackTrace(System.err);
        }
    }
    
    private static void printVersion() {
        String header = "ACHE Crawler "+VERSION;
        for (int i = 0; i < header.length(); i++) {
            System.out.print("-");
        }
        System.out.println();
        System.out.println(header);
        for (int i = 0; i < header.length(); i++) {
            System.out.print("-");
        }
        System.out.println();
        System.out.println();
    }
    
    
    public static class AcheHelp extends Help {
        
        @Override
        public void run() {
            super.run();
            if(command.isEmpty()) {
                printExamples();
            }
        }
        
        private static void printExamples() {
            System.out.println("EXAMPLES\n");
            System.out.println("    ache startCrawl -c config/sample_config -o data -s config/sample.seeds -m config/sample_model");
            System.out.println("    ache buildModel -c config/sample_config/stoplist.txt -t training_data -o output_model");
            System.out.println("    ache addSeeds -c config/sample_config -o data -s config/sample.seeds");
            System.out.println("    ache startLinkStorage -c config/sample_config -o data -s config/sample.seeds");
            System.out.println("    ache startTargetStorage -c config/sample_config -o data -m config/sample_model");
            System.out.println("    ache startCrawlManager -c config/sample_config");
        }
    }

    @Command(name = "run", description = "Run any available utilitary tool")
    public static class RunCliTool implements Runnable {
        
        @Arguments(description = "Tool to be executed followed by its parameters")
        public List<String> args;
        
        public void run() {
            if(args == null || args.size() == 0) {
                System.out.println("ERROR: Class name of command-line tool not specified.");
                System.exit(1);
            }
            
            String toolClass = args.get(0);
            Class<?> loadedClass = null;
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                try {
                    loadedClass = classLoader.loadClass("achecrawler.tools."+toolClass);
                } catch(ClassNotFoundException e) {
                    // also try full class name
                    loadedClass = classLoader.loadClass(toolClass);
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Unable to find CLI tool named "+toolClass);
                System.exit(1);
            }
            // Execute main() method of loaded class
            String[] params = args.subList(1, args.size()).toArray(new String[args.size()-1]);
            try {
                Method mainMethod = loadedClass.getMethod("main", String[].class);
                mainMethod.invoke(null, (Object) params);
            } catch (Exception e) {
                System.out.printf("Failed to run tool %s.\n\n", loadedClass.getName());
                e.printStackTrace(System.out);
                System.exit(1);
            }
        }
        
    }

    @Command(name = "buildModel", description = "Builds a model for a Smile target classifier")
    public static class BuildModel implements Runnable {
        
        @Option(name = {"-t", "--trainingDataDir"}, required = true,
                description = "Path to folder containing training data")
        String trainingDataPath;

        @Option(name = {"-o", "--outputDir"}, required = true,
                description = "Path to folder which model built should be stored")
        String outputPath;

        @Option(name = {"-c", "--stopWordsFile"}, required = false,
                description = "Path to stopwords file")
        String stopWordsFile;

        @Option(name = {"-l", "--learner"}, required = false,
                description = "Machine-learning algorithm to be used to train the model (SVM, RandomForest)")
        String learner;

        @Option(name = {"-nocv", "--no-cross-validation"}, required = false,
                description = "If should skip cross-validation (train on full data)")
        boolean skipCrossValidation = false;

        @Option(name = {"-mf", "--max-features"}, required = false,
                description = "The maximum number of features to be used")
        int maxFeatures = 5000;

        @Override
        public void run() {
            try {
                new File(outputPath).mkdirs();
                TargetClassifierBuilder builder = new TargetClassifierBuilder(stopWordsFile, true,
                        skipCrossValidation, maxFeatures);
                builder.train(learner, trainingDataPath, outputPath);
            } catch (Exception e) {
                System.out.printf("Failed to build model.\n\n");
                e.printStackTrace(System.out);
                System.exit(1);
            }
        }
        
    }

    @Command(name = "addSeeds", description = "Add seeds used to bootstrap the crawler")
    public static class AddSeeds implements Runnable {
        
        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to a folder to store crawler data")
        String dataOutputPath;
        
        @Option(name = {"-c", "--configDir"}, required = true, description = "Path to configuration files folder")
        String configPath;
        
        @Option(name = {"-m", "--model"}, required = false, description = "")
        String modelPath;
        
        @Option(name = {"-s", "--seed"}, required = true, description = "Path to file of seed URLs")
        String seedPath;
        
        public void run() {
            Configuration config = new Configuration(configPath);
            FrontierManager frontierManager =
                    FrontierManagerFactory.create(config.getLinkStorageConfig(), configPath,
                            dataOutputPath, modelPath, seedPath, null);
            frontierManager.close();
        }
        
    }

    @Command(name = "startCrawl", description = "Starts a crawler")
    public static class StartCrawl implements Runnable {

        @Option(name = {"-cid", "--crawlerId"}, required = false, description = "An unique identifier for this crawler")
        String crawlerId = "default";

        @Option(name = {"-c", "--config"}, required = true, description = "Path to configuration files folder")
        String configPath;
        
        @Option(name = {"-m", "--modelDir"}, required = false, description = "Path to folder containing page classifier model")
        String modelPath;
        
        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to folder which model built should be stored")
        String dataPath;
        
        @Option(name = {"-s", "--seed"}, required = true, description = "Path to file of seed URLs")
        String seedPath;
        
        @Option(name = {"-e", "--elasticIndex"}, required = false, description = "Name of Elasticsearch index to be used")
        String esIndexName;

        @Option(name = {"-t", "--elasticType"}, required = false, description = "Name of Elasticsearch document type to be used")
        String esTypeName;
        
        @Override
        public void run() {
            try {
                Configuration config = new Configuration(configPath);
                CrawlersManager crawlManager = new CrawlersManager(dataPath, config);

                CrawlContext crawlerContext = crawlManager.createCrawler(crawlerId, configPath,
                        seedPath, modelPath, esIndexName, esTypeName);

                RestServer restServer = RestServer.create(config.getRestConfig(), crawlManager);
                restServer.start();

                try {
                    AsyncCrawler crawler = crawlerContext.getCrawler();
                    crawler.startAsync();
                    crawler.awaitTerminated();
                } finally {
                    restServer.shutdown();
                }
            } catch (Throwable e) {
                logger.error("Crawler execution failed: " + e.getMessage() + "\n", e);
            }
        }
        
    }

}
