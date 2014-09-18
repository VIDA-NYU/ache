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

public class BackLinkClassifierImpl implements LinkClassifier{

	private Classifier classifier;
	private Instances instances;
	private WrapperNeighborhoodLinks wrapper;
	private String[] attributes;
	
	public BackLinkClassifierImpl(){
		
	}
	
	public BackLinkClassifierImpl(Classifier classifier, Instances instances, WrapperNeighborhoodLinks wrapper,String[] attributes) {
		this.classifier = classifier;
		this.instances = instances;
		this.wrapper = wrapper;
		this.attributes = attributes;
	}
	
	public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
//		System.out.println("ATTRIBUTES:" + attributes.length);
		LinkRelevance result = null;
		try {
			if(classifier == null){
				result = new LinkRelevance(ln.getLink(),101);				
			}else{
				HashMap urlWords = wrapper.extractLinks(ln, attributes);
				Iterator iter = urlWords.keySet().iterator();
				while(iter.hasNext()){
					String url = (String)iter.next();
//			        System.out.println(">>>>CLASSIF URL:"+url);
			        Instance instance = (Instance)urlWords.get(url);
//			        System.out.println(ln.getAroundString());
			        double[] values = instance.getValues();
//			        for (int i = 0; i < values.length; i++) {
//						if(values[i] > 0){
//							System.out.println(i+":" + values[i]);
//						}
//					}
			        weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
			        instanceWeka.setDataset(instances);
//			        double classificationResult = classifier.classifyInstance(instanceWeka);
//			        System.out.println("RELEVANCE:"+classificationResult);
			        double[] prob = classifier.distributionForInstance(instanceWeka);
//			        System.out.println("PROB:"+prob[0] + ":" + prob[1]);
			        double relevance = prob[0]*100+100;
			        result = new LinkRelevance(ln.getLink(),relevance);
				}
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
