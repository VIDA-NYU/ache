package focusedCrawler.learn.classifier.smile;

public interface DoubleVectorizer<T> {

    double[] toInstance(T object);

}
