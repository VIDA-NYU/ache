package focusedCrawler.dedup;

import focusedCrawler.learn.vectorizer.BinaryTextVectorizer.WeightType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier;
import focusedCrawler.learn.classifier.smile.SmileOnlineClassifier.Learner;
import focusedCrawler.learn.vectorizer.BinaryTextVectorizer;
import focusedCrawler.learn.vectorizer.FeatureStackVectorizer;
import focusedCrawler.learn.vectorizer.HashingVectorizer;
import focusedCrawler.learn.vectorizer.IndexedVectorizer;
import focusedCrawler.learn.vectorizer.UrlAlignmentVectorizer;
import focusedCrawler.tokenizers.Tokenizers;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "RunOnlineDedupClassifier", description = "")
public class RunOnlineDedupClassifier extends CliTool {

    private static final int NOT_DUPLICATE = 0;
    private static final int DUPLICATE = 1;
    static int[] classes = new int[] {NOT_DUPLICATE, DUPLICATE};

    public enum Features {
        TERMS, TERMS_HASHING, RULES, URL_TOKENS, ALL
    }

    @Option(name = {"-i", "--input-path"}, required = false)
    String inputPath = "/home/aeciosantos/workdata/dedup/";

    @Option(name = {"--train"}, required = false)
    String trainFile = inputPath + "crawleval_dups.csv.train";

    @Option(name = {"--test"}, required = false)
    String testFile = inputPath + "crawleval_dups.csv.test";

    @Option(name = {"--timestamp"}, required = false)
    boolean timestamp = true;

    @Option(name = {"--learner"}, required = false)
    Learner learner = Learner.SVM;

    @Option(name = {"--features"}, required = false)
    Features features = Features.TERMS;

    @Option(name = "--output-file", description = "The output file", required = false)
    private String outputFile = "/home/aeciosantos/workdata/dedup/smile.";

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new RunOnlineDedupClassifier());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading training data...");
        List<DupLine> trainFileData = readInputFile(trainFile, timestamp);
        System.out.println("training data size: " + trainFileData.size());

        System.out.println("Grouping duplicates by content hash...");
        Map<String, List<String>> urlsByHash = groupByContentHash(trainFileData);

        List<String> trainingData = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        int dupCount = 0;
        int nodupCount = 0;
        for (DupLine dup : trainFileData) {
            int numberOfDups = urlsByHash.get(dup.hash).size();
            int instanceClass = numberOfDups > 1 ? DUPLICATE : NOT_DUPLICATE;
            double percentNoDup = nodupCount / (double) (dupCount + nodupCount);
            if (instanceClass == NOT_DUPLICATE && percentNoDup > 0.5d) {
                continue;
            }
            if (instanceClass == DUPLICATE) {
                dupCount++;
            } else {
                nodupCount++;
            }
            trainingData.add(dup.url);
            labels.add(instanceClass);
        }


        System.out.println("Training vectorizer...");
        IndexedVectorizer vectorizer = createVectorizer(urlsByHash, trainingData, labels);

        System.out.println("Building model...");
        String[] featuresArray = vectorizer.getFeaturesAsArray();
        SmileOnlineClassifier<String> classifier = new SmileOnlineClassifier<>(
                learner, featuresArray, classes, vectorizer);
        classifier.buildModel(trainingData, labels);


        System.out.println("Reading test data...");
        List<DupLine> testFileData = readInputFile(testFile, timestamp);
        System.out.println("test data size: " + testFileData.size());

        System.out.println("Testing model...");
        evaluate(classifier, testFileData);

        System.out.println("Sorting by predition...");
        testFileData.sort((DupLine d1, DupLine d2) -> {
            return Double.compare(d2.dupProb, d1.dupProb); // reverse
        });

        System.out.println("Writring results file...");

        FileWriter f = new FileWriter(outputFile + learner + ".txt");
        for (DupLine d : testFileData) {
            String actualClass = d.numOfDups > 1 ? "0" : "1";
            f.write(actualClass);
            f.write(" qid:1 1:0.0\n");
        }
        f.close();
    }

    private IndexedVectorizer createVectorizer(Map<String, List<String>> urlsByHash,
            List<String> trainingData, List<Integer> labels) {
        IndexedVectorizer vectorizer;
//        BinaryTextVectorizer termsVectorizer =
//                new BinaryTextVectorizer(Tokenizers.alphaNumeric(), true);
        BinaryTextVectorizer termsVectorizer = new BinaryTextVectorizer.Builder()
                .withQuadraticFeatures(true)
                .withTokenizer(Tokenizers.alphaNumeric())
                .withWeightType(WeightType.NB_LOG_RATIO)
                .withMaxFeatures(1000)
                .withMinDocFrequency(10)
                .build();
        HashingVectorizer termsHashingVectorizer =
                new HashingVectorizer(Tokenizers.alphaNumeric(), 12, true);
        UrlAlignmentVectorizer rulesVectorizer = new UrlAlignmentVectorizer();
//        BinaryTextVectorizer urlTokensVectorizer = new BinaryTextVectorizer(Tokenizers.url(), true);
        BinaryTextVectorizer urlTokensVectorizer = new BinaryTextVectorizer.Builder()
                .withQuadraticFeatures(true)
                .withTokenizer(Tokenizers.url())
                .withWeightType(WeightType.NB_LOG_RATIO)
                .withMaxFeatures(1000)
                .withMinDocFrequency(10)
                .build();
        switch (features) {
            case TERMS:
                termsVectorizer.fit(trainingData, labels);
                vectorizer = termsVectorizer;
                break;
            case TERMS_HASHING:
                termsHashingVectorizer.fit(trainingData);
                vectorizer = termsHashingVectorizer;
                break;
            case RULES:
                rulesVectorizer.fit(urlsByHash);
                vectorizer = rulesVectorizer;
                break;
            case URL_TOKENS:
                urlTokensVectorizer.fit(trainingData);
                vectorizer = urlTokensVectorizer;
                break;
            case ALL:
                termsVectorizer.fit(trainingData, labels);
                rulesVectorizer.fit(urlsByHash);
                urlTokensVectorizer.fit(trainingData, labels);
                FeatureStackVectorizer all = new FeatureStackVectorizer(termsVectorizer,
                        rulesVectorizer, urlTokensVectorizer);
                vectorizer = all;
                break;
            default:
                throw new IllegalArgumentException("Invalid feature set");
        }
        return vectorizer;
    }

    private static void evaluate(SmileOnlineClassifier<String> classifier,
            List<DupLine> testFileData) {
        int dupCount = 0;
        int nodupCount = 0;
        int total = 0, tp = 0, fp = 0, tn = 0, fn = 0;
        for (DupLine dup : testFileData) {

            // int numberOfDups = urlsByHash.get(dup.hash).size();
            int numberOfDups = dup.numOfDups;
            int actualClass = numberOfDups > 1 ? DUPLICATE : NOT_DUPLICATE;

            //
            // Class balancing
            //
            double percentNoDup = nodupCount / (double) (dupCount + nodupCount);
            // System.out.printf("Percent NoDup: %.4f\n", percentNoDup);
            if (actualClass == NOT_DUPLICATE && percentNoDup > 0.5d) {
                continue;
            }

            if (actualClass == DUPLICATE) {
                dupCount++;
            } else {
                nodupCount++;
            }

            //
            // Metrics
            //
            double[] result = classifier.classify(dup.url);
            double score = result[0];
            dup.dupProb = score;

            int prediction = score > 0.5d ? NOT_DUPLICATE : DUPLICATE;
            // System.out.printf("%.4f %s %s\n", score, prediction, actualClass);
            if (prediction == DUPLICATE && actualClass == DUPLICATE) {
                tp++;
            } else if (prediction == DUPLICATE && actualClass == NOT_DUPLICATE) {
                // predicted as duplicate, but was not_duplicate
                fp++;
            } else if (prediction == NOT_DUPLICATE && actualClass == NOT_DUPLICATE) {
                tn++;
            } else if (prediction == NOT_DUPLICATE && actualClass == DUPLICATE) {
                // predicted as not_duplicate, but was duplicate
                fn++;
            }

            total++;
            if (total % 5000 == 0) {
                printMetrics(tp, fp, tn, fn);
            }

        }
        System.out.println("Final result:");
        printMetrics(tp, fp, tn, fn);
        System.out.printf("tp: %d fp: %d fn: %d tn: %d\n", tp, fp, fn, tn);
    }

    private static void printMetrics(int tp, int fp, int tn, int fn) {
        double precision = (tp / (double) (tp + fp));
        double recall =  (tp / (double) (tp + fn));
        double f1 = 2 * precision * recall / (precision + recall);
        double acc = (tp + tn) / (double)(tp + fp + tn + fn);
        double fdr = fp / (double) (fp + tp);
        double npv = tn / (double) (tn + fn);
        System.out.printf(
                "f1: %.4f acc: %.4f precision: %.4f recall: %.4f fdr: %.4f npv: %.4f\n",
                f1, acc, precision, recall, fdr, npv);
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

    @SuppressWarnings("unused")
    private static class DupLine {
        public int numOfDups;
        public long timestamp;
        public String url;
        public String hash;
        public double dupProb;
    }

    public static List<DupLine> readInputFile(String filename, boolean timestamp)
            throws IOException {

        List<DupLine> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ");

                int size = 3;
                if (timestamp) {
                    size += 1;
                }

                if (split.length != size) {
                    System.out.printf("Failed to match line: %s\n", line);
                    continue;
                }

                DupLine lineObj = new DupLine();
                int i = 0;
                lineObj.numOfDups = Integer.parseInt(split[i++]);
                lineObj.hash = split[i++];
                if (timestamp) {
                    lineObj.timestamp = Long.parseLong(split[i++]);
                }
                lineObj.url = split[i++];

                lines.add(lineObj);

            }
        }
        return lines;
    }


}
