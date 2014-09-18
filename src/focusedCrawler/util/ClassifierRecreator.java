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
package focusedCrawler.util;

import focusedCrawler.link.NeighborhoodPersistent;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.classifier.util.FilterData;
import focusedCrawler.link.classifier.util.Instance;
import focusedCrawler.link.classifier.util.WordField;
import focusedCrawler.link.classifier.util.WordFrequency;
import focusedCrawler.link.classifier.util.WordFrequencyComparator;

import focusedCrawler.util.parser.LinkNeighborhood;

import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.ParameterFile;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.Collections;
import java.net.URLEncoder;
import java.net.URLDecoder;

import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.string.PorterStemmer;

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
public class ClassifierRecreator {

  private NeighborhoodPersistent backlinkManager;

  private String dataDir;

  private StringBuffer output;

  private WrapperNeighborhoodLinks wrapper;

  private HashSet usedUrls;

  private StopList stoplist;

  private PorterStemmer stemmer;

  private Vector[] linesLevel;
  
  private Vector[] lns;

  public ClassifierRecreator(String dataDir, NeighborhoodPersistent backlinkManager, WrapperNeighborhoodLinks wrapper, StopList stoplist) {
    this.backlinkManager = backlinkManager;
    this.dataDir = dataDir;
    this.wrapper = wrapper;
    this.usedUrls = new HashSet();
    this.output = new StringBuffer();
    this.stoplist = stoplist;
    this.stemmer = new PorterStemmer();
  }

  public ClassifierRecreator(WrapperNeighborhoodLinks wrapper, StopList stoplist) {
	  this.wrapper = wrapper;
	  this.usedUrls = new HashSet();
	  this.output = new StringBuffer();
	  this.stoplist = stoplist;
	  this.stemmer = new PorterStemmer();
  }

  
  public WrapperNeighborhoodLinks getWrapper(){
	  return wrapper;
  }

  public String[] execute(Vector[] lns, int levels, File wekaFile) throws IOException {
	  this.lns = lns;
	  return execute(levels, wekaFile);
  }
  
  public String[] execute(int levels, File wekaFile) throws IOException {
    FileOutputStream fout = new FileOutputStream(wekaFile,false);
    DataOutputStream dout = new DataOutputStream( fout );
    linesLevel = new Vector[levels];
    for (int i = 0; i < linesLevel.length; i++) {
      linesLevel[i] = new Vector();
    } 
    output.append("@relation linkClassifier\n");
    String[] features = null;
    String[] ids = null;
    if(backlinkManager != null){
    	ids = retrieveIds(dataDir);
        System.out.println(">>>>" + ids.length + " ELEMENTS");
    	features = featureSelection(levels,ids);	
    }else{
        Vector<LinkNeighborhood[]> temp = new Vector<LinkNeighborhood[]>();
        for (int i = 0; i < lns.length; i++) {
        	for (int j = 0; j < lns[i].size(); j++) {
                LinkNeighborhood[] neighbors = new LinkNeighborhood[1];
            	neighbors[0] = (LinkNeighborhood) lns[i].elementAt(j);
                temp.add(neighbors);
			}
    	}
        features = selectBestFeatures(temp);
    }
    
    for (int i = 0; i < features.length; i++) {
      output.append ("@attribute " + features[i] + " REAL \n");
    }
    usedUrls.clear();
    output.append("@attribute level {0");
    for (int i = 1; i < levels; i++) {
      output.append(",");
      output.append(i);
    }

    output.append("}\n");
    output.append("\n");
    output.append("@data\n");
    dout.writeBytes(output.toString());
    dout.flush();
    if(backlinkManager != null){
        for (int i = 0; i < ids.length; i++) {
            depth(ids[i],0,levels,features);
        }
    }else{
    	generatLines(features);
    }
    int minSize = Integer.MAX_VALUE;
    for (int i = 0; i < levels; i++) {
    	System.out.println(">>>>>:"+ i + ":" + linesLevel[i].size());
    }
//    for (int i = 0; i < levels; i++) {
//    	if (linesLevel[i].size() < minSize && i < 2){
//    		minSize = linesLevel[i].size();
//    	}
//    }
    System.out.println(">>>>>>MIN_SIZE"+minSize);
    for (int i = 0; i < levels; i++) {
    	Vector levelLines = linesLevel[i];
    	System.out.println(">>SIZE:"+linesLevel[i].size());
    	for (int j = 0; j < linesLevel[i].size() && j < minSize*1.1; j++) {
    		StringBuffer line = (StringBuffer)levelLines.elementAt(j);
    		dout.writeBytes(line.toString());
    	}
    	linesLevel[i].clear();
    }
    dout.close();
    output = new StringBuffer();
    usedUrls.clear();
    return features;
  }

  private String[] retrieveIds(String rootDir){
	  Vector<String> tempList = new Vector<String>();
	  File[] dirs = (new File(rootDir)).listFiles();
	  for (int i = 0; i < dirs.length; i++) {
		  File[] files = (dirs[i]).listFiles();
		  for (int j = 0; j < files.length; j++) {
			  String name = files[j].getName();
			  tempList.add(name.substring(0,name.lastIndexOf("_")));
			  System.out.println(name.substring(0,name.lastIndexOf("_")));
		  }
	  }
	  String[] list = new String[tempList.size()];
	  tempList.toArray(list);
	  return list;
  }
  
  
  private void generatLines(String[] features) throws IOException {
	  for (int i = 0; i < lns.length; i++) {
		  Vector levelLn = lns[i];
		  for (int j = 0; j < levelLn.size(); j++) {
			LinkNeighborhood ln = (LinkNeighborhood)levelLn.elementAt(j);
			  StringBuffer line = new StringBuffer();
			  HashMap featureValue = wrapper.extractLinks(ln,features);
			  Iterator iter = featureValue.keySet().iterator();
			  while(iter.hasNext()){
				  String url = (String) iter.next();
				  if(!usedUrls.contains(url)){
					  Instance instance = (Instance) featureValue.get(url);
					  double[] values = instance.getValues();
					  line.append("{");
					  boolean containsValue = false;
					  for (int l = 0; l < values.length; l++) {
						  if(values[l] > 0){
							  containsValue = true;
							  line.append(l + " " +(int)values[l]);
							  line.append(",");
						  }
					  }
					  line.append(values.length + " " + i);
					  line.append("}");
					  line.append("\n");
					  if(containsValue){
						  linesLevel[i].add(line);
					  }else{
			        	   line = new StringBuffer();        	   
			           }
					  usedUrls.add(url);
				  }
			  }
		  }
	  }
  }

  
   private void depth(String id, int level, int limit, String[] features) throws IOException {
     StringBuffer line = new StringBuffer();
     if(level >= limit){
       return;
     }
     LinkNeighborhood[] neighbors = backlinkManager.select(id);
     if(neighbors == null){
       return;
     }
     for (int i = 0; i < neighbors.length; i++) {
       String nextUrl = neighbors[i].getLink().toString();
       neighbors[i].setURL(new java.net.URL(URLDecoder.decode(id)));
       HashMap featureValue = wrapper.extractLinks(neighbors[i],features);
       Iterator iter = featureValue.keySet().iterator();
       while(iter.hasNext()){
         String url = (String) iter.next();
        if(!usedUrls.contains(url)){
           Instance instance = (Instance) featureValue.get(url);
           double[] values = instance.getValues();
           line.append("{");
           boolean containsValue = false;
           for (int l = 0; l < values.length; l++) {
        	   if(values[l] > 0){
        		   containsValue = true;
                   line.append(l + " " +(int)values[l]);
                   line.append(",");
        	   }
           }
           line.append(values.length + " " + level);
           line.append("}");
           line.append("\n");
           if(containsValue){
        	   linesLevel[level].add(line);
           }else{
        	   line = new StringBuffer();        	   
           }
          usedUrls.add(url);
          depth(nextUrl,level+1, limit,features);
         }
       }
       line = new StringBuffer();
     }
   }

   private boolean goodInput(double[] values, int level){
     int count = 0;
     for (int i = 0; i < values.length; i++) {
       if(values[i] > 0){
         count++;
       }
     }
     return ((count >= 3 && level == 0) || (count >= 1 && level == 1) || (count >= 0 && level == 2));
   }


  private String[] featureSelection(int levels, String[] ids) throws IOException {
    System.out.println("TOTAL ELEMENTS:"+ids.length);
    Vector allNeighbors = new Vector();
    for (int i = 0; i < ids.length; i++) {
    	System.out.println(">>>DEBUG1");
    	getNeighbors(allNeighbors, ids[i],0,levels);
    }
    return selectBestFeatures(allNeighbors);
  }


  private void getNeighbors(Vector allNeighbors,String id, int level, int limit) throws IOException {
    if(level >= limit){
      return;
    }
    LinkNeighborhood[] neighborsTemp = backlinkManager.select(id);
    if(neighborsTemp == null){
      return;
    }
    LinkNeighborhood[] neighbors = new LinkNeighborhood[1];
    neighbors[0] = neighborsTemp[0];
    for (int i = 0; i < neighbors.length; i++) {
      neighbors[i].setURL(new java.net.URL(URLDecoder.decode(id)));
    }
    allNeighbors.add(neighbors);
    for (int i = 0; i < neighbors.length; i++) {
      String url = neighbors[0].getLink().toString();
      if(!usedUrls.contains(url)){
        usedUrls.add(url);
        getNeighbors(allNeighbors, url,level+1, limit);
      }
    }
    System.out.println(">>>NEIGHBOR SIZE" + allNeighbors.size());
  }

  private String[] selectBestFeatures(Vector allNeighbors){
    Vector finalWords = new Vector();
    HashSet usedURLTemp = new HashSet();
    HashMap urlWords = new HashMap();
    HashMap anchorWords = new HashMap();
    HashMap aroundWords = new HashMap();
    for (int l = 0; l < allNeighbors.size(); l++) {
      LinkNeighborhood[] neighborhood = (LinkNeighborhood[])allNeighbors.elementAt(l);
      if(neighborhood == null){
        continue;
      }
      for (int i = 0; i < neighborhood.length; i++) {
        LinkNeighborhood element = neighborhood[i];
        //anchor
        String[] anchorTemp = element.getAnchor();
        for (int j = 0; j < anchorTemp.length; j++) {
          String word = anchorTemp[j];
          if(stoplist.eIrrelevante(word)){
            continue;
          }
//          System.out.println(word);
          WordFrequency wf = (WordFrequency) anchorWords.get(word);
          if (wf != null) {
            anchorWords.put(word, new WordFrequency(word, wf.getFrequency()+1));
          }
          else {
            anchorWords.put(word, new WordFrequency(word, 1));
          }
        }

        //around
        String[] aroundTemp = element.getAround();
        for (int j = 0; j < aroundTemp.length; j++) {
          String word = aroundTemp[j];
          if(stoplist.eIrrelevante(word)){
            continue;
          }

          WordFrequency wf = (WordFrequency) aroundWords.get(word);
          if (wf != null) {
            aroundWords.put(word, new WordFrequency(word, wf.getFrequency()+1));
          }
          else {
            aroundWords.put(word, new WordFrequency(word, 1));
          }
        }

        //url
        if(!usedURLTemp.contains(element.getLink().toString())){
          usedURLTemp.add(element.getLink().toString());
          PaginaURL pageParser = new PaginaURL(element.getLink(), 0, 0, element.getLink().toString().length(), element.getLink().toString(), stoplist);
          String[] urlTemp = pageParser.palavras();
          for (int j = 0; j < urlTemp.length; j++) {
            String word =  stemmer.stem(urlTemp[j]);
            if(word == null || stoplist.eIrrelevante(word)){
              continue;
            }
            WordFrequency wf = (WordFrequency) urlWords.get(word);
//            System.out.println(">>>>>>URL:" + word);
            if (wf != null) {
              urlWords.put(word, new WordFrequency(word, wf.getFrequency()+1));
            }
            else {
              urlWords.put(word, new WordFrequency(word, 1));
            }
          }
        }
      }
    }

    String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

    Vector aroundVector = new Vector(aroundWords.values());
    Collections.sort(aroundVector,new WordFrequencyComparator());
    FilterData filterData1 = new FilterData(300,2);
    Vector aroundFinal = filterData1.filter(aroundVector,null);
    String[] aroundTemp = new String[aroundFinal.size()];

//    System.out.println("AROUND:"+aroundVector);
    for (int i = 0; i < aroundFinal.size(); i++) {
      WordFrequency wf = (WordFrequency)aroundFinal.elementAt(i);
      System.out.println("around_"+wf.getWord()  + ":" + wf.getFrequency());
      finalWords.add("around_"+wf.getWord());
      aroundTemp[i] = wf.getWord();
    }
    fieldWords[WordField.AROUND] = aroundTemp;

    
    Vector urlVector = new Vector(urlWords.values());
//    System.out.println("URL1:"+urlVector);
    Collections.sort(urlVector,new WordFrequencyComparator());
    FilterData filterData2 = new FilterData(350,2);
    Vector urlFinal = filterData2.filter(urlVector,(Vector)aroundFinal.clone());
    String[] urlTemp = new String[urlFinal.size()];

//    String[] urlTemp = new String[3];

//    System.out.println("URL:"+urlVector);

    for (int i = 0; i < urlTemp.length; i++) {
      WordFrequency wf = (WordFrequency)urlFinal.elementAt(i);
      System.out.println("url_"+wf.getWord()  + ":" + wf.getFrequency());
      finalWords.add("url_"+wf.getWord());
      urlTemp[i] = wf.getWord();
    }
    fieldWords[WordField.URLFIELD] = urlTemp;

    Vector anchorVector = new Vector(anchorWords.values());
    Collections.sort(anchorVector,new WordFrequencyComparator());
    FilterData filterData3 = new FilterData(350,2);
    Vector anchorFinal = filterData3.filter(anchorVector,null);
    String[] anchorTemp = new String[anchorFinal.size()];

//    System.out.println("ANCHOR:"+anchorVector);
    for (int i = 0; i < anchorFinal.size(); i++) {
      WordFrequency wf = (WordFrequency)anchorFinal.elementAt(i);
      System.out.println("anchor_"+wf.getWord() + ":" + wf.getFrequency());
      finalWords.add("anchor_"+wf.getWord());
      anchorTemp[i] = wf.getWord();
    }
    fieldWords[WordField.ANCHOR] = anchorTemp;

    wrapper.setFeatures(fieldWords);

    String[] features = new String[finalWords.size()];
    finalWords.toArray(features);
    return features;
  }


  public static void main(String[] args) {
    ParameterFile config = new ParameterFile(args[0]);

    System.out.println("Initializing...");
    StopList stoplist = null;
    try {
      stoplist = new StopListArquivo(config.getParam("STOPLIST_FILES"));
      WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);

      String[] attributes = config.getParam("ATTRIBUTES", " ");

      PersistentHashtable backlinks = new PersistentHashtable(config.getParam("BACKLINK_DIRECTORY"),1000);
      NeighborhoodPersistent neigh = new NeighborhoodPersistent(backlinks);
      File dataDir = new File(config.getParam("FORM_STORAGE_DIRECTORY"));
      System.out.println("Executing...");
      File wekaFile = new File(config.getParam("WEKA_FILE"));
      ClassifierRecreator classifierRecreator = new ClassifierRecreator(config.getParam("FORM_STORAGE_DIRECTORY"),neigh,wrapper,stoplist);
      classifierRecreator.execute(config.getParamInt("LEVEL"),wekaFile);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    } catch (CacheException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
