package focusedCrawler.link.classifier;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.link.classifier.builder.WordField;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListFile;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class LinkClassifierRegression implements LinkClassifier{

	  private Classifier classifier;
	  private Instances instances;
	  private LinkNeighborhoodWrapper wrapper;
	  private String[] attributes;
	  
	  public LinkClassifierRegression(Classifier classifier, Instances instances, LinkNeighborhoodWrapper wrapper, String[] attributes) {
		  this.classifier = classifier;
		  this.instances = instances;
		  this.wrapper = wrapper;
		  this.attributes = attributes;
	  }
	  
	public LinkRelevance[] classify(Page page)
			throws LinkClassifierException {
	    LinkRelevance[] linkRelevance = null;
	    try {
	      Map<String, Instance> urlWords = wrapper.extractLinks(page, attributes);
	      linkRelevance = new LinkRelevance[urlWords.size()];
	      Iterator<String> iter = urlWords.keySet().iterator();
	      int count = 0;
	      while(iter.hasNext()){
	        String url = (String)iter.next();
	        Instance instance = (Instance)urlWords.get(url);
	        double[] values = instance.getValues();
	        weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
	        instanceWeka.setDataset(instances);
	        double classificationResult = classifier.classifyInstance(instanceWeka);
//	        double[] prob = classifier.distributionForInstance(instanceWeka);
	        double relevance = -1;
	        if(isInitialPage(url)){
	        	relevance = classificationResult*100 + 99;
	        }else{
//	        	relevance = 100 + (prob[(int)classificationResult]*100)-1;
	        }
	        	
//	        System.out.println(">>>>RELEVANCE:" + relevance);
//	        double relevance = classificationResult*100 + random.nextInt(100);
	        linkRelevance[count] = new LinkRelevance(new URL(url),relevance);
	        count++;
	      }
	    }catch (MalformedURLException ex) {
	      ex.printStackTrace();
	      throw new LinkClassifierException(ex.getMessage());
	    }catch(Exception ex){
	      ex.printStackTrace();
	      throw new LinkClassifierException(ex.getMessage());
	    }
	    return linkRelevance;
	}

	private boolean isInitialPage(String urlStr) throws MalformedURLException {
	     boolean result = false;
	     URL url = new URL(urlStr);
	     String file = url.getFile();
	     if(file.equals("/") || file.equals("")){
	       result = true;
	     }
	     return result;
	}
	
	public LinkRelevance classify(LinkNeighborhood ln)
			throws LinkClassifierException {
	    LinkRelevance linkRel = null;
	    try {
	      Map<String, Instance> urlWords = wrapper.extractLinks(ln, attributes);
	      Iterator<String> iter = urlWords.keySet().iterator();
	      while(iter.hasNext()){
	        String url = (String)iter.next();
	        Instance instance = (Instance)urlWords.get(url);
	        double[] values = instance.getValues();
	        weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
	        instanceWeka.setDataset(instances);
	        double classificationResult = classifier.classifyInstance(instanceWeka);
	        double[] prob = classifier.distributionForInstance(instanceWeka);
	        double relevance = classificationResult*100 + prob[(int)classificationResult]*100;	
	        linkRel = new LinkRelevance(new URL(url),relevance);
	      }
	    }
	    catch (MalformedURLException ex) {
	      ex.printStackTrace();
	      throw new LinkClassifierException(ex.getMessage());
	    }
	    catch (Exception ex) {
	      ex.printStackTrace();
	      throw new LinkClassifierException(ex.getMessage());
	    }
	    return linkRel;
	}
	
	public static void main(String[] args) {
		try{
		ParameterFile config = new ParameterFile(args[0]);
		LinkClassifier linkClassifier = null;
	    StopList stoplist = new StopListFile(config.getParam("STOPLIST_FILES"));
	      LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(stoplist);
	      String[] attributes = config.getParam("ATTRIBUTES", " ");
	      
	      String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];
	      List<String> tempURL = new ArrayList<String>();
	      List<String> tempAnchor = new ArrayList<String>();
	      List<String> tempAround = new ArrayList<String>();
	      
	      for (int i = 0; i < attributes.length; i++) {
	    	  if(attributes[i].contains("url_")){
	    		  tempURL.add(attributes[i]);
	    	  }
	    	  if(attributes[i].contains("anchor_")){
	    		  tempAnchor.add(attributes[i]);
	    	  }
	    	  if(attributes[i].contains("around_")){
	    		  tempAround.add(attributes[i]);
	    	  }
	      }

	      fieldWords[WordField.URLFIELD] = new String[tempURL.size()];
	      fieldWords[WordField.ANCHOR] = new String[tempAnchor.size()];
	      fieldWords[WordField.AROUND] = new String[tempAround.size()];
	      tempURL.toArray(fieldWords[WordField.URLFIELD]);
	      tempAnchor.toArray(fieldWords[WordField.ANCHOR]);
	      tempAround.toArray(fieldWords[WordField.AROUND]);
	      wrapper.setFeatures(fieldWords);
	      
	      InputStream is =  new FileInputStream(config.getParam("FILE_CLASSIFIER"));
	      ObjectInputStream objectInputStream = new ObjectInputStream(is);
	      Classifier classifier = (Classifier) objectInputStream.readObject();
	      objectInputStream.close();
	      
	      weka.core.FastVector vectorAtt = new weka.core.FastVector();
	      for (int i = 0; i < attributes.length; i++) {
	    	  vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
	      }
//	      String[] classValues = config.getParam("CLASS_VALUES", " ");
//	      weka.core.FastVector classAtt = new weka.core.FastVector();
//	      for (int i = 0; i < classValues.length; i++) {
//	    	  classAtt.addElement(classValues[i]);
//	      }
	      vectorAtt.addElement(new weka.core.Attribute("class",attributes.length));
	      System.out.println("SIZE:" + vectorAtt.size());
	      
	      Instances insts = new Instances("link_classification", vectorAtt, 1);
	      System.out.println("SIZE" + attributes.length);
	      insts.setClassIndex(attributes.length);
//		    linkClassifier = new LinkClassifierImpl(classifier, insts, wrapper,
//		                                            attributes,
//		                                            config.getParamInt("LEVEL"));
	      linkClassifier = new LinkClassifierRegression(classifier, insts, wrapper,attributes);
	      LinkNeighborhood ln = new LinkNeighborhood(new URL("http://www.new.com/sport"));
	      ln.setAnchor(new String[]{"advertis","subscrib","opinion","site", "obituari"});
	      ln.setAround(new String[]{"advertis","subscrib","opinion","site", "obituari"});
	      LinkRelevance lr = linkClassifier.classify(ln);
	      System.out.println(lr.getRelevance());

		}catch(Exception ex){
			ex.printStackTrace();
			
		}
		
	}

}
