package achecrawler.link.classifier.builder;

import java.util.HashMap;

/**
 * <p>
 * Description: An instance represents the features used by the link classifier to classify a link
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class Instance {

    private HashMap<String, Integer> featureHash;
    private String[] features;
    private double[] values;

    public Instance(String[] features) {
        this.setFeatures(features);
    }

    public void setFeatures(String[] features) {
        this.features = features;
        this.values = new double[features.length];
        this.featureHash = new HashMap<>(features.length);
        for (int i = 0; i < features.length; i++) {
            this.featureHash.put(features[i], i);
        }
    }

    public void setValue(String feat, Double val) {
        Integer index = featureHash.get(feat);
        if (index != null) {
            values[index] = val;
        }
    }

    public String[] getFeatures() {
        return features;
    }

    public double[] getValues() {
        return values;
    }

    public HashMap<String, Integer> getHash() {
        return featureHash;
    }

    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < features.length; i++) {
            temp.append(features[i]);
            temp.append(" ");
            temp.append((int) values[i]);
            temp.append(",");
        }
        return temp.toString();
    }

}
