package focusedCrawler.link.classifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.core.Instances;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.classifier.util.Instance;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.string.StopList;

public class LNClassifier {

	private Classifier classifier;
	private Instances instances;
	private WrapperNeighborhoodLinks wrapper;
	private String[] attributes;

	public LNClassifier(Classifier classifier, Instances instances,
	                    WrapperNeighborhoodLinks wrapper, String[] attributes) {
		this.classifier = classifier;
		this.instances = instances;
		this.wrapper = wrapper;
		this.attributes = attributes;
	}
	
	public double[] classify(LinkNeighborhood ln) throws Exception {
		Map<String, Instance> urlWords = wrapper.extractLinksFull(ln, attributes);
		Iterator<String> iter = urlWords.keySet().iterator();
		String url = iter.next();
		Instance instance = (Instance)urlWords.get(url);
		double[] values = instance.getValues();
		weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
		instanceWeka.setDataset(instances);
		double[] probs = classifier.distributionForInstance(instanceWeka);
		return probs;
	}
	
	public static LNClassifier create(String featureFilePath,
	                                  String modelFilePath,
	                                  StopList stoplist)
                                      throws ClassNotFoundException,
                                             IOException {
	    ParameterFile config = new ParameterFile(featureFilePath); 
	    String[] attributes = config.getParam("ATTRIBUTES", " ");
	    String[] classValues = config.getParam("CLASS_VALUES", " ");
	    return create(attributes, classValues, modelFilePath, stoplist);
	}
	
	public static LNClassifier create(String[] attributes, String[] classValues,
	                                  String modelFilePath, StopList stoplist)
                                      throws ClassNotFoundException,
                                             IOException {
	    weka.core.FastVector vectorAtt = new weka.core.FastVector();
	    for (int i = 0; i < attributes.length; i++) {
	        vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
	    }
	    weka.core.FastVector classAtt = new weka.core.FastVector();
	    for (int i = 0; i < classValues.length; i++) {
	        classAtt.addElement(classValues[i]);
	    }
	    vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
	    Instances insts = new Instances("link_classification", vectorAtt, 1);
	    insts.setClassIndex(attributes.length);
	    
	    
	    WrapperNeighborhoodLinks wrapper = loadWrapper(attributes, stoplist);
	    
	    Classifier classifier = loadClassifier(modelFilePath);
	    
	    return new LNClassifier(classifier, insts, wrapper, attributes);
	    
	}
    
    public static WrapperNeighborhoodLinks loadWrapper(String[] attributes, StopList stoplist) {
        WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);
        wrapper.setFeatures(attributes);
        return wrapper;
    }
    
    private static Classifier loadClassifier(String modelFilePath) 
            throws IOException, ClassNotFoundException {
        InputStream is = null;
        try {
            is = new FileInputStream(modelFilePath);
        }
        catch (FileNotFoundException ex1) {
            // FIXME
            ex1.printStackTrace();
        }
        ObjectInputStream objectInputStream = new ObjectInputStream(is);
        Classifier classifier = (Classifier) objectInputStream.readObject();
        objectInputStream.close();
        return classifier;
    }
	
}
