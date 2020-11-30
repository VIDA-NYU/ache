package achecrawler.tools;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.config.Configuration;
import achecrawler.crawler.async.FetchedResultHandler;
import achecrawler.crawler.async.HttpDownloader;
import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.RobotsTxtHandler;
import achecrawler.crawler.async.SitemapXmlHandler;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.link.LinkStorage;
import achecrawler.link.frontier.Frontier;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.TargetRepositoryFactory;
import achecrawler.target.TargetStorage;
import achecrawler.target.model.Page;
import achecrawler.target.repository.TargetRepository;
import achecrawler.util.CliTool;
import achecrawler.util.CloseableIterator;
import achecrawler.util.LinkFilter;
import achecrawler.util.MetricsManager;
import io.airlift.airline.Command;
import io.airlift.airline.Option;


@Command(name = "ReplayCrawl",
        description = "Replays a crawl based on data from a previous crawl using the new configurations.")
public class ReplayCrawl extends CliTool {

    private static final Logger logger = LoggerFactory.getLogger(ReplayCrawl.class);

    @Option(name = {"-irp", "--input-data-path"}, required = true,
            description = "Path to input crawler data directory")
    String inputDataPath;

    @Option(name = {"-icp", "--input-config-path"}, required = true,
            description = "Path to config path of the input crawl")
    String inputConfigPath;

    @Option(name = {"-ilf", "--input-link-filter"}, required = false,
            description = "Path to a .yml link fiter file")
    private String inputLinkFilterPath;

    @Option(name = {"-cid", "--crawlerId"}, required = false,
            description = "An unique identifier for this crawler")
    String crawlerId = "default";

    @Option(name = {"-c", "--config"}, required = true,
            description = "Path to configuration files folder")
    String configPath;

    @Option(name = {"-m", "--modelDir"}, required = false,
            description = "Path to folder containing page classifier model")
    String modelPath;

    @Option(name = {"-o", "--outputDir"}, required = true,
            description = "Path to folder which model built should be stored")
    String dataPath;

    @Option(name = {"-s", "--seed"}, required = false, description = "Path to file of seed URLs")
    String seedPath;

    @Option(name = {"-e", "--elasticIndex"}, required = false,
            description = "Name of Elasticsearch index to be used")
    String esIndexName;

    @Option(name = {"-t", "--elasticType"}, required = false,
            description = "Name of Elasticsearch document type to be used")
    String esTypeName;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new ReplayCrawl());
    }

    @Override
    public void execute() throws Exception {

        Configuration config = new Configuration(configPath);

        MetricsManager metricsManager = new MetricsManager(false, dataPath);

        LinkStorage linkStorage = LinkStorage.create(configPath, seedPath, dataPath,
                modelPath, config.getLinkStorageConfig(), metricsManager);

        TargetStorage targetStorage = TargetStorage.create(configPath, modelPath, dataPath,
                esIndexName, esTypeName, config.getTargetStorageConfig(), linkStorage,
                metricsManager);

        Configuration inputConfig = new Configuration(inputConfigPath);
        TargetRepository inputRepository = TargetRepositoryFactory.create(inputDataPath,
                null, null, inputConfig.getTargetStorageConfig());

        String directory = Paths.get(inputDataPath,
                inputConfig.getLinkStorageConfig().getLinkDirectory()).toString();

        Frontier inputFrontier = new Frontier(directory,
                inputConfig.getLinkStorageConfig().getMaxCacheUrlsSize(),
                inputConfig.getLinkStorageConfig().getPersistentHashtableBackend());

        LinkFilter inputLinkFilter = null;
        if (inputLinkFilterPath != null) {
            inputLinkFilter = new LinkFilter.Builder()
                    .fromYamlFile(inputLinkFilterPath)
                    .build();
        }


        this.replay(crawlerId, targetStorage, linkStorage, config, inputRepository,
                inputFrontier, inputLinkFilter);


        inputFrontier.close();
        inputRepository.close();
        targetStorage.close();
        linkStorage.close();
        metricsManager.close();
    }

    public void replay(String crawlerId, TargetStorage targetStorage, LinkStorage linkStorage,
            Configuration config, TargetRepository inputRepository, Frontier inputFrontier,
            LinkFilter inputLinkFilter)
            throws Exception {

        Map<LinkRelevance.Type, HttpDownloader.Callback> handlers = new HashMap<>();

        HttpDownloaderConfig downloaderConfig = config.getCrawlerConfig().getDownloaderConfig();
        handlers.put(LinkRelevance.Type.FORWARD,
                new FetchedResultHandler(crawlerId, targetStorage));
        handlers.put(LinkRelevance.Type.SITEMAP, new SitemapXmlHandler(linkStorage));
        handlers.put(LinkRelevance.Type.ROBOTS, new RobotsTxtHandler(linkStorage,
                downloaderConfig.getUserAgentName()));

        int processedPages = 0;
        int ignoredPages = 0;
        try (CloseableIterator<Page> it = inputRepository.pagesIterator()) {
            while (it.hasNext()) {

                if (processedPages % 1000 == 0) {
                    double ignoredPercent =
                            processedPages > 0 ? (100 * ignoredPages / (double) processedPages) : 0;
                    System.out.printf("processed_pages = %d ignored = %d  ignored_percent = %.2f\n",
                            processedPages, ignoredPages, ignoredPercent);
                }

                try {
                    Page page = it.next();

                    String requestedUrl = page.getRequestedUrl();
                    if (inputLinkFilter != null && !inputLinkFilter.accept(requestedUrl)) {
                        // logger.warn("Ignoring link because of filter: {}", requestedUrl);
                        ignoredPages++;
                        continue;
                    }

                    String contentType = page.getContentType();
                    if (contentType == null || contentType.isEmpty()) {
                        logger.warn("Ignoring page with no content type.");
                        ignoredPages++;
                        continue;
                    }

                    LinkRelevance lr = inputFrontier.get(requestedUrl);
                    if (lr == null) {
                        logger.warn("Ignoring link because it is not present in the frontier: {}",
                                requestedUrl);
                        ignoredPages++;
                        continue;
                    }

                    // Change relevance to positive (because it is turned into a
                    // negative value after the page is crawled)
                    lr = new LinkRelevance(lr.getURL(), Math.abs(lr.getRelevance()), lr.getType());

                    String finalUrl = page.getFinalUrl();
                    Metadata responseHeaders = createHeadersMetadata(page);
                    FetchedResult result = new FetchedResult(requestedUrl,
                            finalUrl, page.getFetchTime(), responseHeaders,
                            page.getContent(), page.getContentType(), 0, null,
                            page.getFinalUrl(), 0, "", 200, "OK");

                    handlers.get(lr.getType()).completed(lr, result);

                } catch (Exception e) {
                    logger.error("An unexpected error happened.", e);
                }
                processedPages++;
            }

            double ignoredPercent =
                    processedPages > 0 ? (100 * ignoredPages / (double) processedPages) : 0;
            System.out.printf("processed_pages = %d ignored = %d  ignored_percent = %.2f\n",
                    processedPages, ignoredPages, ignoredPercent);
            System.out.printf("Processed %s pages.\n", processedPages);
            System.out.printf("done.\n");
        }
    }

    private Metadata createHeadersMetadata(Page page) {
        Map<String, List<String>> headers = page.getResponseHeaders();
        Metadata metadata = new Metadata();
        for (Entry<String, List<String>> header : headers.entrySet()) {
            for (String value : header.getValue()) {
                metadata.set(header.getKey(), value);
            }
        }
        return metadata;
    }

}
