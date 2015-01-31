/*
############################################################################
##
## Copyright (C) 2006-2009 University of Utah. All rights reserved.
##
## This file is part of DeepPeep.
##
## This file may be used under the terms of the GNU General Public
## License version 2.0 as published by the Free Software Foundation
## and appearing in the file LICENSE.GPL included in the packaging of
## this file.  Please review the following to ensure GNU General Public
## Licensing requirements will be met:
## http://www.opensource.org/licenses/gpl-license.php
##
## If you are unsure which license is appropriate for your use (for
## instance, you are interested in developing a commercial derivative
## of DeepPeep), please contact us at deeppeep@sci.utah.edu.
##
## This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
## WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
##
############################################################################
*/
package focusedCrawler.link.classifier.builder.wrapper;

import focusedCrawler.link.classifier.util.Instance;
import focusedCrawler.link.classifier.util.WordField;
import focusedCrawler.util.Page;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.PorterStemmer;
import focusedCrawler.util.parser.LinkNeighborhood;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

/**
 * <p>Description: This class from a predefined set of words extracts for
 * each link the frequency of these words given a page. These words are
 * the features used by the Link Classifier.</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class WrapperNeighborhoodLinks {

  private StopList stoplist;
  private String[][] fieldWords;
  private PorterStemmer stemmer;

  public WrapperNeighborhoodLinks(StopList stoplist) {
    this.stoplist = stoplist;
    stemmer = new PorterStemmer();
  }

  public WrapperNeighborhoodLinks() {
    this.stoplist = null;
  }

  public void setFeatures(String[][] fieldWords){
    this.fieldWords = fieldWords;
  }

  public void setFeatures(String[] features){
	  String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];
	  Vector<String> aroundTemp = new Vector<String>();
	  Vector<String> altTemp = new Vector<String>();
	  Vector<String> srcTemp = new Vector<String>();
	  Vector<String> urlTemp = new Vector<String>();
	  Vector<String> anchorTemp = new Vector<String>();
	  for (int i = 0; i < features.length; i++) {
		if(features[i].startsWith("around_")){
			String[] parts = features[i].split("_");
			aroundTemp.add(parts[1]);
		}
		if(features[i].startsWith("alt_")){
			String[] parts = features[i].split("_");
			altTemp.add(parts[1]);
		}
		if(features[i].startsWith("src_")){
			String[] parts = features[i].split("_");
			srcTemp.add(parts[1]);
		}
		if(features[i].startsWith("url_")){
			String[] parts = features[i].split("_");
			urlTemp.add(parts[1]);
		}
		if(features[i].startsWith("anchor_")){
			String[] parts = features[i].split("_");
			anchorTemp.add(parts[1]);
		}
	  }
	  String[] around = new String[aroundTemp.size()];
	  aroundTemp.toArray(around);
	  fieldWords[WordField.AROUND] = around;
	  String[] alt = new String[altTemp.size()];
	  altTemp.toArray(alt);
	  fieldWords[WordField.ALT] = alt;
	  String[] src = new String[srcTemp.size()];
	  srcTemp.toArray(src);
	  fieldWords[WordField.SRC] = src;
	  String[] url = new String[urlTemp.size()];
	  urlTemp.toArray(url);
	  fieldWords[WordField.URLFIELD] = url;
	  String[] anchor = new String[anchorTemp.size()];
	  anchorTemp.toArray(anchor);
	  fieldWords[WordField.ANCHOR] = anchor;
	  this.fieldWords = fieldWords;
	  
  }
  
  /**
   * Extract the information from links as word in the URL, anchor and around
   * links
   * @param page Page page containing the links to be obtained
   * @param features String[] pre-defined words
   * @return HashMap mapping url -> instance
   * @throws MalformedURLException
   */

  public HashMap extractLinks(PaginaURL page, String[] features) throws MalformedURLException {
    HashMap linkFields = extractLinks(page);
    return mapFeatures(linkFields, features);
  }


  public HashMap extractLinks(LinkNeighborhood[] linkNeighboors, String[] features) throws MalformedURLException {
    HashMap linkFields = extractLinks(linkNeighboors);
    return mapFeatures(linkFields, features);
  }

  public HashMap extractLinks(LinkNeighborhood linkNeighboor, String[] features) throws MalformedURLException {
    HashMap linkFields = extractLinks(linkNeighboor);
//    System.out.println(">>>MAPPING...");
    return mapFeatures(linkFields, features);
  }

  public HashMap extractLinksFull(LinkNeighborhood linkNeighboor, String[] features) throws MalformedURLException {
//	  System.out.println(">>" + linkNeighboor.getLink().toString());
	  HashMap linkFields = extractLinksFull(linkNeighboor);
	  return mapFeatures(linkFields, features);
  }

  private HashMap mapFeatures(HashMap linkFields, String[] features){
    HashMap result = new HashMap();
    Iterator wordsFields = linkFields.keySet().iterator();

    while (wordsFields.hasNext()) {
      Instance instance = new Instance(features);
//      System.out.println(">>>Instance:"+instance.toString());
      String url = (String)wordsFields.next();
//      System.out.println("1.URL:" + url);
      WordField[] words = (WordField[])linkFields.get(url);
      for(int j = 0; j < words.length; j++){
        WordField wordField = words[j];
        String field = (WordField.FIELD_NAMES[wordField.getField()]).toLowerCase();
        String word = wordField.getWord();
        if(wordField.getField() == WordField.URLFIELD || wordField.getField() == WordField.SRC){
//        	if(wordField.getField() == WordField.SRC){
//        		System.out.println("D1:" + wordField.getWord());
//        	}
        	
        	Vector wordsTemp = searchSubstring(wordField.getWord(),wordField.getField());
        	for (int i = 0; i < wordsTemp.size(); i++) {
        		word = (String)wordsTemp.elementAt(i);
        		word = field + "_"  + word;
        		instance.setValue(word, new Double(1));
        	}
        }else{
//        	word = stemming(word);
        	if(word != null){
        		word = field + "_"  + word;
        		instance.setValue(word, new Double(1));
        	}
        }
//        System.out.println(">>>AFTER Instance:"+instance.toString());
      }
//      System.out.println(">>INST..." + instance.toString());
      result.put(url,instance);
    }
    return result;
  }



  private String stemming(String word) {
	    String new_word = "";
	    try {
	      new_word = stemmer.stem(word);
	      if (new_word.indexOf("No term") != -1 || new_word.indexOf("Invalid term") != -1) {
	        new_word = word;
	      }
	    }
	    catch (Exception e) {
	      new_word = word;
	    }
	    return new_word;
	  }

    private Vector searchSubstring(String word, int field){

     Vector result = new Vector();
     String[] words = fieldWords[field];
     for (int i = 0; i < words.length; i++) {
    	 String tempWord = words[i]; 
    	 int index = tempWord.indexOf("_");
    	 if(index != -1){
    		 tempWord = tempWord.substring(index+1);	 
    	 }
    	 if(word != null && word.toLowerCase().equals(tempWord)){
//    		 System.out.println(">>>" + word);
    		 result.add(tempWord);
    	 }
     }
     return result;
   }

    
    private  HashMap extractLinksFull(LinkNeighborhood ln) throws  MalformedURLException {
    	HashMap result = new HashMap();
    	Vector words = new Vector();
    	String urlStr = ln.getLink().toString();
    	getURLWords(urlStr, words);
    	if(ln.getImgSrc() != null){
            PaginaURL pageParser = new PaginaURL(new URL("http://"), 0, 0, ln.getImgSrc().length(), ln.getImgSrc(), stoplist);
            String[] terms = pageParser.palavras();
            for (int i = 0; i < terms.length; i++) {
//            	System.out.println(">>TERM:" + terms[i]);
            	words.add(new WordField(WordField.SRC, stemming(terms[i])));
    		}
    	}
    	String[] anchor = ln.getAnchor();
    	for (int j = 0; j < anchor.length; j++) {
    		WordField wf = new WordField(WordField.ANCHOR, stemming(anchor[j]));
    		words.add(wf);
    	}
    	String[] around = ln.getAround();
    	for (int j = 0; j < around.length; j++) {
    		words.add(new WordField(WordField.AROUND, stemming(around[j])));
    	}
    	String[] alt = ln.getImgAlt();
    	for (int j = 0; alt != null && j < alt.length; j++) {
    		words.add(new WordField(WordField.ALT, stemming(alt[j])));
    	}
    	WordField[] wordsReturn = null;
    	if (words.size() > 0) {
    		wordsReturn = new WordField[words.size()];
    		words.toArray(wordsReturn);
    		result.put(urlStr, wordsReturn);
    	}
    	return result;
    }

    
   private  HashMap extractLinks(LinkNeighborhood ln) throws MalformedURLException {
	   HashMap result = new HashMap();
	   Vector words = new Vector();
	   String urlStr = ln.getLink().toString();
	   getURLWords(urlStr, words);
	   String[] anchor = ln.getAnchor();
	   for (int j = 0; j < anchor.length; j++) {
		   words.add(new WordField(WordField.ANCHOR, stemming(anchor[j])));
	   }
	   String[] around = ln.getAround();
	   for (int j = 0; j < around.length; j++) {
		   words.add(new WordField(WordField.AROUND, stemming(around[j])));
	   }
	   WordField[] wordsReturn = null;
	   if (words.size() > 0) {
		   wordsReturn = new WordField[words.size()];
		   words.toArray(wordsReturn);
		   result.put(urlStr, wordsReturn);
	   }
	   return result;
  }

   private  HashMap extractLinks(LinkNeighborhood[] linkNeighboors) throws
      MalformedURLException {
	   HashMap result = new HashMap();
	   for (int i = 0; i < linkNeighboors.length; i++) {
		   Vector words = new Vector();
		   LinkNeighborhood ln = linkNeighboors[i];
		   String urlStr = ln.getLink().toString();
		   getURLWords(urlStr,words);
		   String[] anchor = ln.getAnchor();
		   for (int j = 0; j < anchor.length; j++) {
			   words.add(new WordField(WordField.ANCHOR,anchor[j]));
		   }
		   String[] around = ln.getAround();
		   for (int j = 0; j < around.length; j++) {
			   words.add(new WordField(WordField.AROUND,around[j]));
		   }
		   WordField[] wordsReturn = null;
		   if (words.size() > 0) {
			   wordsReturn = new WordField[words.size()];
			   words.toArray(wordsReturn);
			   result.put(urlStr,wordsReturn);
		   }
	   }
	   return result;
   }

   private HashMap extractLinks(PaginaURL pageParser) throws
      MalformedURLException {

    LinkNeighborhood[] linkNeighboors = pageParser.getLinkNeighboor();
    return extractLinks(linkNeighboors);
  }


  /**
   * Extract the words close to a given hyperlink
   *
   * @param page Page - page to be scanned
   * @param link String - link to be found
   * @throws MalformedURLException
   * @return String[] - bag of words close to given hyperlink
   */
  public WordField[] getNeighboorhood(Page page, String link) throws
      MalformedURLException {
    String pageStr = page.getContent();
    PaginaURL pageParser = new PaginaURL(page.getURL(), 0, 0,pageStr.length(),pageStr, stoplist);
    HashMap result = extractLinks(pageParser);
    WordField[] words = (WordField[])result.get(link);
    return  words;
  }

  public LinkNeighborhood getNeighboorhoodLN(Page page, String link) throws MalformedURLException {
	  LinkNeighborhood ln = null;
	  String pageStr = page.getContent();
	  PaginaURL pageParser = new PaginaURL(page.getURL(), 0, 0,pageStr.length(),pageStr, stoplist);
	  LinkNeighborhood[] lns = pageParser.getLinkNeighboor();
	  for (int i = 0; i < lns.length; i++) {
		if(lns[i].getLink().toString().equals(link)){
			ln = lns[i];
		}
	  }
	  return  ln;
  }

  
  /**
   * Put the words in URL into bag of words
   *
   * @param urlStr String
   * @param wordsFields Vector list of word
   * @throws MalformedURLException
   */

  private void getURLWords(String urlStr,Vector wordsFields) throws  MalformedURLException {

	  URL url = new URL(urlStr);
	  String host = url.getHost();
	  int index = host.lastIndexOf(".");
	  if(index != -1){
		  host = "host_" + host.substring(index+1);  
		  wordsFields.add(new WordField(WordField.URLFIELD,host));
	  }
	  PaginaURL pageParser = new PaginaURL(url, 0, 0, url.getFile().length(), url.getFile(), stoplist);
	  String[] terms = pageParser.palavras();
	  for (int i = 0; i < terms.length; i++) {
//		  System.out.print(terms[i] + " ");
		  wordsFields.add(new WordField(WordField.URLFIELD,stemming(terms[i])));
	  }
  }



  public static void main(String[] args) throws MalformedURLException {


    try {
      WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks();
      StringBuffer buf = new StringBuffer();
      BufferedReader in = new BufferedReader(new FileReader(args[0]));
      for (String line = in.readLine(); line != null; line=in.readLine()) {
        buf.append(line);
        buf.append("\n");
      }
//      util.ParameterFile config = new util.ParameterFile (args[1]);

      String pageStr = buf.toString();

      String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

      fieldWords[WordField.URLFIELD] = new String[]{ "search", "book", "lib", "shop1", "archiv1"};
      fieldWords[WordField.ANCHOR] = new String[]{"book", "search", "advanc", "librari", "press", "resourc1"};
      fieldWords[WordField.AROUND] = new String[]{"book", "search", "titl", "advanc", "shop1", "resourc1"};

      wrapper.setFeatures(fieldWords);
      URL url = new URL("http://www.barnesnoble.com/");
      Page page = new Page(url, pageStr);


//       HashMap map = wrapper.extractLinks(page,feat);
      HashMap map = null;
       String link = "http://www.amazon.co.uk/exec/obidos/tg/stores/static/-/books/search/ref=sr_sp_psbooks_1_1/202-7151130-0719022";
       WordField[] wordField = wrapper.getNeighboorhood(page,link);
       for (int i = 0; i < wordField.length; i++) {
         System.out.print(wordField[i] + " ");
       }


       Set keyset = map.keySet();
       Iterator itr = keyset.iterator();
       System.out.println("Key size : " + keyset.size());
       while (itr.hasNext()) {
         String key = (String)itr.next();
         System.out.println(key);
         Instance temp = (Instance) map.get(key);
         System.out.println("URL:"+key);
         System.out.println(temp.toString() + " ");


       }


    }
    catch (FileNotFoundException ex) {
    }
    catch (IOException ex) {
      // @todo Handle this exception
    }


    /*util.download.DownloaderBuffered downloader;



    StopList st = null;

    util.ParameterFile config = new util.ParameterFile (args[1]);

    try {

      st = new util.string.StopListArquivo(config.getParam("STOPLIST_FILES"));

    }

    catch (IOException ex) {

    }

    try {

        downloader = new util.download.ExtractorProxyDownloader(

            new util.download.DownloaderSocket(new util.ParameterFile(args[2])));



       System.out.println("Tipo do Downloader : " +

                          downloader.getClass().getName());

       downloader.setId(config.getParam("DOWNLOADER_ID"));

       hwc.submission.Submitter submitter = new hwc.submission.Submitter(downloader);



      WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(st);

      String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

       fieldWords[WordField.URLFIELD] = config.getParam("FIELD_URL", " ");

       fieldWords[WordField.ANCHOR] = config.getParam("FIELD_ANCHOR", " ");

       if(config.getParam("FIELD_AROUND") != null){

         fieldWords[WordField.AROUND] = config.getParam("FIELD_AROUND", " ");

       }else{

         fieldWords[WordField.AROUND] = new String[0];

       }

       if(config.getParam("FIELD_TITLE") != null){

         fieldWords[WordField.TITLE] = config.getParam("FIELD_TITLE", " ");

       }else{

         fieldWords[WordField.TITLE] = new String[0];

       }

       if(config.getParam("FIELD_TEXT") != null){

         fieldWords[WordField.TEXT] = config.getParam("FIELD_TEXT", " ");

       }else{

         fieldWords[WordField.TEXT] = new String[0];

       }

       String[] attributes = config.getParam("ATTRIBUTES", " ");

       wrapper.setFeatures(fieldWords);

       Page p1 = submitter.downloadURL(new URL(args[0]));

       wrapper.extractLinks(p1,attributes);

      System.out.println("END");

    }

    catch (IOException ex1) {

      ex1.printStackTrace();

    }

    catch (DownloaderException ex2) {

      ex2.printStackTrace();

    }

    catch (SubmissionException ex3) {

     ex3.printStackTrace();

    }



*/



  }



}

