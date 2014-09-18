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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;
import java.util.Collections;
import java.net.URLEncoder;
import java.net.URLDecoder;

import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.string.PorterStemmer;

public class PerpClassifierRecreator {

  private NeighborhoodPersistent backlinkManager;

  private HashMap<String, WordFrequency> filePerp;

  private StringBuffer output;

  private WrapperNeighborhoodLinks wrapper;

  private HashSet usedUrls;

  private StopList stoplist;

  private PorterStemmer stemmer;
  
  private int minSize = Integer.MAX_VALUE;
  
  private String perpDir;

  public PerpClassifierRecreator(String perpDir, NeighborhoodPersistent backlinkManager, WrapperNeighborhoodLinks wrapper, StopList stoplist) throws IOException {
	    
	  this.backlinkManager = backlinkManager;
    this.wrapper = wrapper;
    this.usedUrls = new HashSet();
    this.output = new StringBuffer();
    this.stoplist = stoplist;
    this.stemmer = new PorterStemmer();
    this.perpDir = perpDir;
    filePerp = new HashMap<String, WordFrequency>();

  }

  
  
  private void defineClasses(String perpDir) throws NumberFormatException, IOException{
	  filePerp = new HashMap<String, WordFrequency>();
	  String[] files = new File(perpDir).list();
	  for (int i = 0; i < files.length; i++) {
		  if(files.length >  45){
			  i = i+1;
			  if(i >= files.length){
				  break;
			  }
		  }
		  BufferedReader input = new BufferedReader(new FileReader(new File(perpDir + File.separator + files[i])));
		  for (String line = input.readLine(); line != null; line = input.readLine()) {
			  if(!line.contains("FILE_NOT_FOUND")){
				  String[] parts = line.split(" ");
				  if(parts.length == 5){
					  String name = parts[0];
					  String perpStr = parts[4];
					  name = name.substring(name.lastIndexOf("/")+1,name.length());
					  double perp = 0;
					  if(!perpStr.contains("nan") && !perpStr.contains("FILE")){
						  perp = Double.parseDouble(perpStr);
						  WordFrequency wf = new WordFrequency(name, (int)perp);
						  filePerp.put(name, wf);    		
					  }
				  }
			  }
		  }
	  }
	  HashMap<String, WordFrequency> perpTemp = new HashMap<String, WordFrequency>();
	  Vector<WordFrequency> values = new Vector<WordFrequency>(filePerp.values());
	  Collections.sort(values, new WordFrequencyComparator());
	  int classSize = values.size()/3;
	  int initialClassSize = classSize;
	  int classValue = 2;
	  for (int i = 0; i < values.size(); i++){
		  WordFrequency wf = values.elementAt(i);
		  if(i == classSize){
			  if(classValue == 0){
				  break;
			  }
			  classValue = classValue-1;
			  classSize = classSize + initialClassSize;
			  System.out.println(classValue + "="+wf.getFrequency());
		  }
//		  System.out.println(classValue + "=" + wf.getWord() + ":" +wf.getFrequency());
		  wf.setFrequency(classValue);
		  perpTemp.put(wf.getWord(),wf);
//		  filePerp.put(wf.getWord(),wf);
	  }
	  filePerp = perpTemp;
  }
  
  private HashMap<String, LinkNeighborhood> featuresHash = new HashMap<String, LinkNeighborhood>();
  
  public PerpClassifierRecreator(String perpDir, String featuresFile,WrapperNeighborhoodLinks wrapper, StopList stoplist) throws IOException {
	  this.perpDir = perpDir;
	  this.wrapper = wrapper;
	    this.usedUrls = new HashSet();
	    this.output = new StringBuffer();
	    this.stoplist = stoplist;
	    this.stemmer = new PorterStemmer();
	    BufferedReader input1 = new BufferedReader(new FileReader(new File(featuresFile)));
        for(String line = input1.readLine(); line != null; line = input1.readLine()) {
//        	System.out.println(line);
			int lastIndex = line.lastIndexOf("/");
			line = line.substring(lastIndex+1,line.length());
			line = URLDecoder.decode(line);
//			System.out.println(line);
			if(!line.startsWith("http")){
				continue;
			}
			LinkNeighborhood ln = LinkNeighborhood.createLN(line);
//        	System.out.println(ln.getLink().toString() + ":" + ln.toString());
        	featuresHash.put(URLEncoder.encode(ln.getLink().toString()), ln);
        }
//        defineClasses(perpDir);
//	    String[] files = new File(perpDir).list();
//	    int[] classCounts = new int[3];
//	    for (int i = 0; i < files.length; i++) {
//	        BufferedReader input = new BufferedReader(new FileReader(new File(perpDir + File.separator + files[i])));
//	        for (String line = input.readLine(); line != null; line = input.readLine()) {
//	        	if(!line.contains("FILE_NOT_FOUND")){
//	            	String[] parts = line.split(" ");
//	            	if(parts.length == 5){
//	                	String name = parts[0];
//	                	String perpStr = parts[4];
//	                	name = name.substring(name.lastIndexOf("/")+1,name.length());
//	                	double perp = 0;
//	                	if(!perpStr.contains("nan") && !perpStr.contains("FILE")){
//	                		perp = Double.parseDouble(perpStr);
//	                		int classValue = (int)(perp/100);
//	                        if(classValue > 2){
//	                     	   classValue = 2;
//	                        }
//	                		classCounts[classValue] = classCounts[classValue]+ 1;
////	                    	System.out.println("NAME:" + name + ",Perp:" + perp);
//	                    	filePerp.put(name, new Double(perp));    		
//	                	}
//	            	}
//	        	}
//	        }
//		}
//	    for (int i = 0; i < classCounts.length; i++) {
//	    	System.out.println("class:" + i + ":" + classCounts[i]);
//	    	if(classCounts[i] < minSize){
//				minSize = classCounts[i]; 
//	    	}
//	    }
//	    System.out.println("MIN_SIZE:" + minSize);
	  }
  
  
  public WrapperNeighborhoodLinks getWrapper(){
    return wrapper;
  }

  public String[] execute(File wekaFile) throws IOException {

	  defineClasses(this.perpDir);
    FileOutputStream fout = new FileOutputStream(wekaFile,false);
    DataOutputStream dout = new DataOutputStream( fout );

//    System.out.println(">>>>" + filePerp.size() + " PAGES");
    output.append("@relation linkClassifier\n");
    String[] features = featureSelection();
    for (int i = 0; i < features.length; i++) {
      output.append ("@attribute " + features[i] + " real \n");
    }
    usedUrls.clear();
    output.append("@attribute class {0,1,2}");
    output.append("\n");
    output.append("\n");
    output.append("@data\n");
    dout.writeBytes(output.toString());
    dout.flush();
    int[] counts = new int[3];
    Iterator<String> iter = filePerp.keySet().iterator();
    while(iter.hasNext()){
    	String name = iter.next();
//    	System.out.println("NAME:" + name);
//    	Double value = filePerp.get(name);
//    	double perp = value.doubleValue();
//		int classValue = (int)(perp/100);
		int classValue = filePerp.get(name).getFrequency();
//        if(classValue > 2){
//     	   classValue = 2;
//        }
//        if(counts[classValue] < minSize * 1.3){
        	createLine(name,classValue,features,dout);
//        	counts[classValue] = counts[classValue] + 1;
//        }
        
        
    }
    dout.close();
    output = new StringBuffer();
    usedUrls.clear();
    return features;
  }


   private void createLine(String formID, double perp, String[] features, DataOutputStream dout) throws IOException {
     StringBuffer line = new StringBuffer();
     LinkNeighborhood[] neighbors = null;
     if(backlinkManager == null){
    	 neighbors = new LinkNeighborhood[1];
    	 neighbors[0] = featuresHash.get(formID);
    	 if(neighbors[0] == null){
    		 return;
    	 }
     }else{
    	 neighbors = backlinkManager.select(formID);
     }
     
     if(neighbors == null){
       return;
     }
     for (int i = 0; i < neighbors.length; i++) {
       String nextUrl = neighbors[i].getLink().toString();
       neighbors[i].setURL(new java.net.URL(URLDecoder.decode(formID)));
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
//           int classValue = (int)(perp/100);
//           if(classValue > 2){
//        	   classValue = 2;
//           }
           line.append(values.length + " " + (int)perp);
           line.append("}");
           line.append("\n");
           if(containsValue){
               dout.writeBytes(line.toString());        	   
           }
           usedUrls.add(url);
         }
       }
       line = new StringBuffer();
     }
   }

  private String[] featureSelection() throws IOException {
    System.out.println("TOTAL PAGES:"+filePerp.size());
    Vector<LinkNeighborhood> allNeighbors = new Vector<LinkNeighborhood>();
    Iterator<String> iter = filePerp.keySet().iterator();
    while(iter.hasNext()){
    	String name = iter.next();
    	LinkNeighborhood[] neighbors = null;
        if(backlinkManager == null){
       	 	neighbors = new LinkNeighborhood[1];
       	 	neighbors[0] = featuresHash.get(name);
       	 	if(neighbors[0] == null){
       	 		continue;
       	 	}
        }else{
       	 	neighbors = backlinkManager.select(name);
        }

        if(neighbors == null){
          continue;
        }
//        System.out.println(name);
        for (int i = 0; i < neighbors.length; i++) {
//            System.out.println(neighbors[i]);
        	String url = neighbors[i].getLink().toString();
          if(!usedUrls.contains(url)){
            usedUrls.add(url);
            allNeighbors.add(neighbors[i]);
          }
       }
    }
    System.out.println("VECTOR SIZE:" + allNeighbors.size());
    return selectBestFeatures(allNeighbors);
  }



  private String[] selectBestFeatures(Vector<LinkNeighborhood> allNeighbors){
    Vector finalWords = new Vector();
    HashSet usedURLTemp = new HashSet();
    HashMap urlWords = new HashMap();
    HashMap anchorWords = new HashMap();
    HashMap aroundWords = new HashMap();
    for (int l = 0; l < allNeighbors.size(); l++) {
    	
    	LinkNeighborhood element = (LinkNeighborhood)allNeighbors.elementAt(l);
    	if(element == null){
    		continue;
    	}
        //anchor
    	String[] anchorTemp = element.getAnchor();
    	for (int j = 0; anchorTemp != null && j < anchorTemp.length; j++) {
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
//        System.out.println(anchorWords.size());
        //around
        String[] aroundTemp = element.getAround();
        for (int j = 0; aroundTemp != null && j < aroundTemp.length; j++) {
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
//        System.out.println(aroundWords.size());
        //url
        if(!usedURLTemp.contains(element.getLink().toString())){
          usedURLTemp.add(element.getLink().toString());
          PaginaURL pageParser = new PaginaURL(null, 0, 0, element.getLink().getFile().length(), element.getLink().getFile(), stoplist);
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
//        System.out.println(urlWords.size());
    }

    String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

    Vector aroundVector = new Vector(aroundWords.values());
    Collections.sort(aroundVector,new WordFrequencyComparator());
    FilterData filterData1 = new FilterData(700,2);
    Vector aroundFinal = filterData1.filter(aroundVector,null);
    String[] aroundTemp = new String[aroundFinal.size()];


    Vector urlVector = new Vector(urlWords.values());
//    System.out.println("URL1:"+urlVector);
    Collections.sort(urlVector,new WordFrequencyComparator());
    FilterData filterData2 = new FilterData(300,2);
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

    System.out.println("AROUND:"+aroundVector);
    for (int i = 0; i < aroundFinal.size(); i++) {
      WordFrequency wf = (WordFrequency)aroundFinal.elementAt(i);
      System.out.println("around_"+wf.getWord()  + ":" + wf.getFrequency());
      finalWords.add("around_"+wf.getWord());
      aroundTemp[i] = wf.getWord();
    }
    fieldWords[WordField.AROUND] = aroundTemp;

    Vector anchorVector = new Vector(anchorWords.values());
    Collections.sort(anchorVector,new WordFrequencyComparator());
    FilterData filterData3 = new FilterData(600,2);
    Vector anchorFinal = filterData3.filter(anchorVector,null);
    String[] anchorTemp = new String[anchorFinal.size()];


    System.out.println("ANCHOR:"+anchorVector);
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
//      PersistentHashtable backlinks = new PersistentHashtable(config.getParam("BACKLINK_DIRECTORY"),1000);
//      NeighborhoodPersistent neigh = new NeighborhoodPersistent(backlinks);
      System.out.println("Executing...");
      File wekaFile = new File(args[1]);
//      PerpClassifierRecreator classifierRecreator = new PerpClassifierRecreator(args[2],neigh,wrapper,stoplist);
      PerpClassifierRecreator classifierRecreator = new PerpClassifierRecreator(args[2],args[3],wrapper,stoplist);
      System.out.println(args[2]);
      classifierRecreator.execute(wekaFile);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    
//    catch (CacheException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	} 
    catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
