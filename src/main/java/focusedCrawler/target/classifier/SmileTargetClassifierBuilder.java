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
package focusedCrawler.target.classifier;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import focusedCrawler.util.SmileUtil;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListFile;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;
import focusedCrawler.util.vsm.VSMVector;
import smile.classification.RandomForest;
import smile.classification.SVM;
import smile.data.AttributeDataset;
import smile.data.parser.ArffParser;
import smile.math.kernel.LinearKernel;
import smile.validation.ConfusionMatrix;
import smile.validation.CrossValidation;
import smile.math.Math;

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
public class SmileTargetClassifierBuilder {

	private static Logger logger = LoggerFactory.getLogger(SmileTargetClassifierBuilder.class);

    protected VSMVector[][] trainingExamples = null;

    protected VSMVector[][] testExamples = null;

	protected int numOfFeatures = Integer.MAX_VALUE;

	protected int minDF = 5;
    
	protected HashMap<String, VSMElement> df = new HashMap<>();

	protected boolean isForm = false;
  
	protected StopList stoplist;
	
	protected static int responseIndex = 0;
    public SmileTargetClassifierBuilder(File dir, File dirTest, StopList stoplist) throws SAXException, IOException {
    	this(dir,dirTest,stoplist,Integer.MAX_VALUE);
    }
    
    public SmileTargetClassifierBuilder(File input, File inputTest, StopList stoplist, int numOfElems) throws SAXException, IOException {
    	trainingExamples = new VSMVector[2][];
    	this.stoplist = stoplist;
    	if((new File (input + File.separator + "positive")).isDirectory()){
    		File[] positiveFiles = new File (input + File.separator + "positive").listFiles();
    		System.out.println("POSITIVE:" + positiveFiles.length);
    		File[] negativeFiles = new File (input + File.separator + "negative").listFiles();
    		System.out.println("NEGATIVE:" + negativeFiles.length);
    		int[] negIndexes = selectRandomNum(1,negativeFiles.length, numOfElems);
    		trainingExamples[1] = createVSM(negativeFiles, stoplist,negIndexes,true);
    		int[] posIndexes = selectRandomNum(1,positiveFiles.length, numOfElems);
    		trainingExamples[0] = createVSM(positiveFiles, stoplist,posIndexes,true);
    	}else{
    		trainingExamples[0] = createVSM(new File (input + File.separator + "positive"), stoplist);
    		trainingExamples[1] = createVSM(new File (input + File.separator + "negative"), stoplist);
    	}
    	if(inputTest != null){
    		testExamples = new VSMVector[2][];
    		if((new File (inputTest + File.separator + "positive")).isDirectory()){
    			File temp = new File (inputTest + File.separator + "positive");
    			System.out.println(temp.toString());
    			File[] positiveTestFiles = temp.listFiles();
    			trainingExamples[0] = createVSM(positiveTestFiles, stoplist,false);
    			File[] negativeTestFiles = new File (inputTest + File.separator + "negative").listFiles();
    			trainingExamples[1] = createVSM(negativeTestFiles, stoplist,false);
    		}else{
    			trainingExamples[0] = createVSM(new File (inputTest + File.separator + "positive"), stoplist);
    			trainingExamples[1] = createVSM(new File (inputTest + File.separator + "negative"), stoplist);
    		}
    	}
    }

    public SmileTargetClassifierBuilder(String[][] pages, StopList stoplist, int size) throws SAXException, IOException {
    	trainingExamples = new VSMVector[size][];
    	for (int i = 0; i < size; i++) {
			String[] levelPages = pages[i];
			trainingExamples[i] = createVSM(levelPages,stoplist);				
		}
    }

    
    private int[] selectRandomNum(long seed, int range, int elems){
    	if(elems > range){
    		elems = range;
    	}
    	int count = 0;
    	Random random = new Random(seed);
    	int next = random.nextInt(range);
    	HashSet<Integer> nums = new HashSet<>();
    	int[] result = new int[elems];
    	while(count < elems){
    		Integer num = new Integer(next);
    		if(!nums.contains(num)){
    			result[count] = next;
    			nums.add(num);
    			count++;
    		}
    		next = random.nextInt(range);
    	}
    	return result;
    }
  
    protected VSMVector[] createVSM(String[] pages, StopList stoplist) throws SAXException{
    	Vector<VSMVector> tempVSM = new Vector<VSMVector>();
    	for (int i = 0; i < pages.length; i++) {
    		try{
    			if(pages[i] == null){
    				continue;
    			}
    			VSMVector vsm = new VSMVector(pages[i],stoplist);
    			tempVSM.add(vsm);
    			Iterator<VSMElement> iterator1 = vsm.getElements();
    			while (iterator1.hasNext()) {
    				VSMElement elem = (VSMElement)iterator1.next();
    				VSMElement value = (VSMElement)df.get(elem.getWord());  
    				if(value == null){
    					df.put(elem.getWord(), new VSMElement(elem.getWord(),1));
    				}else{
    					df.put(elem.getWord(), new VSMElement(elem.getWord(),value.getWeight() +1));
    				}	
    			}
    		}catch(IOException ex){
			  ex.printStackTrace();
    		}
    	}
    	VSMVector[] examples = new VSMVector[tempVSM.size()];
    	tempVSM.toArray(examples);
    	return examples;
    }

    
    protected VSMVector[] createVSM(File file, StopList stoplist) throws SAXException{
    	Vector<VSMVector> tempVSM = new Vector<VSMVector>();
    	try{
    		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    		for(String line = reader.readLine(); line != null; line = reader.readLine()){
    			VSMVector vsm = new VSMVector(line,stoplist);
    			tempVSM.add(vsm);
    			Iterator<VSMElement> iterator1 = vsm.getElements();
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
    		reader.close();
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
    				Iterator<VSMElement> iterator1 = vsm.getElements();
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
    		}catch(IOException ex){
			  ex.printStackTrace();
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
  
    public String[] centroid2Smile(String output) throws FileNotFoundException,IOException {
    	OutputStream fout= new FileOutputStream(output,false);
    	OutputStream bout= new BufferedOutputStream(fout);
    	OutputStreamWriter outputFile = new OutputStreamWriter(bout);
    	StringBuffer header = new StringBuffer();
    	header.append("@RELATION TSFC");
    	header.append("\n");
    	header.append("\n");
    	StringBuffer tail = new StringBuffer();
  
    	Vector<VSMElement> bestWordsForm  = new Vector<>(df.values());
    	Collections.sort(bestWordsForm, new VSMElementComparator());
    	for(int i=0; i<=numOfFeatures && i < bestWordsForm.size(); i++){
    		VSMElement elem = (VSMElement)bestWordsForm.elementAt(i);
    		if(elem.getWeight() > minDF){
    			header.append("@ATTRIBUTE ");
                if(elem.getWord().equals("class")){
                    //This is a hack, weka does not allow attribute with name class.
                    elem.setWord("class-random-string");
                }
    			header.append(elem.getWord());
    			attributes.add(elem.getWord());
    			header.append(" REAL");
    			header.append("\n");
    			responseIndex++;
    		}
    	}
    	header.append("@ATTRIBUTE class {");
    	for (int i = 0; i < trainingExamples.length-1; i++) {
    		header.append("CLASS_"+i+",");
    	}
    	header.append("CLASS_"+ (trainingExamples.length-1) +"}");

    	
    	tail.append("\n");
    	tail.append("\n");
    	tail.append("@DATA");
    	tail.append("\n");
    	for (int l = 0; l < trainingExamples.length; l++) {
        	for (int i = 0; i < trainingExamples[l].length; i++) {
        		VSMVector formTemp = trainingExamples[l][i];
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
        		tail.append(attributes.size() + " CLASS_"+l+"}");
        		tail.append("\n");
        	}
		}
    	outputFile.write(header.toString());
    	outputFile.flush();
    	outputFile.write(tail.toString());
    	outputFile.close();
    	if(testExamples != null){
    		createTestFile(output, bestWordsForm,header);
    	}
    	String[] atts = new String[attributes.size()];
    	attributes.toArray(atts);
    	return atts;
    }

    private void createTestFile(String output, Vector<VSMElement> bestWordsForm, StringBuffer header) throws
            FileNotFoundException, IOException {
    	OutputStream fout= new FileOutputStream(output+"_test",false);
    	OutputStream bout= new BufferedOutputStream(fout);
    	OutputStreamWriter outputFile = new OutputStreamWriter(bout);
    	StringBuffer tail = new StringBuffer();
    	tail.append("\n");
    	tail.append("\n");
    	tail.append("@DATA");
    	tail.append("\n");
    	for (int l = 0; l < testExamples.length; l++) {
        	for (int i = 0; i < testExamples[l].length; i++) {
        		VSMVector examples = testExamples[l][i];
        		tail.append("{");
        		for (int j = 0; j < attributes.size(); j++) {
                	VSMElement elemForm = examples.getElement(attributes.elementAt(j));
                    if (elemForm != null){
                    	tail.append(j);
                    	tail.append(" ");
                        tail.append((int)elemForm.getWeight());
                        tail.append(",");
                    }
        		}
        		tail.append(attributes.size() + " CLASS_"+l+"}");
        		tail.append("\n");
        	}
		}
    	outputFile.write(header.toString());
    	outputFile.flush();
    	outputFile.write(tail.toString());
    	outputFile.close();
    }

  
    public static void createInputFile(String stopwordsFile,
                                       String trainingPath,
                                       String smileInputFile) {
    	try {
    	    StopList stopwords = null;
    		File testDataPath = null;
    		File trainingDataPath = new File(trainingPath);
    		
    		if(stopwordsFile != null && !stopwordsFile.isEmpty()) {
    		    stopwords = new StopListFile(stopwordsFile);
    		} else {
    		    stopwords = StopListFile.DEFAULT;
    		}
    		
    		SmileTargetClassifierBuilder builder =
                    new SmileTargetClassifierBuilder(trainingDataPath, testDataPath, stopwords);
            builder.centroid2Smile(smileInputFile);
    	}
    	catch (SAXException | IOException ex1) {
    		throw new RuntimeException("Failed to generate smile input file.", ex1);
    	}
    }
    
    public static void trainModel(String trainingPath, String outputPath, String learner) throws IOException, ParseException  {
        if(learner==null) {
            learner = "SVM";
        }
        
        System.out.println("Training "+ learner+" model...");
        String modelFilePath = outputPath + "/pageclassifier.model";
		ArffParser arffParser = new ArffParser();
		arffParser.setResponseIndex(responseIndex);
		FileInputStream fis = new FileInputStream(new File(trainingPath + "/smile_input.arff")); 
		System.out.println(trainingPath + "/smile_input.arff");
		AttributeDataset trainingData = arffParser.parse(fis);
		double[][] x = trainingData.toArray(new double[trainingData.size()][]);
		int[] y = trainingData.toArray(new int[trainingData.size()]);
		
		if(learner.equals("SVM")) {
			long start = System.currentTimeMillis();
			//Train the Model
        	SVM<double[]> svmModel = new SVM<double[]>(new LinearKernel(), 0.01);
        	svmModel.learn(x, y);
        	svmModel.finish();
        	svmModel.trainPlattScaling(x, y);
        	long timeTaken = (System.currentTimeMillis() - start)/1000; 
        	System.out.println("Time taken to build model: " +  timeTaken +" seconds");
        	//Test on training data. 
        	start = System.currentTimeMillis();
        	int[] predictedY = new int[y.length]; 
        	predictedY = svmModel.predict(x);
        	timeTaken = (System.currentTimeMillis() - start)/1000;
        	System.out.println("Time taken to test model on training data:" + timeTaken + " seconds");
        	System.out.println();
        	System.out.println();
        	System.out.println("=== Error on training data ===");
        	printStatistics(predictedY, y);
        	
        	System.out.println("=== Cross-validation ===");
        	int numberOfFolds = 10;
        	CrossValidation cv = new CrossValidation(y.length, numberOfFolds);
        	int minError = Integer.MAX_VALUE;
        	SVM<double[]> finalModel = null;
        	for (int i = 0; i < numberOfFolds; i++) {
        		double[][] trainx = Math.slice(x, cv.train[i]);
        	    int[] trainy = Math.slice(y, cv.train[i]);
        	    SVM<double[]> svm = new SVM<>(new LinearKernel(), 0.01);
        	    svm.learn(trainx, trainy);
        	    svmModel.finish();
            	svmModel.trainPlattScaling(x, y);
            	int error = 0;
            	int[] index = cv.test[i];
        	    for(int v: index) {
        	    	if (y[v] != svm.predict(x[v])) {
        	            error++;
        	    	}
        	    }
        	    if(error < minError) {
        	    	finalModel = svm;
        	    }
        	}
        	predictedY = finalModel.predict(x);
        	printStatistics(predictedY, y);
        	SmileUtil.writeSmileClassifier(modelFilePath, finalModel);
        } else if(learner.equals("RandomForest")) {
//        	TODO: CrossValidation crossValidation = new CrossValidation(trainingData.size(), 5);
        	RandomForest randomForest = new RandomForest(x, y, 100);
        	SmileUtil.writeSmileClassifier(modelFilePath, randomForest);
        } else {
            System.out.println("Unknow learner: "+learner);
            return;
        }
    }
    
    private static void printStatistics(int[] predictedY, int[]  y) {
    	int totalNumberOfInstances = y.length;
    	int correctlyClassifiedInstances = getCountOfCorrectlyClassifiedInstances(predictedY, y);
    	
    	System.out.println("Correctly Classified Instances: " + correctlyClassifiedInstances );
    	System.out.println("Incorrectly Classified Instances: " + (totalNumberOfInstances - correctlyClassifiedInstances));
    	System.out.println("Total Number of Instances: " +  totalNumberOfInstances );  
    	System.out.println();
    	System.out.println();
    	System.out.println("=== Confusion Matrix ===");
    	ConfusionMatrix confusionMatrix = new ConfusionMatrix(y , predictedY);
    	System.out.println(confusionMatrix);
	}

	private static int getCountOfCorrectlyClassifiedInstances(int[] prediction, int[] truth){
    	int count = 0;
    	for(int i = 0; i < truth.length; i++) {
    		if(prediction[i] == truth[i]) {
    			count++;
    		}
    	}
    	return count;
    }
    public static void createFeaturesFile(String outputPath, String trainingPath) {
        File features = new File(outputPath + File.separator + "pageclassifier.features");
        try {
            features.createNewFile();
            FileWriter featuresWriter = new FileWriter(features);
            //featuresWriter.write("");
            featuresWriter.write("CLASS_VALUES  S NS" + "\n" + "ATTRIBUTES");
            String wekkaFilePath = trainingPath + "/smile_input.arff";
            Scanner wekkaFileScanner = new Scanner(new File(wekkaFilePath));
            while(wekkaFileScanner.hasNext()){
                String nextLine = wekkaFileScanner.nextLine();
                String[] splittedLine = nextLine.split(" ");
                if(splittedLine.length>=3 && splittedLine[0].equals("@ATTRIBUTE") && splittedLine[2].equals("REAL"))
                    featuresWriter.write(" "+splittedLine[1]);
            }
            featuresWriter.write("\n");
            wekkaFileScanner.close();
            featuresWriter.flush();
            featuresWriter.close();
        } catch (IOException e) {
            logger.error("IO Exception while creating wekka pageclassifier.features file. ",e);
        }
    }

}
