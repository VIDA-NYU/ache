package focusedCrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.parser.LinkNeighborhood;
import smile.classification.Classifier;

public class LinkClassifierAuthority implements LinkClassifier{

	  private LinkNeighborhoodWrapper wrapper;
	  private String[] attributes;
	  private Classifier classifier;
	  private Instances instances;
	  
	  public LinkClassifierAuthority(Classifier classifier, Instances instances, LinkNeighborhoodWrapper wrapper,String[] attributes) {
		  this.wrapper = wrapper;
		  this.attributes = attributes;
		  this.classifier = classifier;
		  this.instances = instances;
	  }
	  
	  public LinkClassifierAuthority() {
	  }

	  public LinkClassifierAuthority(LinkNeighborhoodWrapper wrapper,String[] attributes) {
		  this.wrapper = wrapper;
		  this.attributes = attributes;
	  }
	  
	  public LinkRelevance[] classify(Page page) throws LinkClassifierException {
		  try {
		      LinkNeighborhood[] lns = page.getParsedData().getLinkNeighborhood();
		      
		      LinkRelevance[] linkRelevance = null;
			  if(classifier != null){
				  HashMap<String, Instance> urlWords = wrapper.extractLinks(lns, attributes);
				  linkRelevance = new LinkRelevance[urlWords.size()];
		          
		          int count = 0;
		          
		          for (Map.Entry<String, Instance> entry : urlWords.entrySet()) {
		              
		        	  URL url = new URL(entry.getKey());
		        	  double relevance = -1;
		        	  if(!page.getURL().getHost().equals(url.getHost())){
		        		  Instance instance = entry.getValue();
		        		  double[] values = instance.getValues();
		        		  weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
		        		  instanceWeka.setDataset(instances);
		        		  double[] prob = classifier.distributionForInstance(instanceWeka);
		        		  relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE + (prob[0]*100);
		        	  }
			          linkRelevance[count] = new LinkRelevance(url, relevance);
			          count++;
		          }
			  } else {
				  linkRelevance = new LinkRelevance[lns.length];
				  for (int i = 0; i < lns.length; i++) {
					  double relevance = -1;
					  if(!page.getURL().getHost().equals(lns[i].getLink().getHost())){
						  relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE+1;
					  }
					  linkRelevance[i] = new LinkRelevance(lns[i].getLink(), relevance);
				  }
			  }
			  return linkRelevance;
			  
		  } catch (Exception e) {
			  throw new LinkClassifierException(e.getMessage(), e);
		  }
	  }

	@Override
	public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
		  LinkRelevance linkRel = null;
		  try{
		      HashMap<String, Instance> urlWords = wrapper.extractLinks(ln, attributes);
		      
	    	  for (Map.Entry<String, Instance> entry : urlWords.entrySet()) {
		    	  double relevance = -1;
		    	  if(isRootPage(entry.getKey())){
		    		  if(classifier != null){
		    			  Instance instance = (Instance) entry.getValue();
		    			  double[] values = instance.getValues();
		    			  weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
		    			  instanceWeka.setDataset(instances);
		    			  double[] prob = classifier.distributionForInstance(instanceWeka);
		    			  if(prob[0] == 1){
		    				  prob[0] = 0.99;
		    			  }
	    				  relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE + (prob[0]*100);		    			  
		    		  }else{
		    			  relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE+1;		            	
		    		  }
		    	  }
	    		  linkRel = new LinkRelevance(new URL(entry.getKey()),relevance);
		      }
		  } catch (Exception e) {
			  throw new LinkClassifierException("Failed to classify link", e);
		  }
		  return linkRel;
	}
		  
	private boolean isRootPage(String urlStr) throws MalformedURLException {
		boolean result = false;
		URL url = new URL(urlStr);
		String file = url.getFile();
		if(file.equals("/") || file.equals("") || file.equals("index.htm") || file.equals("index.html")){
			result = true;
		}
		return result;
	}

}
