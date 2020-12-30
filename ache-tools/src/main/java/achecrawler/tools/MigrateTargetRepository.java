package achecrawler.tools;

import achecrawler.config.Configuration;
import achecrawler.target.TargetRepositoryFactory;
import achecrawler.target.TargetStorageConfig;
import achecrawler.target.model.Page;
import achecrawler.target.repository.TargetRepository;
import achecrawler.util.CliTool;
import achecrawler.util.CloseableIterator;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="MigrateTargetRepository", description="Migrate an repository to another repository")
public class MigrateTargetRepository extends CliTool {
    
    @Option(name = "--input-path", required = true, description = "Path to old input data_target folder")
    private String inputPath;
    
    @Option(name = "--input-config", required = true, description = "Path to config of the input repository")
    private String inputConfigPath;

    @Option(name = "--output-path", required = true, description = "Path to new output data_target folder")
    private String outputPath;
    
    @Option(name = "--output-config", required = true, description = "Path to config of the output repository")
    private String outputConfigPath;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new MigrateTargetRepository());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputPath);
        System.out.println("Writing output file at: " + outputPath);
        System.out.println();

        int processedPages = 0;
        
        TargetStorageConfig inputConfig = new Configuration(inputConfigPath).getTargetStorageConfig();
        TargetRepository oldRep = TargetRepositoryFactory.create(inputPath, null, null, inputConfig);
        
        TargetStorageConfig outputConfig = new Configuration(outputConfigPath).getTargetStorageConfig();
        TargetRepository newRep = TargetRepositoryFactory.create(outputPath, null, null, outputConfig);
        
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
