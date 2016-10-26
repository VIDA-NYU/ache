package focusedCrawler.tools;


import org.kohsuke.args4j.Option;

import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FileSystemTargetRepository.FileContentIterator;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.util.CliTool;


public class MigrateToFilesTargetRepository extends CliTool {
    
    @Option(name = "--input-path", required = true, usage = "Path to old input data_target folder")
    private String inputPath;

    @Option(name = "--output-path", required = true, usage = "Path to new output data_target folder")
    private String outputPath;

    @Option(name = "--hash-file-name", required = false, usage = "If the repository uses hashed file names")
    private boolean hashFilename = true;
    
    @Option(name = "--compressed-data", required = false, usage = "If the repository uses compressed files")
    private boolean compressData = true;
    
    @Option(name = "--data-format", required = false, usage = "The data format used by the old repository")
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
        
        FileSystemTargetRepository oldRep = new FileSystemTargetRepository(inputPath, dataFormat, hashFilename, compressData);
        FilesTargetRepository newRep = new FilesTargetRepository(outputPath);
        
        try(FileContentIterator<TargetModelJson> oldIt = oldRep.iterator()) {
            while(oldIt.hasNext()) {
                TargetModelJson target = oldIt.next();
                newRep.insert(target);
                
                processedPages++;
                if (processedPages % 1000 == 0) {
                    System.out.printf("Migrated %s pages...\n", processedPages);
                }
                
            }
        }
        
        System.out.printf("Finished processing %d pages.\n", processedPages);
    }

}
