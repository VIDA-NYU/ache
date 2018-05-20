package focusedCrawler.learn.vectorizer;

import focusedCrawler.tokenizers.Tokenizer;
import focusedCrawler.tokenizers.Tokenizers;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;

public class BinaryTextVectorizer extends IndexedVectorizer {

    private static final int ALPHA = 1;

    private final Tokenizer tokenizer;
    private final boolean quadraticFeatures;
    private final int ngramSize;
    private final int minDocFrequency;
    private final int maxFeatures;
    private final WeightType weightType;

    private Object2IntMap<String> docFrequency;
    private Object2IntMap<String> class0Frequency;
    private Object2IntMap<String> class1Frequency;
    private double[] nbLogRatio;

    public BinaryTextVectorizer() {
        this(new Builder());
    }

    public BinaryTextVectorizer(Tokenizer tokenizer, boolean quadraticFeatures) {
        this(new Builder()
                .withTokenizer(tokenizer)
                .withQuadraticFeatures(quadraticFeatures));
    }

    public BinaryTextVectorizer(Builder builder) {
        this.tokenizer = builder.tokenizer;
        this.quadraticFeatures = builder.quadraticFeatures;
        this.ngramSize = builder.ngramSize;
        this.minDocFrequency = builder.minDocFrequency;
        this.maxFeatures = builder.maxFeatures;
        this.weightType = builder.weightType;
        this.docFrequency = new Object2IntOpenHashMap<>();
        if (weightType == WeightType.NB_LOG_RATIO) {
            this.class0Frequency = new Object2IntOpenHashMap<>();
            this.class1Frequency = new Object2IntOpenHashMap<>();
        }
    }

    public void fit(List<String> trainingData) {
        this.fit(trainingData, null);
    }


    public void fit(List<String> trainingData, List<Integer> labels) {
        if (labels == null && weightType == WeightType.NB_LOG_RATIO) {
            throw new IllegalArgumentException("Labels are required for NB weights.");
        }
        if (labels != null && labels.size() != trainingData.size()) {
            throw new IllegalArgumentException("Training data and labels must have same size.");
        }

        for (int i = 0; i < trainingData.size(); i++) {
            String text = trainingData.get(i);
            List<String> tokens = tokenizer.tokenize(text);
            Set<String> features = createFeatures(tokens);
            int label = (labels == null) ? 0 : labels.get(i);
            countTokens(features, label);
        }

        Set<String> allFeatures = selectFeatures();

        // index feature in super class
        for (String feature : allFeatures) {
            super.addFeature(feature);
        }

        if (weightType == WeightType.NB_LOG_RATIO) {
            computeNaiveBayesLogRatios();
        }

        // not needed anymore
        this.docFrequency = null;
        this.class0Frequency = null;
        this.class1Frequency = null;
    }

    /**
     * Selects features that occur at least minDocFrequency times, and at most maxFeatures.
     * If number of features exceed maxFeatures, features are selected based on document frequency.
     */
    public Set<String> selectFeatures() {

        List<TermCount> termCounts = new ArrayList<>();
        for (Object2IntMap.Entry<String> entry : this.docFrequency.object2IntEntrySet()) {
            termCounts.add(new TermCount(entry.getKey(), entry.getIntValue()));
        }

        termCounts.sort(TermCount.DESC_ORDER_COMPARATOR);

        Set<String> selectedFeatures = new HashSet<>();
        for (TermCount termCount : termCounts) {
            // prune by minimum document frequency
            if (termCount.count >= this.minDocFrequency) {
                selectedFeatures.add(termCount.term);
            }
            // prune by max feature count
            if (selectedFeatures.size() >= this.maxFeatures) {
                break;
            }
        }

        return selectedFeatures;
    }

    /**
     * Compute naive bayes log-ratio weights for the features.
     * Reference: Sida Wang and Christopher D. Manning. Baselines and bigrams: simple,
     * good sentiment and topic classification. ACL, 2012.
     */
    private void computeNaiveBayesLogRatios() {
        int d = numberOfFeatures();
        double[] p = new double[d];
        double[] q = new double[d];
        double sumP = 0d;
        double sumQ = 0d;
        for (int i = 0; i < d; i++) {
            p[i] = ALPHA + class0Frequency.getInt(getFeature(i));
            q[i] = ALPHA + class1Frequency.getInt(getFeature(i));
            sumP += p[i];
            sumQ += q[i];
        }
        for (int i = 0; i < d; i++) {
            p[i] = p[i] / sumP;
            q[i] = q[i] / sumQ;
        }
        double[] r = new double[d];
        for (int i = 0; i < d; i++) {
            r[i] = Math.log(p[i] / q[i]);
        }
        this.nbLogRatio = r;
    }

    private void countTokens(Set<String> features, int label) {

        for (String feature : features) {
            // Compute term document frequency
            int df = docFrequency.getInt(feature); // equals zero if absent
            docFrequency.put(feature, df + 1);

            // Compute per-class term doc frequencies for Naive Bayes log-ratio feature weights
            if (weightType == WeightType.NB_LOG_RATIO) {
                Object2IntMap<String> classFrequency;
                if (label == 0) {
                    classFrequency = class0Frequency;
                } else if (label == 1) {
                    classFrequency = class1Frequency;
                } else {
                    throw new IllegalArgumentException(
                            "NB log-ratio weights are supported only for binary classes");
                }
                int cf = classFrequency.getInt(feature);
                classFrequency.put(feature, cf + 1);
            }
        }
    }

    private Set<String> createFeatures(List<String> tokens) {
        if (!quadraticFeatures && ngramSize < 2) {
            // no need to generate additional features
            return new HashSet<>(tokens);
        }

        Set<String> features = new HashSet<>(tokens);

        // Generate quadratic features (pairwise occurrences)
        if (quadraticFeatures) {
            for (int i = 0; i < tokens.size(); i++) {
                for (int j = i + 1; j < tokens.size(); j++) {
                    features.add(tokens.get(i) + "_" + tokens.get(j));
                }
            }
        }

        // Generates n-grams up to size 4
        if (ngramSize > 0) {
            String t = null;
            String l = null;
            String ll = null;
            String lll;

            for (int i = 0, size = tokens.size(); i < size; ++i) {
                lll = ll;
                ll = l;
                l = t;
                t = tokens.get(i);
                if (t.isEmpty()) {
                    continue;
                }
                if (ngramSize > 1 && l != null) {
                    // 2-gram
                    features.add(l + "_" + t);
                    if (ngramSize > 2 && ll != null) {
                        // 3-gram
                        features.add(ll + "_" + l + "_" + t);
                        if (ngramSize > 3 && lll != null) {
                            // 4-gram
                            features.add(lll + "_" + ll + "_" + l + "_" + t);
                        }
                    }
                }
            }
        }
        return features;
    }

    @Override
    public SparseVector transform(String text) {
        List<String> tokens = tokenizer.tokenize(text);
        Set<String> features = createFeatures(tokens);
        Set<String> knownFeatures = filterFeatures(features);
        if (weightType == WeightType.BINARY) {
            return SparseVector.binary(knownFeatures, this);
        } else if (weightType == WeightType.NB_LOG_RATIO) {
            Object2DoubleMap<String> featureWeights = new Object2DoubleOpenHashMap<>();
            for (String f : knownFeatures) {
                featureWeights.put(f, this.nbLogRatio[getIndexOfFeature(f)]);
            }
            return SparseVector.weights(featureWeights, this);
        } else {
            throw new IllegalStateException("Feature count type not supported");
        }
    }

    private Set<String> filterFeatures(Set<String> features) {
        Set<String> result = new HashSet<>(features.size());
        for (String f : features) {
            if (super.contains(f)) {
                result.add(f);
            }
        }
        return result;
    }

    public static class Builder {

        public int ngramSize = 0;
        private Tokenizer tokenizer = Tokenizers.alphaNumeric();
        private boolean quadraticFeatures = false;
        private int minDocFrequency = 0;
        private int maxFeatures = 5000;
        private WeightType weightType = WeightType.BINARY;

        public Builder withTokenizer(Tokenizer tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        public Builder withQuadraticFeatures(boolean quadraticFeatures) {
            this.quadraticFeatures = quadraticFeatures;
            return this;
        }

        public Builder withNgramSize(int ngramSize) {
            this.ngramSize = ngramSize;
            return this;
        }

        public Builder withMinDocFrequency(int minDocFrequency) {
            this.minDocFrequency = minDocFrequency;
            return this;
        }

        public Builder withMaxFeatures(int maxFeatures) {
            this.maxFeatures = maxFeatures;
            return this;
        }

        public Builder withWeightType(WeightType weightType) {
            this.weightType = weightType;
            return this;
        }

        public BinaryTextVectorizer build() {
            return new BinaryTextVectorizer(this);
        }

    }

    public enum WeightType {
        BINARY, // Simple binary weights
        NB_LOG_RATIO // Naive Bayes log-ratio (from NB-SVM)
    }

    private static class TermCount {

        private static Comparator<TermCount> DESC_ORDER_COMPARATOR = (o1, o2) -> Integer.compare(o2.count, o1.count);

        private String term;
        private int count;

        public TermCount(String term, int count) {
            this.term = term;
            this.count = count;
        }

    }

}
