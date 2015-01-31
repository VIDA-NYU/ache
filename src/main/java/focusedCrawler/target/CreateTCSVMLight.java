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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Vector;

import org.xml.sax.SAXException;

import focusedCrawler.util.string.StopList;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;
import focusedCrawler.util.vsm.VSMVector;

public class CreateTCSVMLight extends CreateTCWekaInput{

	private VSMVector[] unlabeledExamples = null;
	   
    public CreateTCSVMLight(File dir, File dirTest, StopList stoplist, int numOfElems) throws SAXException,
		MalformedURLException, IOException {
    		super(dir,dirTest,stoplist,numOfElems);
    }
    
    public String[] centroid2Weka(String output, String unlabelDir) throws FileNotFoundException,
    		IOException, SAXException {
    	File[] unlabFiles = new File(unlabelDir).listFiles();
    	unlabeledExamples = createVSM(unlabFiles, stoplist,false);
    	FileOutputStream fout = new FileOutputStream(output,false);
    	DataOutputStream dout = new DataOutputStream( fout );
    	Vector<String> attributes = new Vector<String>(); 
    	StringBuffer tail = new StringBuffer();
    	Vector bestWordsForm  = new Vector(df.values());
    	Collections.sort(bestWordsForm, new VSMElementComparator());
    	
    	for (int i = 0; i < positiveExamples.length; i++) {
    		VSMVector formTemp = positiveExamples[i];
//    		formTemp.squaredNormalization();
    		tail.append("1 ");
    		for(int j=0; j<=numOfFeatures && j < bestWordsForm.size(); j++){
    			VSMElement elem = (VSMElement)bestWordsForm.elementAt(j);
    			if(elem.getWeight() > minDF){
    				VSMElement elemForm = formTemp.getElement(elem.getWord());
    				if (elemForm != null){
    					tail.append(j+1);
    					tail.append(":");
    					tail.append((int)elemForm.getWeight());
    					tail.append(" ");
    				}
    			}
    		}
    		tail.append("\n");
    	}
    	if(negativeExamples != null){
        	for (int i = 0; i < negativeExamples.length; i++) {
        		VSMVector formTemp = negativeExamples[i];
//        		formTemp.squaredNormalization();
        		tail.append("-1 ");
        		for(int j=0; j<=numOfFeatures && j < bestWordsForm.size(); j++){
        			VSMElement elem = (VSMElement)bestWordsForm.elementAt(j);
        			if(elem.getWeight() > minDF){
        				VSMElement elemForm = formTemp.getElement(elem.getWord());
        				if (elemForm != null){
        					tail.append(j+1);
        					tail.append(":");
        					tail.append((int)elemForm.getWeight());
        					tail.append(" ");
        				}
        			}
        		}
        		tail.append("\n");
        	}
    	}

//    	for (int i = 0; i < unlabeledExamples.length; i++) {
//    		VSMVector formTemp = unlabeledExamples[i];
////    		formTemp.squaredNormalization();
//    		tail.append("0 ");
//    		for(int j=0; j<=numOfFeatures && j < bestWordsForm.size(); j++){
//    			VSMElement elem = (VSMElement)bestWordsForm.elementAt(j);
//    			if(elem.getWeight() > minDF){
//    				VSMElement elemForm = formTemp.getElement(elem.getWord());
//    				if (elemForm != null){
//    					tail.append(j+1);
//    					tail.append(":");
//    					tail.append((int)elemForm.getWeight());
//    					tail.append(" ");
//    				}
//    			}
//    		}
//    		tail.append("\n");
//    	}
    	
    	dout.writeBytes(tail.toString());
    	dout.close();
    	if(positiveTestExamples != null){
    		createTestFile(output, bestWordsForm);
    	}
  
    	String[] atts = new String[attributes.size()];
    	attributes.toArray(atts);
    	return atts;
    }

    private void createTestFile(String output, Vector bestWordsForm) throws
    FileNotFoundException, IOException {
    	
    	FileOutputStream fout = new FileOutputStream(output+"_test",false);
    	DataOutputStream dout = new DataOutputStream( fout );
    	StringBuffer tail = new StringBuffer();

    	for (int i = 0; i < positiveTestExamples.length; i++) {
    		VSMVector formTemp = positiveTestExamples[i];
//    		formTemp.squaredNormalization();
    		tail.append("1 ");
    		for(int j=0; j<=numOfFeatures && j < bestWordsForm.size(); j++){
    			VSMElement elem = (VSMElement)bestWordsForm.elementAt(j);
    			if(elem.getWeight() > minDF){
    				VSMElement elemForm = formTemp.getElement(elem.getWord());
    				if (elemForm != null){
    					tail.append(j+1);
    					tail.append(":");
    					tail.append(elemForm.getWeight());
    					tail.append(" ");
    				}
    			}
    		}
    		tail.append("\n");
    	}

    	for (int i = 0; i < negativeTestExamples.length; i++) {
    		VSMVector formTemp = negativeTestExamples[i];
//    		formTemp.squaredNormalization();
    		tail.append("-1 ");
    		for(int j=0; j<=numOfFeatures && j < bestWordsForm.size(); j++){
    			VSMElement elem = (VSMElement)bestWordsForm.elementAt(j);
    			if(elem.getWeight() > minDF){
    				VSMElement elemForm = formTemp.getElement(elem.getWord());
    				if (elemForm != null){
    					tail.append(j+1);
    					tail.append(":");
    					tail.append(elemForm.getWeight());
    					tail.append(" ");
    				}
    			}
    		}
    		tail.append("\n");
    	}
    	dout.writeBytes(tail.toString());
    	dout.close();
    }

   
	/**
	 * @param args
	 */
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
	      CreateTCSVMLight createwekainput = new CreateTCSVMLight(dir, dirTest, st,1000);
	      createwekainput.centroid2Weka(args[2],args[4]);
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
