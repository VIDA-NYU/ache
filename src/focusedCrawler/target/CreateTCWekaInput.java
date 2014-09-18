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
package focusedCrawler.target;

import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMVector;
import focusedCrawler.util.vsm.VSMElementComparator;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.FileComparator;

import java.io.File;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Collections;
import java.util.Random;

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
public class CreateTCWekaInput {

	protected VSMVector[] positiveExamples = null;
	protected VSMVector[] negativeExamples = null;

	protected VSMVector[] positiveTestExamples = null;
	protected VSMVector[] negativeTestExamples = null;

	protected int numOfFeatures = Integer.MAX_VALUE;

	protected int minDF = 2;
    
	protected HashMap df = new HashMap();

	protected boolean isForm = false;
  
	protected StopList stoplist;
	

    public CreateTCWekaInput(File dir, File dirTest, StopList stoplist) throws SAXException,
      MalformedURLException, IOException {
    	this(dir,dirTest,stoplist,Integer.MAX_VALUE);
    }
  
  public CreateTCWekaInput(File input, File inputTest, StopList stoplist, int numOfElems) throws SAXException,
  		MalformedURLException, IOException {
	  
	  this.stoplist = stoplist;
	  if((new File (input + File.separator + "positive")).isDirectory()){
		  File[] positiveFiles = new File (input + File.separator + "positive").listFiles();
		  System.out.println("POSITIVE:" + positiveFiles.length);
		  File[] negativeFiles = new File (input + File.separator + "negative").listFiles();
		  System.out.println("NEGATIVE:" + negativeFiles.length);
		  int[] negIndexes = selectRandomNum(1,negativeFiles.length, numOfElems);
		  negativeExamples = createVSM(negativeFiles, stoplist,negIndexes,true);
		  int[] posIndexes = selectRandomNum(1,positiveFiles.length, numOfElems);
	  	  positiveExamples = createVSM(positiveFiles, stoplist,posIndexes,true);
	  }else{
		  positiveExamples = createVSM(new File (input + File.separator + "positive"), stoplist);
		  negativeExamples = createVSM(new File (input + File.separator + "negative"), stoplist);
	  }
	  if(inputTest != null){
		  if((new File (inputTest + File.separator + "positive")).isDirectory()){
			  File temp = new File (inputTest + File.separator + "positive");
			  System.out.println(temp.toString());
			  if(temp.isDirectory()){
				  System.out.println("DIR" + temp.toString());
			  }
			  File[] positiveTestFiles = temp.listFiles();
			  System.out.println("TEST:" + positiveTestFiles.length);
			  positiveTestExamples = createVSM(positiveTestFiles, stoplist,false);
		  	  System.out.println("D5");
			  File[] negativeTestFiles = new File (inputTest + File.separator + "negative").listFiles();
			  negativeTestExamples = createVSM(negativeTestFiles, stoplist,false);
		  	  System.out.println("D6");
		  }else{
			  positiveTestExamples = createVSM(new File (inputTest + File.separator + "positive"), stoplist);
			  negativeTestExamples = createVSM(new File (inputTest + File.separator + "negative"), stoplist);
		  }
	  }
  }
  
  private File[] sortFiles(File[] files){
	  File[] result = new File[files.length];
	  Vector filesTemp = new Vector();
	  for (int i = 0; i < files.length; i++) {
		  filesTemp.add(files[i]);
	  }
	  Collections.sort(filesTemp,new FileComparator());
	  filesTemp.toArray(result);
	  return result;
  }
  
  private int[] selectRandomNum(long seed, int range, int elems){
	  if(elems > range){
		  elems = range;
	  }
//	  System.out.println("RANGE:" + range);
//	  System.out.println("SEED:" + seed);
//	  System.out.println("ELEMS:" + elems);
	  int count = 0;
	  Random random = new Random(seed);
	  int next = random.nextInt(range);
	  HashSet nums = new HashSet();
	  int[] result = new int[elems];
	  while(count < elems){
		  Integer num = new Integer(next);
		  if(!nums.contains(num)){
//			  System.out.print(next + ",");
			  result[count] = next;
			  nums.add(num);
			  count++;
		  }
		  next = random.nextInt(range);
	  }
//	  System.out.println("");
	  return result;
  }
  
  protected VSMVector[] createVSM(File file, StopList stoplist) throws SAXException{
	  Vector<VSMVector> tempVSM = new Vector<VSMVector>();
	  try{
		  BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      
		  for(String line = reader.readLine(); line != null; line = reader.readLine()){
			  VSMVector vsm = new VSMVector(line,stoplist);
			  tempVSM.add(vsm);
			  Iterator iterator1 = vsm.getElements();
			  while (iterator1.hasNext()) {
				  VSMElement elem = (VSMElement)iterator1.next();
				  VSMElement value = (VSMElement)df.get(elem.getWord());  
				  if(value == null){
					  df.put(elem.getWord(), new VSMElement(elem.getWord(),1));
				  }else{
					  df.put(elem.getWord(), new VSMElement(elem.getWord(),value.getWeight() +1));
				  }	
			  }
//			  System.out.println(vsm.toString());
		  }
	  }catch(IOException ex){
		ex.printStackTrace();  
	  }

	  VSMVector[] examples = new VSMVector[tempVSM.size()];
	  tempVSM.toArray(examples);
	  return examples;
  }
  
  
  protected VSMVector[] createVSM(File[] files, StopList stoplist, int[] indexes, boolean addToFeatures) throws SAXException{
	
	  Vector<VSMVector> tempVSM = new Vector<VSMVector>();
	  for (int i = 0; i < files.length && i < indexes.length; i++) {
		  try{
			  VSMVector vsm = new VSMVector(files[indexes[i]].toString(),isForm,stoplist);
			  tempVSM.add(vsm);
			  if(addToFeatures){
				  Iterator iterator1 = vsm.getElements();
				  while (iterator1.hasNext()) {
					  VSMElement elem = (VSMElement)iterator1.next();
					  VSMElement value = (VSMElement)df.get(elem.getWord());  
					  if(value == null){
						  df.put(elem.getWord(), new VSMElement(elem.getWord(),1));
					  }else{
						  df.put(elem.getWord(), new VSMElement(elem.getWord(),value.getWeight() +1));
					  }	
				  }
			  }
//			  System.out.println(vsm.toString());
		  }catch(IOException ex){
			  
		  }
	  }
	  VSMVector[] examples = new VSMVector[tempVSM.size()];
	  tempVSM.toArray(examples);
	  return examples;
	  
  }
  
  protected VSMVector[] createVSM(File[] files, StopList stoplist, boolean addToFeatures) throws IOException, SAXException{
	  
	  int[] indexes = new int[files.length];
	  for (int i = 0; i < indexes.length; i++) {
		indexes[i] = i;
	  }
	  return createVSM(files, stoplist, indexes, addToFeatures);
  }
  
  Vector<String> attributes = new Vector<String>(); 
  
  public String[] centroid2Weka(String output) throws FileNotFoundException,
      IOException {
//    FileOutputStream fout = new FileOutputStream(output,false);
//    DataOutputStream dout = new DataOutputStream( fout );
    
	  OutputStream fout= new FileOutputStream(output,false);
	  OutputStream bout= new BufferedOutputStream(fout);
	  OutputStreamWriter outputFile = new OutputStreamWriter(bout);

    

    StringBuffer header = new StringBuffer();
    header.append("@RELATION TSFC");
    header.append("\n");
    header.append("\n");
    StringBuffer tail = new StringBuffer();

//    Iterator iterator1 = centroidForm.getElements();
//    Vector bestWordsForm = new Vector();
//    while (iterator1.hasNext()) {
//     bestWordsForm.add(iterator1.next());
//   }
   
    Vector bestWordsForm  = new Vector(df.values());
    Collections.sort(bestWordsForm, new VSMElementComparator());
//   for (int i = 0; i < 10; i++) {
//	   VSMElement elem1 = (VSMElement)bestWordsForm.elementAt(i);
//	   System.out.println(elem1.getWord());
//	   System.out.println(elem1.getWeight());
//   }
//   System.out.println("Print header...");
    
    for(int i=0; i<=numOfFeatures && i < bestWordsForm.size(); i++){
    	VSMElement elem = (VSMElement)bestWordsForm.elementAt(i);
    	if(elem.getWeight() > minDF){
    		header.append("@ATTRIBUTE ");
        	header.append(elem.getWord());
        	attributes.add(elem.getWord());
        	header.append(" REAL");
        	header.append("\n");
    	}
    }
    header.append("@ATTRIBUTE class {S,NS}");
    tail.append("\n");
    tail.append("\n");
    tail.append("@DATA");
    tail.append("\n");
//   System.out.println("Print positive...");
    for (int i = 0; i < positiveExamples.length; i++) {
      VSMVector formTemp = positiveExamples[i];
//      formTemp.squaredNormalization();
      tail.append("{");
      for (int j = 0; j < attributes.size(); j++) {
      	VSMElement elemForm = formTemp.getElement(attributes.elementAt(j));
        if (elemForm != null){
        	tail.append(j);
        	tail.append(" ");
            tail.append((int)elemForm.getWeight());
            tail.append(",");
        }
      }
      tail.append(attributes.size() + " S}");
      tail.append("\n");
    }

//   System.out.println("Print negative...");
    for (int i = 0; i < negativeExamples.length; i++) {
      VSMVector formTemp = negativeExamples[i];
//      formTemp.squaredNormalization();
      tail.append("{");
      for (int j = 0; j < attributes.size(); j++) {
        	VSMElement elemForm = formTemp.getElement(attributes.elementAt(j));
            if (elemForm != null){
            	tail.append(j);
            	tail.append(" ");
                tail.append((int)elemForm.getWeight());
                tail.append(",");
            }
      }
      tail.append(attributes.size() + " NS}");
      tail.append("\n");
    }
    
    outputFile.write(header.toString());
    outputFile.flush();
    outputFile.write(tail.toString());
    outputFile.close();
    if(positiveTestExamples != null){
      createTestFile(output, bestWordsForm,header);
    }
    
    String[] atts = new String[attributes.size()];
    attributes.toArray(atts);
    return atts;
  }

  private void createTestFile(String output, Vector bestWordsForm, StringBuffer header) throws
            FileNotFoundException, IOException {

	  OutputStream fout= new FileOutputStream(output+"_test",false);
	  OutputStream bout= new BufferedOutputStream(fout);
	  OutputStreamWriter outputFile = new OutputStreamWriter(bout);

	  
      StringBuffer tail = new StringBuffer();
      tail.append("\n");
      tail.append("\n");
      tail.append("@DATA");
      tail.append("\n");
//      System.out.println("Print positve Test...");
      
      for (int i = 0; i < positiveTestExamples.length; i++) {
          VSMVector formTemp = positiveTestExamples[i];
//          formTemp.squaredNormalization();
          tail.append("{");
          for (int j = 0; j < attributes.size(); j++) {
            	VSMElement elemForm = formTemp.getElement(attributes.elementAt(j));
                if (elemForm != null){
                	tail.append(j);
                	tail.append(" ");
                    tail.append((int)elemForm.getWeight());
                    tail.append(",");
                }
                
          }
          tail.append(attributes.size() + " S}");
          tail.append("\n");
        }

      
//   System.out.println("Print negative Test...");
   for (int i = 0; i < negativeTestExamples.length; i++) {
	      VSMVector formTemp = negativeTestExamples[i];
//	      formTemp.squaredNormalization();
          tail.append("{");
          for (int j = 0; j < attributes.size(); j++) {
            	VSMElement elemForm = formTemp.getElement(attributes.elementAt(j));
                if (elemForm != null){
                	tail.append(j);
                	tail.append(" ");
                	tail.append((int)elemForm.getWeight());
                	tail.append(",");
                }
          }
	      tail.append(attributes.size() + " NS}");
	      tail.append("\n");
	    }
   		outputFile.write(header.toString());
   		outputFile.flush();
   		outputFile.write(tail.toString());
   		outputFile.close();
  }

  
  public static void main(String[] args) {
    StopList st = null;
      try {
        st = new focusedCrawler.util.string.StopListArquivo(args[0]);
      }
      catch (IOException ex) {

      }
    File dir = new File(args[1]);
    File dirTest = null;
    try{
        dirTest = new File(args[3]);
    }catch(Exception e){

    }
    try {
      CreateTCWekaInput createwekainput = new CreateTCWekaInput(dir, dirTest, st);
      createwekainput.centroid2Weka(args[2]);
    }
    catch (MalformedURLException ex1) {
      ex1.printStackTrace();
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
    }
    catch (SAXException ex1) {
      ex1.printStackTrace();
    }
  }

}
