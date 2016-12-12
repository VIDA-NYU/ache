package focusedCrawler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.ConfigService;
import focusedCrawler.crawler.async.AsyncCrawler;
import focusedCrawler.crawler.async.AsyncCrawlerConfig;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.classifier.LinkClassifierFactoryException;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.FrontierManagerFactory;
import focusedCrawler.link.frontier.FrontierPersistentException;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.target.classifier.WekaTargetClassifierBuilder;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.storage.Storage;
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
                BuildModel.class,
                AddSeeds.class,
                StartLinkStorage.class,
                StartCrawlManager.class,
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
                    loadedClass = classLoader.loadClass("focusedCrawler.tools."+toolClass);
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

    @Command(name = "buildModel", description = "Builds a model for a Weka target classifier")
    public static class BuildModel implements Runnable {
        
        @Option(name = {"-t", "--trainingDataDir"}, required = true, description = "Path to folder containing training data")
        String trainingPath;

        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to folder which model built should be stored")
        String outputPath;

        @Option(name = {"-c", "--stopWordsFile"}, required = false, description = "Path to stopwords file")
        String stopWordsFile;

        @Option(name = {"-l", "--learner"}, required = false, description = "Machine-learning algorithm to be used to train the model (SMO, RandomForest)")
        String learner;
        
        @Override
        public void run() {
            
            new File(outputPath).mkdirs();
            
            // generate the input for weka
            System.out.println("Preparing training data...");
            WekaTargetClassifierBuilder.createInputFile(stopWordsFile, trainingPath, trainingPath + "/weka.arff" );
            
            // generate the model
            System.out.println("Training model...");
            WekaTargetClassifierBuilder.trainModel(trainingPath, outputPath, learner);
            
            // generate features file
            System.out.println("Creating feature file...");
            WekaTargetClassifierBuilder.createFeaturesFile(outputPath,trainingPath);
            
            System.out.println("done.");
        }
        
    }

    @Command(name = "addSeeds", description = "Add seeds used to bootstrap the crawler")
    public static class AddSeeds implements Runnable {
        
        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to a folder to store crawler data")
        String dataOutputPath;
        
        @Option(name = {"-c", "--configDir"}, required = true, description = "Path to configuration files folder")
        String configPath;
        
        @Option(name = {"-s", "--seed"}, required = true, description = "Path to file of seed URLs")
        String seedPath;
        
        public void run() {
            ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
            FrontierManager frontierManager = FrontierManagerFactory.create(config.getLinkStorageConfig(), configPath, dataOutputPath, seedPath, null);
            frontierManager.close();
        }
        
    }

    @Command(name = "startLinkStorage", description = "Starts a LinkStorage server")
    public static class StartLinkStorage implements Runnable {

        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to a folder to store link storage data")
        String dataOutputPath;
        
        @Option(name = {"-c", "--configDir"}, required = true, description = "Path to configuration files folder")
        String configPath;
        
        @Option(name = {"-m", "--model"}, required = true, description = "")
        String modelPath;

        @Option(name = {"-s", "--seed"}, required = false, description = "Path to the file containing seed URLs")
        String seedPath;
        
        public void run() {
            try {
                ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
                LinkStorage.runServer(configPath, seedPath, dataOutputPath, modelPath, config.getLinkStorageConfig());
            } catch (Throwable t) {
                logger.error("Something bad happened to LinkStorage :(", t);
            }
        }

    }

    @Command(name = "startTargetStorage", description = "Starts a TargetStorage server")
    public static class StartTargetStorage implements Runnable {

        @Option(name = {"-c", "--config"}, required = true, description = "Path to configuration files folder")
        String configPath;
        
        @Option(name = {"-m", "--modelDir"}, required = true, description = "Path to folder containing page classifier model")
        String modelPath;
        
        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to folder which model built should be stored")
        String dataOutputPath;
        
        @Option(name = {"-e", "--elasticIndex"}, required = true, description = "Name of elastic search index to be used")
        String elasticIndexName;

        @Override
        public void run() {
            try {
                ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
                TargetStorage.runServer(configPath, modelPath, dataOutputPath, elasticIndexName, config);
            } catch (Throwable t) {
                logger.error("Something bad happened to TargetStorage :(", t);
            }
            
        }

    }

    @Command(name = "startCrawlManager", description = "Starts a LinkStorage server")
    public static class StartCrawlManager implements Runnable {

        @Option(name = {"-c", "--config"}, required = true, description = "Path to configuration files folder")
        String configPath;

        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to a folder to store crawl manager data")
        String dataPath;

        @Override
        public void run() {
            try {
                ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
                AsyncCrawler.run(config, dataPath);
                
            } catch (Throwable t) {
                logger.error("Something bad happened to CrawlManager :(", t);
            }
        }

    }

    @Command(name = "startCrawl", description = "Starts a crawler")
    public static class StartCrawl implements Runnable {

        @Option(name = {"-c", "--config"}, required = true, description = "Path to configuration files folder")
        String configPath;
        
        @Option(name = {"-m", "--modelDir"}, required = true, description = "Path to folder containing page classifier model")
        String modelPath;
        
        @Option(name = {"-o", "--outputDir"}, required = true, description = "Path to folder which model built should be stored")
        String dataOutputPath;
        
        @Option(name = {"-s", "--seed"}, required = true, description = "Path to file of seed URLs")
        String seedPath;
        
        @Option(name = {"-e", "--elasticIndex"}, required = false, description = "Name of elastic search index to be used")
        String elasticIndexName;

        @Override
        public void run() {
            
            ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
            
            try {
                MetricsManager metricsManager = new MetricsManager();
                
                Storage linkStorage = LinkStorage.createLinkStorage(configPath, seedPath,
                        dataOutputPath, modelPath, config.getLinkStorageConfig(), metricsManager);

                // start target storage
                Storage targetStorage = TargetStorage.createTargetStorage(
                            configPath, modelPath, dataOutputPath, elasticIndexName,
                            config.getTargetStorageConfig(), linkStorage);
                
                AsyncCrawlerConfig crawlerConfig = config.getCrawlerConfig();
                
                // start crawl manager
                AsyncCrawler crawler = new AsyncCrawler(targetStorage, linkStorage, crawlerConfig,
                                                        dataOutputPath, metricsManager);
                try {
                    crawler.run();
                } finally {
                    crawler.shutdown();
                    metricsManager.close();
                }

            }
            catch (LinkClassifierFactoryException | FrontierPersistentException  e) {
                logger.error("Problem while creating LinkStorage", e);
            }
            catch (IOException e) {
                logger.error("Problem while starting crawler.", e);
            }
            catch (Throwable e) {
                logger.error("Crawler execution failed.", e);
            }
        }
        
    }

}
