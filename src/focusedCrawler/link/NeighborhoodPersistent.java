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
package focusedCrawler.link;

import focusedCrawler.util.persistence.PersistentHashtable;

import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.string.PorterStemmer;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * <p> </p>
 *
 * <p>Description: This class stores and retrieves information about the backlinks of a page.</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class NeighborhoodPersistent {

  private PersistentHashtable backlinkGraph;

  private final String DELIM = "###";

  private PorterStemmer stemmer;

  public NeighborhoodPersistent(PersistentHashtable graph) {
    this.backlinkGraph = graph;
    stemmer = new PorterStemmer();
  }

  private long totalInsert = 0;

  private int counter  =0 ;

  /**
   * This method stores the backlinks of a page.
   * @param page
   * @throws IOException
   */
  public void insert(PaginaURL page) throws IOException {
    counter++;
    long totalSelect = 0;
   LinkNeighborhood[] neighs = page.getLinkNeighboor();
//   System.out.println(">>>>MAIN:"+page.getURL().toString());
   HashSet<String> tempURLs = new HashSet<String>();
   for (int i = 0; i < neighs.length; i++) {
     LinkNeighborhood link = neighs[i];
     String url = link.getLink().toString();
     if (!tempURLs.contains(url) && !url.endsWith("pdf") &&
    		 !url.endsWith("jpg") && !url.endsWith("gif") &&
    		 !url.endsWith("ps") && !url.contains("%") 
    		 && url.length() < 210) {		 
    	 tempURLs.add(url);	 
//         StringBuffer anchorBuffer = new StringBuffer();
//         String[] anchors = link.getAnchor();
//         for (int j = 0; j < anchors.length; j++) {
//           anchorBuffer.append(anchors[j]);
//           anchorBuffer.append(" ");
//         }
//         System.out.println(">>>>ANCHOR:"+anchorBuffer.toString());
//         StringBuffer aroundBuffer = new StringBuffer();
//         String[] around = link.getAround();
//         for (int j = 0; j < around.length; j++) {
//           aroundBuffer.append(around[j]);
//           aroundBuffer.append(" ");
//         }
//         System.out.println(">>>>AROUND:"+aroundBuffer.toString());
//         String value = page.getURL().toString() + ":" + anchorBuffer.toString() + ":" + aroundBuffer.toString();
         String value = page.getURL().toString() + ":" + link.getAnchorString() + ":" + link.getAroundString();
         long initSelect = System.currentTimeMillis();
         String backlinks = (String)backlinkGraph.get(url);
         long finalSelect = System.currentTimeMillis();
         totalSelect = totalSelect + (finalSelect - initSelect);
         if(backlinks == null){
             long initInsert = System.currentTimeMillis();
             backlinkGraph.put(url,value);
             long finalInsert = System.currentTimeMillis();
             totalInsert = totalInsert + (finalInsert - initInsert);
         }
//         if(backlinks != null){
//           value = backlinks + DELIM + value;
//         }
     }
     
//     System.out.println(">>>>URL:"+url);
   }
   double averageInsert = totalInsert/counter;
   System.out.println("AVERAGE INSERT:" + averageInsert);
   System.out.println("TOTAL SELECT:" + totalSelect);
  }

  public void insert(LinkNeighborhood ln, String urlRoot) throws IOException {
      String value = urlRoot + ":" + ln.getAnchorString() + ":" + ln.getAroundString();
      String backlinks = (String)backlinkGraph.get(ln.getLink().toString());
      if(backlinks == null){
//    	  System.out.println(ln.getLink().toString() + "->" + value);
          backlinkGraph.put(ln.getLink().toString(),value);
//          backlinkGraph.commit();
      }
  }
  
  /**
   * This method selects the backlinks of a URL.
   * @param url
   * @return
   * @throws IOException
   */
  
  public LinkNeighborhood[] select(String url) throws IOException {
	  Vector result = new Vector();
//    System.out.println("KEY:"+key);
	  String value = null;
	  try {
		  String key = URLDecoder.decode(url);
		  if(key == null){
			  return null;
		  }
		  value = (String)backlinkGraph.get(key);
	  }
	  catch (java.lang.NumberFormatException ex) {
//       System.out.println(">>>VALUE:"+value);
		  return null;
	  }

//    System.out.println("URL:" + url + " VALUE:"+value);
	  if(value == null){//this happens when the forms are close to seeds
		  return null;
	  }
	  int ini = 0;
	    int end = value.indexOf(DELIM);
	    do{
	      LinkNeighborhood neigh = null;
	      String unit = null;
	      end = value.indexOf(DELIM);
	      if(end == -1){
	        unit = value;
	      }else{
	        unit = value.substring(ini,end);
	      }
	//      System.out.println(">>UNIT:"+unit);
	
	      String unitTemp = unit.substring("http://".length());
	      String urlTemp = unitTemp.substring(0, unitTemp.indexOf(":"));
	//      System.out.println("URL"+ urlTemp);
	      neigh = new LinkNeighborhood(new URL("http://" + urlTemp));
	      unitTemp = unitTemp.substring(unitTemp.indexOf(":")+1);
	      String anchor = unitTemp.substring(0,unitTemp.indexOf(":"));
	//      System.out.println("ANCHOR"+ anchor);
	      StringTokenizer tokenizer1 = new StringTokenizer(anchor," ");
	      Vector temp  = new Vector();
	      while(tokenizer1.hasMoreTokens()){
	        String str = tokenizer1.nextToken();
	        try {
	          str = stemmer.stem(str);
	        }
	        catch (java.lang.StringIndexOutOfBoundsException ex) {
	
	        }
	        temp.add(str);
	      }
	      String[] anchorWords = new String[temp.size()];
	      temp.toArray(anchorWords);
	      neigh.setAnchor(anchorWords);
	
	
	      String around = unitTemp.substring(unitTemp.indexOf(":")+1,unitTemp.length());
	//      System.out.println("AROUND"+ around);
	      tokenizer1 = new StringTokenizer(around," ");
	      temp  = new Vector();
	      String str = "";
	      while(tokenizer1.hasMoreTokens()){
	        str = tokenizer1.nextToken();
	        try {
	          str = stemmer.stem(str);
	        }
	        catch (java.lang.StringIndexOutOfBoundsException ex) {
	
	        }
	        temp.add(str);
	      }
	      String[] aroundWords = new String[temp.size()];
	      temp.toArray(aroundWords);
	      neigh.setAround(aroundWords);
	      result.add(neigh);
	
	      end = value.indexOf(DELIM);
	      ini = end + DELIM.length();
	      value = value.substring(ini);
	      ini=0;
	//      System.out.println(">>>VALUE:"+value);
	    }while(end != -1);
	
	    LinkNeighborhood[] backlinks = new LinkNeighborhood[result.size()];
	    result.toArray(backlinks);
	    return backlinks;
  }
  
  public static void main(String[] args) {
//	  ParameterFile config = new ParameterFile(args[0]);
	   PersistentHashtable backlinkHash;
	try {
//		backlinkHash = new PersistentHashtable(
//				   config.getParam("BACKLINK_DIRECTORY"),config.getParamInt("MAX_CACHE_BACKLINK_SIZE"));

		HashSet<String> target = new HashSet<String>(); 
		BufferedReader input1 = new BufferedReader(new FileReader(new File(args[2])));
		for (String line = input1.readLine(); line != null;line = input1.readLine()) {
			target.add(line);
		}		
//		String dir = input.readLine();
//		int index = dir.indexOf("data_target");
//		dir = dir.substring(0,index) + "data_backlinks/dir";
//		System.out.println(dir);
		backlinkHash = new PersistentHashtable(args[0],10000);
		NeighborhoodPersistent neighborhood = new NeighborhoodPersistent(backlinkHash);
		BufferedReader input = new BufferedReader(new FileReader(new File(args[1])));
		for (String line = input.readLine(); line != null;line = input.readLine()) {
//			System.out.print(line + "##");
//			String origLine = line;
//			int lastIndex = line.lastIndexOf("/");
//			line = line.substring(lastIndex+1,line.length());
//			line = URLDecoder.decode(line);
//			System.out.println(line);
			LinkNeighborhood[] neighs = neighborhood.select(URLEncoder.encode(line));
			if(neighs != null){
				
				System.out.println(line + "::"  + neighs[0].toString());
////				System.out.print(origLine + "::" + neighs[0].toString());
				for (int i = 0; i < neighs.length; i++) {
//					
					LinkNeighborhood[] neighs1 = neighborhood.select(neighs[i].getLink().toString());
					if(neighs1 != null){
						if(target.contains(neighs[i].getLink().toString())){
							System.out.print(neighs[i].getLink().toString()+ "::"  + neighs1[i].toString());
						}
//						else{
//							System.out.print(neighs[i].getLink().toString()+ "::"  + neighs1[i].toString());
//						}
					}
////					else{
////						System.out.print(line);
////					}
//					
////					System.out.print(neighs[i].getLink().toString());
				}
				System.out.println("");
			}
////			else{
////				System.out.println(line);
////			}
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
//     backlinkHash.notSaveEntry();


	  
	  
  }
}
