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

import java.io.IOException;
import java.nio.file.Path;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.target.model.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.SmileUtil;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListFile;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMVector;
import smile.classification.SoftClassifier;
import smile.classification.SVM;

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
public class SmileTargetClassifier implements TargetClassifier {

	private final SoftClassifier<double[]> classifier;
	private final String[] attributes;
	private final StopList stoplist;
    private final double relevanceThreshold;
  
	public SmileTargetClassifier(SoftClassifier<double[]> classifier, double relevanceThreshold, 
			String[] attributes, StopList stoplist) {
		this.classifier = classifier;
		this.relevanceThreshold = relevanceThreshold;
		this.attributes = attributes;
		this.stoplist = stoplist;
	}

	public TargetRelevance classify(Page page) throws TargetClassifierException{
		try{
			double[] classificationResult = distributionForInstance(page);
            if (classificationResult[0] < relevanceThreshold) {
				return TargetRelevance.IRRELEVANT;
			} else {
			    return TargetRelevance.RELEVANT;
			}
		}catch(Exception ex){
			throw new TargetClassifierException(ex.getMessage(), ex);
		}
	}

    private double[] distributionForInstance(Page page) throws TargetClassifierException {
        double[] result = new double[2];
        try {
            double[] values = getValues(page);
            synchronized (classifier) {
                ((SVM<double[]>) classifier).predict(values, result); //predict returns int
            }
        } catch (Exception ex) {
            throw new TargetClassifierException(ex.getMessage(), ex);
        }
        return result;
    }
  
    private double[] getValues(Page page) throws IOException, SAXException {
        VSMVector vsm = new VSMVector(page.getContentAsString(), stoplist, true);
        double[] values = new double[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            VSMElement elem = vsm.getElement(attributes[i]);
            if (elem == null) {
                values[i] = 0;
            } else {
                values[i] = elem.getWeight();
            }
        }
        return values;
    }

	public static TargetClassifier create(String modelPath,
	                                      double relevanceThreshold,
	                                      StopListFile stopwordsFile)
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
            ParameterFile featureConfig = new ParameterFile(featureFile);
            SoftClassifier<double[]> classifier = SmileUtil.loadSmileClassifier(modelFile);
            String[] attributes = featureConfig.getParam("ATTRIBUTES", " ");
            String[] classValues = featureConfig.getParam("CLASS_VALUES", " ");
            return new SmileTargetClassifier(classifier, relevanceThreshold, attributes, stoplist);
    }

    
    static class WekaClassifierConfig {
        public String features_file = "pageclassifier.features";
        public String model_file = "pageclassifier.features";
        public String stopwords_file = null;
        public double relevanceThreshold = 0.5;
    }
    
    public static class Builder {

        public TargetClassifier build(Path basePath, ObjectMapper yaml, JsonNode parameters) throws IOException {

            WekaClassifierConfig params = yaml.treeToValue(parameters, WekaClassifierConfig.class);
            params.model_file = resolveRelativePath(basePath, params.model_file);
            params.features_file = resolveRelativePath(basePath, params.features_file);
            params.stopwords_file = resolveRelativePath(basePath, params.stopwords_file);

            return SmileTargetClassifier.create(
                    params.model_file,
                    params.features_file,
                    params.relevanceThreshold,
                    params.stopwords_file);
        }

        private String resolveRelativePath(Path basePath, String relative) {
            if (relative == null) {
                return null;
            }
            return basePath.resolve(relative).toFile().getAbsolutePath();
        }

    }

}
