package focusedCrawler.link.classifier;

import java.io.Reader;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class ClassifierInstance {

	
	public Classifier loadClassifier(Reader reader) throws Exception{
		Instances data = new Instances(reader);
		reader.close();
		data.setClassIndex(data.numAttributes() - 1);
		 // create new instance of scheme
		Classifier classifier = new weka.classifiers.functions.SMO();
		 // set options
		classifier.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
		classifier.buildClassifier(data); 
		return classifier;
	}
	
}
