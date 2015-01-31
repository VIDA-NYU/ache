package focusedCrawler.query;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;






import weka.classifiers.Classifier;
import weka.core.Instances;


import focusedCrawler.util.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.string.PorterStemmer;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.target.TargetClassifier;
import focusedCrawler.target.TargetClassifierException;
import focusedCrawler.target.TargetClassifierImpl;
import focusedCrawler.util.vsm.VSMVector;
import focusedCrawler.util.vsm.VSMElement;

public class RelevanceFeedback {

	private String appID = "87E3D4B83FBB733A778CCB8CF950FF62BD6243FC";
	
	private String urlQuery = "http://api.bing.net/xml.aspx?AppId=87E3D4B83FBB733A778CCB8CF950FF62BD6243FC&Version=2.2&Market=en-US&Sources=web+spell&web.count=50&xmltype=attributebased&Query=";
	
	
	private int connectTimeout = 5000;
	private int readTimeout = 5000;
	private int iterations = 20;
	private StopList stoplist = null;
	private TargetClassifier classifier;
	private int total = 0;
	private VSMVector positive = new VSMVector();
	private VSMVector negative = new VSMVector();
	
//	private HashMap<String,Integer> urlCode = new HashMap<String, Integer>();

	private Vector<HashSet<String>> usedQueries = new Vector<HashSet<String>>();
	private HashSet<String> usedURLs = new HashSet<String>();
	
//	private InvertedIndex invIndex = new InvertedIndex();
	public RelevanceFeedback(){
		
	}
	
	
	public RelevanceFeedback(StopList stoplist, TargetClassifier classifier) throws MalformedURLException, IOException, SAXException{
		this.stoplist = stoplist;
		this.classifier = classifier;
	}
	
	public void execute(String initialQuery) throws MalformedURLException, IOException, SAXException, TargetClassifierException{
		Page page = downloadResults(initialQuery);
//		System.out.println("Page:" + page.getContent());
		HashSet<String> temp = new HashSet<String>();
		PorterStemmer stem = new PorterStemmer();
		String[] queryWords = initialQuery.split("\\+");
		for (int j = 0; j < queryWords.length; j++) {
			temp.add(queryWords[j].trim());
			usedTerms.add(stem.stem(queryWords[j].trim()));
		}
		String currentQuery = initialQuery;
		usedQueries.add(temp);
		previousQuery = initialQuery;
		for (int i = 0; i < iterations; i++) {
			int count = 0;
			System.out.println("Iteration:" + i);
			String[] urls = parseXMLPage(page);
			int newPages = 0;
			Vector<Page> tempPages = new Vector<Page>();
			VSMVector positiveTemp = new VSMVector();
			VSMVector negativeTemp = new VSMVector();
			for (int j = 0; j < urls.length; j++) {
				if(!usedURLs.contains(urls[j]) && !urls[j].endsWith("pdf") && !urls[j].endsWith("pps")){
					newPages++;
//					System.out.println("URL:" + urls[j]);
					page = downloadPage(new URL(urls[j]));
					
					if(page != null){
						boolean relevant = classifier.classify(page);
						if(relevant){
							System.out.println(getClass().getName());
							tempPages.add(page);
							VSMVector positiveTemp1 = new VSMVector(page.getContent(),stoplist,false);
							positiveTemp1.normalize();
							positiveTemp.addVector(positiveTemp1);
							count++;
						}else{
//							System.out.println("NonRelevant:" + urls[j]);
							VSMVector negativeTemp1 = new VSMVector(page.getContent(),stoplist,false);
							negativeTemp1.normalize();
							negativeTemp.addVector(negativeTemp1);
						}
					}
					usedURLs.add(urls[j]);
				}
			}
			Page[] pages = new Page[tempPages.size()];
			tempPages.toArray(pages);
			total = total + count;
			double currentPrecision = (double)count/(double)newPages;
			currentQuery = nextQuery(currentPrecision,newPages,currentQuery,positiveTemp,negativeTemp);
			System.out.println("PREICSION:" + currentPrecision);
			System.out.println("NEW PAGES:" + newPages);
			System.out.println("NEXT QUERY:" + currentQuery);
			page = downloadResults(currentQuery);
		}
		System.out.println("TOTAL RELEVANT:" + total);
	}
	
	private int previousQuerySize = 2;
	
	private String previousQuery = "";
	
	private int pointer = 1;
	
	private HashSet<String> usedTerms = new HashSet<String>();
	
	private int offsetCounter = 1;
	
	private String nextQuery(double currentPrecision, int currentUnique, String currentQuery, VSMVector pos, VSMVector neg){
		String offset = "&$skip=50";
		PorterStemmer stem = new PorterStemmer();
		boolean changeQuery = false;
		if(currentPrecision > 0.5){
			positive.addVector(pos);
			negative.addVector(neg);
		}else{
			changeQuery = true;
		}
		if(currentUnique <= 0.5){
			changeQuery = true;
		}
		double proportionUnique = (double)currentUnique/(double)50;
		if(proportionUnique <= 0.5){
			changeQuery = true;
		}
		if(!changeQuery && (offsetCounter/50) +1 <= 6){
			int index = currentQuery.indexOf(offset);
			if(index != -1){
				previousQuery = currentQuery.substring(0,currentQuery.indexOf(offset));	
			}else{
				previousQuery = currentQuery;
			}
			
			offsetCounter = offsetCounter+50;
			return previousQuery + offset+offsetCounter;
		}
		offsetCounter = 1;
		int index = currentQuery.indexOf(offset);
		String tempQuery = "";
		if(index != -1){
			tempQuery = currentQuery.substring(0,currentQuery.indexOf(offset));	
		}else{
			tempQuery = currentQuery;
		}
		String[] currentQueryWords = tempQuery.split("\\+");
		for (int i = 0; i < currentQueryWords.length; i++) {
			VSMElement elem = positive.getElement(currentQueryWords[i]);
			elem.setWeight(currentPrecision*elem.getWeight());
			positive.addElement(elem);
		}
		VSMVector tempPos = new VSMVector();
		tempPos.addVector(positive);
//		tempPos.normalize();
//		System.out.println(invIndex.toString());
		VSMVector tempNeg = new VSMVector();
		tempNeg.addVector(negative);
//		tempNeg.normalize();
		tempNeg.negativeVector();
		tempPos.addVector(tempNeg);
		String queryTemp = null;
		VSMElement[] elems = tempPos.topElements(tempPos.getArrayElements().length);
		for (int i = 0; i < elems.length && i < 10; i++) {
			System.out.print(elems[i].getWord() + " ");
		}
		System.out.println("");
		int querySize = previousQuerySize;

		HashSet<String> qWords = new HashSet<String>();
		if(currentPrecision <= 0.5){
			querySize++;
		}else{
			if(currentUnique <= 0.5){
				querySize--;
			}
		}
		
		int usedTermsSize = usedTerms.size();
		if(queryTemp == null){
			queryTemp = elems[0].getWord();
			String tempWord = stem.stem(elems[0].getWord());
			if(!usedTerms.contains(tempWord)){
				usedTerms.add(tempWord);
			}
			if(!qWords.contains(tempWord)){
				qWords.add(tempWord);
			}
			for (int i = pointer,step=1; step < querySize-1; i++,step++) {
				tempWord = stem.stem(elems[i].getWord());
				if(!usedTerms.contains(tempWord)){
					usedTerms.add(tempWord);
				}
				if(!qWords.contains(tempWord)){
					queryTemp = queryTemp + "+" + elems[i].getWord();
					qWords.add(tempWord);
				}else{
					step--;
				}
			}
		}
		previousQuerySize = querySize;
//		pointer++;
/*Using clusters*/
//		StringBuffer query = new StringBuffer();
//		query.append("(");
//		for (int i = 0; i < clusters.length; i++) {
//			if(i > 0){
//				query.append(")+OR+(");
//			}
//			VSMElement[] elems = clusters[i].getCentroid().topElements(clusters[i].getCentroid().getArrayElements().length);
//			query.append(elems[0].getWord());	
//			for (int j = 1; j < querySize; j++) {
//				query.append("+" + elems[j].getWord());
//			}
//		}
//		query.append(")");
		int lastElement = querySize-1;
		String query = "";
		boolean newQuery = false;
		while(!newQuery){
			boolean newQueryTemp = false;
			String tempWord = stem.stem(elems[lastElement].getWord());
			if(usedTermsSize < usedTerms.size() || !qWords.contains(tempWord) && !usedTerms.contains(tempWord)){
				newQuery = true;
				query = queryTemp + "+" + elems[lastElement].getWord();
				if(!usedTerms.contains(tempWord)){
					usedTerms.add(tempWord);
				}
//				for (int i = 0; i < usedQueries.size(); i++) {
//					newQueryTemp = false;
//					HashSet<String> set = usedQueries.elementAt(i);
//					String[] queryWords = query.split("\\+");
//					for (int j = 0; j < queryWords.length; j++) {
//						if(!set.contains(stem.stem(queryWords[j]))){
//							newQueryTemp = true;
//						}
//					}
//					if(!newQueryTemp){
//						newQuery = false;
//					}
//					
//				}
			}
			lastElement++;
		}
		HashSet<String> temp = new HashSet<String>();
		String[] queryWords = query.split("\\+");
		for (int j = 0; j < queryWords.length; j++) {
			temp.add(stem.stem(queryWords[j].trim()));
		}
		usedQueries.add(temp);
		
//		negative = new VSMVector();
//		positive = new VSMVector();
//		Vector<WordFrequencyMap> temp = new Vector<WordFrequencyMap>(relevantSample.values());
//		Collections.sort(temp,new WordFrequencyComparator());
//		String query = null;
//		for(int i = 0;i<temp.size() && query == null;i++){
//			if(!usedQueries.contains(temp.elementAt(i).getWord())){
//				query = temp.elementAt(i).getWord();
//				usedQueries.add(query);
//			}
//		}
		previousQuery = currentQuery;
		return query.toString();
	}
	
	private void addToSample(Page page, HashMap<String, WordFrequencyMap> sample){
        PaginaURL pageParser = null;
        pageParser = new PaginaURL(page.getURL(), 0, 0,
                                       page.getContent().length(),
                                       page.getContent(), stoplist);
//        System.out.println("URL>>>"+page.getURL());
        String[] words = pageParser.palavras();
        int[] occurrencies = pageParser.ocorrencias();
        for (int i = 0; i < words.length; i++) {
        	int frequency = 1;
            Object value = sample.get(words[i]);
            if(value != null){
                frequency = ((WordFrequencyMap)value).getFrequency() + 1;
            }
            if(!words[i].equals("-") && !words[i].equals("&") && words[i].length() > 2){
            	sample.put(words[i], new WordFrequencyMap(words[i],frequency));
            }
        }
    }
	
	private Page downloadResults(String keyword){
		String top = "5";
		keyword = keyword.replaceAll(" ", "%20");
		String accountKey="d9zIG4ICwyPiUzBz0pDB9fvGr/UKDqk82fYBlJlXmhc";
		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);
		URL url = null;
		StringBuffer output = new StringBuffer();
		try {
			url = new URL("https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Web?Query=%27" + keyword + "%27&$top="+ top);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String line;
			while ((line = br.readLine()) != null) {
				output = output.append(line);
			}
//		      System.out.println(output);
			conn.disconnect();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Page(url, output.toString());
	}
	
	private Page downloadPage(URL urlCon) throws IOException {
		
		System.out.println("Downloading URL:" + urlCon.toString());

//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
	    URLConnection yc = urlCon.openConnection();
//	    System.out.println("SETTING TIMEUOT...");
	    yc.setConnectTimeout(connectTimeout);
	    yc.setReadTimeout(readTimeout);

	    if(yc.getContentType()!=null && !yc.getContentType().contains("text") || urlCon.toString().endsWith("rtf")){
	    	return null;
	    }
	    
	    StringBuffer buffer = new StringBuffer();
	    try{
	      BufferedReader inCon = new BufferedReader(new InputStreamReader(yc.
	          getInputStream()));
	      String inputLine;
	      while ((inputLine = inCon.readLine()) != null) {
	        buffer.append(inputLine);
	      }
	      inCon.close();
	    }catch(java.lang.IllegalArgumentException ex){
//	      System.out.println("ILLEGAL ARGUMENT!!!\n");
	      return null;
	    }
	    catch(java.net.SocketTimeoutException ex){
//	      System.out.println("TIMEOUT EXCEPTION!!!\n");
	      return null;
	    }
	    catch(java.io.FileNotFoundException ex) {
//	      System.out.println("REMOTE FILE NOT FOUND!!!\n");
	      return null;
	    }
	    catch(java.net.UnknownHostException ex) {
//	      System.out.println("UNKNOWN HOST!!!\n");
	      return null;
	    }catch(Exception ex){
//	      System.out.println("Generic Exception\n");
	      return null;

	    }
	    Page pageRes = new Page(urlCon, buffer.toString());
//	    System.out.println("FINISHED TO DOWNLOAD THE PAGE : " + urlCon.toString() + "\n");
//	    System.out.println(pageRes.getContent());
	    return pageRes;
	}

	private String[] parseXMLPage(Page page) throws SAXException, IOException{
	    DOMParser parser = new DOMParser();
	    Vector<String> urls = new Vector<String>();
	    parser.parse(new InputSource(new BufferedReader(new StringReader(page.getContent()))));
	    Document doc = parser.getDocument();
	    NodeList list = doc.getElementsByTagName("d:Url");
	    for (int j = 0; j < list.getLength(); j++) {
	    	Node node = list.item(j);
	    	NodeList children = node.getChildNodes();
	    	Node child = children.item(0);
	    	System.out.println(child.getTextContent());
	    	urls.add(child.getTextContent());
//	        NamedNodeMap attrs = node.getAttributes();
//	         for (int i = 0; i < attrs.getLength(); i++) {
//	             Node attr = attrs.item(i);
//	             String attrName = ((attr.getNodeName().trim()).toLowerCase());
//	             String attrValue = ((attr.getNodeValue().trim()).toLowerCase());
//	             if(attrName.equals("url")){
//	            	 urls.add(attrValue);
////		 	    	 System.out.println(attrName+":"+attrValue);	            	 
//	             }
//	         }
	    }
	    String[] res = new String[urls.size()];
	    urls.toArray(res);
	    return res;
	}
	
	
	public static void main(String[] args) {
//		RelevanceFeedback rf = new RelevanceFeedback();
//		try {
//			rf.execute("Human Traffic");
//		} catch (IOException | SAXException
//				| TargetClassifierException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
  	  		ParameterFile config = new ParameterFile(args[0]);
  	        StopList stoplist = new StopListArquivo(config.getParam("STOPLIST_FILES"));
  	  		InputStream is = new FileInputStream(config.getParam("FILE_CLASSIFIER"));
  	        ObjectInputStream objectInputStream = new ObjectInputStream(is);
  	        Classifier classifier = (Classifier) objectInputStream.readObject();
  	        String[] attributes = config.getParam("ATTRIBUTES", " ");
  	        weka.core.FastVector vectorAtt = new weka.core.FastVector();
  	        for (int i = 0; i < attributes.length; i++) {
  	          vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
  	        }
  	        String[] classValues = config.getParam("CLASS_VALUES", " ");
  	        weka.core.FastVector classAtt = new weka.core.FastVector();
  	        for (int i = 0; i < classValues.length; i++) {
  	          classAtt.addElement(classValues[i]);
  	        }
  	        vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
  	        Instances insts = new Instances("target_classification", vectorAtt, 1);
  	        insts.setClassIndex(attributes.length);

  	        TargetClassifier targetClassifier = new TargetClassifierImpl(classifier, insts, attributes, stoplist);

			
			RelevanceFeedback rf = new RelevanceFeedback(stoplist, targetClassifier);
			rf.execute(args[1]);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TargetClassifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
