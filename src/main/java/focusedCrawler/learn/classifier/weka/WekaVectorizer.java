package focusedCrawler.learn.classifier.weka;

import weka.core.Instance;

public interface WekaVectorizer<T> {

    Instance toInstance(T object);

}
