package focusedCrawler.tools;

import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import focusedCrawler.target.classifier.TargetClassifier;
import focusedCrawler.target.classifier.TargetClassifier.TargetRelevance;
import focusedCrawler.target.classifier.TargetClassifierFactory;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FileSystemTargetRepository;
import focusedCrawler.target.repository.FileSystemTargetRepository.DataFormat;
import focusedCrawler.target.repository.FileSystemTargetRepository.FileContentIterator;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.target.repository.FilesTargetRepository.RepositoryIterator;
import focusedCrawler.util.CliTool;
import focusedCrawler.util.parser.PaginaURL;


public class PrintClassifierProbabilitiesTargetRepository extends CliTool {
    
    @Option(name = "--input-path", required = true, usage = "Path to old input data_target folder")
    private String inputPath;

    @Option(name = "--output-file", required = false, usage = "Path to output file containing URL-probabilities")
    private String outputFile;
    
    @Option(name = "--model-path", required = true, usage = "The path to the target classifier to be used")
    private String modelPath;

    @Option(name = "--hash-file-name", required = false, usage = "If the repository uses hashed file names")
    private boolean hashFilename = true;
    
    @Option(name = "--compressed-data", required = false, usage = "If the repository uses compressed files")
    private boolean compressData = true;
    
    @Option(name = "--files-repository", required = false, usage = "If the new FilesTargetRepository")
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
        
        if(filesRepository) {
            FilesTargetRepository repository = new FilesTargetRepository(inputPath);
            try(RepositoryIterator it = repository.iterator()) {
                while(it.hasNext()) {
                    TargetModelJson target = it.next();
                    printClassifierOutput(classifier, target, out);
                }
            }
        } else {
            FileSystemTargetRepository repository = new FileSystemTargetRepository(inputPath, DataFormat.JSON, hashFilename, compressData);
            try(FileContentIterator<TargetModelJson> it = repository.iterator()) {
                while(it.hasNext()) {
                    TargetModelJson target = it.next();
                    printClassifierOutput(classifier, target, out);
                }
            }
        }

    }

    private void printClassifierOutput(TargetClassifier classifier, TargetModelJson target, PrintStream out) {
        try {
            Page page = new Page(target);
            PaginaURL pageParser = new PaginaURL(page);
            page.setParsedData(new ParsedData(pageParser));
            
            TargetRelevance relevance = classifier.classify(page);
            
            out.printf("%.6f %s\n", relevance.getRelevance(), target.getUrl());
            count++;
            if(count % 1000 == 0) {
                System.out.printf("Processed %d files...\n", count);
            }
        } catch (Exception e) {
            System.err.printf("Failed to process URL: %s", target.getUrl());
            e.printStackTrace(System.err);
        }
    }

}
