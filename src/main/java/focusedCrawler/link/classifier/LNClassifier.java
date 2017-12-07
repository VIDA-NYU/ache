package focusedCrawler.link.classifier;

import java.util.Iterator;
import java.util.Map;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.SmileUtil;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.string.StopList;
import smile.classification.SoftClassifier;

public class LNClassifier {

	private final SoftClassifier<double[]> classifier;
	private final LinkNeighborhoodWrapper wrapper;
	private final String[] attributes;

	
	public LNClassifier(SoftClassifier<double[]> classifier, LinkNeighborhoodWrapper wrapper,
			String[] attributes) {
		this.classifier = classifier;
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
        	double[] prob = new double[2];
	        int predictedValue = classifier.predict(values, prob);
        	return prob;
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
	    LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(attributes, stoplist);
	    SoftClassifier<double[]> classifier = SmileUtil.loadSmileClassifier(modelFilePath);
	    return new LNClassifier(classifier, wrapper, attributes);
	}
	
}
