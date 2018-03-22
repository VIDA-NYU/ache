package focusedCrawler.dedup;

import focusedCrawler.config.Configuration;
import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.FrontierManagerFactory;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.target.repository.RocksDBTargetRepository;
import focusedCrawler.util.CliTool;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;


@Command(name = "CrawlSimulation",
        description = "Run a crawl simulation based on data from a previous crawl using the new configurations.")
public class CrawlSimulation extends CliTool {

    @Option(name = {"-ir", "--input-repository"}, required = true,
            description = "Path to input crawler data directory")
    private String inputRepositoryPath;

    @Option(name = {"-c", "--config"}, required = true,
            description = "Path to configuration files folder")
    private String configPath;

    @Option(name = {"-m", "--modelDir"}, required = false,
            description = "Path to folder containing page classifier model")
    private String modelPath;

    @Option(name = {"-o", "--outputDir"}, required = true,
            description = "Path to folder which model built should be stored")
    private String dataPath;

    @Option(name = {"-s", "--seed"}, required = false, description = "Path to file of seed URLs")
    private String seedPath;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new CrawlSimulation());
    }

    @Override
    public void execute() throws Exception {

        Configuration config = new Configuration(configPath);
        MetricsManager metricsManager = new MetricsManager(false, dataPath);

        LinkStorageConfig linkStorageConfig = config.getLinkStorageConfig();
        FrontierManager frontierManager = FrontierManagerFactory.create(linkStorageConfig,
                configPath, dataPath, modelPath, seedPath, metricsManager);

        RocksDBTargetRepository inputRepository = new RocksDBTargetRepository(inputRepositoryPath);
        this.replay(frontierManager, inputRepository);

        inputRepository.close();
        frontierManager.close();
        metricsManager.close();
    }

    public void replay(FrontierManager frontierManager, RocksDBTargetRepository downloader)
            throws Exception {

        int processedPages = 0;
        int missingLinks = 0;
        LinkRelevance link = frontierManager.nextURL();
        while (true) {
            if(link.getType() == LinkRelevance.Type.FORWARD) {
                Page page = downloader.get(link.getURL().toString());
                if (page != null) {
                    System.out.println(page.getRequestedUrl());
                    page.setLinkRelevance(link);
                    page.setCrawlerId("default");
                    if (page.isHtml()) {
                        PaginaURL pageParser = new PaginaURL(page);
                        page.setParsedData(new ParsedData(pageParser));
                    }
                    frontierManager.insertOutlinks(page);
                } else {
                    missingLinks++;
                }
                if (processedPages % 1000 == 0) {
                    System.out.printf("processed_pages = %d missing = %d\n", processedPages, missingLinks);
                }
            }
            try {
                link = frontierManager.nextURL();
            } catch (DataNotFoundException e) {
                if (e.ranOutOfLinks()) {
                    break;
                }
            }
            processedPages++;
        }
        System.out.printf("Total processed %s pages.\n", processedPages);
    }

}
