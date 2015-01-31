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

import java.util.StringTokenizer;

import weka.classifiers.functions.SMO;
import weka.classifiers.Evaluation;


public class ClassifyBuilder {

	
  public String buildClassifier(String trainFile, String outputModel) throws Exception{
	  double max = Double.MIN_NORMAL;
	  double cValue = 0;
	  for (double c = 1; c > 0.1 ; c= c-0.2) {
		  SMO classifier = new SMO();
		  String[] argum = new String[]{"-t",trainFile, "-C", ""+c, "-v", "-d",outputModel+c};
		  String output = Evaluation.evaluateModel(classifier, argum);
		  int index = output.indexOf("Correctly Classified Instances");
		  if(index >= 0){
			  int end = output.indexOf("%",index);
			  String line = (output.substring(index,end)).trim();
			  line = line.substring(line.lastIndexOf(" "));
			  double accuracy = Double.parseDouble(line.trim());
			  if(accuracy > max){
				  max = accuracy;
				  cValue = c;
			  }
		  }	      
	  }
	  System.out.println("C:" + cValue);
	  return outputModel+cValue;
  }
	
  public void testClassifier(String testFile, String outputModel) throws Exception{
	  SMO classifier = new SMO();
	  String[] argum = new String[]{"-T",testFile,"-l",outputModel,"-i"};
	  String output = Evaluation.evaluateModel(classifier, argum);
	  int index = output.indexOf("F-Measure");
	  if(index >= 0){
		  index = output.indexOf("\n",index);
		  int end = output.indexOf("\n",index+1);
		  String line = (output.substring(index,end)).trim();
		  System.out.println(line);
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
  }

}
