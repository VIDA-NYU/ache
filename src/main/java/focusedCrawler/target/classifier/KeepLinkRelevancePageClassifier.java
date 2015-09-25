package focusedCrawler.target.classifier;

import focusedCrawler.util.Page;
import focusedCrawler.util.Target;

public class KeepLinkRelevancePageClassifier implements TargetClassifier {

    private TargetClassifier targetClassifier;

    public KeepLinkRelevancePageClassifier(TargetClassifier targetClassifier) {
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
