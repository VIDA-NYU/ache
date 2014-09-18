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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.parser.LinkNeighborhood;

/**
 *
 * <p> </p>
 *
 * <p>Description: This classifier implements some heuristics to
 * set priority of links. </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class LinkClassifierHeuristic implements LinkClassifier{

  private WrapperNeighborhoodLinks wrapper;
  private String[] attributes;
  private Random randomGenerator;
  private int maxRandom = 10000;
  private  int[]  weights = new int[]{8,4,2,1};

  public LinkClassifierHeuristic(WrapperNeighborhoodLinks wrapper,String[] attributes) {
    this.wrapper = wrapper;
    this.attributes = attributes;
    this.randomGenerator = new Random();
  }

  /**
   * This method classifies links from a given page based on some heuristics.
   *
   * @param page Page
   * @return LinkRelevance[]
   */

  public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {

    LinkRelevance[] linkRelevance = null;
    try{
      HashMap urlWords = wrapper.extractLinks(page, attributes);
      linkRelevance = new LinkRelevance[urlWords.size()];
      Iterator iter = urlWords.keySet().iterator();
      int count = 0;
      while(iter.hasNext()){
        String urlStr = (String)iter.next();
        Instance instance = (Instance)urlWords.get(urlStr);
        double resultClassification = randomGenerator.nextInt(maxRandom);
        double[] values = instance.getValues();
//        for (int i = 0; i < attributes.length; i++) {

//          if(attributes[i].indexOf("job") != -1 || attributes[i].indexOf("career") != -1)

//            if(values[i] > 0){

//              resultClassification = weights[1];

//            }

//        }

//        if(resultClassification == weights[1]){

          URL url = new URL(urlStr);
          String file = url.getFile();
          if (file.equals("/") || file.equals("")) {
            resultClassification = maxRandom + 100;
          }
          else {
            file = file.substring(file.lastIndexOf("/"));
            if (file.length() < 15 && file.indexOf("search") != -1) {
              resultClassification = maxRandom + 200;
            }
          }
          for (int i = 0; i < attributes.length; i++) {
            if(attributes[i].indexOf("anchor_search") != -1 || attributes[i].indexOf("anchor_advanced") != -1
               || attributes[i].indexOf("anchor_database") != -1 || attributes[i].indexOf("around_database") != -1
               || attributes[i].indexOf("around_search") != -1  || attributes[i].indexOf("anchor_journals") != -1
                || attributes[i].indexOf("around_journals") != -1 || attributes[i].indexOf("anchor_access") != -1
             || attributes[i].indexOf("around_journal") != -1 || attributes[i].indexOf("anchor_journal") != -1
                 || attributes[i].indexOf("around_access") != -1 ) //|| attributes[i].indexOf("anchor_access") != -1)
              // reference, available
              if(values[i] > 0){
//                System.out.println("URL:" + page.getURL());

//                System.out.println(">>>>>>>>>>>>>ATTRIBUTE:"+attributes[i]);
                resultClassification = maxRandom + 200;
              }
          }
//        }
        linkRelevance[count] = new LinkRelevance(new URL(urlStr),resultClassification);
        count++;
      }
    }
    catch (MalformedURLException ex) {
      ex.printStackTrace();
      throw new LinkClassifierException(ex.getMessage());
    }catch(Exception ex){
      ex.printStackTrace();
      throw new LinkClassifierException(ex.getMessage());
    }
    return linkRelevance;
  }

  public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException{
    return null;
  }


}

