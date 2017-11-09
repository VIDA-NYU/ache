package focusedCrawler.learn.vectorizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class IndexedVectorizer implements Vectorizer {

    private Map<String, Integer> featureIdx = new HashMap<>();
    private List<String> features = new ArrayList<>();

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

}
