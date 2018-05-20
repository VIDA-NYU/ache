package focusedCrawler.learn.vectorizer;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a sparse vector using a map of feature index to feature values.
 * 
 * @author aeciosantos
 *
 */
public class SparseVector {

    private final Int2DoubleMap features;

    public SparseVector() {
        this.features = new Int2DoubleOpenHashMap();
        this.features.defaultReturnValue(0d);
    }

    public double get(String feature, Vectorizer vectorizer) {
        int idx = vectorizer.getIndexOfFeature(feature);
        return idx < 0 ? 0d : get(idx);
    }

    /**
     * Returns the values of the given index. If index does not exist, return zero as default value
     */
    public double get(int idx) {
        return features.get(idx);
    }

    /**
     * Horizontally stack features, i.e, creates a new vector by appending all entries of the given
     * vector to this vector. New indexes of entries added are computed based on the current number
     * of features in this vector.
     * 
     * @param vector a SparseVector containing the features to be added to this vector.
     */
    public void hstack(SparseVector vector) {
        int size = this.features.size();
        for (Int2DoubleMap.Entry entry : vector.features.int2DoubleEntrySet()) {
            int key = entry.getKey() + size;
            double value = entry.getValue();
            this.features.put(key, value);
        }
    }

    public static SparseVector binary(List<String> features, Vectorizer vectorizer) {
        return binary(new HashSet<>(features), vectorizer);
    }

    public static SparseVector binary(Set<String> features, Vectorizer vectorizer) {
        SparseVector vector = new SparseVector();
        for (String f : features) {
            vector.features.put(vectorizer.getIndexOfFeature(f), 1.0d);
        }
        return vector;
    }

    public static SparseVector weights(Object2DoubleMap<String> featureWeights, Vectorizer vectorizer) {
        SparseVector vector = new SparseVector();
        for (Object2DoubleMap.Entry<String> kv : featureWeights.object2DoubleEntrySet()) {
            String feature = kv.getKey();
            double weight = kv.getDoubleValue();
            vector.features.put(vectorizer.getIndexOfFeature(feature), weight);
        }
        return vector;
    }

    public double[] toDoubleVector(Vectorizer textVectorizer) {
        int n = textVectorizer.numberOfFeatures();
        double[] vector = new double[n];
        for (int i = 0; i < n; i++) {
            vector[i] = get(i);
        }
        return vector;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SparseVector{");
        for (Int2DoubleMap.Entry kv : features.int2DoubleEntrySet()) {
            sb.append(kv.getIntKey());
            sb.append(":");
            sb.append(kv.getDoubleValue());
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
