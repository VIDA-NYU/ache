package achecrawler.target.classifier;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import achecrawler.target.model.Page;

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

    public static class Builder {

        public TargetClassifier build(Path basePath, ObjectMapper yaml, JsonNode parameters) throws JsonProcessingException, IOException {
            if(parameters != null) {
                TargetClassifier smileClassifier = new SmileTargetClassifier.Builder().build(basePath, yaml, parameters);
                return new KeepLinkRelevanceTargetClassifier(smileClassifier);
            } else {
                return new KeepLinkRelevanceTargetClassifier(null);
            }
        }

    }

}
