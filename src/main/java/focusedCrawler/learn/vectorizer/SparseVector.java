package focusedCrawler.learn.vectorizer;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;

/**
 * Represents a sparse vector using a map of feature index to feature values.
 * 
 * @author aeciosantos
 *
 */
public class SparseVector {

    private Int2DoubleMap features = new Int2DoubleRBTreeMap();

    public double get(String feature, Vectorizer vectorizer) {
        int idx = vectorizer.getIndexOfFeature(feature);
        return idx < 0 ? 0d : get(idx);
    }

    public double get(int idx) {
        Double value = features.get(idx);
        return value == null ? 0d : value;
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
        SparseVector vector = new SparseVector();
        for (String f : features) {
            vector.features.put(vectorizer.getIndexOfFeature(f), 1.0d);
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

}
