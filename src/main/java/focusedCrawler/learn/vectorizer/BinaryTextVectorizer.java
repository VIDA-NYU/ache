package focusedCrawler.learn.vectorizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import focusedCrawler.tokenizers.Tokenizer;
import focusedCrawler.tokenizers.Tokenizers;

public class BinaryTextVectorizer extends IndexedVectorizer {

    private final Tokenizer tokenizer;
    private final boolean quadraticFeatures;

    public BinaryTextVectorizer() {
        this(Tokenizers.alphaNumeric(), false);
    }

    public BinaryTextVectorizer(Tokenizer tokenizer, boolean quadraticFeatures) {
        this.tokenizer = tokenizer;
        this.quadraticFeatures = quadraticFeatures;
    }

    public void fit(List<String> trainingData) {
        for (String text : trainingData) {
            partialFit(text);
        }
    }

    public void partialFit(String text) {
        List<String> tokens = tokenizer.tokenize(text);
        partialFit(tokens);
    }

    private void partialFit(List<String> tokens) {
        List<String> features = createFeatures(tokens);
        for (String feature : features) {
            super.addFeature(feature);
        }
    }

    private List<String> createFeatures(List<String> tokens) {
        if (quadraticFeatures) {
            List<String> features = new ArrayList<>(tokens);
            for (int i = 0; i < tokens.size(); i++) {
                for (int j = i + 1; j < tokens.size(); j++) {
                    features.add(tokens.get(i) + tokens.get(j));
                }
            }
            return features;
        } else {
            return tokens;
        }
    }

    public SparseVector fitTransform(String text) {
        List<String> tokens = tokenizer.tokenize(text);
        List<String> features = createFeatures(tokens);
        this.partialFit(features);
        return SparseVector.binary(features, this);
    }

    @Override
    public SparseVector transform(String url) {
        List<String> instanceFeatures = tokenizer.tokenize(url);;
        return transform(instanceFeatures);
    }

    private SparseVector transform(List<String> instance) {
        List<String> features = createFeatures(instance);
        List<String> knownFeatures = filterFeatures(features);
        return SparseVector.binary(knownFeatures, this);
    }

    private List<String> filterFeatures(List<String> features) {
        List<String> copy = new ArrayList<>(features);
        Iterator<String> it = copy.iterator();
        while (it.hasNext()) {
            if (!super.contains(it.next()))
                it.remove();
        }
        return copy;
    }

}
