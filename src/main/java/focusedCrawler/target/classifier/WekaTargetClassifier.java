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
package focusedCrawler.target.classifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.xml.sax.SAXException;

import weka.classifiers.Classifier;
import weka.core.Instances;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListFile;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMVector;

/**
 * <p> </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class WekaTargetClassifier implements TargetClassifier {

	private final Classifier classifier;
	private final Instances instances;
	private final String[] attributes;
	private final StopList stoplist;
    private final double relevanceThreshold;
  
	public WekaTargetClassifier(Classifier classifier, double relevanceThreshold,
	                            Instances instances, String[] attributes, StopList stoplist){
		this.classifier = classifier;
        this.relevanceThreshold = relevanceThreshold;
		this.instances = instances;
		this.attributes = attributes;
		this.stoplist = stoplist;
	}

	public TargetRelevance classify(Page page) throws TargetClassifierException{
		try{
			double[] classificationResult = distributionForInstance(page);
			final double relevanceProbability = classificationResult[0];
            if (relevanceProbability > relevanceThreshold) {
				return new TargetRelevance(true, relevanceProbability);
			} else {
			    return new TargetRelevance(false, relevanceProbability);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw new TargetClassifierException(ex.getMessage());
		}
	}

	public double[] distributionForInstance(String target) throws TargetClassifierException {
		double[] result = null;
		try{
			double[] values = getValues(new Page(null,target));
			weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
			instanceWeka.setDataset(instances);
			result = classifier.distributionForInstance(instanceWeka);
		}catch(Exception ex){
			ex.printStackTrace();
			throw new TargetClassifierException(ex.getMessage());
	    }
		return result;
	}

  
	public double[] distributionForInstance(Page page) throws TargetClassifierException{
		double[] result = null;
	    try{
	    	double[] values = getValues(page);
	    	weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
	    	instanceWeka.setDataset(instances);
	    	result = classifier.distributionForInstance(instanceWeka);
	    }catch(Exception ex){
	    	ex.printStackTrace();
	    	throw new TargetClassifierException(ex.getMessage());
	    }
	    return result;
	}
  
	private double[] getValues(Page page) throws IOException, SAXException {
		VSMVector vsm = null;
		vsm = new VSMVector(page.getSource(),stoplist,true);

		double[] values = new double[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			VSMElement elem = vsm.getElement(attributes[i]);
			if (elem == null) {
				values[i] = 0;
			}else{
				values[i] = elem.getWeight();
			}
		}
		return values;
	}

	public static TargetClassifier create(String modelPath,
	                                      double relevanceThreshold,
	                                      String stopwordsFile)
                                          throws IOException {
	    return create(modelPath + "/pageclassifier.model",
	                  modelPath + "/pageclassifier.features",
	                  relevanceThreshold,
	                  stopwordsFile);
	    
	}
	
    public static TargetClassifier create(String modelFile,
                                          String featureFile,
                                          double relevanceThreshold,
                                          String stopwordsFile)
                                          throws IOException {
        StopListFile stoplist;
        if(stopwordsFile != null && !stopwordsFile.isEmpty()) {
            stoplist = new StopListFile(stopwordsFile);
        } else {
            stoplist = StopListFile.DEFAULT;
        }
        return create(modelFile, featureFile, relevanceThreshold, stoplist);
    }
	
    public static TargetClassifier create(String modelFile,
                                          String featureFile,
                                          double relevanceThreshold,
                                          StopList stoplist)
                                          throws IOException {
        try {
            ParameterFile featureConfig = new ParameterFile(featureFile);

            InputStream is = new FileInputStream(modelFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            Classifier classifier = (Classifier) objectInputStream.readObject();
            is.close();

            String[] attributes = featureConfig.getParam("ATTRIBUTES", " ");
            weka.core.FastVector vectorAtt = new weka.core.FastVector();
            for (int i = 0; i < attributes.length; i++) {
                vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
            }
            String[] classValues = featureConfig.getParam("CLASS_VALUES", " ");
            weka.core.FastVector classAtt = new weka.core.FastVector();
            for (int i = 0; i < classValues.length; i++) {
                classAtt.addElement(classValues[i]);
            }
            vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
            Instances insts = new Instances("target_classification", vectorAtt, 1);
            insts.setClassIndex(attributes.length);
            
            return new WekaTargetClassifier(classifier, relevanceThreshold, insts, attributes, stoplist);

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not find file: " + modelFile, e);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not deserialize classifier from file:"+modelFile, e);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not load classifier.", e);
        }
    }
  
}
