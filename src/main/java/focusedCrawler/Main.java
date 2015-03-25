package focusedCrawler;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.functions.SMO;
import focusedCrawler.crawler.CrawlerManager;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.AddSeeds;
import focusedCrawler.target.CreateWekaInput;
import focusedCrawler.target.TargetStorage;

/**
 * <p>Description: This is the main entry point for working with the components of the focusedCrawler </p>
 */

public class Main {
	
	public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        if (args.length > 0) {
            if ("addSeeds".equals(args[0]) && args.length == 4) {
                addSeeds(args[1], args[2], args[3]);
            } else if ("buildModel".equals(args[0]) && args.length == 4) {
                buildModel(args[1], args[2], args[3]);
            } else if ("startLinkStorage".equals(args[0]) && args.length == 4) {
                startLinkStorage(args[1], args[2], args[3]);
            } else if ("startTargetStorage".equals(args[0]) && args.length == 5) {
                startTargetStorage(args[1], args[2], args[3], args[4]);
            } else if ("startCrawlManager".equals(args[0]) && args.length == 2) {
                startCrawlManager(args[1]);
            } else {
                printUsage();
                System.exit(1);
            }
        } else {
            printUsage();
            System.exit(1);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void buildModel(String targetStorageConfigPath, String trainingPath, String outputPath) {
        // generate the input for weka
        new File(outputPath).mkdirs();
        CreateWekaInput.main(new String[]{targetStorageConfigPath, trainingPath, trainingPath + "/weka.arff"});

        // generate the model
        SMO.main(new String[]{"-M", "-d", outputPath + "/pageclassifier.model", "-t", trainingPath + "/weka.arff"});
    }

    private static void addSeeds(final String dataOutputPath,
                                 final String configPath,
                                 final String seedPath){
        createOutputPathStructure(dataOutputPath);
        AddSeeds.main(new String[]{configPath, seedPath, dataOutputPath});
    }

    private static void startLinkStorage(final String dataOutputPath,
                                         final String configPath,
                                         final String seedPath){
        try {
            LinkStorage.main(new String[]{configPath, seedPath, dataOutputPath});
        } catch (Throwable t) {
            logger.error("Something bad happened to LinkStorage :(", t);
        }
    }

    private static void startTargetStorage(final String dataOutputPath,
                                           final String configPath,
                                           final String modelPath,
                                           final String langDetectProfilePath){
         try {
             TargetStorage.main(new String[]{configPath, modelPath, dataOutputPath, langDetectProfilePath});
         } catch (Throwable t) {
             logger.error("Something bad happened to TargetStorage :(", t);
         }
    }

    private static void startCrawlManager(final String configPath){
        try {
            CrawlerManager.main(new String[]{configPath});
        } catch (Throwable t) {
            logger.error("Something bad happened to CrawlManager :(", t);
        }

    }

    private static void startFormStorage(){
        //Not used yet
    }

    //THIS FUNCTION IS NOT USED NOW
    private static void startCrawl(final String dataOutputPath,
                                   final String configPath,
                                   final String seedPath,
                                   final String modelPath,
                                   final String langDetectProfilePath) {

        // set up the data directories
        createOutputPathStructure(dataOutputPath);

        // add seeds
        AddSeeds.main(new String[]{configPath, seedPath, dataOutputPath});

        // start link storage
        ExecutorService crawlServices = Executors.newFixedThreadPool(3);
        crawlServices.submit(new Runnable() {
            @Override
            public void run() {
                startLinkStorage(dataOutputPath, configPath, seedPath);
            }
        });

        // start target storage
        crawlServices.submit(new Runnable() {
            @Override
            public void run() {
               startTargetStorage(dataOutputPath, configPath, modelPath, langDetectProfilePath);
            }
        });

        // start crawl manager
        crawlServices.submit(new Runnable() {
            @Override
            public void run() {
                startCrawlManager(configPath);
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void createOutputPathStructure(String dataOutputPath) {
        File dataOutput = new File(dataOutputPath);
        if (dataOutput.exists()) {
            logger.warn("Data output path already exists, deleting everything");
            dataOutput.delete();
        }

        dataOutput.mkdirs();
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
        System.out.println();
        // TODO package the profiles with gradle build or mash them into the resources
        // lang detect profile can be downloaded from https://code.google.com/p/language-detection/wiki/Downloads
        System.out.println("ache buildModel <target storage config path> <training data path> <output path>");
        System.out.println("ache addSeeds <data output path> <config path> <seed path>");
        System.out.println("ache startLinkStorage <data output path> <config path> <seed path>");
        System.out.println("ache startTargetStorage <data output path> <config path> <model path> <lang detect profile path>");
        System.out.println("ache startCrawlManager <config path>");
        System.out.println();
        System.out.println();
        System.out.println("Examples:");
        System.out.println("ache buildModel config/sample_config/target_storage.cfg training_data output_model");
        System.out.println("ache addSeeds data config/sample_config config/sample.seeds");
        System.out.println("ache startLinkStorage data config/sample_config config/sample.seeds");
        System.out.println("ache startTargetStorage data config/sample_config config/sample_config libs/profiles");
        System.out.println("ache startCrawlManager config/sample_config");

    }

}
