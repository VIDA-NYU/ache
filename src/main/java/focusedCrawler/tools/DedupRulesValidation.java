package focusedCrawler.tools;

import static java.util.Arrays.asList;

import com.google.common.collect.Lists;
import focusedCrawler.dedup.rules.RewriteRule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class DedupRulesValidation {

    static Map<String, String> urlContent = new HashMap<String, String>();
    static Set<String> dust = new HashSet<String>();

    static final int K = 10;
    static double MAX_FALSE_POSITIVE_RATE = 0.3;
    static int MIN_SUPPORT = 5;

    public static void main(String[] args) throws IOException {

        String[] filename = args;

        for (int i = 0; i < filename.length; i++) {
            System.out.println("========================================");
            System.out.println(filename[i]);
            System.out.println();

            List<String> lines =
                    Files.readAllLines(Paths.get("data", filename[i]), StandardCharsets.UTF_8);
            // Collections.shuffle(lines, new Random(System.nanoTime()));

            int partitionSize = lines.size() / 3;
            List<List<String>> partition = Lists.partition(lines, partitionSize);

            Map<String, List<String>> trainSet = parseLines(partition.get(0));
            Map<String, List<String>> validationSet = parseLines(partition.get(1));
            Map<String, List<String>> testSet = parseLines(partition.get(2));
            System.out.println();

            Map<String, List<String>> trainDupClusters = getDupClusters(trainSet);
            System.out.println("     Train Dup-Clusters: " + trainDupClusters.size());

            Map<String, List<String>> validationDupClusters = getDupClusters(validationSet);
            System.out.println("Validation Dup-Clusters: " + validationDupClusters.size());

            Map<String, List<String>> testDupClusters = getDupClusters(testSet);
            System.out.println("Validation Dup-Clusters: " + testDupClusters.size());

            Set<RewriteRule> candidateRules = createCandidateRules(trainDupClusters);


            List<RewriteRule> validRules = validateRules(validationSet, candidateRules);
            System.out.println("Valid rules: " + validRules.size());

            testRules(testSet, validRules);
        }
    }

    private static Map<String, List<String>> parseLines(List<String> lines) {
        Map<String, List<String>> dataset = new HashMap<>();
        for (String line : lines) {
            List<String> split = asList(line.split(" "));
            String contentHash = split.get(0);
            List<String> urls = split.subList(1, split.size());
            dataset.put(contentHash, urls);
            if (urls.size() > 1) {
                for (String url : urls) {
                    dust.add(url);
                }
            }
        }
        System.out.println("Number of lines: " + dataset.size());
        return dataset;
    }

    private static Map<String, List<String>> getDupClusters(Map<String, List<String>> urlSet) {
        Map<String, List<String>> dupClusters = new HashMap<>();
        for (Entry<String, List<String>> entry : urlSet.entrySet()) {
            List<String> urls = entry.getValue();
            if (urls.size() > 1) {
                dupClusters.put(entry.getKey(), entry.getValue());
                // System.out.println(entry.getKey() +" - "+ entry.getValue());
            }
        }

        return dupClusters;
    }

    private static Set<RewriteRule> createCandidateRules(Map<String, List<String>> trainSet) {
        Set<RewriteRule> rules = new HashSet<>();
        for (Entry<String, List<String>> entry : trainSet.entrySet()) {
            // String contentHash = entry.getKey();
            // System.out.println("Creating rule for: "+contentHash);
            List<String> urls = entry.getValue();
            rules.add(new RewriteRule(urls, K));
            // System.out.println(rule);
        }
        return rules;
    }

    private static List<RewriteRule> validateRules(Map<String, List<String>> validationSet,
            Set<RewriteRule> candidateRules) {

        System.out.println("\n=== VALIDATION ===");

        List<RewriteRule> validRules = new ArrayList<>();
        Map<String, Set<String>> canonicalTable = new HashMap<>();

        for (RewriteRule rule : candidateRules) {

            int totalOriginal = 0;
            int support = 0;
            int fpp = 0;
            Set<String> normalizedUrls = new HashSet<>();

            for (Entry<String, List<String>> entry : validationSet.entrySet()) {
                List<String> urls = entry.getValue();
                for (String url : urls) {
                    totalOriginal++;
                    String canonical = url;
                    // System.out.println("mathces: "+url);
                    // String boom =
                    // "https://www.ar15.com/forums/t_1_2/1854214_____________________________________________________________________________________________________.html";
                    // !url.equals(boom ) &&
                    if (rule.matches(url)) {
                        canonical = rule.rewrite(url);
                    }
                    if (!canonical.equals(url)) {
                        Set<String> dups = canonicalTable.get(canonical);
                        if (dups == null) {
                            dups = new HashSet<>();
                            canonicalTable.put(canonical, dups);
                        }
                        dups.add(url);
                    }
                    // System.out.println("done.");
                    normalizedUrls.add(canonical);
                }
            }

            for (Entry<String, Set<String>> canonicalEntry : canonicalTable.entrySet()) {
                String canonical = canonicalEntry.getKey();
                // System.out.println("canonical"+canonical);
                Set<String> bucket = canonicalEntry.getValue();
                if (bucket.size() > 1) {
                    for (String original : bucket) {
                        support++;
                        if (!dust.contains(original) && !dust.contains(canonical)) {
                            fpp++;
                        }
                    }
                }
            }

            double reduction = (totalOriginal - normalizedUrls.size()) / (double) totalOriginal;
            double fpr = support > 0 ? fpp / (double) support : 0;

            if (support >= MIN_SUPPORT) {
                if (fpr <= MAX_FALSE_POSITIVE_RATE) {
                    validRules.add(rule);
                    System.out.println();
                    System.out.println(rule);
                    System.out.println("  original: " + totalOriginal);
                    System.out.println("normalized: " + normalizedUrls.size());
                    System.out.println("   support: " + support);
                    System.out.println("       fpr: " + fpr);
                    System.out.println(" reduction: " + reduction);
                }
            }
            System.out.println();
        }
        return validRules;
    }

    private static void testRules(Map<String, List<String>> testSet, List<RewriteRule> validRules) {
        System.out.println("\n=== TEST ===");
        Set<String> normalizedUrls = new HashSet<>();
        int totalOriginal = 0;
        int fpp = 0;
        for (Entry<String, List<String>> entry : testSet.entrySet()) {
            List<String> urls = entry.getValue();
            for (String url : urls) {
                totalOriginal++;
                String normalized = normalize(validRules, url);
                if (!normalized.equals(url)) {
                    if (!dust.contains(url)) {
                        fpp++;
                        // System.out.println("WRONG:");
                        // System.out.println("orig: "+url);
                        // System.out.println("norm: "+normalized);
                    } else {
                        // System.out.println("CORRECT:");
                        // System.out.println("orig: "+url);
                        // System.out.println("norm: "+normalized);
                    }
                    // System.out.println();
                }
                normalizedUrls.add(normalized);
            }
        }

        double fpr = normalizedUrls.size() > 0 ? fpp / (double) normalizedUrls.size() : 0;

        System.out.println();
        System.out.println("  original: " + totalOriginal);
        System.out.println("normalized: " + normalizedUrls.size());
        System.out.println(
                " reduction: " + (totalOriginal - normalizedUrls.size()) / (double) totalOriginal);
        System.out.println("       fpr: " + fpr);
        System.out.println();
    }

    private static String normalize(List<RewriteRule> validRules, String url) {
        String normalized = url;
        for (RewriteRule rule : validRules) {
            if (rule.matches(url)) {
                normalized = rule.rewrite(url);
            }
        }
        return normalized;
    }

}
