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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;
import focusedCrawler.util.Page;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

public class EMClassifier {

private String[] attributes;
	
	private StopList stoplist; 
	
	private String rootDir = "/home/lbarbosa/webdb/lbarbosa/improvClassifier_book";
	
	private String wekaFileDir = rootDir + File.separator + "wekaInput" + File.separator;

	private String cleanScript = "sh " + rootDir + "/script/runClean.sh";
	
	private String pageDir = rootDir + "/pageDir/";
	
//	private String pageDirTain = "/home/lbarbosa/webdb/lbarbosa/improvClassifier/pageDir_test/";
	private String pageDirTain = rootDir + "/pageDir/";
	
	private int posCount = 1000;
	
	private int negCount = 1000;
	
	private int iteration = 1;	

	public EMClassifier() throws IOException{
		this.stoplist = new StopListArquivo(rootDir + "/conf/stoplist.txt");
	}

	public void execute() throws Exception{
		//create C_form
		String model = buildClassifier("form");
		for (int i = 0; i < 200; i++) {
			System.out.println("Testing classifier...");
			testClassifier(wekaFileDir + File.separator + "weka_form_test", model);
			System.out.println("Runing classifier...");
			runDevSet(model,rootDir + File.separator + "devData_form", rootDir + File.separator + "output_form");
			Runtime.getRuntime().exec(cleanScript);
			System.out.println("ITERATION:" + iteration);
			System.out.println("Building classifier...");
			model = buildClassifier("form");
			iteration++;
		}
	}
	
	
	private String buildClassifier(String suffix) throws Exception{
		String trainingData = rootDir + File.separator + "trainData_" + suffix;
//		System.out.println("TRAIN:" +trainingData);
		String trainWekafile =  wekaFileDir + "weka_" + suffix;
		String testFileDir = rootDir + File.separator + "testData_" + suffix;
		String outputModel = rootDir + File.separator + "model" + File.separator + "model_" + suffix;
		CreateWekaInput createWekaFile = new CreateWekaInput(new File(trainingData),new File(testFileDir),stoplist);
		attributes = createWekaFile.centroid2Weka(trainWekafile);
		double max = Double.MIN_VALUE;
		double cValue = 0;
		int count = 0;
		for (double c = 0.0625; count < 1 ; c= c*0.5) {
			SMO classifier = new SMO();
			String[] argum = new String[]{"-t",trainWekafile, "-C", ""+c, "-v", "-M", "-d",outputModel+c};
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
	
	  private void runDevSet(String model, String developData, String outputDir) throws IOException, ClassNotFoundException, TargetClassifierException{
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
		  File[] list = new File(developData).listFiles();
		  int total = 0;
		  System.out.println("TOTAL:"+list.length);
		  Vector<VSMElement> posDistances = new Vector<VSMElement>();
		  Vector<VSMElement> negDistances = new Vector<VSMElement>();
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
				double[] prob = targetClassifier.distributionForInstance((new Page(null,src)));
				if(isTarget){
					posDistances.add(new VSMElement(list[i].toString(),prob[0]));
					FileOutputStream fout = new FileOutputStream(outputDir+ File.separator + list[i].getName(),false);
				    DataOutputStream dout = new DataOutputStream(fout);
				    dout.writeBytes(src);
				    dout.close();
				}else{
					negDistances.add(new VSMElement(list[i].toString(),prob[1]));
					FileOutputStream fout = new FileOutputStream(outputDir + "_non" + File.separator + list[i].getName(),false);
				    DataOutputStream dout = new DataOutputStream(fout);
				    dout.writeBytes(src);
				    dout.close();
					total++;	
				}
		  }
		  Collections.sort(posDistances,new VSMElementComparator());
		  Collections.sort(negDistances,new VSMElementComparator());
		  if(posDistances.size() < 1 && negDistances.size() < 1){
			  System.exit(0);
		  }

		  for (int i = 0; i < posDistances.size() && i < 10; i++) {
			  VSMElement elem = posDistances.elementAt(i);
			  String fileName = elem.getWord();
			  System.out.println(elem.getWeight() + " " + fileName);
			  Runtime.getRuntime().exec("mv " + fileName + " " + rootDir + File.separator + "trainData_form" + File.separator + "positive" + File.separator);
			  posCount++;
		  }
		  System.out.println("--------");
		  for (int i = 0; i < negDistances.size() && i < 10; i++) {
			  VSMElement elem = negDistances.elementAt(i);
			  String fileName = elem.getWord();
			  System.out.println(elem.getWeight() + " " + fileName);
			  Runtime.getRuntime().exec("mv " + fileName + " " + rootDir + File.separator + "trainData_form" + File.separator + "negative" + File.separator);
			  negCount++;
		  }

		  
		  System.out.println("NON:"+total);
	  }

	  public static void main(String[] args) {
		  try {
			  EMClassifier em = new EMClassifier();
			  em.execute();
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	}
