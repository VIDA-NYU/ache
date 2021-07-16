package achecrawler.tools;


import java.io.FileWriter;
import java.net.URL;

import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.target.repository.FilesTargetRepository;
import achecrawler.util.CliTool;
import achecrawler.util.CloseableIterator;
import achecrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="FindCorruptedUrls", description="Find corrupted URLs in a repository")
public class FindCorruptedUrls extends CliTool {
    
    @Option(name = "--input-path", required = true, description = "Path to old input data_target folder")
    private String inputPath;

    @Option(name = "--output-file", required = true, description = "Path to output file with extracted URLs")
    private String outputPath;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new FindCorruptedUrls());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputPath);
        System.out.println("Writing output file at: " + outputPath);
        System.out.println();

        int processedPages = 0;
        FileWriter file = new FileWriter(outputPath);
        
        FilesTargetRepository oldRep = new FilesTargetRepository(inputPath);
        try (CloseableIterator<Page> oldIt = oldRep.pagesIterator()) {
            while (oldIt.hasNext()) {
                try {
                    Page page = oldIt.next();
                    if (page.isHtml()) {
                        PaginaURL pageParser = new PaginaURL(page);
                        page.setParsedData(new ParsedData(pageParser));
                        URL[] links = page.getParsedData().getLinks();
                        if(links != null && links.length > 0) {
                            for(URL link : links) {
                                String host = link.getHost();
                                if(link.toString().endsWith("%2F")) {
                                    file.write(host);
                                    file.write(" ");
                                    file.write(link.toString());
                                    file.write("\n");
                                }
                            }
                        }
                    }
                    
                } catch(Exception e) {
                    System.out.println("Ignoring file due to failure.");
                    e.printStackTrace(System.out);
                    continue;
                }
                file.flush();
                processedPages++;
                if (processedPages % 1000 == 0) {
                    System.out.printf("Processed %s pages...\n", processedPages);
                }

            }
        }
        file.close();
        
        System.out.printf("Finished processing %d pages.\n", processedPages);
    }

}
