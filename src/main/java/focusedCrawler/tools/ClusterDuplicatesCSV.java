package focusedCrawler.tools;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import focusedCrawler.crawler.crawlercommons.filters.basic.BasicURLNormalizer;
import focusedCrawler.dedup.DupDetector.DupData;
import focusedCrawler.minhash.DuplicatePageIndexer;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

@Command(name = "ClusterDuplicatesCSV", description = "Clusters near duplicate records in a CSV file.")
public class ClusterDuplicatesCSV extends CliTool {

    private static final Set<String> BAD_QUERY_ELEMENTS = ImmutableSet.of(
        "utm_campaign",
        "utm_medium",
        "utm_source",
        "fb_ref"
    );

    @Option(name = "--input-file", required = true)
    private String inputFile;

    @Option(name = "--output-file", required = true)
    private String outputFile;

    @Option(name = "--similarity")
    double similarity = 0.8;

    @Option(name = "--samples")
    int samples = 1000000;

    @Option(name = "--shingles")
    int shingles = 9;

    @Option(name = "--hashes")
    int hashes = 512;

    @Option(name = "--text-header")
    private String textHeader = "text";

    @Option(name = "--url-header")
    private String urlHeader = "url";

    @Option(name = "--timestamp-header")
    private String timestampHeader = "timestamp";

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new ClusterDuplicatesCSV());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from file: " + inputFile);
        System.out.println("Base output file: " + outputFile);

        BufferedReader br;
        if (inputFile.endsWith("gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(inputFile));
            br = new BufferedReader(new InputStreamReader(gzip));
        } else {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        }


        // CSV columns
        final String header = br.readLine();
        final List<String> headers = parseHeaders(header);
        final int URL = findHeaderPosition(headers, urlHeader);
        final int TEXT = findHeaderPosition(headers, textHeader);
        final int TIMESTAMP = findHeaderPosition(headers, timestampHeader);

        DuplicatePageIndexer dedup = new DuplicatePageIndexer.Builder()
                .setMinJaccardSimilarity(similarity)
                .setNumberOfSamples(samples)
                .setNumberOfShingles(shingles)
                .setNumberOfHashes(hashes)
                .build();

        Map<String, CSVRecord> pages = new HashMap<>();
        Set<String> fingerprints = new HashSet<>();

        BasicURLNormalizer normalizer = new BasicURLNormalizer(BAD_QUERY_ELEMENTS, true);

        int processed = 0;
        int duplicates = 0;
        CSVParser csvParser = CSVFormat.DEFAULT.withHeader(header).parse(br);

        for (CSVRecord record : csvParser) {
            String text = record.get(TEXT);
            String url = record.get(URL);

            String normalizedUrl = normalizer.filter(url);
            String domain = new URL(normalizedUrl).getHost();
            String sha1 = Hashing.sha1().hashString(domain + text, StandardCharsets.UTF_8).toString();
            if (normalizedUrl == null) {
                System.out.println("Skipping invalid URL=[" + url + "]");
                continue;
            }
            if (pages.containsKey(normalizedUrl) || fingerprints.contains(sha1) ) {
                duplicates++;
            } else {
                dedup.detectAndIndex(normalizedUrl, text);
                pages.put(normalizedUrl, record);
                fingerprints.add(sha1);
            }
            processed++;
            if (processed % 1000 == 0) {
                System.out.printf("processed=%d duplicates=%d current_url=%s\n", processed, duplicates, normalizedUrl);
            }
        }
        System.out.printf("processed = %d duplicates=%d (done)\n", processed, duplicates);

        printNormalizedUrlMappingsFile(URL, pages);

        DupData dupData = dedup.getDuplicationSample();
        System.out.println("Duplicate clusters: " + dupData.duplicates.size());
        System.out.println("Unique pages: " + dupData.unique.size());

        processed = 0;
        List<List<String>> dupClusters = dupData.duplicates;

        System.out.printf("Sorting %d duplicate clusters\n", dupClusters.size());
        dupClusters.sort((list1, list2) -> Integer.compare(list2.size(), list1.size()));

        System.out.println("Sorting URLs from each dup cluster by time");
        for (List<String> duplicateUrls : dupClusters) {
            duplicateUrls.sort((String url1, String url2) -> {
                String timestampUrl1 = pages.get(url1).get(TIMESTAMP);
                String timestampUrl2 = pages.get(url2).get(TIMESTAMP);
                return timestampUrl1.compareTo(timestampUrl2);
            });
        }

        System.out.println("Printing dup clusters to file...");
        int clusterId = 0;


        try (PrintStream f = new PrintStream(outputFile)) {

            List<String> newHeaders = new ArrayList<>();
            newHeaders.add("cluster_id");
            newHeaders.add("normalized_url");
            newHeaders.addAll(headers);

            String[] newHeadersArray = newHeaders.toArray(new String[]{});

            CSVPrinter csvPrinter = new CSVPrinter(f, CSVFormat.DEFAULT.withHeader(newHeadersArray));

            for (List<String> duplicateUrls : dupClusters) {
                for (String normalizedUrl : duplicateUrls) {

                    // add two new fields before
                    List<String> values = new ArrayList<>();
                    values.add(String.valueOf(clusterId));
                    values.add(normalizedUrl);

                    // add remaining CSV fields
                    CSVRecord record = pages.get(normalizedUrl);
                    for (int i = 0; i < record.size(); i++) {
                        values.add(record.get(i));
                    }
                    csvPrinter.printRecord(values);
                }
                clusterId++;
                processed++;
                if (processed % 100 == 0) {
                    System.out.printf("processed = %d\n", processed);
                }
            }
        }

        System.out.println("done.");
    }

    private List<String> parseHeaders(String headerLine) {
        String[] headers = headerLine.split(",");
        List<String> headersList = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i];
            if (h.startsWith("\"") && h.endsWith("\"")) {
                h = h.substring(1, h.length() - 1);
            }
            System.out.printf("Detected column header \"%s\" at position %d\n", h, i);
            headersList.add(h);
        }
        return headersList;
    }

    private void printNormalizedUrlMappingsFile(int URL, Map<String, CSVRecord> pages)
            throws FileNotFoundException {
        try (PrintStream urlsFile = new PrintStream(inputFile + ".normalized-urls.tsv")) {
            for (Map.Entry<String, CSVRecord> kv : pages.entrySet()) {
                String normalizedUrl = kv.getKey();
                String url = pages.get(normalizedUrl).get(URL);
                urlsFile.print(url);
                urlsFile.print("\t");
                urlsFile.print(normalizedUrl);
            }
        }
    }

    private int findHeaderPosition(List<String> headers, String headerName) {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (headerName.equals(header)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find CSV header named: " + headerName);
    }

}