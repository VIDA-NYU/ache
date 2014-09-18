package focusedCrawler.link.classifier;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

public class LinkClassifierBreadthSearch implements LinkClassifier{

	private WrapperNeighborhoodLinks wrapper;
	private String[] attributes;
	private Random randomGenerator;

	public LinkClassifierBreadthSearch(WrapperNeighborhoodLinks wrapper,String[] attribute) {
		this.wrapper = wrapper;
		this.attributes = attribute;
		this.randomGenerator = new Random();
	}

	  
	public LinkRelevance[] classify(PaginaURL page)
			throws LinkClassifierException {
	     LinkRelevance[] linkRelevance = null;

	     HashMap urlWords = null;
	     try {
	    	 urlWords = wrapper.extractLinks(page, attributes);
	          //urlWords = wrapper.extractLinks(page);
	    	 linkRelevance = new LinkRelevance[urlWords.size()];
	    	 Iterator iter = urlWords.keySet().iterator();
	          int count = 0;
	          while (iter.hasNext()) {
	            String url = (String) iter.next();
	            int level = (int) (page.getRelevance()/100);
	            double relevance = (level-1) * 100 + randomGenerator.nextInt(100);
	            if(relevance < -1){
	            	relevance = -1;
	            }
	            linkRelevance[count] = new LinkRelevance(new URL(url), relevance);
	            count++;
	            }
	        }
	        catch (MalformedURLException ex) {
	          ex.printStackTrace();
	          throw new LinkClassifierException(ex.getMessage());
	        }
	        return linkRelevance;	
	}

	public LinkRelevance classify(LinkNeighborhood ln)
			throws LinkClassifierException {
		// TODO Auto-generated method stub
		return null;
	}

}
