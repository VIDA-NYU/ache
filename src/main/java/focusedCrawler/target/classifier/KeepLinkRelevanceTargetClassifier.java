package focusedCrawler.target.classifier;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.Target;

public class KeepLinkRelevanceTargetClassifier implements TargetClassifier {

    private TargetClassifier targetClassifier;

    public KeepLinkRelevanceTargetClassifier(TargetClassifier targetClassifier) {
        this.targetClassifier = targetClassifier;
    }

    @Override
    public TargetRelevance classify(Target target) throws TargetClassifierException {
        Page page = (Page) target;
        if(targetClassifier == null) {
            return new TargetRelevance(true, page.getRelevance());
        }
        else{
            boolean relevant = targetClassifier.classify(target).isRelevant();
            return new TargetRelevance(relevant, page.getRelevance()); 
        }
    }

}
