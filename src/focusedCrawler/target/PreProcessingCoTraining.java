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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.xml.sax.SAXException;

import focusedCrawler.util.vsm.VSMVector;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;


public class PreProcessingCoTraining {

	private StopList stoplist;
	
	private HashMap<String,Vector<String>> hash = new HashMap<String, Vector<String>>();
	
	public PreProcessingCoTraining() throws IOException{
		stoplist = new StopListArquivo("/home/lbarbosa/webdb/lbarbosa/improvClassifier/conf/stoplist.txt");
	}
	
	
	public void execute(String formData, String posDir, String outputDir) throws MalformedURLException, IOException, SAXException{
		loadHash(formData);
		VSMVector pos = loadPosCentroid(posDir);
		Iterator<String> iter = hash.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next(); 
			Vector<String> formFiles = hash.get(key);
			double max = Double.MIN_VALUE;
			String result = "";
			if(formFiles != null && formFiles.size() > 1){
				for (int i = 0; i < formFiles.size(); i++) {
					VSMVector formExample = new VSMVector(formFiles.elementAt(i),false,stoplist);
					formExample.normalize();
					double sim = pos.vectorSpaceSimilarity(formExample);
					if(sim > max){
						max = sim;
						result = formFiles.elementAt(i);
					}
				}
//				System.out.println("cp " + result + " " + outputDir + File.separator + key);
				Runtime.getRuntime().exec("cp " + result + " " + outputDir + File.separator + key);
			}else{
				if(formFiles != null && formFiles.size() > 0){
//					System.out.println("cp " + result + " " + outputDir + File.separator + key);
					Runtime.getRuntime().exec("cp " + formFiles.firstElement() + " " + outputDir + File.separator + key);
				}
			}
			
		}
	}
	
	private void loadHash(String formData){

		File[] forms = new File(formData).listFiles();
		for (int i = 0; i < forms.length; i++) {
			String name = forms[i].getName();
			name = name.substring(0,name.indexOf("_"));
			Vector<String> value = hash.get(name);
			if(value == null){
				value = new Vector<String>();
			}
			value.add(forms[i].toString());
			hash.put(name, value);
		}

	}
	
	private VSMVector loadPosCentroid(String trainDir) throws MalformedURLException, IOException, SAXException{
		File[] positiveFiles = new File(trainDir).listFiles();
		VSMVector posVector = new VSMVector();
		for (int i = 0; i < positiveFiles.length; i++) {
			VSMVector positiveFormExample = new VSMVector(positiveFiles[i].toString(),false,stoplist);
			positiveFormExample.normalize();
			posVector.addVector(positiveFormExample);
		}
		return posVector;
	}
	
	public static void main(String[] args) {
		try {
			PreProcessingCoTraining pt = new PreProcessingCoTraining();
			pt.execute(args[0], args[1], args[2]);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
}
