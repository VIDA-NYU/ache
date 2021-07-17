package achecrawler.learn.classifier.smile;

public interface DoubleVectorizer<T> {

    double[] toDoubleVector(T object);

}
