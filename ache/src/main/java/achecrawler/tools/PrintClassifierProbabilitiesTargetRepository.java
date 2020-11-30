package achecrawler.tools;

import java.io.PrintStream;

import achecrawler.target.classifier.TargetClassifier;
import achecrawler.target.classifier.TargetClassifierFactory;
import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.target.repository.FileSystemTargetRepository;
import achecrawler.target.repository.FileSystemTargetRepository.DataFormat;
import achecrawler.target.repository.FilesTargetRepository;
import achecrawler.target.repository.TargetRepository;
import achecrawler.util.CliTool;
import achecrawler.util.CloseableIterator;
import achecrawler.util.parser.PaginaURL;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "PrintClassifierProbabilitiesTargetRepository")
public class PrintClassifierProbabilitiesTargetRepository extends CliTool {
    
    @Option(name = "--input-path", required = true, description = "Path to old input data_target folder")
    private String inputPath;

    @Option(name = "--output-file", required = false, description = "Path to output file containing URL-probabilities")
    private String outputFile;
    
    @Option(name = "--model-path", required = true, description = "The path to the target classifier to be used")
    private String modelPath;

    @Option(name = "--hash-file-name", required = false, description = "If the repository uses hashed file names")
    private boolean hashFilename = true;
    
    @Option(name = "--compressed-data", required = false, description = "If the repository uses compressed files")
    private boolean compressData = true;
    
    @Option(name = "--files-repository", required = false, description = "If the new FilesTargetRepository")
    private boolean filesRepository = false;
    
    private int count = 0;
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new PrintClassifierProbabilitiesTargetRepository());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputPath);
        System.out.println("Writing output file at: " + outputFile);

        PrintStream out = System.out;
        if(outputFile != null) {
            out = new PrintStream(outputFile);
        }
        
        TargetClassifier classifier = TargetClassifierFactory.create(modelPath);
        TargetRepository repository;
        if (filesRepository) {
            repository = new FilesTargetRepository(inputPath);
        } else {
            repository = new FileSystemTargetRepository(inputPath,
                    DataFormat.JSON, hashFilename, compressData);
        }

        try (CloseableIterator<Page> it = repository.pagesIterator()) {
            while (it.hasNext()) {
                Page target = it.next();
                printClassifierOutput(classifier, target, out);
            }
        }

        repository.close();
    }

    private void printClassifierOutput(TargetClassifier classifier, Page page, PrintStream out) {
        try {
            PaginaURL pageParser = new PaginaURL(page);
            page.setParsedData(new ParsedData(pageParser));
            
            TargetRelevance relevance = classifier.classify(page);
            
            out.printf("%.6f %s\n", relevance.getRelevance(), page.getFinalUrl());
            count++;
            if(count % 1000 == 0) {
                System.out.printf("Processed %d files...\n", count);
            }
        } catch (Exception e) {
            System.err.printf("Failed to process URL: %s", page.getFinalUrl());
            e.printStackTrace(System.err);
        }
    }

}
