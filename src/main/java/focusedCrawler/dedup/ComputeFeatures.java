package focusedCrawler.dedup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;
import focusedCrawler.learn.vectorizer.FeatureStackVectorizer;
import focusedCrawler.learn.vectorizer.SparseVector;
import focusedCrawler.learn.vectorizer.UrlAlignmentVectorizer;
import focusedCrawler.learn.vectorizer.Vectorizer;
import focusedCrawler.tokenizers.Tokenizers;

public class ComputeFeatures {

    public static void main(String[] args) throws IOException {

        String inputPath = "data/dedup/";
        String trainFile = inputPath + "crawleval_dups.csv.train";
        String testFile = inputPath + "crawleval_dups.csv.test";

        String outputPath = "data/dedup/output/";
        new File(outputPath).mkdirs();

        System.out.println("Reading training data...");
        List<DupLine> trainFileData = readInputFile(trainFile);

        System.out.println("Grouping duplicates by content hash...");
        Map<String, List<String>> urlsByHash = groupByContentHash(trainFileData);
        List<DupCluster> trainDupClusters = createDupClusters(urlsByHash);

        String trainFileName;
        String testFileName;
        String featuresFileName;
        Vectorizer vectorizer;

        String feature = "stack";

        if ("align".equals(feature)) {
            System.out.println("Fitting URL alignment vectorizer on training data...");

            UrlAlignmentVectorizer alignVectorizer = new UrlAlignmentVectorizer();
            alignVectorizer.fit(urlsByHash);

            trainFileName = outputPath + new File(trainFile).getName() + ".ranklib.align";
            testFileName = outputPath + new File(testFile).getName() + ".ranklib.align";
            featuresFileName = outputPath + new File(trainFile).getName() + ".features.align";

            vectorizer = alignVectorizer;
        } else if ("terms".equals(feature)) {
            System.out.println("Fitting text vectorizer on training data...");

            BinaryTextVectorizer textVectorizer = new BinaryTextVectorizer();
            List<String> urls = toUrlList(trainDupClusters);
            textVectorizer.fit(urls);

            trainFileName = outputPath + new File(trainFile).getName() + ".ranklib.terms";
            testFileName = outputPath + new File(testFile).getName() + ".ranklib.terms";
            featuresFileName = outputPath + new File(trainFile).getName() + ".features.terms";

            vectorizer = textVectorizer;
        } else if ("urlparts".equals(feature)) {
            System.out.println("Fitting URL parser vectorizer on training data...");

            BinaryTextVectorizer parserVectorizer = new BinaryTextVectorizer(Tokenizers.url(), false);
            List<String> urls = toUrlList(urlsByHash);
            parserVectorizer.fit(urls);

            trainFileName = outputPath + new File(trainFile).getName() + ".ranklib.urlparts";
            testFileName = outputPath + new File(testFile).getName() + ".ranklib.urlparts";
            featuresFileName = outputPath + new File(trainFile).getName() + ".features.urlparts";

            vectorizer = parserVectorizer;
        } else if ("stack".equals(feature)) {
            System.out.println("Fitting stacked vectorizers on training data...");

            BinaryTextVectorizer textVectorizer = new BinaryTextVectorizer();
            textVectorizer.fit(toUrlList(trainDupClusters));

            UrlAlignmentVectorizer urlAlignmentVectorizer = new UrlAlignmentVectorizer();
            urlAlignmentVectorizer.fit(urlsByHash);

            BinaryTextVectorizer urlparserVectorizer = new BinaryTextVectorizer(Tokenizers.url(), false);
            urlparserVectorizer.fit(toUrlList(urlsByHash));

            trainFileName = outputPath + new File(trainFile).getName() + ".ranklib.stack";
            testFileName = outputPath + new File(testFile).getName() + ".ranklib.stack";
            featuresFileName = outputPath + new File(trainFile).getName() + ".features.stack";

            vectorizer = new FeatureStackVectorizer(textVectorizer, urlparserVectorizer);

        } else {
            throw new IllegalArgumentException("Invalid feature set");
        }


        System.out.println("Output train file: " + trainFileName);
        System.out.println("Output test file: " + testFileName);
        System.out.println("Features file: " + featuresFileName);
        PrintStream trainOutput = new PrintStream(trainFileName);
        PrintStream testOutput = new PrintStream(testFileName);
        PrintStream featuresOutput = new PrintStream(featuresFileName);

        System.out.println("Vectorizing training data....");
        Map<String, Integer> domainToQidMap = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> qids = new ArrayList<>();
        List<SparseVector> trainingData = new ArrayList<>();
        for (DupLine dup : trainFileData) {

            String domain = new URL(dup.url).getHost();
            Integer domainQid = domainToQidMap.get(domain);
            if (domainQid == null) {
                domainQid = 1;
                // domainQid = domainToQidMap.size()+1;
                domainToQidMap.put(domain, domainQid);
            }

            SparseVector instance = vectorizer.transform(dup.url);
            trainingData.add(instance);
            labels.add(urlsByHash.get(dup.hash).size() > 1 ? "1" : "0");
            qids.add(domainToQidMap.get(domain));
        }

        System.out.println("# of features: " + vectorizer.numberOfFeatures());

        System.out.println("Creating training data file...");
        for (int i = 0; i < trainingData.size(); i++) {
            SparseVector instance = trainingData.get(i);
            String label = labels.get(i);
            Integer qid = qids.get(i);
            trainOutput.println(createRanklibLine(vectorizer, instance, label, qid));
        }

        System.out.println("Printing features file...");
        for (int i = 0; i < vectorizer.numberOfFeatures(); i++) {
            featuresOutput.printf("%d %s\n", i + 1, vectorizer.getFeature(i));
        }

        System.out.println("Vectorizing test data....");
        List<DupLine> testData = readInputFile(testFile);

        for (int i = 0; i < testData.size(); i++) {
            DupLine dup = testData.get(i);

            String label = dup.numOfDups > 1 ? "1" : "0";
            Integer qid = domainToQidMap.get(new URL(dup.url).getHost());

            SparseVector instance = vectorizer.transform(dup.url);

            testOutput.println(createRanklibLine(vectorizer, instance, label, qid));
        }

        featuresOutput.close();
        trainOutput.close();
        testOutput.close();

        System.out.println("done.");
    }

    private static List<String> toUrlList(Map<String, List<String>> urlsByHash) {
        List<String> urls = new ArrayList<>();
        for (Entry<String, List<String>> dup : urlsByHash.entrySet()) {
            for (String url : dup.getValue()) {
                urls.add(url);
            }
        }
        return urls;
    }

    private static List<String> toUrlList(List<DupCluster> trainDupClusters) {
        List<String> urls = new ArrayList<>();
        for (DupCluster dup : trainDupClusters) {
            for (String url : dup.getDupUrls()) {
                urls.add(url);
            }
        }
        return urls;
    }

    private static List<DupCluster> createDupClusters(Map<String, List<String>> urlsByHash) {
        List<DupCluster> clusters = new ArrayList<>();
        for (Entry<String, List<String>> kv : urlsByHash.entrySet()) {
            String contentDigest = kv.getKey();
            List<String> dupUrls = kv.getValue();
            clusters.add(new DupCluster(contentDigest, dupUrls));
        }
        return clusters;
    }

    private static Map<String, List<String>> groupByContentHash(List<DupLine> trainData) {
        Map<String, List<String>> urlsByHash = new HashMap<>();
        for (DupLine d : trainData) {
            List<String> list = urlsByHash.get(d.hash);
            if (list == null) {
                list = new ArrayList<>();
                urlsByHash.put(d.hash, list);
            }
            list.add(d.url);
        }
        return urlsByHash;
    }

    private static String createRanklibLine(Vectorizer vectorizer, SparseVector instance,
            String label, Integer qid) {
        StringBuilder builder = new StringBuilder();
        builder.append(label);
        builder.append(" ");
        builder.append("qid:" + qid);
        for (int idx = 0; idx < vectorizer.numberOfFeatures(); idx++) {
            double value = instance.get(idx);
            builder.append(String.format(" %d:%.6f", idx + 1, value));
        }
        return builder.toString();
    }

    @SuppressWarnings("unused")
    private static class DupLine {
        public int numOfDups;
        public long timestamp;
        public String url;
        public String hash;
    }

    public static List<DupLine> readInputFile(String filename) throws IOException {

        Pattern p = Pattern.compile("^(\\d+) (\\w+) (\\d+) (.*)$");

        List<DupLine> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {

                Matcher matcher = p.matcher(line);
                if (!matcher.find()) {
                    System.out.printf("Failed to match line: %s", line);
                    continue;
                }

                DupLine lineObj = new DupLine();
                lineObj.numOfDups = Integer.parseInt(matcher.group(1));
                lineObj.hash = matcher.group(2);
                lineObj.timestamp = Long.parseLong(matcher.group(3));
                lineObj.url = matcher.group(4);

                lines.add(lineObj);
            }
        }

        return lines;
    }

}
