package focusedCrawler.link.classifier;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.parser.LinkNeighborhood;
import smile.classification.SoftClassifier;


/**
 * This class implements the link classifier for the hub links.
 * @author lbarbosa
 *
 */
public class LinkClassifierHub implements LinkClassifier{

	private SoftClassifier<double[]> classifier;
	private LinkNeighborhoodWrapper wrapper;
	private String[] attributes;
	
	public LinkClassifierHub(){
		
	}
	
	public LinkClassifierHub(SoftClassifier<double[]> classifier, LinkNeighborhoodWrapper wrapper,String[] attributes) {
		this.classifier = classifier;
		this.wrapper = wrapper;
		this.attributes = attributes;
	}
	
	public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
		LinkRelevance result = null;
		try {
			if(classifier == null){
				result = new LinkRelevance(ln.getLink(),LinkRelevance.DEFAULT_HUB_RELEVANCE+1);				
			}else{
				Map<String, Instance> urlWords = wrapper.extractLinks(ln, attributes);
				Iterator<String> iter = urlWords.keySet().iterator();
				while(iter.hasNext()){
					String url = (String)iter.next();
			        Instance instance = (Instance)urlWords.get(url);
			        double[] values = instance.getValues();
			        double[] prob = new double[2];
			        classifier.predict(values, prob);
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
	public LinkRelevance[] classify(Page page)
			throws LinkClassifierException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
