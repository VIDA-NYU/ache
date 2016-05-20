package focusedCrawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import focusedCrawler.util.storage.Storage;

/**
 * <p>
 * Description: This is the main entry point for working with the components of
 * the focusedCrawler
 * </p>
 */
public class Main {
    
	public static final String VERSION = Main.class.getPackage().getImplementationVersion();
    
	public static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private static Options[] allOptions;
    private static String[] commandName;

    public static void main(String... args) {
    	printVersion();
        try {
            CommandLineParser parser = new DefaultParser();
            
            Options startCrawlOptions = new Options();
            Options addSeedsOptions = new Options();
            Options startCrawlManagerOptions = new Options();
            Options buildModelOptions = new Options();
            Options startTargetStorageOptions = new Options();
            Options startLinkStorageOptions = new Options();
            
            startCrawlOptions.addOption("e", "elasticIndex", true, "ElasticSearch index name");
            startCrawlOptions.addOption("o", "outputDir", true, "Path to a folder to store crawler data");
            startCrawlOptions.addOption("c", "configDir", true, "Path to configuration files folder");
            startCrawlOptions.addOption("s", "seed", true, "Path to the file of seed URLs");
            startCrawlOptions.addOption("m", "modelDir", true, "Path to folder containing page classifier model");
            
            addSeedsOptions.addOption("o", "outputDir", true, "Path to a folder to store crawler data");
            addSeedsOptions.addOption("c", "configDir", true, "Path to configuration files folder");
            addSeedsOptions.addOption("s", "seed", true, "Path to file of seed URLs");
            
            buildModelOptions.addOption("c", "stopWordsFile", true, "Path to stopwords file");
            buildModelOptions.addOption("t", "trainingDataDir", true, "Path to training data folder");
            buildModelOptions.addOption("o", "outputDir", true, "Path to folder which model built should be stored");
            buildModelOptions.addOption("l", "learner", true, "Machine-learning algorithm to be used to train the model (SMO, RandomForest)");
            
            startTargetStorageOptions.addOption("o", "outputDir", true, "Path to folder which model built should be stored");
            startTargetStorageOptions.addOption("c", "configDir", true, "Path to configuration files folder");
            startTargetStorageOptions.addOption("m", "modelDir", true, "Path to folder containing page classifier model");
            
            
            startLinkStorageOptions.addOption("o", "outputDir", true, "Path to a folder to store crawler data");
            startLinkStorageOptions.addOption("c", "configDir", true, "Path to configuration files folder");
            startLinkStorageOptions.addOption("s", "seed", true, "Path to the file of seed URLs");
            
            startCrawlManagerOptions.addOption("c", "configDir", true, "Path to configuration files folder");
            
            allOptions = new Options[] { 
                    startCrawlOptions,
                    addSeedsOptions,
                    startCrawlManagerOptions,
                    buildModelOptions,
                    startTargetStorageOptions,
                    startLinkStorageOptions};
            
            commandName = new String[] { 
                    "startCrawl",
                    "addSeeds",
                    "startCrawlManager",
                    "buildModel",
                    "startTargetStorage",
                    "startLinkStorage" };

            if (args.length == 0) {
                printUsage();
                System.exit(1);
            }

            CommandLine cmd;
            if ("startCrawl".equals(args[0])) {
                cmd = parser.parse(startCrawlOptions, args);
                startCrawl(cmd);
            } 
            else if ("addSeeds".equals(args[0])) {
                cmd = parser.parse(addSeedsOptions, args);
                addSeeds(cmd);
            }
            else if ("buildModel".equals(args[0])) {
                cmd = parser.parse(buildModelOptions, args);
                buildModel(cmd);
            }
            else if ("startLinkStorage".equals(args[0])) {
                cmd = parser.parse(startLinkStorageOptions, args);
                startLinkStorage(cmd);
            }
            else if ("startTargetStorage".equals(args[0])) {
                cmd = parser.parse(startTargetStorageOptions, args);
                startTargetStorage(cmd);
            }
            else if ("startCrawlManager".equals(args[0])) {
                cmd = parser.parse(startCrawlManagerOptions, args);
                startCrawlManager(getMandatoryOptionValue(cmd, "configDir"));
            } else {
                printUsage();
                System.exit(1);
            }
        }
        catch(MissingArgumentException e) {
            printMissingArgumentMessage(e);
            System.exit(1);
        }
        catch(ParseException e) {
            printError(e);
            System.exit(1);
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

    private static void printError(ParseException e) {
        System.out.println(e);
        System.out.println("Unable to parse the input. Did you enter the parameters correctly?\n");
        printUsage();
    }
    
    private static void printMissingArgumentMessage(MissingArgumentException e) {
        System.out.println("Unable to parse the input. "+e.getMessage()+"\n");
        printUsage();
    }

    private static void buildModel(CommandLine cmd) throws MissingArgumentException {
        String stopWordsFile = getMandatoryOptionValue(cmd, "stopWordsFile");
        String trainingPath = getMandatoryOptionValue(cmd, "trainingDataDir");
        String outputPath = getMandatoryOptionValue(cmd, "outputDir"); 
        String learner = getOptionalOptionValue(cmd, "learner");
        
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

    private static void addSeeds(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getMandatoryOptionValue(cmd, "outputDir");
        String configPath = getMandatoryOptionValue(cmd, "configDir");
        String seedPath = getMandatoryOptionValue(cmd, "seed");
        ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
        FrontierManager frontierManager = FrontierManagerFactory
                .create(config.getLinkStorageConfig(), configPath, dataOutputPath, seedPath, null);
        frontierManager.close();
    }

    private static void startLinkStorage(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getMandatoryOptionValue(cmd, "outputDir");
        String configPath = getMandatoryOptionValue(cmd, "configDir");
        String seedPath = getMandatoryOptionValue(cmd, "seed");
        String modelPath = getMandatoryOptionValue(cmd, "modelDir");
        try {
            ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
            LinkStorage.runServer(configPath, seedPath, dataOutputPath, modelPath, config.getLinkStorageConfig());
        } catch (Throwable t) {
            logger.error("Something bad happened to LinkStorage :(", t);
        }
    }

    private static void startTargetStorage(CommandLine cmd) throws MissingArgumentException {
        String configPath = getMandatoryOptionValue(cmd, "configDir");
        String modelPath = getMandatoryOptionValue(cmd, "modelDir");
        String dataOutputPath = getMandatoryOptionValue(cmd, "outputDir");
        String elasticIndexName = getOptionalOptionValue(cmd, "elasticIndex");
        try {
            ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
            TargetStorage.runServer(configPath, modelPath, dataOutputPath, elasticIndexName, config);
        } catch (Throwable t) {
            logger.error("Something bad happened to TargetStorage :(", t);
        }
    }

    private static void startCrawlManager(final String configPath) {
        try {
            ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
            AsyncCrawler.run(config);
            
        } catch (Throwable t) {
            logger.error("Something bad happened to CrawlManager :(", t);
        }
    }

    private static void startCrawl(CommandLine cmd) throws MissingArgumentException {
        String seedPath = getMandatoryOptionValue(cmd, "seed");
        String configPath = getMandatoryOptionValue(cmd, "configDir");
        String modelPath = getMandatoryOptionValue(cmd, "modelDir");
        String dataOutputPath = getMandatoryOptionValue(cmd, "outputDir");
        String elasticIndexName = getOptionalOptionValue(cmd, "elasticIndex");
        
        ConfigService config = new ConfigService(Paths.get(configPath, "ache.yml").toString());
        
        try {
            Storage linkStorage = LinkStorage.createLinkStorage(configPath, seedPath,
                    dataOutputPath, modelPath, config.getLinkStorageConfig());

            // start target storage
            Storage targetStorage = TargetStorage.createTargetStorage(
            		configPath, modelPath, dataOutputPath, elasticIndexName,
                    config.getTargetStorageConfig(), linkStorage);
            
            AsyncCrawlerConfig crawlerConfig = config.getCrawlerConfig();
            
            // start crawl manager
            AsyncCrawler crawler = new AsyncCrawler(targetStorage, (LinkStorage) linkStorage, crawlerConfig);
            crawler.run();

        }
        catch (LinkClassifierFactoryException | FrontierPersistentException  e) {
            logger.error("Problem while creating LinkStorage", e);
        }
        catch (IOException e) {
            logger.error("Problem while starting crawler.", e);
        }

    }

    private static String getMandatoryOptionValue(CommandLine cmd, final String optionName)
            throws MissingArgumentException {
        String optionValue = cmd.getOptionValue(optionName);
        if (optionValue == null) {
            throw new MissingArgumentException("Parameter "+optionName+" can not be empty.");
        }
        return optionValue;
    }
    
    private static String getOptionalOptionValue(CommandLine cmd, final String optionName){
        String optionValue = cmd.getOptionValue(optionName);
        return optionValue;
    }

    private static void printUsage() {

        HelpFormatter formatter = new HelpFormatter();
        for (int i = 0; i < allOptions.length; i++) {
            formatter.printHelp(commandName[i], allOptions[i], true);
            System.out.println();
        }

        System.out.println("Examples:\n");
        System.out.println("ache buildModel -c config/sample_config/stoplist.txt -t training_data -o output_model");
        System.out.println("ache addSeeds -o data -c config/sample_config -s config/sample.seeds");
        System.out.println("ache startLinkStorage -o data -c config/sample_config -s config/sample.seeds");
        System.out.println("ache startTargetStorage -o data -c config/sample_config -m config/sample_model");
        System.out.println("ache startCrawlManager -c config/sample_config");
    }
}
