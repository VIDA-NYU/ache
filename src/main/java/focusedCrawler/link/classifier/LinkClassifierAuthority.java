package focusedCrawler.link.classifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import weka.classifiers.Classifier;
import weka.core.Instances;

import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.link.classifier.util.Instance;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

public class LinkClassifierAuthority implements LinkClassifier{

	  private WrapperNeighborhoodLinks wrapper;
	  private String[] attributes;
	  private Classifier classifier;
	  private Instances instances;
	  
	  public LinkClassifierAuthority(Classifier classifier, Instances instances, WrapperNeighborhoodLinks wrapper,String[] attributes) {
		  this.wrapper = wrapper;
		  this.attributes = attributes;
		  this.classifier = classifier;
		  this.instances = instances;
	  }
	  
	  public LinkClassifierAuthority() {
	  }

	  
	  public LinkClassifierAuthority(WrapperNeighborhoodLinks wrapper,String[] attributes) {
		  this.wrapper = wrapper;
		  this.attributes = attributes;
	  }

	  
	  public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {
		  LinkRelevance[] linkRelevance = null;
		  try {
			  if(classifier != null){
				  HashMap urlWords = wrapper.extractLinks(page, attributes);
				  linkRelevance = new LinkRelevance[urlWords.size()];
		          Iterator iter = urlWords.keySet().iterator();
		          int count = 0;
		          while (iter.hasNext()) {
		        	  String urlStr = (String) iter.next();
		        	  URL url = new URL(urlStr);
		        	  double relevance = -1;
		        	  if(!page.getURL().getHost().equals(url.getHost())){
		        		  Instance instance = (Instance)urlWords.get(urlStr);
		        		  double[] values = instance.getValues();
		        		  weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
		        		  instanceWeka.setDataset(instances);
		        		  double[] prob = classifier.distributionForInstance(instanceWeka);
		        		  relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE + (prob[0]*100);
		        	  }
			          linkRelevance[count] = new LinkRelevance(url, relevance);
			          count++;
		          }
			  }else{
				  LinkNeighborhood[] lns = page.getLinkNeighboor();
				  linkRelevance = new LinkRelevance[lns.length];
				  for (int i = 0; i < lns.length; i++) {
					  double relevance = -1;
					  if(!page.getURL().getHost().equals(lns[i].getLink().getHost())){
						  relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE+1;
					  }
					  linkRelevance[i] = new LinkRelevance(lns[i].getLink(), relevance);
				  }
			  }
		  }catch (MalformedURLException ex) {
			  ex.printStackTrace();
			  throw new LinkClassifierException(ex.getMessage());
		  }catch (Exception e) {
			  e.printStackTrace();
			  throw new LinkClassifierException(e.getMessage());
		  }
		  return linkRelevance;
	  }

	@Override
	public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
		  LinkRelevance linkRel = null;
		  try{
		      HashMap urlWords = wrapper.extractLinks(ln, attributes);
		      Iterator iter = urlWords.keySet().iterator();
		      while(iter.hasNext()){
		    	  String url = (String)iter.next();
		    	  double relevance = -1;
		    	  if(isRootPage(url)){
		    		  if(classifier != null){
		    			  Instance instance = (Instance)urlWords.get(url);
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
	    		  linkRel = new LinkRelevance(new URL(url),relevance);
		      }
		  } catch (MalformedURLException ex) {
			  ex.printStackTrace();
			  throw new LinkClassifierException(ex.getMessage());
		  } catch (Exception ex) {
			  ex.printStackTrace();
			  throw new LinkClassifierException(ex.getMessage());
		  }
		  return linkRel;
	}
	  
		  
	private boolean isInternalLink(String host, String urlStr) throws IOException{
		URL url = new URL(urlStr);
		return url.getHost().endsWith(host);
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
