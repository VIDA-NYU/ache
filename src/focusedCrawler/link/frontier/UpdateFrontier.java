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
package focusedCrawler.link.frontier;

import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.cache.StringCacheKey;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.link.NeighborhoodPersistent;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.link.classifier.LinkClassifier;

import focusedCrawler.util.parser.LinkNeighborhood;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.io.IOException;
import focusedCrawler.link.classifier.LinkClassifierException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

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
public class UpdateFrontier {

  private NeighborhoodPersistent neighborhoodPersistent;

  private PersistentHashtable urlRelevance;

  public UpdateFrontier(NeighborhoodPersistent neighborPers, PersistentHashtable urlRelevance) {
    this.neighborhoodPersistent = neighborPers;
    this.urlRelevance = urlRelevance;
  }

//  public void saveFrontier() throws FileNotFoundException, IOException {
//    System.out.println(">>>>>SAVING FONTIER:"+urlRelevance.getDirectory() + "/phash.pers" );
//    FileOutputStream fout = new FileOutputStream( urlRelevance.getDirectory() + "/phash.pers" );
//    ObjectOutputStream oous = new ObjectOutputStream(fout);
//    oous.writeObject(urlRelevance.getCache());
//    fout.close();
//    oous.close();
//
//  }

  public void update(LinkClassifier linkClassifier) throws IOException, LinkClassifierException, CacheException {
//	  System.out.println(">>>> " + linkClassifier.getClass().toString());
	  Iterator keys = urlRelevance.getKeys();
    HashMap newCache = new HashMap();
    while (keys.hasNext()) {
      String key = (String)keys.next();
      String url = URLDecoder.decode(key);
      Integer relev = new Integer((String)urlRelevance.get(url));
      if (url == null || relev.intValue() == -1 || relev.intValue() < 200){
        continue;
      }
//      System.out.println(">>>> URL:" + url);
//      System.out.println(">>> RELEV BEFORE:" + urlRelevance.get(url));
      LinkNeighborhood[] neighbors = neighborhoodPersistent.select(url);
      if(neighbors == null){
//        
      }else{
        double maxValue = Double.MIN_VALUE;
        for (int i = 0; i < neighbors.length; i++) {
          neighbors[i].setURL(new URL(url));
          LinkRelevance linkRel = linkClassifier.classify(neighbors[i]);
          if(linkRel.getRelevance() > maxValue){
            maxValue = linkRel.getRelevance();
          }
        }
//        System.out.println(">>> RELEV AFTER:" + maxValue);
        int relevInt = (int)(maxValue);
//        System.out.println(">>> RELEV AFTER:" + maxValue);
//        System.out.println(">>>> URL:" + url + "=" + relevInt);
        newCache.put(URLEncoder.encode(url.toString()), relevInt+"");
//        urlRelevance.put(url.toString(), new Integer(relevInt)+"");
      }
    }
    urlRelevance.updateCache(newCache);
  }

}
