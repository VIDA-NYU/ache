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
package focusedCrawler.link.classifier.builder;

import focusedCrawler.link.classifier.builder.wrapper.WrapperNeighborhoodLinks;
import focusedCrawler.util.Page;
import focusedCrawler.util.parser.SimpleWrapper;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.string.PorterStemmer;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.io.FileWriter;
import java.util.Vector;
import java.net.URLConnection;
import java.io.*;

public class BacklinkSurfer {

  private String googleBacklink; // = "http://www.google.com/search?sourceid=navclient&ie=UTF-8&q=link%3A";
//  private String numBacklink = "&nbq=";   // altavista
  
  private SimpleWrapper wrapperURL;
  private SimpleWrapper wrapperTitle;
  private WrapperNeighborhoodLinks wrapperLinks;
  private HashSet<String> urlsVisited;
  private int connectTimeout;
  private int readTimeout;
  private FileWriter out;
  private HashSet<String> newURLs;
  private HashMap<String, VSMElement>[] levels = new HashMap[3];
  private Vector<LinkNeighborhood>[] lns = new Vector[3];
  
  public BacklinkSurfer(StopList stoplist, SimpleWrapper wrapperURL, 
          String backlink, FileWriter out, int connectTimeout, int readTimeout, int numBack) {
	  this.wrapperURL = wrapperURL;
	  this.wrapperLinks = new WrapperNeighborhoodLinks(stoplist);
	  this.urlsVisited = new HashSet<String>();
	  this.googleBacklink = backlink;
	  this.out = out;
	  this.connectTimeout = connectTimeout;
	  this.readTimeout = readTimeout;
	  this.newURLs = new HashSet<String>();
	  this.levels[0] = new HashMap<String, VSMElement>();
	  this.levels[1] = new HashMap<String, VSMElement>();
	  this.levels[2] = new HashMap<String, VSMElement>();
	  this.lns[0] = new Vector<LinkNeighborhood>();
	  this.lns[1] = new Vector<LinkNeighborhood>();
	  this.lns[2] = new Vector<LinkNeighborhood>();
}

  
  public BacklinkSurfer(StopList stoplist, SimpleWrapper wrapperURL, SimpleWrapper wrapperTitle,
                        String backlink, FileWriter out, int connectTimeout, int readTimeout, int numBack) {
	  this.wrapperURL = wrapperURL;
	  this.wrapperTitle = wrapperTitle;
	  this.wrapperLinks = new WrapperNeighborhoodLinks(stoplist);
	  this.urlsVisited = new HashSet<String>();
	  this.googleBacklink = backlink;
	  this.out = out;
	  this.connectTimeout = connectTimeout;
	  this.readTimeout = readTimeout;
	  this.newURLs = new HashSet<String>();
  }

  public BacklinkSurfer(SimpleWrapper wrapper) {
	  this.wrapperURL = wrapper;
  }

  public BacklinkSurfer(SimpleWrapper wrapperURL, SimpleWrapper wrapperTitle) {
	  this.wrapperURL = wrapperURL;
	  this.wrapperTitle = wrapperTitle;
  }

  
  public Vector[] getLNs() throws MalformedURLException, IOException {
	  return lns;
  }

  
  public String[] getBacklinks(String url) throws MalformedURLException, IOException {
	  String backlink = googleBacklink + url.toString(); //+ numBacklink;
	  Page page = downloadPage(newURL(backlink));
	  if (page == null) {
		  return null;
	  }
	  String[] links = wrapperURL.filterMultipleStrings(page.getContent());
	  for (int i = 0; i < links.length; i++) {
		  String record = links[i];
		  int index = record.indexOf("http");
		  System.out.println(">>>>INDEX:" + index);
		  if(index > 1){
			  record = record.substring(index,record.length());
			  links[i] = record;
		  }
	  }
	  return links;
  }
  
  private BackLinkNeighborhood[] downloadBacklinks(String host) throws IOException{
	  String backlink = "http://lsapi.seomoz.com/linkscape/links/" + host +"?AccessID=member-2e52b09aae&Expires=1365280453&Signature=WFcSAnhBG62xmt2f57bGrqCtiOM%3D&Filter=external&Scope=page_to_page&Limit=50&Sort=page_authority&SourceCols=4&TargetCols=4"; 
	  Page page = downloadPage(newURL(backlink));
	  if (page == null) {
		  return null;
	  }
	  String[] links = wrapperURL.filterMultipleStrings(page.getContent());
	  BackLinkNeighborhood[] backlinks = new BackLinkNeighborhood[links.length];
	  for (int i = 0; i < links.length; i++) {
		  backlinks[i] = new BackLinkNeighborhood();
		  backlinks[i].setLink("http://" + links[i]);
	  }
	  String[] titles = wrapperTitle.filterMultipleStrings(page.getContent());
	  for (int i = 0; i < titles.length; i++) {
		  backlinks[i].setTitle(titles[i]);
	  }
	  return backlinks;
  }
  
  private long lastVisit = 0;
  
  public BackLinkNeighborhood[] getLNBacklinks(URL url) throws MalformedURLException, IOException {
	  if(lastVisit == 0){
		  lastVisit = System.currentTimeMillis();  
	  }else{
		  long diffTime = System.currentTimeMillis() - lastVisit;
		  System.out.println(">>>ELAPSED:" + diffTime);
		  if(diffTime < 10000){
				try {
					Thread.sleep(diffTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
		  }
	  }
	  lastVisit = System.currentTimeMillis();
	  BackLinkNeighborhood[] links = downloadBacklinks(URLEncoder.encode(url.toString().substring(7)));
	  System.out.println("LINKS SIZE:" + links.length);
	  return links;
  }

  
  public String[] getBacklinks(String[] urls) throws MalformedURLException, IOException {
	  Vector<String> finalUrls = new Vector<String>();
	  for (int i = 0; i < urls.length; i++) {
		  String[] links = getBacklinks(urls[i]);
		  for (int j = 0; j < links.length; j++) {
			  finalUrls.add(links[j]);
		}
	  }
	  String[] result = new String[finalUrls.size()];
	  finalUrls.toArray(result);
	  return result;
  }
  
  public void surfingBackwards(String[] urls, int curLevel, int maxLevel) throws
      MalformedURLException, IOException {
	
	  this.newURLs.clear();
	  int count = 0;
	  for (int i = 0; count < 50 && i < urls.length; i++) {
		  if (!urlsVisited.contains(urls[i])){
			  urlsVisited.add(urls[i]);
			  count++;
		  }else{
			  continue;
		  }
		  out.write("SOURCE:" + urls[i] +" LEVEL: 0" + "\n");
		  try {
			  downLevel(newURL(urls[i]), 0, maxLevel);
		  }
		  catch (MalformedURLException ex) {
			  ex.printStackTrace();
		  }
	  }
	  Vector<VSMElement> level0Values = new Vector<VSMElement>(levels[0].values());
	  Collections.sort(level0Values, new VSMElementComparator());
	  System.out.println("TOP WORDS LEVEL 0");
	  System.out.println(level0Values.toString());
	  Vector<VSMElement> level1Values = new Vector<VSMElement>(levels[1].values());
	  Collections.sort(level1Values, new VSMElementComparator());
	  System.out.println("TOP WORDS LEVEL 1");
	  System.out.println(level1Values.toString());
	  Vector<VSMElement> level2Values = new Vector<VSMElement>(levels[2].values());
	  Collections.sort(level2Values, new VSMElementComparator());
	  System.out.println("TOP WORDS LEVEL 2");
	  System.out.println(level2Values.toString());
  }
  
  public HashSet<String> getNewURLs(){
	  return this.newURLs;
  }

  private long sleepTime = 5000;
  
  private Page downloadPage(URL urlCon) throws IOException {
    //                System.out.println("URL:" + urlCon.toString());

	try {
		Thread.sleep(sleepTime);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
    URLConnection yc = urlCon.openConnection();
//    System.out.println("SETTING TIMEUOT...");
    yc.setConnectTimeout(connectTimeout);
    yc.setReadTimeout(readTimeout);

    StringBuffer buffer = new StringBuffer();
    try{
      BufferedReader inCon = new BufferedReader(new InputStreamReader(yc.
          getInputStream()));
      String inputLine;
      while ((inputLine = inCon.readLine()) != null) {
        buffer.append(inputLine + " ");
      }
      inCon.close();
    }catch(java.lang.IllegalArgumentException ex){
      System.out.println("ILLEGAL ARGUMENT!!!\n");
      return null;
    }
    catch(java.net.SocketTimeoutException ex){
      System.out.println("TIMEOUT EXCEPTION!!!\n");
      return null;
    }
    catch(java.io.FileNotFoundException ex) {
      System.out.println("REMOTE FILE NOT FOUND!!!\n");
      return null;
    }
    catch(java.net.UnknownHostException ex) {
      System.out.println("UNKNOWN HOST!!!\n");
      return null;
    }catch(Exception ex){
    	ex.printStackTrace();
      System.out.println("Generic Exception\n");
      return null;

    }
    Page pageRes = new Page(urlCon, buffer.toString());
    System.out.println("FINISHED TO DOWNLOAD THE PAGE : " + urlCon.toString() + "\n");
    return pageRes;
  }

  /**
   * This method makes a deep search in backlinks from url
   * @param url URL - url that one wants to get links
   * @param currentLevel int - current level of deep
   * @param maxLevel int - max level of deep
   * @throws MalformedURLException
   * @throws SubmissionException
   */

  private int count = 0;

  private void downLevel(URL url, int currentLevel, int maxLevel) throws
      MalformedURLException, IOException {
	  
	System.out.println("LEVEL:" + currentLevel + ":" +url.toString());
    out.write("COUNT:" + count + "\n");
    if (currentLevel == maxLevel || count > 3000) {
      out.write("END!!!" + "\n");
      return;
    }
    count++;
    String backlink = googleBacklink + url.toString(); //+ numBacklink;
//    Page page = downloader.downloadURL(newURL(backlink));
//    System.out.println("Download Backlink Level: " + currentLevel +" : " + url.toString());
    Page page = downloadPage(newURL(backlink));
//    System.out.println("FINISHED TO DOWNLOAD : " + url.toString() + "\n");
    if (page == null) {
      return;
    }
    String[] links = wrapperURL.filterMultipleStrings(page.getContent());
    if(links.length == 0){
    	System.out.println("NO_RESULTS");
     out.write("NO_RESULTS" + "\n");
    }
//    System.out.println(">>>SIZE" + links.length);
    //expand nodes
    HashSet<String> hosts = new HashSet<String>();
    hosts.add(url.getHost());
    int count = 0;
    for (int j = 0; j < links.length; j++) {
    	if (!urlsVisited.contains(links[j])) {
    		if(hosts.contains(newURL(links[j]).getHost()) || newURL(links[j]).getHost().contains("pagina.nl")){
    			continue;
    		}
    		hosts.add(newURL(links[j]).getHost());
    		urlsVisited.add(links[j]);
    		newURLs.add(links[j]);
    		try {
//    		  if ( -1 == getNeighborhoodText(newURL(links[j]), url, currentLevel)) {
//    			  continue;
//    		  }
    			System.out.println("LINK:" + currentLevel + ":" + links[j].toString());
    			LinkNeighborhood ln = getNeighborhoodText(newURL(links[j]), url);
    			if(ln != null){
    				lns[currentLevel].add(ln);
    				count++;
    			}
    		}catch (MalformedURLException ex) {
    			ex.printStackTrace();
    		}
    		if((count == 5 && currentLevel==1) || (count == 2 && currentLevel ==2)){
    			break;
    		}
    		downLevel(newURL(links[j]), currentLevel + 1, maxLevel);
    	}
      out.write("SOURCE:" + url + " LEVEL:" + currentLevel + 1 +
    		  " URL:" + links[j] + "\n");
    }
  }



  private URL newURL(String url) throws MalformedURLException {
    if (url.indexOf("http://") == -1) {
      return new URL("http://" + url);
    }
    else {
      return new URL(url);
    }
  }


  private LinkNeighborhood getNeighborhoodText(URL link, URL urlRoot) throws MalformedURLException, IOException {
	  
	  LinkNeighborhood ln = null;
	  Page page = downloadPage(link);
	  if (page == null) {
		  return ln;
	  }
	  ln = wrapperLinks.getNeighboorhoodLN(page, urlRoot.toString());
	  if(ln != null){
		  System.out.println("LN:" + ln.toString());  
	  }
	  return ln;
  }
  
  public static void main(String[] args) throws IOException {
    System.out.print("Create Downloader Start,  ");
    ParameterFile config = new ParameterFile(args[0]);
    FileWriter out = new FileWriter(config.getParam("OUTPUT_FIELDS"));
    String path = args[0];
    path = path.substring(0, path.lastIndexOf("\\") + 1);
//    String inputUrls = config.getParam("URL_INPUT");
//    inputUrls = path + inputUrls;
//    System.out.println("path : " + inputUrls);
    try {
      StopList stoplist = new StopListArquivo(config);
      int level = config.getParamInt("DEEP_BACKLINK");
      SimpleWrapper wrapper = new SimpleWrapper(config.getParam("PATTERN_INI"),
                                                config.getParam("PATTERN_END"));
      int conTimeout = config.getParamInt("CONNECT_TIMEOUT");
      int numBacklink = config.getParamInt("NUM_BACKLINK");
      int readTimeout = config.getParamInt("READ_TIMEOUT");
      BacklinkSurfer surfer = new BacklinkSurfer(stoplist, wrapper,
                                                 config.getParam("BACKLINK"),
                                                 out, conTimeout, readTimeout,
                                                 numBacklink);
      String[] urls = config.getParam("INITIAL_URLS"," ");
//      for (int i = 0; i < level; i++) {
//        String[] urls = getInputUrl(inputUrls, i);
//        surfer.surfingBackwards(urls, 0, level);
//      String[] result = surfer.getBacklinks(urls);
//      for (int i = 0; i < result.length; i++) {
//		System.out.println(result[i]);
//	}
//      LinkNeighborhood[] ln = surfer.getLNBacklinks(args[1]);
//      for (int i = 0; i < ln.length; i++) {
//		System.out.println(ln[i].getLink().toString());
//	}
//        startLevel++;
//      }
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
    }
    out.close();
  }
}



