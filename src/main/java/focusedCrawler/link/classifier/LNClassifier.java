package focusedCrawler.link.classifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.string.StopList;
import smile.classification.Classifier;
import weka.core.Instances;

public class LNClassifier {

	private final Classifier classifier;
	private final Instances instances;
	private final LinkNeighborhoodWrapper wrapper;
	private final String[] attributes;

	public LNClassifier(Classifier classifier, Instances instances,
	                    LinkNeighborhoodWrapper wrapper, String[] attributes) {
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
        synchronized (classifier) {
            weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
            instanceWeka.setDataset(instances);
            double[] probs = classifier.distributionForInstance(instanceWeka);
            return probs;
        }
	}
	
	public static LNClassifier create(String featureFilePath,
	                                  String modelFilePath,
	                                  StopList stoplist) {
	    ParameterFile config = new ParameterFile(featureFilePath); 
	    String[] attributes = config.getParam("ATTRIBUTES", " ");
	    String[] classValues = config.getParam("CLASS_VALUES", " ");
	    return create(attributes, classValues, modelFilePath, stoplist);
	}
	
	public static LNClassifier create(String[] attributes, String[] classValues,
	                                  String modelFilePath, StopList stoplist) {
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
	    
	    LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(attributes, stoplist);
	    
	    Classifier classifier = loadWekaClassifier(modelFilePath);
	    
	    return new LNClassifier(classifier, insts, wrapper, attributes);
	    
	}
    
    private static Classifier loadWekaClassifier(String modelFilePath) {
        try {
            InputStream is = new FileInputStream(modelFilePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            Classifier classifier = (Classifier) objectInputStream.readObject();
            objectInputStream.close();
            return classifier;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Failed to load weka classifier instance from file: " + modelFilePath, e);
        }
    }
	
}
