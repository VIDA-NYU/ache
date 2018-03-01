package focusedCrawler.tools;


import focusedCrawler.target.model.Page;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.util.CliTool;
import focusedCrawler.util.CloseableIterator;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="MigrateToFilesTargetRepository", description="Migrate a FS repository to a FILES repository")
public class MigrateToFilesTargetRepository extends CliTool {
    
    @Option(name = "--input-path", required = true, description = "Path to old input data_target folder")
    private String inputPath;

    @Option(name = "--output-path", required = true, description = "Path to new output data_target folder")
    private String outputPath;

    @Option(name = "--hash-file-name", required = false, description = "If the repository uses hashed file names")
    private boolean hashFilename = false;

    @Option(name = "--compressed-data", required = false, description = "If the repository uses compressed files")
    private boolean compressData = false;

    @Option(name = "--data-format", required = false, description = "The data format used by the old repository")
    private DataFormat dataFormat = DataFormat.JSON;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new MigrateToFilesTargetRepository());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputPath);
        System.out.println("Writing output file at: " + outputPath);
        System.out.println();

        int processedPages = 0;
        
        FileSystemTargetRepository oldRep =
                new FileSystemTargetRepository(inputPath, dataFormat, hashFilename, compressData);
        FilesTargetRepository newRep = new FilesTargetRepository(outputPath);
        
        try (CloseableIterator<Page> oldIt = oldRep.pagesIterator()) {
            while (oldIt.hasNext()) {
                try {
                    Page page = oldIt.next();
                    newRep.insert(page);
                } catch (Exception e) {
                    System.out.println("Ignoring file due to failure.");
                    e.printStackTrace(System.out);
                    continue;
                }

                processedPages++;
                if (processedPages % 1000 == 0) {
                    System.out.printf("Migrated %s pages...\n", processedPages);
                }

            }
        }
        newRep.close();
        oldRep.close();
        System.out.printf("Finished processing %d pages.\n", processedPages);
    }

}
