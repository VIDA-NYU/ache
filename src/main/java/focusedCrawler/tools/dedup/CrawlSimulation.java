package focusedCrawler.tools.dedup;

import focusedCrawler.config.Configuration;
import focusedCrawler.dedup.DupDetector;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.minhash.DupDetectorFactory;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.target.repository.RocksDBTargetRepository;
import focusedCrawler.util.CliTool;
import focusedCrawler.util.CloseableIterator;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import java.io.FileOutputStream;
import java.io.PrintStream;


@Command(name = "CrawlSimulation",
        description = "Run a crawl simulation based on data from a previous crawl using the new configurations.")
public class CrawlSimulation extends CliTool {

    @Option(name = {"-ir", "--input-repository"}, required = true,
            description = "Path to input crawler data directory")
    private String inputRepositoryPath;

    @Option(name = {"--oracle"}, required = false,
            description = "If should only simulate an oracle crawling policy")
    private boolean oracle = false;

    @Option(name = {"-c", "--config"}, required = false,
            description = "Path to configuration files folder")
    private String configPath;

    @Option(name = {"-m", "--modelDir"}, required = false,
            description = "Path to folder containing page classifier model")
    private String modelPath;

    @Option(name = {"-o", "--outputDir"}, required = false,
            description = "Path to folder which model built should be stored")
    private String dataPath;

    @Option(name = {"-s", "--seed"}, required = false, description = "Path to file of seed URLs")
    private String seedPath;

    @Option(name = {"-mf",
            "--metrics-file"}, required = false, description = "Path to file of seed URLs")
    private String metricsFile;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new CrawlSimulation());
    }

    @Override
    public void execute() throws Exception {

        RocksDBTargetRepository inputRepository = new RocksDBTargetRepository(inputRepositoryPath,
                true);

        Configuration config = new Configuration(configPath);
        DupDetector crawlDupDetector = DupDetectorFactory.create(config, dataPath);
        if (oracle) {
            runOracle(inputRepository, crawlDupDetector);
            crawlDupDetector.close();
            inputRepository.close();
            return;
        }

        LinkStorageConfig linkStorageConfig = config.getLinkStorageConfig();
        MetricsManager metricsManager = new MetricsManager(false, dataPath);
        LinkStorage linkStorage = LinkStorage.create(configPath, seedPath, dataPath, modelPath,
                linkStorageConfig, metricsManager, crawlDupDetector);
        try {
            this.replay(linkStorage, inputRepository, crawlDupDetector);
        } finally {
            linkStorage.close();
            metricsManager.close();
            inputRepository.close();
        }
    }

    public void replay(LinkStorage linkStorage, RocksDBTargetRepository downloader,
            DupDetector crawlDupDetector) throws Exception {

        int processedPages = 0;
        int missingLinks = 0;
        int ignoredLinks = 0;
        int crawledPages = 0;
        int duplicatePages = 0;

        PrintStream metricsLog;
        if (metricsFile == null) {
            metricsLog = System.out;
        } else {
            metricsLog = new PrintStream(new FileOutputStream(metricsFile), true);
        }

        LinkRelevance link = linkStorage.selectSync();
        while (true) {

            if (processedPages % 100 == 0) {
                System.out.printf("processed_pages = %d missing = %d ignored = %d\n",
                        processedPages, missingLinks, ignoredLinks);
            }

            if (link.getType() == LinkRelevance.Type.FORWARD) {
                Page page = downloader.get(link.getURL().toString());
                if (page != null) {

                    page.setLinkRelevance(link);
                    page.setCrawlerId("default");

                    if (page.isHtml()) {
                        String text = getCleanText(page);
                        String key = page.getRequestedUrl();
                        boolean isNearDuplicate = crawlDupDetector.detectAndIndex(key, text);
                        page.setNearDuplicate(isNearDuplicate);

                        linkStorage.insert(page);

                        //
                        // Compute metrics and write to log file
                        //
                        crawledPages++;
                        if (isNearDuplicate) {
                            duplicatePages++;
                        }
                        int uniquePages = crawledPages - duplicatePages;
                        double harvestRate = (uniquePages / (double) crawledPages) * 100;
                        metricsLog.printf("%d %d %.4f\n", crawledPages, uniquePages, harvestRate);

                    } else {
                        ignoredLinks++;
                    }
                } else {
                    missingLinks++;
                }

            } else {
                ignoredLinks++;
            }
            processedPages++;
            try {
                link = linkStorage.selectSync();
            } catch (DataNotFoundException e) {
                if (e.ranOutOfLinks()) {
                    break;
                }
            }
        }
        System.out.printf("processed_pages = %d missing = %d ignored = %d\n", processedPages,
                missingLinks, ignoredLinks);
        System.out.printf("Total processed %s pages.\n", processedPages);
    }

    private String getCleanText(Page page) {
        PaginaURL pageParser = new PaginaURL(page);
        page.setParsedData(new ParsedData(pageParser));
        return page.getParsedData().getCleanText();
    }

    /**
     * Simulates the performance of a crawling policy that always crawls non-duplicate pages.
     */
    public void runOracle(RocksDBTargetRepository inputRepository, DupDetector crawlDupDetector)
            throws Exception {

        int processedPages = 0;
        int ignoredLinks = 0;
        int crawledPages = 0;
        int duplicatePages = 0;
        int uniquePages = 0;

        PrintStream metricsLog;
        if (metricsFile == null) {
            metricsLog = System.out;
        } else {
            metricsLog = new PrintStream(new FileOutputStream(metricsFile), true);
        }

        CloseableIterator<Page> it = inputRepository.pagesIterator();

        while (it.hasNext()) {
            Page page = it.next();

            if (processedPages % 100 == 0) {
                System.out.printf("processed_pages = %d ignored = %d\n",
                        processedPages, ignoredLinks);
            }
            // TODO: Should we deal with redirects here?
            if (page.isHtml()) {
                String text = getCleanText(page);
                String key = page.getRequestedUrl();
                boolean isNearDuplicate = crawlDupDetector.detectAndIndex(key, text);

                //
                // Compute metrics and write to log file
                //
                if (isNearDuplicate) {
                    duplicatePages++;
                } else {
                    uniquePages++;
                    crawledPages++;
                    metricsLog.printf("%d %d %.4f\n", uniquePages, uniquePages, 100d);
                }
            } else {
                ignoredLinks++;
            }

            processedPages++;
        }

        for (int i = 0; i < duplicatePages; i++) {
            crawledPages++;
            double harvestRate = (uniquePages / (double) crawledPages) * 100;
            metricsLog.printf("%d %d %.4f\n", crawledPages, uniquePages, harvestRate);
        }

        System.out.printf("processed_pages = %d ignored = %d\n", processedPages, ignoredLinks);
        System.out.printf("Total processed %s pages.\n", processedPages);
    }

}
