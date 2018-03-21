package focusedCrawler.learn.classifier.smile;

public interface DoubleVectorizer<T> {

    double[] toDoubleVector(T object);

}
