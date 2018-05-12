package focusedCrawler.learn.vectorizer;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;

import focusedCrawler.learn.classifier.smile.DoubleVectorizer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public abstract class IndexedVectorizer implements Vectorizer, DoubleVectorizer<String> {

    private static final int ABSENT_VALUE = -1;

    private final Object2IntMap<String> featureIdx;
    private final List<String> features = new ArrayList<>();

    public IndexedVectorizer() {
        this.featureIdx = new Object2IntOpenHashMap<>();
        this.featureIdx.defaultReturnValue(ABSENT_VALUE);
    }

    abstract public void fit(List<String> trainingData);

    public void addFeature(String feature) {
        int idx = featureIdx.getInt(feature);
        if (idx == ABSENT_VALUE) {
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
        return featureIdx.getInt(feature);
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
