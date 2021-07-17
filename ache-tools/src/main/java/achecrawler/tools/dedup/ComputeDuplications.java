package achecrawler.tools.dedup;

import achecrawler.config.Configuration;
import achecrawler.target.TargetRepositoryFactory;
import achecrawler.target.TargetStorageConfig;
import achecrawler.target.model.Page;
import achecrawler.target.repository.TargetRepository;
import achecrawler.util.CliTool;
import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

@Command(name = "ComputeDuplications")
public class ComputeDuplications extends CliTool {

    @Option(name = {"-c", "--config-path"}, required = true)
    private String configPath;

    @Option(name = {"-i", "--input-path"}, required = true)
    private String dataPath;

    @Option(name = "--output-file")
    private String outputFile;

    @Option(name = {"-max", "--max-pages"})
    private int maxPages = Integer.MAX_VALUE;

    public static void main(String[] args) {
        CliTool.run(args, new ComputeDuplications());
    }

    @Override
    public void execute() throws Exception {
        Configuration config = new Configuration(configPath);
        TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();
        TargetRepository repository = TargetRepositoryFactory.create(dataPath, null, null, targetStorageConfig);
        Map<String, Set<String>> contentHashes = computeContentHashes(repository);
        printDups(contentHashes, outputFile);
    }


    private static void printDups(Map<String, Set<String>> contentHashes, String filename)
            throws Exception {

        PrintStream fileWriter = new PrintStream(filename);
        int pages = 0;
        int duplicates = 0;
        for (Entry<String, Set<String>> entry : contentHashes.entrySet()) {
            pages += entry.getValue().size();
            if (entry.getValue().size() > 1) {
                duplicates += entry.getValue().size() - 1; // one of them is the canonical
            }

//            fileWriter.print(entry.getKey());
//            for (String url : entry.getValue()) {
//                fileWriter.print(' ');
//                fileWriter.print(url);
//            }
//            fileWriter.print('\n');

            for (String url : entry.getValue()) {
                fileWriter.print(entry.getValue().size());
                fileWriter.print(' ');
                fileWriter.print(entry.getKey());
                fileWriter.print(' ');
                fileWriter.print(url);
                fileWriter.print('\n');
            }
        }

        System.out.println("    pages: " + pages);
        System.out.println("     dups: " + duplicates);
        System.out.println(
                "dup_ratio: " + String.format("%.2f", 100 * (duplicates / ((double) pages))) + "%");

        fileWriter.close();
    }


    private Map<String, Set<String>> computeContentHashes(
            TargetRepository repository) throws Exception {

        Map<String, Set<String>> contentHashMap = new HashMap<>();
        Iterator<Page> iterator = repository.pagesIterator();
        int i = 0;
        while (iterator.hasNext() && i <= maxPages) {
            i++;
            Page page = iterator.next();

            List<String> contentTypeHeader = page.getResponseHeaders().get("Content-Type");
            if (contentTypeHeader == null) {
                contentTypeHeader = page.getResponseHeaders().get("content-type");
            }

            if (contentTypeHeader == null || contentTypeHeader.size() == 0) {
                continue;
            }

            String contentType = contentTypeHeader.iterator().next();
            if (!contentType.contains("text/html")) {
                continue;
            }

            String text = KeepEverythingExtractor.INSTANCE.getText(page.getContentAsString());
            String contentHash = DigestUtils.md5Hex(text);

            Set<String> dups = contentHashMap.get(contentHash);
            if (dups == null) {
                dups = new HashSet<>();
                contentHashMap.put(contentHash, dups);
            }

            // dups.add(page.getUrl());
            dups.add(page.getFetchTime() + " " + page.getRequestedUrl());
            if (i % 100 == 0) {
                System.out.println("Processed " + i + " pages");
            }
        }
        return contentHashMap;
    }

}
