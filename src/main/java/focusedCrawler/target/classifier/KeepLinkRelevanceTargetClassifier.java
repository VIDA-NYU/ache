package focusedCrawler.target.classifier;

import focusedCrawler.target.model.Page;

public class KeepLinkRelevanceTargetClassifier implements TargetClassifier {

    private TargetClassifier targetClassifier;

    public KeepLinkRelevanceTargetClassifier(TargetClassifier targetClassifier) {
        this.targetClassifier = targetClassifier;
    }

    @Override
    public TargetRelevance classify(Page page) throws TargetClassifierException {
        
        double pageRelevance = page.getLinkRelevance().getRelevance();
        
        boolean isRelevant;
        if(targetClassifier == null) {
            isRelevant = true;
        } else {
            isRelevant = targetClassifier.classify(page).isRelevant();
        }
        
        return new TargetRelevance(isRelevant, pageRelevance); 
    }

}
