package achecrawler.target.classifier;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TargetRelevance {

    public static TargetRelevance RELEVANT = new TargetRelevance(true, 1.0);
    public static TargetRelevance IRRELEVANT = new TargetRelevance(false, 0.0);

    @JsonProperty("relevance")
    private double relevance;

    @JsonProperty("isRelevant")
    private boolean isRelevant;

    public TargetRelevance() {
        // required for JSON deserialization
    }

    public TargetRelevance(boolean isRelevant, double relevance) {
        this.isRelevant = isRelevant;
        this.relevance = relevance;
    }

    public double getRelevance() {
        return relevance;
    }

    @JsonProperty("isRelevant")
    public boolean isRelevant() {
        return isRelevant;
    }

    @Override
    public String toString() {
        return "TargetRelevance [relevance=" + relevance + ", isRelevant=" + isRelevant + "]";
    }

}
