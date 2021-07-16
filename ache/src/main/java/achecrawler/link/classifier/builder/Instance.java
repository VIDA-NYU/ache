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
    private double valueClassification;

    public Instance(String[] features) {
        this.setFeatures(features);
    }

    public void setClassification(String feat, double value) {
        this.valueClassification = value;
    }

    public void setFeatures(String[] features) {
        this.features = features;
        this.values = new double[features.length];
        this.featureHash = new HashMap<String, Integer>(features.length);
        for (int i = 0; i < features.length; i++) {
            this.featureHash.put(features[i], new Integer(i));
        }
    }

    public void setValue(String feat, Double val) {
        Integer index = (Integer) featureHash.get(feat);
        if (index != null) {
            values[index.intValue()] = val.doubleValue();
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

    public boolean checkFeature(String feat) {
        boolean exist = false;
        if (featureHash.get(feat) != null) {
            exist = true;
        }
        return exist;
    }

    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < features.length; i++) {
            temp.append(features[i]);
            temp.append(" ");
            temp.append((int) values[i]);
            temp.append(",");
        }
        temp.append((int) valueClassification);
        return temp.toString();
    }

}
