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
package focusedCrawler.util.vsm;

import java.util.HashMap;
import java.util.Iterator;

import java.net.URL;
import org.w3c.dom.Document;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.net.MalformedURLException;
import org.w3c.dom.NamedNodeMap;

import java.io.IOException;
import java.io.StringReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.string.PorterStemmer;
import focusedCrawler.util.string.StopList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.Collections;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class VSMVector {

  private HashMap elems;

  private PorterStemmer stemmer = new PorterStemmer();
  
  private StopList stoplist;
  
  private String id;
  
  public VSMVector() {
    elems = new HashMap();
  }

  public VSMVector(StopList stoplist) {
	    elems = new HashMap();
	    this.stoplist = stoplist;
  }
  
  public VSMVector(String file, boolean isForm, StopList stoplist) throws MalformedURLException, IOException, SAXException {
	  this.stoplist = stoplist;
      elems = new HashMap();
      if(isForm){
          DOMParser parser = new DOMParser();
          if((file.toLowerCase()).indexOf("<form ") != -1){//verify if the string is the name of file or the content of form
              parser.parse(new InputSource(new BufferedReader(new StringReader(file))));
          }else{
//              System.out.println("FILE:" + file);
              parser.parse(file);
          }
          String srcForm = "";
          Document doc = parser.getDocument();
          NodeList list = doc.getElementsByTagName("form");
          StringBuffer source = new StringBuffer();
          parse(list.item(0), source, new StringBuffer(), "html", stoplist);
          srcForm = source.toString().toLowerCase();
          PaginaURL formPage = new PaginaURL(new URL("http://www"), 0, 0,
                                             srcForm.length(),
                                             srcForm, stoplist);

          stemPage(formPage, true);
      } else {
          StringBuffer content = new StringBuffer();
          BufferedReader input = new BufferedReader(new FileReader(new File(
                  file)));
          for (String line = input.readLine(); line != null;
                  line = input.readLine()) {

              content.append(line);
              content.append("\n");

          }
          input.close();
          String src = content.toString();
          PaginaURL page = new PaginaURL(new URL("http://www"), 0, 0,
                                         src.length(),
                                         src, stoplist);
//          addTitle(page,stoplist);
          stemPage(page, false);
      }
  }

  
  private void addTitle(PaginaURL page, StopList stoplist) throws MalformedURLException{
	  this.stoplist = stoplist;
	  PaginaURL title = new PaginaURL(new URL("http://www"), 0, 0,
    		  page.titulo().length(),
    		  page.titulo(), stoplist);
	  String[] titleWords = title.palavras();
	  String[] metaTerms = page.palavrasMeta();
	  int[] metaOccurrencies = page.ocorrenciasMeta();
	  for (int i = 0; i < metaTerms.length; i++) {
          String word = metaTerms[i].toLowerCase();
 		   word = stemmer.stem(word);
 		   if(word.indexOf("No term") != -1){
             continue;
          }
 		   if(word.length() > 2 ){
 			   word = "meta" + word;
 			   VSMElement vsmElem = this.getElement(word);
 	             if(vsmElem == null){
 	               this.addElement(new VSMElement(word,metaOccurrencies[i]));
 	             }else{
 	               double weight = vsmElem.getWeight();
 	               this.addElement(new VSMElement(word,weight+1));
 	             }
 	         }
      }
	  
      for (int i = 0; i < titleWords.length; i++) {
    	  String word = titleWords[i].toLowerCase();
    	  
  		   word = stemmer.stem(word);
  		   if(word.indexOf("No term") != -1){
              continue;
           }
  		   if(word.length() > 2 ){
  			   word = "title" + word;
  			   VSMElement vsmElem = this.getElement(word);
  			   if(vsmElem == null){
  				   this.addElement(new VSMElement(word,1));
  			   }else{
  				   double weight = vsmElem.getWeight();
  	               this.addElement(new VSMElement(word,weight+1));
  			   }
  		   }
      }
  }

  public VSMVector(String document, StopList stoplist, boolean stem) throws   MalformedURLException {
	  if(!document.contains("<html>")){
		  document = "<html> " + document  + " </html>"; 
	  }
	  this.stoplist = stoplist;
	  elems = new HashMap();
	  PaginaURL page = new PaginaURL(new URL("http://www"), 0, 0,
                                   document.length(),
                                   document, stoplist);
	  
    //addTitle(page,stoplist);
	  if(stem){
		  stemPage(page,false);  
	  }else{
	        String[] words = page.palavras();
	        int[] frequencies = page.ocorrencias();
	        for (int i = 0; i < words.length; i++) {
	            if(frequencies[i] == 0){
	            	 continue;
	             }
//	             if(words[i].length() > 2 ){
	               VSMElement vsmElem = this.getElement(words[i]);
	               if(vsmElem == null){
	                   this.addElement(new VSMElement(words[i],1));
	               }else{
	                   double weight = vsmElem.getWeight();
	                   this.addElement(new VSMElement(words[i],1+weight));
	               }
//	             }
	        }
	  }
  }
  
  public String getId(){
	  return this.id;
  }
  
  public VSMVector(String id, String document, StopList stoplist) throws
  	MalformedURLException {
	  this(document,stoplist);
	  this.id = id;
  }
  
  public VSMVector(String document, StopList stoplist) throws
      MalformedURLException {
	  
	  if(!document.contains("<html>")){
		  document = "<html> " + document + " </html>"; 
	  }
	  this.stoplist = stoplist;
	  elems = new HashMap();
	  PaginaURL page = new PaginaURL(new URL("http://www"), 0, 0,
                                   document.length(),
                                   document, stoplist);
    //addTitle(page,stoplist);
	  stemPage(page, false);

  }

  public VSMVector(PaginaURL page, StopList stoplist) throws  MalformedURLException {
	  this.stoplist = stoplist;
	  elems = new HashMap();
	  stemPage(page, false);
}

  
  public VSMVector(String []words, StopList stoplist) throws MalformedURLException, IOException, SAXException {
	  this.stoplist = stoplist;
	  String word;
    
	  for (int i = 0; i < words.length; i++) {
		  word = stemmer.stem(words[i]);
		  VSMElement vsmElem = this.getElement(word);
		  if(vsmElem == null){
			  this.addElement(new VSMElement(word, 1));
		  }else{
			  double weight = vsmElem.getWeight();
			  this.addElement(new VSMElement(word, 1+weight));
		  }
	  }
  }

  public void addElements(String []words) {
    for (int i = 0; i < words.length; i++) {
    	this.addElement(words[i]);
    }
  }

  public void addElement(String word) {
	  this.addElement(new VSMElement(word, 1));
  }


  public void addElement(VSMElement elem){
//	  if(!stoplist.eIrrelevante(elem.getWord())){
//		  word = stemmer.stem(word);
		  VSMElement vsmElem = this.getElement(elem.getWord());
		  if(vsmElem == null){
			  elems.put(elem.getWord(), elem);
		  }else{
			  double weight = vsmElem.getWeight();
			  elems.put(elem.getWord(),new VSMElement(elem.getWord(), elem.getWeight()+weight));
		  }
//	  }
  }

  public VSMElement getElement(String word){
    return (VSMElement)elems.get(word);
  }

  public Iterator getElements(){
      return elems.values().iterator();
  }

  public VSMElement[] getArrayElements(){
      VSMElement[] elementsTemp = new VSMElement[elems.size()];
      Iterator iterator = elems.values().iterator();
      int count = 0;
      while (iterator.hasNext()) {
          elementsTemp[count] = (VSMElement)iterator.next();
          count++;
      }
    return elementsTemp;
  }

  public int size(){
    return elems.size();
  }

  public void addDFs(HashMap<String, Integer> idfs){
      Iterator<String> iter = elems.keySet().iterator();
      while(iter.hasNext()){
    	  VSMElement elem = (VSMElement)elems.get(iter.next());
    	  if(elem != null){
        	  String term = elem.getWord();
        	  double freq = elem.getWeight();
        	  Integer df = idfs.get(term);
        	  if(df == null){
        		  elem.setWeight(0);
        	  }else{
        		  double weight = freq  / df.doubleValue();
        		  elem.setWeight(weight);
        	  }
    	  }
      }
	  
  }
  
  public double vectorSpaceSimilarityIDF(VSMVector vectorB, HashMap idfs){

    VSMVector vectorA = this;
    double denominatorA = 0;
    double denominatorB = 0;
    VSMElement elem = null;

    Iterator iterA = vectorA.getElements();
    while(iterA.hasNext()){
      elem = (VSMElement)iterA.next();
      if((Integer)idfs.get(elem.getWord()) != null){
        int idf = ((Integer)idfs.get(elem.getWord())).intValue();
        double weight = elem.getWeight()*Math.log((double)idfs.size()/(double)idf);
        denominatorA = denominatorA + (weight*weight);
      }
    }

      Iterator iterB = vectorB.getElements();
      while(iterB.hasNext()){
        elem = (VSMElement)iterB.next();
        if((Integer)idfs.get(elem.getWord()) != null){
          int idf = ( (Integer) idfs.get(elem.getWord())).intValue();
          double weight = elem.getWeight() *
              Math.log( (double) idfs.size() / (double) idf);
          denominatorB = denominatorB + (weight * weight);
        }
      }

      double numerator = 0;
      iterA = vectorA.getElements();
      while(iterA.hasNext()){
        VSMElement elemA = (VSMElement)iterA.next();
        VSMElement elemB = vectorB.getElement(elemA.getWord());
        if( elemB != null){

          if(idfs.get(elemA.getWord()) != null){
              int idf = ( (Integer) idfs.get(elemA.getWord())).intValue();
              double weightA = elemA.getWeight() * Math.log( (double) idfs.size() / (double) idf);
              double weightB = elemB.getWeight() * Math.log( (double) idfs.size() / (double) idf);
              numerator = numerator + weightA*weightB;
          }

        }
      }
      double weight = numerator/(Math.sqrt(denominatorA)*Math.sqrt(denominatorB));
      return weight;
    }

  public double jaccardSimilarity(VSMVector vectorB){

	  VSMVector vectorA = this;
      

      double numerator = 0;
      Iterator iterA = vectorA.getElements();
      while(iterA.hasNext()){
    	  VSMElement elemA = (VSMElement)iterA.next();
    	  VSMElement elemB = vectorB.getElement(elemA.getWord());
          if( elemB != null){ //overlap
//            numerator = numerator + elemA.getWeight()*elemB.getWeight();
        	  numerator = numerator + 1;
          }
      }
      double denominator = vectorA.size() + vectorB.size() - numerator;
//        System.out.println("NUMERATOR:"+numerator);
//        System.out.println("NUMERATOR:"+denominator);
      return numerator/denominator;
  }
  
  public double intersection(VSMVector vectorB){
	  VSMVector vectorA = this;
      double numerator = 0;
      Iterator iterA = vectorA.getElements();
      while(iterA.hasNext()){
    	  VSMElement elemA = (VSMElement)iterA.next();
    	  VSMElement elemB = vectorB.getElement(elemA.getWord());
          if( elemB != null){ //overlap
//            numerator = numerator + elemA.getWeight()*elemB.getWeight();
        	  numerator = numerator + 1;
          }
      }
      return numerator;
  }


    public VSMVector clone(){
    	VSMVector res = new VSMVector(stoplist);
        Iterator iter = this.getElements();
        while(iter.hasNext()){
          VSMElement tempElem = (VSMElement)iter.next();
          res.addElement(tempElem);
        }
    	return res;
    }
    
    public double vectorSpaceSimilarity(VSMVector vectorB){

      VSMVector vectorA = this;
      double denominatorA = 0;
      double denominatorB = 0;
      VSMElement elem = null;

      Iterator iterA = vectorA.getElements();
      while(iterA.hasNext()){
        elem = (VSMElement)iterA.next();
          double weight = elem.getWeight();
          denominatorA = denominatorA + (weight*weight);
      }
      if(denominatorA == 0){
          return 0;
      }
        Iterator iterB = vectorB.getElements();
        while(iterB.hasNext()){
          elem = (VSMElement)iterB.next();
          double weight = elem.getWeight();
          denominatorB = denominatorB + (weight * weight);
        }
        if(denominatorB == 0){
            return 0;
        }
        double numerator = 0;
        iterA = vectorA.getElements();
        while(iterA.hasNext()){
          VSMElement elemA = (VSMElement)iterA.next();
          VSMElement elemB = vectorB.getElement(elemA.getWord());
          if( elemB != null){
              double weightA = elemA.getWeight();
              double weightB = elemB.getWeight();
              numerator = numerator + weightA*weightB;
          }
        }
        
        double den = (Math.sqrt(denominatorA)*Math.sqrt(denominatorB));
        double weight = numerator/den;
//        if(weight > 0.52 && weight < 0.53){
//            System.out.println("A:" + vectorA.toString());
//            System.out.println("B:" + vectorB.toString());
//            System.out.println("NUMERATOR:"+numerator);
//            System.out.println("DENOMINA:"+denominatorA);
//            System.out.println("DENOMINB:"+denominatorB);
//            System.out.println("DENOMIN:"+den);
//            System.out.println("SIM:"+weight);
//        }
        
        return weight;
      }

    public void addVector(VSMVector pageVector){

      VSMVector centroidVector = this;

      Iterator iter = pageVector.getElements();

      while(iter.hasNext()){
        VSMElement tempElem = (VSMElement)iter.next();
        centroidVector.addElement(tempElem);
      }
    }

    public void multiplyWeights(double factor){
    	Iterator  iter = elems.keySet().iterator();
    	while(iter.hasNext()){
    		String word = (String)iter.next();
    		VSMElement elem = (VSMElement)elems.get(word);
    		elems.put(word,new VSMElement(word,elem.getWeight()*factor));
    	}
    }

    
    public void negativeVector(){
    	Iterator  iter = elems.keySet().iterator();
    	while(iter.hasNext()){
    		String word = (String)iter.next();
    		VSMElement elem = (VSMElement)elems.get(word);
    		elems.put(word,new VSMElement(word,-elem.getWeight()));
    	}
    }

    public static HashMap calculateIDFs(VSMVector[] vectors) throws IOException,
        SAXException {

      HashMap idfs = new HashMap();

      for (int i = 0; i < vectors.length; i++) {

        VSMVector pageVector = vectors[i];
        Iterator iter = pageVector.getElements();

        while(iter.hasNext()){

          String word = ((VSMElement)iter.next()).getWord();

          Integer ocur = (Integer)idfs.get(word);
          if( ocur == null){
            idfs.put(word,new Integer(1));
          }else{
            idfs.put(word,new Integer(ocur.intValue()+1));
          }
        }
      }

      return idfs;
    }

    public HashMap calculateWordOccurence(VSMVector[] vectors) throws IOException,
        SAXException {

      HashMap idfs = new HashMap();

      for (int i = 0; i < vectors.length; i++) {

        VSMVector pageVector = vectors[i];
        Iterator iter = pageVector.getElements();

        while(iter.hasNext()){
            VSMElement elem = (VSMElement)iter.next();
            String word = elem.getWord();
            Integer ocur = (Integer)idfs.get(word);
            if( ocur == null){
                idfs.put(word,new Integer((int)elem.getWeight()));
            }else{
                idfs.put(word,new Integer(ocur.intValue()+(int)elem.getWeight()));
            }
        }
      }

      return idfs;
    }

    
    private void stemPage(PaginaURL page){

        String[] words = page.palavras();

        int[] frequencies = page.ocorrencias();
        for (int i = 0; i < words.length; i++) {
          String word = null;

          try{
            word = stemWord(words[i]);
            if(word == null){
         	   continue;
            }
          }catch(Exception e){
            continue;
          }
          if(frequencies[i] == 0){
         	 continue;
          }
          if(word.length() > 2 ){
            VSMElement vsmElem = this.getElement(word);
            if(vsmElem == null){
                this.addElement(new VSMElement(word,1));
            }else{
                double weight = vsmElem.getWeight();
                this.addElement(new VSMElement(word,1+weight));
            }
          }
      }
   }

    
    private void stemPage(PaginaURL page, boolean isForm){
    	
       String[] words = page.palavras();
       int[] frequencies = page.ocorrencias();
       for (int i = 0; i < words.length; i++) {
         String word = null;

         try{

        	 frequencies[i] = 1;
        	 word = stemWord(words[i]);
        	 if(word == null){
        		 continue;
        	 }
         }catch(Exception e){
        	 continue;
         }
         if(frequencies[i] == 0){
        	 continue;
         }
         if(word.length() > 2 ){
        	 VSMElement vsmElem = this.getElement(word);
             if(vsmElem == null){
            	 this.addElement(new VSMElement(word,frequencies[i]));
             }else{
            	 double weight = vsmElem.getWeight();
            	 this.addElement(new VSMElement(word,frequencies[i]+weight));
             }
         }
     }
  }

    private String stemWord(String word){
        if(word.indexOf("font-") != -1 || word.indexOf("padding") != -1
                || word.indexOf("border") != -1 || word.indexOf("margin") != -1
                || word.indexOf("background") != -1 || word.indexOf("color") != -1
                || word.indexOf("width") != -1 || word.indexOf("field") != -1
                || word.indexOf("verdana") != -1 || word.indexOf("helvetica") != -1
                || word.indexOf("sans") != -1 || word.indexOf("arial") != -1){ //parser bug
        	return null;
        }
        word = stemmer.stem(word);
        if(word.indexOf("No term") != -1){
        	return null;
        }
        return word;
    }

    public void normalizebyMax(){
    	VSMElement[] topElems = topElements(1);
    	double max = topElems[0].getWeight();
    	Iterator iter = elems.values().iterator();
    	double total = 0;
    	while(iter.hasNext()){
    		VSMElement elem = (VSMElement)iter.next();
    		total = total + elem.getWeight();
    	}
    	if(total != 0){
    		iter = elems.keySet().iterator();
    		while(iter.hasNext()){
    			String word = (String)iter.next();
    			VSMElement elem = (VSMElement)elems.get(word);
    			elems.put(word,new VSMElement(word,elem.getWeight()/max));
    		}
    	}
    }

    
    
    public void normalize(){
    	Iterator iter = elems.values().iterator();
    	double total = 0;
    	while(iter.hasNext()){
    		VSMElement elem = (VSMElement)iter.next();
    		total = total + elem.getWeight();
    	}
    	if(total != 0){
    		iter = elems.keySet().iterator();
    		while(iter.hasNext()){
    			String word = (String)iter.next();
    			VSMElement elem = (VSMElement)elems.get(word);
    			elems.put(word,new VSMElement(word,elem.getWeight()/total));
    		}
    	}
    }
  
  
  public void squaredNormalization(){
	  Iterator iter = elems.values().iterator();
	  double total = 0;
	  while(iter.hasNext()){
		  VSMElement elem = (VSMElement)iter.next();
		  total = total +  Math.sqrt(elem.getWeight());
	  }
	  if(total != 0){
		  iter = elems.keySet().iterator();
		  while(iter.hasNext()){
			  String word = (String)iter.next();
			  VSMElement elem = (VSMElement)elems.get(word);
			  elems.put(word,new VSMElement(word,Math.sqrt(elem.getWeight())/total));
		  }
	  }
  }

  public void normalizeOverElements() {
      double total = elems.size();
      Iterator iter = elems.keySet().iterator();
      while (iter.hasNext()) {
          String word = (String) iter.next();
          VSMElement elem = (VSMElement) elems.get(word);
          elems.put(word, new VSMElement(word, elem.getWeight() / total));
      }
  }

  public VSMElement[] topElements(int n){
	  VSMElement[] res = new VSMElement[n];
	  Vector temp = new Vector();
      Iterator iter = elems.values().iterator();
      while(iter.hasNext()){
          VSMElement elem = (VSMElement)iter.next();
          temp.add(elem);
      }
      Collections.sort(temp,new VSMElementComparator());
      for (int i = 0; i < temp.size() && i < n; i++) {
          res[i] = (VSMElement)temp.elementAt(i);
      }
	  return res;
  }
  
  public String toString(){
      StringBuffer buf = new StringBuffer();
      Vector temp = new Vector();
      Iterator iter = elems.values().iterator();
      while(iter.hasNext()){
          VSMElement elem = (VSMElement)iter.next();
          temp.add(elem);
      }
      Collections.sort(temp,new VSMElementComparator());
      buf.append("[");
      for (int i = 0; i < temp.size(); i++) {
          VSMElement elem = (VSMElement)temp.elementAt(i);
          buf.append(elem.toString());
          buf.append(",");
      }
      buf.append("]");
      return buf.toString();
  }

  public void remove(String word){
	  elems.remove(word);
  }
  
  private void parse(Node node, StringBuffer source, StringBuffer sourceTemp, String father,StopList stoplist) {
//    System.out.println(node.getClass().getName());
//    System.out.println("Name "+ node.getNodeName());
//    System.out.println("Type "+ node.getNodeType());
//    System.out.println("Value "+ node.getNodeValue());
      if(node == null){
        return;
      }
      String value = node.getNodeValue() + " of";
       if(Node.TEXT_NODE == node.getNodeType()){

           if(value.trim().indexOf("<") == -1){

              PaginaURL pageTemp = null;
              String[] words = null;
              try {
                pageTemp = new PaginaURL(new URL("http://www"), 0, 0,
                                                   value.length(),
                                                   value, stoplist);
                words = pageTemp.palavras();
              }
              catch (MalformedURLException ex) {

              }
              for(int i = 0; words != null && i < words.length; i++){

//              String stem = stemmer.stem(words[i]);
//              if(stem.equals("Invalid term")){
//                stem = words[i];
//              }
//              if(stem.indexOf("check") != -1){
//                stem = "check";
//              }
//
//              if(!father.equals("OPTION")){
//                source.append("body");
//                source.append(stem);
//                source.append(" ");
//              }else{
//                source.append(stem);
//                source.append(" ");
//              }
                String stem = words[i];
                try{
                  stem = stemmer.stem(words[i]);
                }catch(Exception e){
                }
                if(!father.equals("OPTION")){

                  if(stem.equals("Invalid term")){
                    stem = words[i];
                  }
                  if(stem.indexOf("check") != -1){
                    stem = "check";
                  }
                  source.append("body");
                  source.append(stem);
                  source.append(" ");
                }else{
                  source.append(stem);
                  source.append(" ");
                }
              }


              }
           return;
         }
         if(node.getNodeName().equals("INPUT")){
           NamedNodeMap attrs = node.getAttributes();
           for (int i = 0; i < attrs.getLength(); i++) {
             Node attr = attrs.item(i);
             String attrName = ((attr.getNodeName().trim()).toLowerCase());
             String attrValue = ((attr.getNodeValue().trim()).toLowerCase());
             if(attrName.equals("type") && !attrValue.equals("hidden")){
               source = source.append(sourceTemp);
               sourceTemp.delete(0,sourceTemp.length());
             }
           }
         }
         father = node.getNodeName();
         NodeList children = node.getChildNodes();
         if (children != null) {
           int len = children.getLength();
           for (int i = 0; i < len; i++){
             parse(children.item(i),source,sourceTemp, father, stoplist);
           }
         }
       }

  public static void main(String[] args) {

        try {
            StopList st = new focusedCrawler.util.string.StopListArquivo(args[0]);
//            VSMVector vsm1 = new VSMVector(args[1], false, st);
//            VSMVector vsm2 = new VSMVector(args[2], false, st);
//            System.out.println(vsm1.vectorSpaceSimilarity(vsm2));
            File[] posLabeledFiles = new File(args[1]).listFiles();
            File[] negLabeledFiles = new File(args[3]).listFiles();
            File[] testFiles = new File(args[3]).listFiles();
//            VSMVector vsmFormLabeled = new VSMVector();
            Vector<VSMElement> elems = new Vector<VSMElement>();
            VSMVector posvsmPageLabeled = null;
            VSMVector posvsmPageLabeledSpec = null;
            VSMVector negvsmPageLabeled = null;
            VSMVector negvsmPageLabeledSpec = null;
//            VSMVector vsmFormTest = new VSMVector();
//            VSMVector vsmPageTest = new VSMVector();
            for (int i = 0; i < posLabeledFiles.length; i++) {
//            	System.out.println("----------" + labeledFiles[i].toString());
                if(posvsmPageLabeled == null){
                	posvsmPageLabeled = new VSMVector(posLabeledFiles[i].toString(), false, st);
                	posvsmPageLabeledSpec = new VSMVector(args[2] + "/"+ posLabeledFiles[i].getName(), false, st);
                }else{
                	VSMVector vsmPageTemp = new VSMVector(posLabeledFiles[i].toString(), false, st);
                	VSMVector vsmPageTemp1 = new VSMVector(args[2] + "/"+ posLabeledFiles[i].getName(), false, st);
                	posvsmPageLabeled.addVector(vsmPageTemp);
                	posvsmPageLabeledSpec.addVector(vsmPageTemp1);
                }
            }
            for (int i = 0; i < negLabeledFiles.length; i++) {
//            	System.out.println("----------" + labeledFiles[i].toString());
                if(negvsmPageLabeled == null){
                	negvsmPageLabeled = new VSMVector(negLabeledFiles[i].toString(), false, st);
                	negvsmPageLabeledSpec = new VSMVector(args[4] + "/"+ negLabeledFiles[i].getName(), false, st);
                }else{
                	VSMVector vsmPageTemp = new VSMVector(negLabeledFiles[i].toString(), false, st);
                	VSMVector vsmPageTemp1 = new VSMVector(args[4] + "/" + negLabeledFiles[i].getName(), false, st);
                	negvsmPageLabeled.addVector(vsmPageTemp);
                	negvsmPageLabeledSpec.addVector(vsmPageTemp1);
                }
            }

            for (int i = 0; i < testFiles.length; i++) {
                VSMVector vsmPageTemp = new VSMVector(testFiles[i].toString(), false, st);
                System.out.println("----------" + testFiles[i].toString());
                double posSim = posvsmPageLabeled.vectorSpaceSimilarity(vsmPageTemp);
                
                VSMVector vsmPageTemp1 = new VSMVector(args[4] + "/" +testFiles[i].getName(), false, st);
                double posSim1 = posvsmPageLabeledSpec.vectorSpaceSimilarity(vsmPageTemp1);
                double posSim2 = (posSim + posSim1)/2;
                

                System.out.println("----------" + testFiles[i].toString());
                double negSim = negvsmPageLabeled.vectorSpaceSimilarity(vsmPageTemp);
                double negSim1 = posvsmPageLabeledSpec.vectorSpaceSimilarity(vsmPageTemp1);
                
                double negSim2 = (negSim + negSim1)/2;

                
                //                double negSim = negvsmPageLabeled.vectorSpaceSimilarity(vsmPageTemp);
                double margin = posSim-negSim;
                elems.add(new VSMElement(testFiles[i].getName(), margin));
            }
            Collections.sort(elems, new VSMElementComparator());
            for (int i = 0; i < elems.size(); i++) {
				System.out.println(elems.elementAt(i).getWord() + ":" + elems.elementAt(i).getWeight());
			}
//            double total = 0;
//            for (int i = 0; i < labeledFiles.length; i++) {
//            	total = total + vsmFormLabeled.vectorSpaceSimilarity(vsm[i]);
//            }
//            total = total/vsm.length;
//            int pos = 0;
//            for (int i = 0; i < testFiles.length; i++) {
////                if(testFiles[i].toString().indexOf("_") != -1){
//                    VSMVector vsmFormTemp = new VSMVector(testFiles[i].toString(), true, st);
//                    double sim = vsmFormLabeled.vectorSpaceSimilarity(vsmFormTemp);
//                    if(sim < 0.08){
//                    	pos++;
//                        System.out.println("FILE:" + testFiles[i].getName());
//                        System.out.println("FORM SIMILARITY:" + vsmFormLabeled.vectorSpaceSimilarity(vsmFormTemp));
//                        System.out.println("Cohesion:" + total);
//                        if(pos < 70){
//                        	Runtime.getRuntime().exec("cp " + testFiles[i] + " /home/lbarbosa/webdb/lbarbosa/improvClassifier/temp_neg/.");
//                        }
//                    }
//                    
////                    vsmFormTest.addVector(vsmFormTemp);
////                }else{
////                    VSMVector vsmPageTemp = new VSMVector(testFiles[i].toString(), false, st);
////                    vsmPageTest.addVector(vsmPageTemp);
////                }
//            }
//            System.out.println("TOTAL:"+pos);
//            System.out.println("PAGE SIMILARITY:" + vsmPageTest.vectorSpaceSimilarity(vsmPageLabeled));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       }
}
