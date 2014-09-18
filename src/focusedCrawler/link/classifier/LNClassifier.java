package focusedCrawler.link.classifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;

import weka.classifiers.Classifier;
import weka.core.Instances;

import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.classifier.util.Instance;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.parser.LinkNeighborhood;

public class LNClassifier {

	private Classifier classifier;
	private Instances instances;
	private WrapperNeighborhoodLinks wrapper;
	private String[] attributes;

	public LNClassifier(Classifier classifier, Instances instances, WrapperNeighborhoodLinks wrapper, String[] attributes) {
		this.classifier = classifier;
		this.instances = instances;
		this.wrapper = wrapper;
		this.attributes = attributes;
	}
	
	public double[] classify(LinkNeighborhood ln) throws Exception {
		HashMap urlWords = wrapper.extractLinksFull(ln, attributes);
		Iterator iter = urlWords.keySet().iterator();
		String url = (String)iter.next();
		Instance instance = (Instance)urlWords.get(url);
//		String[] features = instance.getFeatures();
		double[] values = instance.getValues();
//		System.out.println("LN:" + ln.getAnchorString());
//		for (int i = 0; i < values.length; i++) {
//			if(values[i] > 0) { 
//				System.out.print(features[i] + "=" + values[i] + ",");
//			}
//		}
//		System.out.println("");
		weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
		instanceWeka.setDataset(instances);
		double[] probs = classifier.distributionForInstance(instanceWeka);
//		System.out.println(probs[0]);
		return probs;
	}

	public double[] classifyEP(LinkNeighborhood ln) throws Exception {
		HashMap urlWords = wrapper.extractLinksFull(ln, attributes);
		Iterator iter = urlWords.keySet().iterator();
		String url = (String)iter.next();
		Instance instance = (Instance)urlWords.get(url);
		double[] values = instance.getValues();
		int index = instance.getValues().length-2;
		values[index] = ln.getLink().getFile().split("/").length;
		index++;
		values[index] = ln.getLink().getFile().length();
		weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
		instanceWeka.setDataset(instances);
		return classifier.distributionForInstance(instanceWeka);
	}

	public static LNClassifier loadClassifier(String fileName) throws IOException, ClassNotFoundException{
		ParameterFile config = new ParameterFile(fileName);
		InputStream is = new FileInputStream(config.getParam("FILE_CLASSIFIER"));
		ObjectInputStream objectInputStream = new ObjectInputStream(is);
		Classifier classifier = (Classifier) objectInputStream.readObject();
		String[] attributes = config.getParam("ATTRIBUTES", " ");
		System.out.println(attributes.length);
		weka.core.FastVector vectorAtt = new weka.core.FastVector();
		for (int i = 0; i < attributes.length; i++) {
			vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
		}
		String[] classValues = config.getParam("CLASS_VALUES", " ");
		weka.core.FastVector classAtt = new weka.core.FastVector();
		for (int i = 0; i < classValues.length; i++) {
			classAtt.addElement(classValues[i]);
		}
		vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
		Instances insts = new Instances("target_classification", vectorAtt, 1);
		insts.setClassIndex(attributes.length);
		WrapperNeighborhoodLinks wrapper = LinkClassifierFactoryImpl.loadWrapper(attributes);
		LNClassifier lnClassifier = new LNClassifier(classifier, insts, wrapper, attributes);
		return lnClassifier;
	}


	
	
}
