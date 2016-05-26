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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;
import weka.classifiers.Classifier;
import weka.core.Instances;


public class LinkClassifierRootPage implements LinkClassifier{

  private LinkNeighborhoodWrapper wrapper;
  private String[] attributes;
  private Classifier classifier;
  private Instances instances;

  
  public LinkClassifierRootPage(LinkNeighborhoodWrapper wrapper,String[] attribute) {
     this.wrapper = wrapper;
     this.attributes = attribute;
   }

  
  public LinkClassifierRootPage(Classifier classifier, Instances instances, LinkNeighborhoodWrapper wrapper,String[] attribute) {
	  this(wrapper,attribute);
	  this.classifier = classifier;
	  this.instances = instances;
  }

//  private int countTemp = 0;
  /**
   * classify
   *
   * @param page Page
   * @return LinkRelevance[]
   */
  public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {
        LinkRelevance[] linkRelevance;
        Map<String, Instance> urlWords;
        try {
          urlWords = wrapper.extractLinks(page, attributes);
          linkRelevance = new LinkRelevance[urlWords.size()];
          Iterator<String> iter = urlWords.keySet().iterator();
          int count = 0;
          while (iter.hasNext()) {
            String urlStr = (String) iter.next();
            URL url = new URL(urlStr);
	        Instance instance = (Instance)urlWords.get(urlStr);
	        double[] values = instance.getValues();
	        weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
	        instanceWeka.setDataset(instances);
	        double[] prob = classifier.distributionForInstance(instanceWeka);
            double relevance = -1;

            if(page.getRelevance() > 100 && page.getRelevance() < 200){
            	if(isInitialPage(urlStr) && !page.getURL().getHost().equals(url.getHost())){
            		relevance = 200 + (prob[0]*100);
            		url = new URL(url.getProtocol(), url.getHost(), "/");
            	}
            }

            linkRelevance[count] = new LinkRelevance(url, relevance);
//            System.out.println(url.toString() + ":" + relevance);
            count++;
            }
        }
        catch (MalformedURLException ex) {
          ex.printStackTrace();
          throw new LinkClassifierException(ex.getMessage());
        } catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			throw new LinkClassifierException(ex.getMessage());
		}
        return linkRelevance;
  }


  public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException{
	  LinkRelevance linkRel = null;
	  try{
	      Map<String, Instance> urlWords = wrapper.extractLinks(ln, attributes);
	      Iterator<String> iter = urlWords.keySet().iterator();
	      while(iter.hasNext()){
	        String url = (String)iter.next();
	        Instance instance = (Instance)urlWords.get(url);
	        double[] values = instance.getValues();
	        weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
	        instanceWeka.setDataset(instances);
	        double[] prob = classifier.distributionForInstance(instanceWeka);
	        double relevance;
	        relevance = 200 + (prob[0]*100);	
	        linkRel = new LinkRelevance(new URL(url),relevance);
	      }
	  } catch (MalformedURLException ex) {
		  ex.printStackTrace();
		  throw new LinkClassifierException(ex.getMessage());
	  } catch (Exception ex) {
		  ex.printStackTrace();
		  throw new LinkClassifierException(ex.getMessage());
	  }
	  return linkRel;
  }

  private boolean isInitialPage(String urlStr) throws MalformedURLException {
     boolean result = false;
     URL url = new URL(urlStr);
     String file = url.getFile();
     if(file.equals("/") || file.equals("")){
       result = true;
     }
     return result;
   }
}

