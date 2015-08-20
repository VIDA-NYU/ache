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

import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;


public class Frontier {
	
	protected PersistentHashtable urlRelevance;
	protected HashMap<String,Integer> hostPages = null;
	private boolean useScope = false;
    private final LinkSelectionStrategy linkSelector;
	
	public Frontier(PersistentHashtable urlRelevance, LinkSelectionStrategy linkSelector,
                    HashMap<String,Integer> scope)  {
		this.urlRelevance = urlRelevance;
		this.linkSelector = linkSelector;
		if(scope == null) {
		    this.useScope = false;
		    this.hostPages = new HashMap<String, Integer>();
		} else {
		    this.hostPages = scope;
		    this.useScope = true;
		}
	}
	
	public Frontier(PersistentHashtable urlRelevance, LinkSelectionStrategy linkSelector) {
		this(urlRelevance, linkSelector, null);
	}
	
	public void commit(){
		urlRelevance.commit();
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
				result.add(URLDecoder.decode(tuples[i].getKey(), "UTF-8"));
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
				result.add(URLDecoder.decode(tuples[i].getKey(), "UTF-8"));
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
				result.add(URLDecoder.decode(tuples[i].getKey(), "UTF-8"));
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
				result.add(URLDecoder.decode(tuples[i].getKey(), "UTF-8"));
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
				result.add(URLDecoder.decode(tuples[i].getKey(), "UTF-8"));
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
	 
	 public LinkRelevance[] select(int numberOfLinks) throws FrontierPersistentException {
	     return linkSelector.select(numberOfLinks);
	 }
	
	  public void close(){
		  urlRelevance.close();
	  }
	
}
