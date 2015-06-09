package focusedCrawler;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.functions.SMO;
import focusedCrawler.crawler.CrawlerManager;
import focusedCrawler.crawler.CrawlerManagerException;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.AddSeeds;
import focusedCrawler.target.CreateWekaInput;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageFactoryException;

/**
 * <p>
 * Description: This is the main entry point for working with the components of
 * the focusedCrawler
 * </p>
 */

public class Main {
    
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private static Options[] allOptions;
    private static String[] commandName;

    public static void main(String... args) {
        try {
            CommandLineParser parser = new DefaultParser();
            
            Options startCrawlOptions = new Options();
            Options addSeedsOptions = new Options();
            Options startCrawlManagerOptions = new Options();
            Options buildModelOptions = new Options();
            Options startTargetStorageOptions = new Options();
            Options startLinkStorageOptions = new Options();
            
            startCrawlOptions.addOption("o", "outputDir", true, "data output path");
            startCrawlOptions.addOption("c", "configDir", true, "config directory path");
            startCrawlOptions.addOption("s", "seed", true, "path to the seed file");
            startCrawlOptions.addOption("m", "modelDir", true, "model directory path");
            startCrawlOptions.addOption("l", "langDetect", true, "path to language detection profile");
            
            addSeedsOptions.addOption("o", "outputDir", true, "data output path");
            addSeedsOptions.addOption("c", "configDir", true, "config directory path");
            addSeedsOptions.addOption("s", "seed", true, "path to the seed file");
            
            buildModelOptions.addOption("c", "targetStorageConfig", true, "config file path");
            buildModelOptions.addOption("t", "trainingDataDir", true, "training data path");
            buildModelOptions.addOption("o", "outputDir", true, "data output path");
            
            startTargetStorageOptions.addOption("o", "outputDir", true, "data output path");
            startTargetStorageOptions.addOption("c", "configDir", true, "config directory path");
            startTargetStorageOptions.addOption("m", "modelDir", true, "model directory path");
            startTargetStorageOptions.addOption("l", "langDetect", true, "path to language detection profile");
            
            startLinkStorageOptions.addOption("o", "outputDir", true, "data output path");
            startLinkStorageOptions.addOption("c", "configDir", true, "config directory path");
            startLinkStorageOptions.addOption("s", "seed", true, "path to the seed file");
            
            startCrawlManagerOptions.addOption("c", "configDir", true, "config directory path");
            
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
                startCrawlManager(getOptionValue(cmd, "configDir"));
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
        String stopwordsFile = getOptionValue(cmd, "targetStorageConfig");
        String trainingPath = getOptionValue(cmd, "trainingDataDir");
        String outputPath = getOptionValue(cmd, "outputDir"); 
        // generate the input for weka
        new File(outputPath).mkdirs();
        CreateWekaInput.main(new String[] { stopwordsFile, trainingPath, trainingPath + "/weka.arff" });
        // generate the model
        SMO.main(new String[] { "-M", "-d", outputPath + "/pageclassifier.model", "-t", trainingPath + "/weka.arff" });
    }

    private static void addSeeds(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getOptionValue(cmd, "outputDir");
        String configPath = getOptionValue(cmd, "configDir");
        String seedPath = getOptionValue(cmd, "seed");
        createOutputPathStructure(dataOutputPath);
        AddSeeds.main(new String[] { configPath, seedPath, dataOutputPath });
    }

    private static void startLinkStorage(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getOptionValue(cmd, "outputDir");
        String configPath = getOptionValue(cmd, "configDir");
        String seedPath = getOptionValue(cmd, "seed");
        try {
            LinkStorage.main(new String[] { configPath, seedPath, dataOutputPath });
        } catch (Throwable t) {
            logger.error("Something bad happened to LinkStorage :(", t);
        }
    }

    private static void startTargetStorage(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getOptionValue(cmd, "outputDir");
        String configPath = getOptionValue(cmd, "configDir");
        String modelPath = getOptionValue(cmd, "modelDir");
        String langDetectPath = getOptionValue(cmd, "langDetect");
        try {
            TargetStorage.main(new String[]{configPath, modelPath, dataOutputPath, langDetectPath});
        } catch (Throwable t) {
            logger.error("Something bad happened to TargetStorage :(", t);
        }
    }

    private static void startCrawlManager(final String configPath) {
        try {
            CrawlerManager.main(new String[] { configPath });
        } catch (Throwable t) {
            logger.error("Something bad happened to CrawlManager :(", t);
        }
    }

    private static void startCrawl(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getOptionValue(cmd, "outputDir");
        String configPath = getOptionValue(cmd, "configDir");
        String seedPath = getOptionValue(cmd, "seed");
        String modelPath = getOptionValue(cmd, "modelDir");
        String langDetectProfilePath = getOptionValue(cmd, "langDetect");
        
        // set up the data directories
        createOutputPathStructure(dataOutputPath);

        // add seeds
        AddSeeds.main(new String[] { configPath, seedPath, dataOutputPath });

        ParameterFile linkStorageConfig = new ParameterFile(configPath
                + "/link_storage/link_storage.cfg");

        try {
            Storage linkStorage = LinkStorage.createLinkStorage(configPath, seedPath,
                    dataOutputPath, linkStorageConfig);

            // start target storage
            String targetConfFile = configPath + "/target_storage/target_storage.cfg";
            ParameterFile targetStorageConfig = new ParameterFile(targetConfFile);

            Storage targetStorage = TargetStorage.createTargetStorage(configPath, modelPath,
                    dataOutputPath, targetStorageConfig, linkStorage);

            String crawlerConfigFile = configPath + "/crawler/crawler.cfg";

            // start crawl manager
            CrawlerManager manager = CrawlerManager.createCrawlerManager(crawlerConfigFile,
                    linkStorage, targetStorage);
            manager.start();

        } catch (StorageFactoryException e) {
            logger.error("Problem while creating Storage", e);
        } catch (CrawlerManagerException e) {
            logger.error("Problem while creating CrawlerManager", e);
        } catch (Exception e) {
            logger.error("Problem while starting crawler.", e);
        }

    }

    private static String getOptionValue(CommandLine cmd, final String optionName)
            throws MissingArgumentException {
        String optionValue = cmd.getOptionValue(optionName);
        if (optionValue == null) {
            throw new MissingArgumentException("Parameter "+optionName+" can not be empty.");
        }
        return optionValue;
    }

    private static void createOutputPathStructure(String dataOutputPath) {
        File dataOutput = new File(dataOutputPath);
        if (dataOutput.exists()) {
            logger.warn("Data output path already exists, resuming crawl...");
        } else {
            dataOutput.mkdirs();
        }

        new File(dataOutput, "data_monitor").mkdirs();
        new File(dataOutput, "data_target").mkdirs();
        new File(dataOutput, "data_negative").mkdirs();
        new File(dataOutput, "data_url").mkdirs();
        new File(dataOutput, "data_url/dir").mkdirs();
        new File(dataOutput, "data_host/").mkdirs();
        new File(dataOutput, "data_backlinks/").mkdirs();
        new File(dataOutput, "data_backlinks/dir").mkdirs();
        new File(dataOutput, "data_backlinks/hubHash").mkdirs();
        new File(dataOutput, "data_backlinks/authorityHash").mkdirs();
        new File(dataOutput, "data_backlinks/url").mkdirs();
        new File(dataOutput, "data_backlinks/auth_id").mkdirs();
        new File(dataOutput, "data_backlinks/auth_graph").mkdirs();
        new File(dataOutput, "data_backlinks/hub_id").mkdirs();
        new File(dataOutput, "data_backlinks/hub_graph").mkdirs();
    }

    private static void printUsage() {

        HelpFormatter formatter = new HelpFormatter();
        for (int i = 0; i < allOptions.length; i++) {
            formatter.printHelp(commandName[i], allOptions[i], true);
            System.out.println();
        }

        // TODO package the profiles with gradle build or mash them into the
        // resources
        // lang detect profile can be downloaded from
        // https://code.google.com/p/language-detection/wiki/Downloads
        // TODO: Model path in startTargetStorage?

        System.out.println("Examples:\n");
        System.out.println("ache buildModel -c config/sample_config/target_storage.cfg -t training_data -o output_model");
        System.out.println("ache addSeeds -o data -c config/sample_config -s config/sample.seeds");
        System.out.println("ache startLinkStorage -o data -c config/sample_config -s config/sample.seeds");
        System.out.println("ache startTargetStorage -o data -c config/sample_config -m config/sample_config -l libs/profiles");
        System.out.println("ache startCrawlManager -c config/sample_config");
    }
}
