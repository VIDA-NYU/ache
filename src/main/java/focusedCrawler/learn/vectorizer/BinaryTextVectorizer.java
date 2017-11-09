package focusedCrawler.learn.vectorizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import focusedCrawler.dedup.DupCluster;
import focusedCrawler.util.AlphaNumTokenizer;

public class BinaryTextVectorizer extends IndexedVectorizer {

    public boolean quadraticFeatures = false;

    public BinaryTextVectorizer() {}

    public BinaryTextVectorizer(boolean quadraticFeatures) {
        this.quadraticFeatures = quadraticFeatures;
    }

    public void fit(List<DupCluster> trainData) {
        for (DupCluster dup : trainData) {
            for (String url : dup.getDupUrls()) {
                partialFit(AlphaNumTokenizer.parseTokens(url));
            }
        }
    }

    public void partialFit(List<String> tokens) {
        List<String> features = createFeatures(tokens);
        for (String feature : features) {
            super.addFeature(feature);
        }
    }

    private List<String> createFeatures(List<String> instanceFeatures) {
        if (quadraticFeatures) {
            List<String> features = new ArrayList<>(instanceFeatures);
            for (int i = 0; i < instanceFeatures.size(); i++) {
                for (int j = i + 1; j < instanceFeatures.size(); j++) {
                    features.add(instanceFeatures.get(i) + instanceFeatures.get(j));
                }
            }
            return features;
        } else {
            return instanceFeatures;
        }
    }

    public SparseVector fitTransform(List<String> instanceFeatures) {
        List<String> features = createFeatures(instanceFeatures);
        partialFit(features);
        return SparseVector.binary(features, this);
    }

    @Override
    public SparseVector transform(String url) {
        List<String> instanceFeatures = AlphaNumTokenizer.parseTokens(url);;
        return transform(instanceFeatures);
    }

    public SparseVector transform(List<String> instance) {
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
