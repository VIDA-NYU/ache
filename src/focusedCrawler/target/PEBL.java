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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.xml.sax.SAXException;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

import focusedCrawler.util.Page;
import focusedCrawler.util.vsm.VSMVector;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;

public class PEBL {

	private StopList stoplist;

	private String[] attributes;
	
	private String rootDir = "/home/lbarbosa/webdb/lbarbosa/improvClassifier_book";
	
	private String trainingData = rootDir + File.separator + "trainData_form";
	
	private String posDir = trainingData + File.separator + "positive";
	
	private String unlabelDir = rootDir + File.separator + "devData_form";
	
	private String cleanScript = "sh " + rootDir + "/script/runClean.sh";
	
	private String wekaFileDir = rootDir + File.separator + "wekaInput" + File.separator;
	
	public void PEBL() throws IOException {
		this.stoplist = new StopListArquivo(rootDir + "/conf/stoplist.txt");
	}
	
	public void execute() throws Exception{
		selectInitialSet(trainingData+"_ini"+ File.separator + "positive" ,trainingData + File.separator + "positive");
		firstStep();
		System.out.println("Building classifier...");
		String model = buildClassifier("form");
		for (int i = 0; i < 200; i++) {
			System.out.println("Testing classifier...");
			testClassifier(wekaFileDir + File.separator + "weka_form_test", model);
			System.out.println("Runing classifier...");
			runDevSet(model);
			System.out.println("Moving data form...");
			Runtime.getRuntime().exec(cleanScript);
			System.out.println("ITERATION:" + i);
			System.out.println("Building classifier...");
			model = buildClassifier("form");
		}
	}
	
	private void selectInitialSet(String source, String destination) throws IOException{
		System.out.println("SOURCE:" + source);
		System.out.println("DSTINATION:" + destination);
		Random rand = new Random(1);
		File[] files = (new File(source)).listFiles();
		HashSet<Integer> nums = new HashSet<Integer>();
		int next = rand.nextInt(files.length);
		int count = 0;
		while(count < 25){
			if(!nums.contains(new Integer(next))){
				Runtime.getRuntime().exec("cp " + source + File.separator + files[next].getName() + " " + destination);
				nums.add(new Integer(next));
				count++;
			}
			next = rand.nextInt(files.length);
		}
	}
	
	private void firstStep() throws Exception{
		HashMap posFeatures = new HashMap();
		HashMap unlFeatures = new HashMap();
		File[] pos = new File(posDir).listFiles();
		File[] unl = new File(unlabelDir).listFiles();
		for (int i = 0; i < pos.length; i++) {
			VSMVector positiveExample = new VSMVector(pos[i].toString(),false,stoplist);
			VSMElement[] elems = positiveExample.getArrayElements();
			for (int j = 0; j < elems.length; j++) {
				Integer freq = (Integer)posFeatures.get(elems[j].getWord());
				if(freq == null){
					posFeatures.put(elems[j].getWord(), new Integer(1));
				}else{
					posFeatures.put(elems[j].getWord(), new Integer(freq.intValue()+1));
				}
			}
		}
//		System.out.println("POS" + posFeatures.toString());
		
		for (int i = 0; i < unl.length; i++) {
			VSMVector unlabeledExample = new VSMVector(unl[i].toString(),false,stoplist);
			VSMElement[] elems = unlabeledExample.getArrayElements();
			for (int j = 0; j < elems.length; j++) {
				Integer freq = (Integer)unlFeatures.get(elems[j].getWord());
				if(freq == null){
					unlFeatures.put(elems[j].getWord(), new Integer(1));
				}else{
					unlFeatures.put(elems[j].getWord(), new Integer(freq.intValue()+1));
				}
			}
		}
		
		Vector selectedFeatures = new Vector();
		Iterator iter = posFeatures.keySet().iterator();
		while(iter.hasNext()){
			String word = (String)iter.next();
			double freqPos = (double)((Integer)posFeatures.get(word)).intValue()/(double)pos.length;
			double freqUnlab = 0;
			if(unlFeatures.get(word) != null){
				freqUnlab = (double)((Integer)unlFeatures.get(word)).intValue()/(double)unl.length;	
			}
//			System.out.println("##POS:" + freqPos);
//			System.out.println("##UNL:" + freqUnlab);
			if(freqPos > freqUnlab){
				selectedFeatures.add(word);
			}
		}
//		System.out.println("##SIZE:" + selectedFeatures.size());
		for (int i = 0; i < unl.length; i++) {
			VSMVector unlabeledExample = new VSMVector(unl[i].toString(),false,stoplist);
			boolean selected = false;
			for (int j = 0; j < selectedFeatures.size() && !selected; j++) {
				String word = (String)selectedFeatures.elementAt(j);
				if(unlabeledExample.getElement(word) != null){
					selected = true;
				}
			}
			if(!selected){
				Runtime.getRuntime().exec("mv " + unl[i].toString() + " " + trainingData + File.separator + "negative" + File.separator);
			}
		}
	}
	
	private String buildClassifier(String suffix) throws Exception{
		String trainingData = rootDir + File.separator + "trainData_" + suffix;
//		System.out.println("TRAIN:" +trainingData);
		String trainWekafile =  wekaFileDir + "weka_" + suffix;
		String testFileDir = rootDir + File.separator + "testData_" + suffix;
		String outputModel = rootDir + File.separator + "model" + File.separator + "model_" + suffix;
		CreateTCWekaInput createWekaFile = new CreateTCWekaInput(new File(trainingData),new File(testFileDir),stoplist);
		attributes = createWekaFile.centroid2Weka(trainWekafile);
		double max = Double.MIN_VALUE;
		double cValue = 0;
		int count = 0;
		for (double c = 0.0625; count < 1 ; c= c*0.5) {
			SMO classifier = new SMO();
			String[] argum = new String[]{"-t",trainWekafile, "-C", ""+c, "-v", "-d",outputModel+c};
			String output = Evaluation.evaluateModel(classifier, argum);
			int index = output.indexOf("Correctly Classified Instances");
			if(index >= 0){
				int end = output.indexOf("%",index);
				String line = (output.substring(index,end)).trim();
				line = line.substring(line.lastIndexOf(" "));
				double accuracy = Double.parseDouble(line.trim());
				System.out.println("C="+c + " acc=" + accuracy);
				if(accuracy > max){
					max = accuracy;
					cValue = c;
				}
			}	      
			count++;
			if(c == 1){
				testClassifier(trainWekafile+ "_test",outputModel+c);
			}
		}
		return outputModel+cValue;
	}
	
	
	  private void testClassifier(String testFile, String outputModel) throws Exception{
		  SMO classifier = new SMO();
//		  NaiveBayes classifier = new NaiveBayes();
//		  System.out.println("java -T " + testFile + " -l" + outputModel );
		  String[] argum = new String[]{"-T",testFile,"-l",outputModel,"-i"};
		  String output = Evaluation.evaluateModel(classifier, argum);
		  int index = output.indexOf("F-Measure");
		  if(index >= 0){
			  index = output.indexOf("\n",index);
			  int end = output.indexOf("\n",index+1);
			  String line = (output.substring(index,end)).trim();
			  StringTokenizer tokenizer = new StringTokenizer(line, " ");
			  int count = 0;
			  while(tokenizer.hasMoreTokens()){
				  String word = tokenizer.nextToken();
				  if(count == 2){
					  System.out.println("PRECISION:"+word);
				  }
				  if(count == 3){
					  System.out.println("RECALL:"+word);
				  }
				  if(count == 4){
					  System.out.println("F-MEASURE:"+word);
				  }
				  count++;
			  }	
		  }	      
		  System.out.println("-----------");
	  }
	
	  private void runDevSet(String model) throws IOException, ClassNotFoundException, TargetClassifierException{
		  InputStream is = new FileInputStream(model);
		  ObjectInputStream objectInputStream = new ObjectInputStream(is);
		  Classifier classifier = (Classifier) objectInputStream.readObject();
		  weka.core.FastVector vectorAtt = new weka.core.FastVector();
		  for (int i = 0; i < attributes.length; i++) {
			  vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
		  }
		  String[] classValues = new String[]{"S","NS"};
		  weka.core.FastVector classAtt = new weka.core.FastVector();
		  for (int i = 0; i < classValues.length; i++) {
			  classAtt.addElement(classValues[i]);
		  }
		  vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
		  Instances insts = new Instances("target_classification", vectorAtt, 1);
		  insts.setClassIndex(attributes.length);
		  TargetClassifier targetClassifier = new TargetClassifierImpl(classifier, insts, attributes, stoplist);
		  File[] list = new File(unlabelDir).listFiles();
		  int total = 0;
		  System.out.println("TOTAL:"+list.length);
//		  Vector<VSMElement> posDistances = new Vector<VSMElement>();
//		  Vector<VSMElement> negDistances = new Vector<VSMElement>();
		  for (int i = 0; i < list.length; i++) {
				StringBuffer content = new StringBuffer();
				BufferedReader input = new BufferedReader(new FileReader(list[i]));
				for (String line = input.readLine(); line != null;
				line = input.readLine()) {
					content.append(line);
					content.append("\n");
				}
				String src = content.toString();
				boolean isTarget = targetClassifier.classify(new Page(null,src));
				if(!isTarget){
					Runtime.getRuntime().exec("mv " + list[i].toString() + " " + trainingData + File.separator + "negative" + File.separator);
					total++;	
				}
		  }
		  if(total == 0){
			  System.exit(0);
		  }
	  }
	
	  public static void main(String[] args) {
		try{
		  PEBL pebl = new PEBL();
		  pebl.execute();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
}
