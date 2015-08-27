package focusedCrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
import focusedCrawler.link.classifier.LinkClassifierFactoryException;
import focusedCrawler.link.frontier.AddSeeds;
import focusedCrawler.link.frontier.FrontierPersistentException;
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
            
            startCrawlOptions.addOption("e", "elasticIndex", true, "elastic search index name");
            startCrawlOptions.addOption("o", "outputDir", true, "data output path");
            startCrawlOptions.addOption("c", "configDir", true, "config directory path");
            startCrawlOptions.addOption("s", "seed", true, "path to the seed file");
            startCrawlOptions.addOption("m", "modelDir", true, "model directory path");
            
            addSeedsOptions.addOption("o", "outputDir", true, "data output path");
            addSeedsOptions.addOption("c", "configDir", true, "config directory path");
            addSeedsOptions.addOption("s", "seed", true, "path to the seed file");
            
            buildModelOptions.addOption("c", "stopWordsFile", true, "stopwords file path");
            buildModelOptions.addOption("t", "trainingDataDir", true, "training data path");
            buildModelOptions.addOption("o", "outputDir", true, "data output path");
            
            startTargetStorageOptions.addOption("o", "outputDir", true, "data output path");
            startTargetStorageOptions.addOption("c", "configDir", true, "config directory path");
            startTargetStorageOptions.addOption("m", "modelDir", true, "model directory path");
            
            
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
        // generate the input for weka
        new File(outputPath).mkdirs();
        CreateWekaInput.main(new String[] { stopWordsFile, trainingPath, trainingPath + "/weka.arff" });
        // generate the model
        SMO.main(new String[] { "-M", "-d", outputPath + "/pageclassifier.model", "-t", trainingPath + "/weka.arff" });
        createFeaturesFile(outputPath,trainingPath);
    }
    
    private static void createFeaturesFile(String outputPath, String trainingPath) {
        File features = new File(outputPath + File.separator + "pageclassifier.features");
        try {
            features.createNewFile();
            FileWriter featuresWriter = new FileWriter(features);
            //featuresWriter.write("");
            featuresWriter.write("CLASS_VALUES  S NS" + "\n" + "ATTRIBUTES");
            String wekkaFilePath = trainingPath + "/weka.arff";
            Scanner wekkaFileScanner = new Scanner(new File(wekkaFilePath));
            while(wekkaFileScanner.hasNext()){
                String nextLine = wekkaFileScanner.nextLine();
                String[] splittedLine = nextLine.split(" ");
                if(splittedLine.length>=3 && splittedLine[0].equals("@ATTRIBUTE") && splittedLine[2].equals("REAL"))
                    featuresWriter.write(" "+splittedLine[1]);
            }
            featuresWriter.write("\n");
            wekkaFileScanner.close();
            featuresWriter.flush();
            featuresWriter.close();
        } catch (IOException e) {
            logger.error("IO Exception while creating wekka pageclassifier.features file. ",e);
        }
    }


    private static void addSeeds(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getMandatoryOptionValue(cmd, "outputDir");
        String configPath = getMandatoryOptionValue(cmd, "configDir");
        String seedPath = getMandatoryOptionValue(cmd, "seed");
        AddSeeds.main(new String[] { configPath, seedPath, dataOutputPath });
    }

    private static void startLinkStorage(CommandLine cmd) throws MissingArgumentException {
        String dataOutputPath = getMandatoryOptionValue(cmd, "outputDir");
        String configPath = getMandatoryOptionValue(cmd, "configDir");
        String seedPath = getMandatoryOptionValue(cmd, "seed");
        try {
            LinkStorage.main(new String[] { configPath, seedPath, dataOutputPath });
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
            TargetStorage.run(configPath, modelPath, dataOutputPath, elasticIndexName);
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
        String seedPath = getMandatoryOptionValue(cmd, "seed");
        String configPath = getMandatoryOptionValue(cmd, "configDir");
        String modelPath = getMandatoryOptionValue(cmd, "modelDir");
        String dataOutputPath = getMandatoryOptionValue(cmd, "outputDir");
        String elasticIndexName = getOptionalOptionValue(cmd, "elasticIndex");
        
        // add seeds
        AddSeeds.main(new String[] { configPath, seedPath, dataOutputPath });

        Path linkStorageConf = Paths.get(configPath, "/link_storage/link_storage.cfg");
        ParameterFile linkStorageConfig = new ParameterFile(linkStorageConf.toFile());
        linkStorageConfig.putParam("CONFIG_DIR", configPath);

        try {
            Storage linkStorage = LinkStorage.createLinkStorage(configPath, seedPath,
                    dataOutputPath, linkStorageConfig);

            // start target storage
            String targetConfFile = configPath + "/target_storage/target_storage.cfg";
            ParameterFile targetStorageConfig = new ParameterFile(targetConfFile);

            Storage targetStorage = TargetStorage.createTargetStorage(configPath, modelPath,
                    dataOutputPath, elasticIndexName, targetStorageConfig, linkStorage);

            String crawlerConfigFile = configPath + "/crawler/crawler.cfg";

            // start crawl manager
            CrawlerManager manager = CrawlerManager.createCrawlerManager(crawlerConfigFile,
                    linkStorage, targetStorage);
            manager.start();

        }
        
        catch (StorageFactoryException e) {
            logger.error("Problem while creating TargetStorage", e);
        }
        catch (CrawlerManagerException e) {
            logger.error("Problem while creating CrawlerManager", e);
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
