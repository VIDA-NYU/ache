package focusedCrawler.tools;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.args4j.Option;

import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.CliTool;

public class CountTlds extends CliTool {

    @Option(name = "--input-file",
            required = true,
            usage = "Path to file containing one URL per line")
    private String inputPath;

    @Option(name = "--output-file",
            required = true,
            usage = "Text file with TLD counts")
    private String outputFile;


    public static void main(String[] args) throws Exception {
        CliTool.run(args, new CountTlds());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputPath);
        System.out.println("Writing output file at: " + outputFile);

        int processedPages = 0;
        Map<String, Integer> tldCounts = new HashMap<String, Integer>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = br.readLine()) != null) {

                String tld = new LinkRelevance(line, 0).getTopLevelDomainName();

                Integer tldCount = tldCounts.get(tld);
                if (tldCount == null) {
                    tldCount = new Integer(0);
                }

                tldCount++;

                tldCounts.put(tld, tldCount);

                processedPages++;
                if (processedPages % 1000 == 0) {
                    System.out.printf("Counted %s pages...\n", processedPages);
                }

            }
        }

        try (PrintWriter out = new PrintWriter(new FileOutputStream(outputFile), true)) {
            for (Entry<String, Integer> count : tldCounts.entrySet()) {
                out.printf("%s %d\n", count.getKey(), count.getValue());
            }
        }

        System.out.printf("Finished processing %d pages.\n", processedPages);
    }

}
