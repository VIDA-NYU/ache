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
import focusedCrawler.util.Page;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.parser.LinkNeighborhood;

/**
 *
 * <p>Description:This class implements a baseline crawler setting the link
 * relevance according to the page relevance given by the form classsifier.</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class LinkClassifierBaseline implements LinkClassifier{

  private Random randomGenerator;
  private int limit = 100;

  public LinkClassifierBaseline() {
     this.randomGenerator = new Random();
   }

  /**
   * This method classifies pages according to its relevance given by the form.
   *
   * @param page Page
   * @return LinkRelevance[]
   */
  public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {
        LinkRelevance[] linkRelevance = null;
        try {
        	URL[] links = page.links();
        	linkRelevance = new LinkRelevance[links.length];
        	for (int i = 0; i < links.length; i++) {
            	String url = links[i].toString();
        		double relevance = 100;
//        		relevance = page.getRelevance()*100;
        		if(relevance == 100){
        			relevance = relevance + randomGenerator.nextInt(100);
        		}
        		linkRelevance[i] = new LinkRelevance(new URL(url), relevance);
			}
        }
        catch (MalformedURLException ex) {
        	ex.printStackTrace();
        	throw new LinkClassifierException(ex.getMessage());
        }
        return linkRelevance;
  }

  public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException{
	  return null;
  }
  
 }

