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
package focusedCrawler.target;

import focusedCrawler.util.Page;
import focusedCrawler.util.Target;
import weka.core.Instances;
import weka.classifiers.Classifier;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMVector;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

import org.xml.sax.SAXException;

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
public class TargetClassifierImpl implements TargetClassifier {

	private Classifier classifier;
	private Instances instances;
	private String[] attributes;
	private StopList stoplist;
  
	public TargetClassifierImpl(Classifier classifier, Instances instances, String[] attributes, StopList stoplist){
		this.classifier = classifier;
		this.instances = instances;
		this.attributes = attributes;
		this.stoplist = stoplist;
	}

	public boolean classify(Target target) throws TargetClassifierException{
		boolean relevant = false;
		try{
			double[] values = getValues(target);
			weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
			instanceWeka.setDataset(instances);
			double classificationResult = classifier.classifyInstance(instanceWeka);
			if (classificationResult == 0) {
				relevant = true;
			}
			else {
				relevant = false;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw new TargetClassifierException(ex.getMessage());
		}
		return relevant;
	}

	public double[] distributionForInstance(String target) throws TargetClassifierException{
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

  
	public double[] distributionForInstance(Target target) throws TargetClassifierException{
		double[] result = null;
	    try{
	    	double[] values = getValues(target);
	    	weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
	    	instanceWeka.setDataset(instances);
	    	result = classifier.distributionForInstance(instanceWeka);
	    }catch(Exception ex){
	    	ex.printStackTrace();
	    	throw new TargetClassifierException(ex.getMessage());
	    }
	    return result;
	}
  
	private double[] getValues(Target target) throws IOException, SAXException {
		VSMVector vsm = null;
		vsm = new VSMVector(target.getSource(),stoplist,true);

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

	public static TargetClassifier loadClassifier(String cfg) throws IOException, ClassNotFoundException{
		ParameterFile config = new ParameterFile(cfg);
		StopList stoplist = new StopListArquivo(config.getParam("STOPLIST_FILES"));
		InputStream is = new FileInputStream(config.getParam("FILE_CLASSIFIER"));
		ObjectInputStream objectInputStream = new ObjectInputStream(is);
		Classifier classifier = (Classifier) objectInputStream.readObject();
		String[] attributes = config.getParam("ATTRIBUTES", " ");
		System.out.println(attributes.length);
		weka.core.FastVector vectorAtt = new weka.core.FastVector();
		for (int i = 0; i < attributes.length; i++) {
			vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
		}
		String[] classValues = config.getParam("CLASS_VALUES", " ");
		weka.core.FastVector classAtt = new weka.core.FastVector();
		for (int i = 0; i < classValues.length; i++) {
			classAtt.addElement(classValues[i]);
		}
		vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
		Instances insts = new Instances("target_classification", vectorAtt, 1);
		insts.setClassIndex(attributes.length);
		return new TargetClassifierImpl(classifier, insts, attributes, stoplist);
	}
  
	public static void main(String[] args) {
  		try{
  			TargetClassifier targetClassifier = TargetClassifierImpl.loadClassifier(args[0]);
  		}catch(Exception ex){
  			ex.printStackTrace();
  		}
	}
  
}
