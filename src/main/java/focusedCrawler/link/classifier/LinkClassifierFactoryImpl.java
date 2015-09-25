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
package focusedCrawler.link.classifier;

import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.string.StopList;


import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import weka.core.Instances;
import weka.classifiers.Classifier;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Description: Creates concrete LinkClassifiers</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class LinkClassifierFactoryImpl implements LinkClassifierFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkClassifierFactoryImpl.class);

  private ParameterFile config;
  
  private static StopList stoplist;

  public LinkClassifierFactoryImpl(String stoplistFile, ParameterFile linkStorageConfig) {
    this.config = linkStorageConfig;
    try {
		stoplist = new StopListArquivo(stoplistFile);
	} catch (IOException e) {
		throw new IllegalArgumentException("Could not load stopwords from file: "+stoplistFile, e);
	}
  }


  /**
   * This method creates a concrete LinkClassifier
   * @param className String class name of the LinkClassifier
   * @return LinkClassifier
   * @throws LinkClassifierFactoryException
   */
   public LinkClassifier createLinkClassifier(String className) throws LinkClassifierFactoryException {
	   LinkClassifier linkClassifier = null;
	    try {
	        linkClassifier = setClassifier(className);
	        logger.info("LINK_CLASSIFIER: " + linkClassifier.getClass());
	    }
	    catch (IOException ex) {
	        throw new LinkClassifierFactoryException(ex.getMessage(), ex);
	    }
	    catch (ClassNotFoundException ex) {
	        throw new LinkClassifierFactoryException(ex.getMessage(), ex);
	    }
	    return linkClassifier;
  }


  public LinkClassifier setClassifier(String className) throws IOException, ClassNotFoundException{
	  LinkClassifier linkClassifier = null;
      if(className.indexOf("LinkClassifierBreadthSearch") != -1){
    	  String[] attributes = config.getParam("ATTRIBUTES", " ");
    	  WrapperNeighborhoodLinks wrapper = loadWrapper(attributes);
    	  linkClassifier= new LinkClassifierBreadthSearch(wrapper,attributes);
      }
      if(className.indexOf("LinkClassifierBaseline") != -1){
    	  linkClassifier= new LinkClassifierBaseline();
      }
	  if(className.indexOf("LinkClassifierHub") != -1){
		  linkClassifier = new LinkClassifierHub();
	  }
	  if(className.indexOf("LinkClassifierAuthority") != -1){
		  linkClassifier = new LinkClassifierAuthority();
	  }
	  if(className.indexOf("LinkClassifierImpl") != -1){
    	  String[] attributes = config.getParam("ATTRIBUTES", " ");
    	  WrapperNeighborhoodLinks wrapper = loadWrapper(attributes);
		  String[] classValues = config.getParam("CLASS_VALUES", " ");
    	  weka.core.FastVector vectorAtt = new weka.core.FastVector();
    	  for (int i = 0; i < attributes.length; i++) {
    		  vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
    	  }
    	  weka.core.FastVector classAtt = new weka.core.FastVector();
    	  for (int i = 0; i < classValues.length; i++) {
    		  classAtt.addElement(classValues[i]);
    	  }
    	  vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
    	  Instances insts = new Instances("link_classification", vectorAtt, 1);
    	  System.out.println("SIZE" + attributes.length);
    	  insts.setClassIndex(attributes.length);
    	  Classifier classifier = loadClassifier();
    	  LNClassifier lnClassifier = new LNClassifier(classifier, insts, wrapper, attributes);
    	  linkClassifier = new LinkClassifierImpl(lnClassifier,config.getParamInt("LEVEL"));  
      }
	  if(className.indexOf("MaxDepthLinkClassifier") != -1){
	      linkClassifier = new MaxDepthLinkClassifier(1);
	  }
	  return linkClassifier;  
  }
  
  public static WrapperNeighborhoodLinks loadWrapper(String[] attributes) throws IOException{
	  
      WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);
      wrapper.setFeatures(attributes);
      return wrapper;
  }
  
  private Classifier loadClassifier() throws IOException, ClassNotFoundException{
	  InputStream is = null;
	  try {
		  is = new FileInputStream(config.getParam("FILE_CLASSIFIER"));
	  }
	  catch (FileNotFoundException ex1) {
		  ex1.printStackTrace();
	  }
	  ObjectInputStream objectInputStream = new ObjectInputStream(is);
	  Classifier classifier = (Classifier) objectInputStream.readObject();
	  objectInputStream.close();
	  return classifier;
  }
  
  public static LinkClassifier createLinkClassifierImpl(String[] attributes, String[] classValues, Classifier classifier, String className, int levels) throws IOException {
	  LinkClassifier linkClassifier = null;
	  WrapperNeighborhoodLinks wrapper = loadWrapper(attributes);
	  weka.core.FastVector vectorAtt = new weka.core.FastVector();
	  for (int i = 0; i < attributes.length; i++) {
		  vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
	  }
	  weka.core.FastVector classAtt = new weka.core.FastVector();
	  for (int i = 0; i < classValues.length; i++) {
		  classAtt.addElement(classValues[i]);
	  }
	  vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
	  Instances insts = new Instances("link_classification", vectorAtt, 1);
	  System.out.println("SIZE" + attributes.length);
	  insts.setClassIndex(attributes.length);
	  if(className.indexOf("LinkClassifierImpl") != -1){
		  LNClassifier lnClassifier = new LNClassifier(classifier, insts, wrapper, attributes);
		  linkClassifier = new LinkClassifierImpl(lnClassifier,3);
	  }
	  if(className.indexOf("LinkClassifierAuthority") != -1){
		  linkClassifier = new LinkClassifierAuthority(classifier, insts, wrapper,attributes);
	  }
	  if(className.indexOf("LinkClassifierHub") != -1){
		  linkClassifier = new LinkClassifierHub(classifier, insts, wrapper,attributes);
	  }
	  return linkClassifier;
  }
}

