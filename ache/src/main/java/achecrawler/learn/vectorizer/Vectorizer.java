package achecrawler.learn.vectorizer;

public interface Vectorizer {

    public SparseVector transform(String text);

    public int getIndexOfFeature(String feature);

    public int numberOfFeatures();

    public String getFeature(int i);

}
