package achecrawler.target.classifier;

import achecrawler.target.model.Page;

public interface TargetClassifier {

    public TargetRelevance classify(Page page) throws TargetClassifierException;

}
