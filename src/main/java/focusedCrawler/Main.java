package focusedCrawler;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
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
	private static Options[] allOptions;
	private static String[] commandName;
	public static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String... args) {

		if (args.length > 0) {

			// CLIParser myCLIParser = new CLIParser();
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd;
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
			startCrawlOptions.addOption("l", "langDetect", true,
					"path to language detection profile");
			addSeedsOptions.addOption("o", "outputDir", true, "data output path");
			addSeedsOptions.addOption("c", "configDir", true, "config directory path");
			addSeedsOptions.addOption("s", "seed", true, "path to the seed file");
			buildModelOptions.addOption("c", "targetStorageConfig", true, "config file path");
			buildModelOptions.addOption("t", "trainingDataDir", true, "training data path");
			buildModelOptions.addOption("o", "outputDir", true, "data output path");
			startTargetStorageOptions.addOption("o", "outputDir", true, "data output path");
			startTargetStorageOptions.addOption("c", "configDir", true, "config directory path");
			startTargetStorageOptions.addOption("m", "modelDir", true, "model directory path");
			startTargetStorageOptions.addOption("p", "profileDir", true, "profile directory path");
			startLinkStorageOptions.addOption("o", "outputDir", true, "data output path");
			startLinkStorageOptions.addOption("c", "configDir", true, "config directory path");
			startLinkStorageOptions.addOption("s", "seed", true, "path to the seed file");
			startCrawlManagerOptions.addOption("c", "configDir", true, "config directory path");
			allOptions = new Options[] { startCrawlOptions, addSeedsOptions,
					startCrawlManagerOptions, buildModelOptions, startTargetStorageOptions,
					startLinkStorageOptions };
			commandName = new String[] { "startCrawl", "addSeeds", "startCrawlManager",
					"buildModel", "startTargetStorage", "startLinkStorage" };

			if ("startCrawl".equals(args[0]) && (args.length == 6 || args.length == 11)) {
				try {
					cmd = parser.parse(startCrawlOptions, args);
					if (cmd.hasOption("outputDir")) {
						startCrawl(cmd.getOptionValue("outputDir"),
								cmd.getOptionValue("configDir"), cmd.getOptionValue("seed"),
								cmd.getOptionValue("modelDir"), cmd.getOptionValue("langDetect"));
					} else
						startCrawl(args[1], args[2], args[3], args[4], args[5]);

				} catch (Throwable e) {
					printError(e);
				}
			} else if ("addSeeds".equals(args[0]) && (args.length == 4 || args.length == 7)) {
				try {
					cmd = parser.parse(addSeedsOptions, args);
					if (cmd.hasOption("outputDir")) {
						addSeeds(cmd.getOptionValue("outputDir"), cmd.getOptionValue("configDir"),
								cmd.getOptionValue("seed"));
					} else
						addSeeds(args[1], args[2], args[3]);
				} catch (Throwable e) {
					printError(e);
				}

			} else if ("buildModel".equals(args[0]) && (args.length == 4 || args.length == 7)) {
				try {
					cmd = parser.parse(buildModelOptions, args);
					if (cmd.hasOption("outputDir")) {
						buildModel(cmd.getOptionValue("targetStorageConfig"),
								cmd.getOptionValue("trainingDataDir"),
								cmd.getOptionValue("outputDir"));
					} else
						buildModel(args[1], args[2], args[3]);
				} catch (Throwable e) {
					printError(e);
				}

			} else if ("startLinkStorage".equals(args[0]) && (args.length == 4 || args.length == 7)) {
				try {
					cmd = parser.parse(startLinkStorageOptions, args);
					if (cmd.hasOption("outputDir"))
						startLinkStorage(cmd.getOptionValue("outputDir"),
								cmd.getOptionValue("configDir"), cmd.getOptionValue("seed"));
					else
						startLinkStorage(args[1], args[2], args[3]);
				} catch (Throwable e) {
					printError(e);
				}
			} else if ("startTargetStorage".equals(args[0])
					&& (args.length == 5 || args.length == 9)) {
				try {
					cmd = parser.parse(startTargetStorageOptions, args);
					if (cmd.hasOption("outputDir")) {
						startTargetStorage(cmd.getOptionValue("outputDir"),
								cmd.getOptionValue("configDir"), cmd.getOptionValue("modelDir"),
								cmd.getOptionValue("profileDir"));
					} else
						startTargetStorage(args[1], args[2], args[3], args[4]);
				} catch (Throwable e) {
					printError(e);
				}

			} else if ("startCrawlManager".equals(args[0])
					&& (args.length == 2 || args.length == 3)) {
				try {
					cmd = parser.parse(startCrawlManagerOptions, args);
					if (cmd.hasOption("configDir")) {
						startCrawlManager(cmd.getOptionValue("configDir"));
					} else
						startCrawlManager(args[1]);

				} catch (Throwable e) {
					printError(e);
				}

			} else {
				printUsage();
				System.exit(1);
			}
		} else {
			printUsage();
			System.exit(1);
		}
	}

	private static void printError(Throwable e) {
		// TODO Auto-generated method stub
		System.out.println("Unable to parse the input. Did you enter the parameters correctly? ");
		logger.error("Unable to parse the command line string. ", e);
		System.out.println("The format is :");
		printUsage();
	}

	//
	private static void buildModel(String targetStorageConfigPath, String trainingPath,
			String outputPath) {
		// generate the input for weka
		new File(outputPath).mkdirs();
		CreateWekaInput.main(new String[] { targetStorageConfigPath, trainingPath,
				trainingPath + "/weka.arff" });

		// generate the model
		SMO.main(new String[] { "-M", "-d", outputPath + "/pageclassifier.model", "-t",
				trainingPath + "/weka.arff" });
	}

	private static void addSeeds(final String dataOutputPath, final String configPath,
			final String seedPath) {
		createOutputPathStructure(dataOutputPath);
		AddSeeds.main(new String[] { configPath, seedPath, dataOutputPath });
	}

	private static void startLinkStorage(final String dataOutputPath, final String configPath,
			final String seedPath) {
		try {
			LinkStorage.main(new String[] { configPath, seedPath, dataOutputPath });
		} catch (Throwable t) {
			logger.error("Something bad happened to LinkStorage :(", t);
		}
	}

	private static void startTargetStorage(final String dataOutputPath, final String configPath,
			final String modelPath, final String langDetectProfilePath) {
		try {
			TargetStorage.main(new String[] { configPath, modelPath, dataOutputPath,
					langDetectProfilePath });
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

	// private static void startFormStorage(){
	// //Not used yet
	// }

	private static void startCrawl(final String dataOutputPath, final String configPath,
			final String seedPath, final String modelPath, final String langDetectProfilePath) {

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

		System.out.println("Focused Crawler");
		HelpFormatter formatter = new HelpFormatter();
		for (int i = 0; i < allOptions.length; i++)
			formatter.printHelp(commandName[i], allOptions[i], true);

		// TODO package the profiles with gradle build or mash them into the
		// resources
		// lang detect profile can be downloaded from
		// https://code.google.com/p/language-detection/wiki/Downloads
		// TODO: Model path in startTargetStorage?

		System.out.println("Examples:");
		System.out
				.println("ache buildModel -c config/sample_config/target_storage.cfg -t training_data -o output_model");
		System.out.println("ache addSeeds -o data -c config/sample_config -s config/sample.seeds");
		System.out
				.println("ache startLinkStorage -o data -c config/sample_config -s config/sample.seeds");
		System.out
				.println("ache startTargetStorage -o data -c config/sample_config -m config/sample_config -p libs/profiles");
		System.out.println("ache startCrawlManager -c config/sample_config");

	}

}
