package focusedCrawler.target.classifier;

import focusedCrawler.util.Target;

public abstract class BaseTargetClassifier implements TargetClassifier {
    
    @Override
    public abstract boolean classify(Target target) throws TargetClassifierException;
    
    @Override
    public double[] distributionForInstance(Target target) throws TargetClassifierException {
        double ontopicPropability;
        double offTopicPropability;

        boolean ontopic = classify(target);
        if (ontopic) {
            ontopicPropability = 1d;
            offTopicPropability = 0d;
        } else {
            ontopicPropability = 0d;
            offTopicPropability = 1d;
        }

        double[] result = new double[2];
        result[0] = ontopicPropability;
        result[1] = offTopicPropability;
        return result;
    }
    
}
