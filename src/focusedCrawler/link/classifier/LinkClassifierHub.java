package focusedCrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Instances;

import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.classifier.util.Instance;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

/**
 * This class implements the link classifier for the hub links.
 * @author lbarbosa
 *
 */

public class LinkClassifierHub implements LinkClassifier{

	private Classifier classifier;
	private Instances instances;
	private WrapperNeighborhoodLinks wrapper;
	private String[] attributes;
	
	public LinkClassifierHub(){
		
	}
	
	public LinkClassifierHub(Classifier classifier, Instances instances, WrapperNeighborhoodLinks wrapper,String[] attributes) {
		this.classifier = classifier;
		this.instances = instances;
		this.wrapper = wrapper;
		this.attributes = attributes;
	}
	
	public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
		LinkRelevance result = null;
		try {
			if(classifier == null){
				result = new LinkRelevance(ln.getLink(),LinkRelevance.DEFAULT_HUB_RELEVANCE+1);				
			}else{
				HashMap urlWords = wrapper.extractLinks(ln, attributes);
				Iterator iter = urlWords.keySet().iterator();
				while(iter.hasNext()){
					String url = (String)iter.next();
			        Instance instance = (Instance)urlWords.get(url);
			        double[] values = instance.getValues();
			        weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
			        instanceWeka.setDataset(instances);
			        double[] prob = classifier.distributionForInstance(instanceWeka);
			        double relevance = LinkRelevance.DEFAULT_HUB_RELEVANCE + prob[0]*100;
			        result = new LinkRelevance(ln.getLink(),relevance);
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public LinkRelevance[] classify(PaginaURL page)
			throws LinkClassifierException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
