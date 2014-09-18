/*
############################################################################
##
## Copyright (C) 2006-2009 University of Utah. All rights reserved.
##
## This file is part of DeepPeep.
##
## This file may be used under the terms of the GNU General Public
## License version 2.0 as published by the Free Software Foundation
## and appearing in the file LICENSE.GPL included in the packaging of
## this file.  Please review the following to ensure GNU General Public
## Licensing requirements will be met:
## http://www.opensource.org/licenses/gpl-license.php
##
## If you are unsure which license is appropriate for your use (for
## instance, you are interested in developing a commercial derivative
## of DeepPeep), please contact us at deeppeep@sci.utah.edu.
##
## This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
## WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
##
############################################################################
*/
package focusedCrawler.link.classifier.util;

import java.util.HashMap;

/**
 * <p>Description: An instance represents the features used by the
 * link classifier to classify a link</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class Instance {

  private HashMap featureHash;
  private String[] features;
  private double[] values;
  private double valueClassification;

  public Instance(String[] features) {
    this.setFeatures(features);
  }

  public void setClassification(String feat, double value){
    this.valueClassification = value;
  }

  public void setFeatures(String[] features){
    this.features = features;
    values = new double[features.length];
    featureHash = new HashMap(features.length);
    for (int i = 0; i < features.length; i++) {
      featureHash.put(features[i],new Integer(i));
    }
  }

  public void setValue(String feat, Double val){
    Integer index = (Integer)featureHash.get(feat);
    if(index != null){
      values[index.intValue()] = val.doubleValue();
    }
  }

  public String[] getFeatures(){
    return features;
  }

  public double[] getValues(){
    return values;
  }

  public HashMap getHash(){
    return featureHash;
  }

  public boolean checkFeature(String feat){
    boolean exist = false;
    if(featureHash.get(feat) != null){
      exist = true;
    }
    return exist;
  }

  public String toString(){

    StringBuffer temp = new StringBuffer();
    for (int i = 0; i < features.length; i++) {
//      if(values[i] > 0 && features[i].indexOf("text") == -1 && features[i].indexOf("title") == -1){

//        temp.append(features[i]);

//        temp.append(" ");

//      }

      temp.append(features[i]);
      temp.append(" ");

      temp.append((int)values[i]);
      temp.append(",");
    }

//    temp.append(attributeClassification);

//    temp.append(" ");
    temp.append((int)valueClassification);
    return temp.toString();
  }

}

