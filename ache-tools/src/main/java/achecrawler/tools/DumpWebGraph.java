package achecrawler.tools;

import java.io.PrintStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.config.Configuration;
import achecrawler.target.TargetRepositoryFactory;
import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.target.repository.TargetRepository;
import achecrawler.util.CliTool;
import achecrawler.util.CloseableIterator;
import achecrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "DumpWebGraph", description = "Print the web graph from a data repository")
public class DumpWebGraph extends CliTool {

    private static final Logger logger = LoggerFactory.getLogger(DumpWebGraph.class);

    @Option(name = {"-irp", "--input-data-path"}, required = true,
            description = "Path to input crawler data directory")
    String inputDataPath;

    @Option(name = {"-icp", "--input-config-path"}, required = false,
            description = "Path to config path of the input crawl")
    String inputConfigPath;

    @Option(name = {"-d2d", "--domain-to-domain"}, required = false,
            description = "Whether should print domain-to-domain links")
    boolean domainToDomain = true;

    @Option(name = {"-p2p", "--page-to-page"}, required = false,
            description = "Whether should print page-to-page links")
    boolean pageToPage = false;

    @Option(name = "--output-file", description = "The output file", required = false)
    private String outputFile;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new DumpWebGraph());
    }

    @Override
    public void execute() throws Exception {

        Configuration inputConfig = new Configuration(inputConfigPath);
        TargetRepository inputRepository = TargetRepositoryFactory.create(inputDataPath,
                null, null, inputConfig.getTargetStorageConfig());

        PrintStream pageToPageFile = null;
        if (pageToPage) {
            pageToPageFile = new PrintStream(outputFile + ".page2page.txt");
        }
        PrintStream domainToDomainFile = null;
        if (domainToDomain) {
            domainToDomainFile = new PrintStream(outputFile + ".domain2domain.txt");
        }

        int pages = 0;
        int totalLinks = 0;
        try (CloseableIterator<Page> it = inputRepository.pagesIterator()) {
            while (it.hasNext()) {
                try {
                    Page page = it.next();

                    String finalUrl = page.getFinalUrl();
                    String domain = null;
                    if (domainToDomain) {
                        domain = page.getFinalUrlHost();
                    }

                    if (page.isHtml()) {
                        PaginaURL pageParser = new PaginaURL(page);
                        page.setParsedData(new ParsedData(pageParser));

                        URL[] links = page.getParsedData().getLinks();
                        for (URL link : links) {
                            String linkHost = link.getHost();
                            if (pageToPage) {
                                pageToPageFile.printf("%s %s\n", finalUrl, link.toString());
                            }
                            if (domainToDomain) {
                                domainToDomainFile.printf("%s %s\n", domain, linkHost);
                            }
                            totalLinks++;
                        }
                    }

                } catch (Exception e) {
                    logger.error("An unexpected error happened.", e);
                }
                pages++;
                if (pages % 1000 == 0) {
                    double linksPerPage = pages > 0 ? (totalLinks / (double) pages) : 0;
                    System.out.printf("pages = %d total_links = %d  links_per_page = %.2f\n",
                            pages, totalLinks, linksPerPage);
                }
            }

            if (domainToDomain) {
                domainToDomainFile.close();
            }
            if (pageToPage) {
                pageToPageFile.close();
            }

            double linksPerPage = pages > 0 ? (totalLinks / (double) pages) : 0;
            System.out.printf("pages = %d total_links = %d  links_per_page = %.2f\n",
                    pages, totalLinks, linksPerPage);

            System.out.printf("Processed %s pages.\n", pages);
            System.out.printf("done.\n");
        }

    }

}
