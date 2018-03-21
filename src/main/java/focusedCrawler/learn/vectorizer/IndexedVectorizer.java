package focusedCrawler.learn.vectorizer;

import java.util.ArrayList;
import java.util.List;

import focusedCrawler.learn.classifier.smile.DoubleVectorizer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;

public abstract class IndexedVectorizer implements Vectorizer, DoubleVectorizer<String> {

    private Object2IntMap<String> featureIdx = new Object2IntRBTreeMap<>();
    private List<String> features = new ArrayList<>();

    abstract public void fit(List<String> trainingData);

    public void addFeature(String feature) {
        Integer idx = featureIdx.get(feature);
        if (idx == null) {
            this.featureIdx.put(feature, featureIdx.size());
            this.features.add(feature);
        }
    }

    @Override
    public int numberOfFeatures() {
        return featureIdx.size();
    }

    @Override
    public String getFeature(int idx) {
        return features.get(idx);
    }

    @Override
    public int getIndexOfFeature(String feature) {
        Integer idx = featureIdx.get(feature);
        return idx == null ? -1 : idx;
    }

    public boolean contains(String feature) {
        return featureIdx.containsKey(feature);
    }

    public List<String> getFeatures() {
        return features;
    }

    public String[] getFeaturesAsArray() {
        return (String[]) features.toArray(new String[features.size()]);
    }

    @Override
    public double[] toDoubleVector(String text) {
        SparseVector vector = transform(text);
        return vector.toDoubleVector(this);
    }

}
