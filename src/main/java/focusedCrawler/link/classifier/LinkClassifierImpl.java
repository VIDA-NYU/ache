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

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.classifier.util.Instance;

import weka.classifiers.Classifier;
import weka.core.Instances;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.string.StopList;
import java.io.IOException;
import focusedCrawler.util.parser.LinkNeighborhood;

/**
 *
 * <p> </p>
 *
 * <p>Description: This classifier uses the naive bayes link classifier to
 * set the link priority.</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class LinkClassifierImpl implements LinkClassifier{

	private int[] weights;
	private int intervalRandom = 100;
	private LNClassifier lnClassifier;
  

	public LinkClassifierImpl(LNClassifier lnClassifier, int level) {

		this.weights = new int[]{2,1,0};
		this.lnClassifier = lnClassifier;
	}

//  public String[] getFeatures(){
//    return attributes;
//  }


  /**
   * This method classifies links based on the priority set by the
   * naive bayes link classifier.
   * @param page Page
   * @return LinkRelevance[]
   * @throws LinkClassifierException
   */

  public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {
	  LinkRelevance[] linkRelevance = null;
	  try {
		  LinkNeighborhood[] lns = page.getLinkNeighboor();
		  linkRelevance = new LinkRelevance[lns.length];
		  for (int i = 0; i < lns.length; i++) {
			  linkRelevance[i] = classify(lns[i]);
		  }
	  }catch(Exception ex){
		  ex.printStackTrace();
		  throw new LinkClassifierException(ex.getMessage());
	  }
	  return linkRelevance;
  }

  public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {

	  LinkRelevance linkRel = null;
	  try {
		  double[] prob = lnClassifier.classify(ln);
		  int classificationResult = -1;
		  double maxProb = -1;
		  for (int i = 0; i < prob.length; i++) {
			  if(prob[i] > maxProb){
				  maxProb = prob[i];
				  classificationResult = i;
			  }
		  }
		  double probability = prob[classificationResult]*100;
		  if(probability == 100){
			  probability = 99;
		  }
		  classificationResult = weights[classificationResult];
		  double result = (classificationResult * intervalRandom) + probability ;  	
		  linkRel = new LinkRelevance(ln.getLink(),result);
	  }catch (MalformedURLException ex) {
		  ex.printStackTrace();
		  throw new LinkClassifierException(ex.getMessage());
	  }catch (Exception ex) {
		  ex.printStackTrace();
		  throw new LinkClassifierException(ex.getMessage());
	  }
	  return linkRel;
  }

}
