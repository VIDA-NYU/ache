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
package focusedCrawler.link.frontier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.PriorityQueueLink;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.cache.StringCacheKey;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.vsm.VSMElement;


public class FrontierTargetRepositoryBaseline {
	
	protected PersistentHashtable urlRelevance;
	
	protected HashMap<String,Integer> hostPages = null;
	
	private boolean useScope = false;
	
	private boolean random = false;
	
	private int pagesPerSite = 1000000;
	
//	protected HashMap<String,VSMElement> middle = new HashMap<String, VSMElement>();
	
	
	public FrontierTargetRepositoryBaseline(PersistentHashtable urlRelevance, HashMap<String,Integer> scope)  {
		this.urlRelevance = urlRelevance;
		this.hostPages = scope;
		this.useScope = true;
	}
	
	public FrontierTargetRepositoryBaseline(PersistentHashtable urlRelevance, int pagesPerSite) {
		this.urlRelevance = urlRelevance;
		this.useScope = false;
		this.pagesPerSite = 50;
		this.hostPages = new HashMap<String, Integer>();
	}

	
	public void commit(){
		urlRelevance.commit();
	}
	
	public void setPolicy(boolean rand){
		this.random = rand;
	}

	public double getRelevance(String url){
		String strRel = urlRelevance.get(url);
		if(strRel != null){
			double rel = Double.parseDouble(strRel);
			return rel;
		}else{
			return 0;
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	
	public HashSet<String> visitedAuths() throws Exception{
		HashSet<String> result = new HashSet<String>();
		Tuple[] tuples = urlRelevance.getTable();
		for (int i = 0; i < tuples.length; i++) {
			int value = Integer.parseInt(tuples[i].getValue());
			if(value < -200){
				result.add(URLDecoder.decode(tuples[i].getKey()));
			}
		}
		return result;
	}
	

	public HashSet<String> visitedLinks() throws Exception{
		HashSet<String> result = new HashSet<String>();
		Tuple[] tuples = urlRelevance.getTable();
		for (int i = 0; i < tuples.length; i++) {
			int value = Integer.parseInt(tuples[i].getValue());
			if(value < 0){
				result.add(URLDecoder.decode(tuples[i].getKey()));
			}
		}
		return result;
	}

	
	
	public HashSet<String> unvisitedAuths() throws Exception{
		HashSet<String> result = new HashSet<String>();
		Tuple[] tuples = urlRelevance.getTable();
		for (int i = 0; i < tuples.length; i++) {
			int value = Integer.parseInt(tuples[i].getValue());
			if(value > 200){
				result.add(URLDecoder.decode(tuples[i].getKey()));
			}
		}
		return result;
	}

	public HashSet<String> visitedHubs() throws Exception{
		HashSet<String> result = new HashSet<String>();
		Tuple[] tuples = urlRelevance.getTable();
		for (int i = 0; i < tuples.length; i++) {
			int value = Integer.parseInt(tuples[i].getValue());
			if(value > -200 && value < -100){
				result.add(URLDecoder.decode(tuples[i].getKey()));
			}
		}
		return result;
	}

	public HashSet<String> unvisitedHubs() throws Exception{
		HashSet<String> result = new HashSet<String>();
		Tuple[] tuples = urlRelevance.getTable();
		for (int i = 0; i < tuples.length; i++) {
			int value = Integer.parseInt(tuples[i].getValue());
			if(value > 100 && value < 200){
				result.add(URLDecoder.decode(tuples[i].getKey()));
			}
		}
		return result;
	}

	
	public void update(LinkRelevance linkRelev){
		String url = linkRelev.getURL().toString();
		String strRel = urlRelevance.get(url);
		if(strRel != null){
			if(Integer.parseInt(strRel) > 0){ //not visited url
				double relevance = linkRelev.getRelevance();
				int relevInt = (int) (relevance);
				urlRelevance.put(url, relevInt+"");
			}
		}
	}
	

	public List<String> getFrontierPages() throws Exception
	//public Tuple[] getFrontierPages() throws Exception
	{
		//This function is used to getting all existing links in frontier.
		//Should we get frontier links via cache member of urlRelevant?
		//return urlRelevance.getTable();
		return urlRelevance.getCache();	
	}

	/**
	 * This method inserts a new link into the frontier
	 * @param linkRelev
	 * @return
	 * @throws FrontierPersistentException
	 */
	public boolean insert(LinkRelevance linkRelev) throws FrontierPersistentException {
		boolean inserted = false;
		String url = linkRelev.getURL().toString();
		Integer rel = exist(linkRelev) ;
		double relevance = linkRelev.getRelevance();
		if (rel == null && url.toString().length() < 210) {
			int relevInt = (int) (relevance);
			urlRelevance.put(url, relevInt+"");
			inserted = true;
		}
		
//		System.out.println(linkRelev.getURL().toString() + ":" + linkRelev.getRelevance() + ":" + inserted);
		return inserted;
	}
	
	/**
	 * It verifies whether a given URL was already visited or does not belong to the scope.
	 * @param linkRelev
	 * @return
	 * @throws FrontierPersistentException
	 */
	public Integer exist(LinkRelevance linkRelev) throws FrontierPersistentException {
		String url = linkRelev.getURL().toString();
		String host = linkRelev.getURL().getHost();
		Integer result = null;
		String resStr = urlRelevance.get(url);
		if(resStr != null){
			result = Integer.parseInt(resStr);
		}else{
			result = new Integer(-1);
			if(useScope == true){
				if(hostPages.get(host) != null){
					result = null;	
				}
			}else{
				result = null;
			}
		}
		return result;
	}
	
	private String filterServer(String server){
		if(server.lastIndexOf(".") != -1){
			String serverTemp = server.substring(0,server.lastIndexOf("."));
			int index = serverTemp.lastIndexOf(".");
			if(index != -1){
				server = server.substring(index+1);
			}
		}
		return server;
	}

	/**
	 * It deletes a URL from frontier (marks as visited).
	 * @param linkRelev
	 * @throws FrontierPersistentException
	 */
	public void delete(LinkRelevance linkRelev) throws
	 	FrontierPersistentException {

		String url = linkRelev.getURL().toString();
		if(exist(linkRelev) != null){//we don't want to delete the URL file, it is useful to avoid visiting an old url
			int rel = (int)linkRelev.getRelevance();
			urlRelevance.put(url,-rel+"");
		}
	}
	 public boolean reachLimit(URL url){
		 return false;
	 }
	
	 public LinkRelevance[] select(int numberOfLinks) throws
		FrontierPersistentException {
		 if(useScope){
			 return siteSelection(numberOfLinks);
		 }else{
			 if(random){
				 return randomSelection(numberOfLinks);	 
			 }else{
				 return nonRandomSelection(numberOfLinks);
			 }
		 }
	 }
	 
	 public LinkRelevance[] siteSelection(int numberOfLinks) throws	FrontierPersistentException {
		 LinkRelevance[] result = null;
		 try {
			 Iterator<VSMElement> keys  = urlRelevance.orderedSet().iterator();
			 Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
			 HashMap<String,Integer> hostCount = new HashMap<String, Integer>();
			 for (int count = 0; count < numberOfLinks && keys.hasNext();) {
				 VSMElement elem = keys.next(); 
				 String key = elem.getWord();
				 String url = URLDecoder.decode(key);
				 if (url != null){
					 String host = filterServer(new URL(url).getHost());
					 Integer intCount = hostCount.get(host);
					 if(intCount == null){
						 hostCount.put(host,new Integer(0));
					 }
					 int numOfLinks = hostCount.get(host).intValue();
					 if(numOfLinks < numberOfLinks/hostPages.size()){
						 hostCount.put(host,new Integer(numOfLinks+1));
						 Integer relevInt = new Integer((int)elem.getWeight());
						 if(relevInt != null && relevInt.intValue() != -1){
							 int relev = relevInt.intValue();
							 if(relev > 0){
								 LinkRelevance linkRel = new LinkRelevance(new URL(url),relev);
								 tempList.add(linkRel);
								 count++;
							 }
						 }
					 }
				 }
			 }
			 result = new LinkRelevance[tempList.size()];
			 tempList.toArray(result);
			 System.out.println(">> TOTAL LOADED: " + result.length);
		 }catch (IOException ex) {
			 ex.printStackTrace();
		 }catch (CacheException ex) {
			 ex.printStackTrace();
		 }
		 return result;
	 }

	 
	 public LinkRelevance[] nonRandomSelection(int numberOfLinks) throws	FrontierPersistentException {
		 LinkRelevance[] result = null;
		 int[] classLimits = new int[]{500,1000,5000};
		 int[] classCount = new int[classLimits.length];
		 try {
			 Iterator<VSMElement> keys  = urlRelevance.orderedSet().iterator();
			 Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
			 int count = 0;
			 for (int i = 0; count < numberOfLinks && keys.hasNext(); i++) {
				 VSMElement elem = keys.next(); 
				 String key = elem.getWord();
				 String url = URLDecoder.decode(key);
				 if (url != null){
//					 Integer relevInt = new Integer((String)urlRelevance.get(url));
					 Integer relevInt = new Integer((int)elem.getWeight());
					 if(relevInt != null && relevInt.intValue() != -1){
						 int relev = relevInt.intValue();
						 if(relev > 0){
							 int index = relev/100;
							 if(classCount[index] < classLimits[index]){
								 if(relev == 299 || i % 5 == 0){
									 LinkRelevance linkRel = new LinkRelevance(new URL(url),relev);
//									 System.out.println(url + ":" + relev);
									 tempList.add(linkRel);
									 count++;
									 classCount[index]++;
								 }
							 }
						 }
					 }
				 }
			 }
			 result = new LinkRelevance[tempList.size()];
			 tempList.toArray(result);
			 System.out.println(">> TOTAL LOADED: " + result.length);
		 }catch (IOException ex) {
			 ex.printStackTrace();
		 }catch (CacheException ex) {
			 ex.printStackTrace();
		 }
		 return result;
	 }

	 
	/**
	 * This method returns the next links to be visited by the crawler
	 * @param numberOfLinks
	 * @return
	 * @throws FrontierPersistentException
	 */
	public LinkRelevance[] randomSelection(int numberOfLinks) throws
		FrontierPersistentException {
		HashMap<Integer, Integer> queue = new HashMap<Integer, Integer>();
		LinkRelevance[] result = null;
//		int[] classCount = new int[3];
//		int[] classLimits = new int[]{3000,2000,1000};
		try {
			Iterator keys = urlRelevance.getKeys();
			
			Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
			int count = 0;
			for (int i = 0; count < numberOfLinks && keys.hasNext(); i++) {
				 String key = ((String)keys.next());
				String url = URLDecoder.decode(key);
//				System.out.println(url);
				if (url != null){
//					System.out.println("$$$"+(String)urlRelevance.get(url));
					Integer relevInt = new Integer((String)urlRelevance.get(url));
					if(relevInt != null){
						int relev = relevInt.intValue();
						if(relev > 0){
//							int index = relev/100;
//							if(classCount[index] < classLimits[index]){
								Integer numOccur = ((Integer)queue.get(relevInt));
								int numOccurInt = 0;
								if(numOccur != null){
									numOccurInt++;
								}else{
									numOccurInt = 1;
								}
								queue.put(relevInt,new Integer(numOccurInt));
								LinkRelevance linkRel = new LinkRelevance(new URL(url),relev);
								tempList.add(linkRel);
								count++;
//								classCount[index]++;
//							}
						}
					}
				}
			}
			
			result = new LinkRelevance[tempList.size()];
			tempList.toArray(result);
			System.out.println(">> TOTAL LOADED: " + result.length);
			queue.clear();
		}catch (IOException ex) {
			ex.printStackTrace();
		}catch (CacheException ex) {
			ex.printStackTrace();
		}
		
		return result;
	}
	
	  public void close(){
		  urlRelevance.close();
	  }

	
	public static void main(String[] args) {
		try {
			focusedCrawler.util.ParameterFile config = new focusedCrawler.util.ParameterFile(args[0]);
			String dir = config.getParam("LINK_DIRECTORY");
			PersistentHashtable urls = new PersistentHashtable(dir,1000);
			FrontierTargetRepositoryBaseline frontier = new FrontierTargetRepositoryBaseline(urls,10000);
			int count = 0;
			if(args.length > 1){
				BufferedReader input = new BufferedReader(new FileReader(args[1]));
				for (String line1 = input.readLine(); line1 != null; line1 = input.readLine()) {
					LinkRelevance linkRel = new LinkRelevance(new URL(line1), 299);
					frontier.insert(linkRel);
					count++;
				}
			}else{
				String[] seeds = config.getParam("SEEDS"," ");
				for (int i = 0; i < seeds.length; i++) {
					LinkRelevance linkRel = new LinkRelevance(new URL(seeds[i]), 299);
					frontier.insert(linkRel);
					count++;
				}
			}
			System.out.println("# SEEDS:" + count);
			frontier.close();
		}
		catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (FrontierPersistentException ex) {
			ex.printStackTrace();
		} catch (CacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
