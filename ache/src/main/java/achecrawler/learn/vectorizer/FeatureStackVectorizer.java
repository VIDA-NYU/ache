package achecrawler.learn.vectorizer;

import java.util.List;

public class FeatureStackVectorizer extends IndexedVectorizer {

    private Vectorizer[] vectorizers;

    public FeatureStackVectorizer(Vectorizer... vectorizers) {
        this.vectorizers = vectorizers;
        for (Vectorizer v : vectorizers) {
            for (int i = 0; i < v.numberOfFeatures(); i++) {
                super.addFeature(v.getFeature(i));
            }
        }
    }

    @Override
    public SparseVector transform(String url) {
        SparseVector stackedVector = new SparseVector();
        for (Vectorizer v : vectorizers) {
            stackedVector.hstack(v.transform(url));
        }
        return stackedVector;
    }

    @Override
    public void fit(List<String> trainingData) {
        // no-op
    }

}
