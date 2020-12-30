package achecrawler.tools;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import achecrawler.target.model.Page;
import achecrawler.target.repository.FilesTargetRepository;
import achecrawler.util.CliTool;
import achecrawler.util.CloseableIterator;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="DuplicateDetector", description="Counts duplicates and create deduplicated repository")
public class DuplicateDetector extends CliTool {

    @Option(name = "--input-path",
            required = true,
            description = "Path to directory containing a FILES repository")
    private String inputPath;

    @Option(name = "--output-file",
            required = true,
            description = "Text file containing duplicate statistics per TLD")
    private String outputFile;

    @Option(name = {"--deduped-path"},
            description = "A new FILES repository contaning only unique pages")
    private String dedupedRepositoryPath;
    
    
    public static void main(String[] args) throws Exception {
        CliTool.run(args, new DuplicateDetector());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputPath);
        System.out.println("Writing statistics output file at: " + outputFile);
        
        FilesTargetRepository repository = new FilesTargetRepository(inputPath);
        
        FilesTargetRepository dedupRepository = null;
        if(dedupedRepositoryPath != null && !dedupedRepositoryPath.isEmpty()) {
            dedupRepository = new FilesTargetRepository(dedupedRepositoryPath);
            System.out.println("Writing deduped repository at: " + outputFile);
        }

        int totalPages = 0;
        int dupPages = 0;
        int uniqPages = 0;
        
        Set<String> seen = new HashSet<>();
        
        Map<String, Integer>  totalCounts = new HashMap<>();
        Map<String, Integer> dupCounts = new HashMap<>();
        
        try (CloseableIterator<Page> it = repository.pagesIterator()) {
            while (it.hasNext()) {
                Page page = it.next();
                String host = new URL(page.getFinalUrl()).getHost();

                HashCode code = Hashing.sha1().hashBytes(page.getContent());
                String fingerprint = code.toString();

                if (seen.contains(fingerprint)) {
                    Integer dupCount = dupCounts.get(host);
                    if (dupCount == null) {
                        dupCount = 0;
                    }
                    dupCount++;
                    dupCounts.put(host, dupCount);

                    dupPages++;
                } else {
                    if (dedupRepository != null) {
                        dedupRepository.insert(page);
                    }
                    seen.add(fingerprint);
                    uniqPages++;
                }

                Integer totalCount = totalCounts.get(host);
                if (totalCount == null) {
                    totalCount = 0;
                }
                totalCount++;
                totalCounts.put(host, totalCount);

                totalPages++;
                if (totalPages % 1000 == 0) {
                    System.out.printf("Processed %s pages...\n", totalPages);
                }
            }
        }

        repository.close();
        if(dedupRepository != null) {
            dedupRepository.close();
        }
        
        System.out.printf("Finished processing %d pages (%d unique, %d duplicates, %.2f%%).\n", totalPages, uniqPages, dupPages, dupPages/(double)totalPages);
        
        System.out.println("Printing statistics file...");
        try (PrintWriter out = new PrintWriter(new FileOutputStream(outputFile), true)) {
            for (Entry<String, Integer> hostCount : totalCounts.entrySet()) {
                Integer d = dupCounts.get(hostCount.getKey());
                int dups = d == null ? 0 : d;
                int total = hostCount.getValue();
                out.printf("%s %d %d %.2f\n", hostCount.getKey(), total, dups, dups/(double)total);
            }
        }
        System.out.println("done.");

    }

}
