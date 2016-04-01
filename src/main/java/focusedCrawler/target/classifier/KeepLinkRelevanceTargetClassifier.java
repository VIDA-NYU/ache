package focusedCrawler.target.classifier;

import focusedCrawler.target.model.Page;

public class KeepLinkRelevanceTargetClassifier implements TargetClassifier {

    private TargetClassifier targetClassifier;

    public KeepLinkRelevanceTargetClassifier(TargetClassifier targetClassifier) {
        this.targetClassifier = targetClassifier;
    }

    @Override
    public TargetRelevance classify(Page page) throws TargetClassifierException {
        if(targetClassifier == null) {
            return new TargetRelevance(true, page.getRelevance());
        }
        else{
            boolean relevant = targetClassifier.classify(page).isRelevant();
            return new TargetRelevance(relevant, page.getRelevance()); 
        }
    }

}
