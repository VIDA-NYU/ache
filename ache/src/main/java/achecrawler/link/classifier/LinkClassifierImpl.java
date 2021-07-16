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
package achecrawler.link.classifier;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;

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

    private final int[] weights = new int[] {2, 1, 0};
	private final LNClassifier lnClassifier;

	public LinkClassifierImpl(LNClassifier lnClassifier) {
		this.lnClassifier = lnClassifier;
	}

  /**
   * This method classifies links based on the priority set by the
   * naive bayes link classifier.
   * @param page Page
   * @return LinkRelevance[]
   * @throws LinkClassifierException
   */
  public LinkRelevance[] classify(Page page) throws LinkClassifierException {
      LinkNeighborhood[] lns = page.getParsedData().getLinkNeighborhood();
      LinkNeighborhood ln = null;
	  try {
	      LinkRelevance[] linkRelevance = new LinkRelevance[lns.length];
		  for (int i = 0; i < lns.length; i++) {
            ln = lns[i];
            linkRelevance[i] = classify(ln);
		  }
		  return linkRelevance;
        } catch (Exception ex) {
            throw new LinkClassifierException("Failed to classify link [" + ln.getLink().toString()
                    + "] from page: " + page.getURL().toString(), ex);
        }
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
		  double result = (classificationResult * 100) + probability;
		  linkRel = new LinkRelevance(ln.getLink(),result);
	  } catch (Exception ex) {
	      throw new LinkClassifierException("Failed to classify link: "+ln.getLink().toString(), ex);
	  }
	  return linkRel;
  }

}
