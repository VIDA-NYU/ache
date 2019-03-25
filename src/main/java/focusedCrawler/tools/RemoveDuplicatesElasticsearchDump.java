package focusedCrawler.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import focusedCrawler.minhash.DuplicatePageIndexer;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

@Command(name = "RemoveDuplicatesElasticsearchDump")
public class RemoveDuplicatesElasticsearchDump extends CliTool {

    @Option(name = "--input-file", required = true)
    private String inputFile;

    @Option(name = "--output-file", required = true)
    private String outputFile;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new RemoveDuplicatesElasticsearchDump());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputFile);
        System.out.println("Writing statistics output file at: " + outputFile);

        BufferedReader br;
        if (inputFile.endsWith("gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(inputFile));
            br = new BufferedReader(new InputStreamReader(gzip));
        } else {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

        ObjectMapper mapper = new ObjectMapper();
        DuplicatePageIndexer dedup = new DuplicatePageIndexer.Builder().build();

        int totalPages = 0;
        int dupPages = 0;

        String line;
        while ((line = br.readLine()) != null) {
            JsonNode jsonNode = mapper.readTree(line);
            String text = jsonNode.get("_source").get("text").asText();
            String url = jsonNode.get("_source").get("url").asText();
            boolean isDup = dedup.detectAndIndex(url, text);
            if (isDup) {
                dupPages++;
            } else {
                bw.write(line);
                bw.write('\n');
            }
            totalPages++;
            if (totalPages % 1000 == 0) {
                System.out.printf("processed = %d dup_pages = %d percent_dup = %.3f%%\n", totalPages, dupPages, 100 * (dupPages / (double) totalPages));
            }
        }

        br.close();
        bw.close();
    }

}