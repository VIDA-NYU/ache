package focusedCrawler.target.classifier;

import focusedCrawler.target.model.Page;

public interface TargetClassifier {

    public TargetRelevance classify(Page page) throws TargetClassifierException;

}
