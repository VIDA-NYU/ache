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
package focusedCrawler.util.persistence;
import java.util.Set;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.List;
import java.util.ArrayList;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentLockedException;


import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.cache.CacheFIFO;
import focusedCrawler.util.cache.StringCacheKey;
import focusedCrawler.util.persistence.bdb.BDBHashtable;
import focusedCrawler.util.persistence.bdb.BerkeleyDBHashTable;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

public class PersistentHashtable {

//	private FileIndexer indexer;
//	private Searcher searcher;
	private BerkeleyDBHashTable persistentTable;
//	private CacheFIFO cache;
	private HashMap cache;
//	private HashMap<String, String> cache;
	private int size = 0;
	private int tempCacheSize = 1000;
	private Tuple[] tempList = new Tuple[tempCacheSize]; 
	
	
	public PersistentHashtable(String path, int cacheSize) throws Exception{

		try {
//			this.indexer = new FileIndexer(path);
//			System.out.println("Loading Table...");
			this.persistentTable = new BerkeleyDBHashTable(new File(path));
//			this.cache = new CacheFIFO(cacheSize,cacheSize/10);
			this.cache = new HashMap(cacheSize);
//	    	if(path.endsWith("cfg")){
//	    		focusedCrawler.util.ParameterFile config = new focusedCrawler.util.ParameterFile(path);
//	        	String[] seeds = config.getParam("SEEDS"," ");
//	        	for (int i = 0; i < seeds.length; i++) {
//	        		this.put(seeds[i], "10000001");
//	        	}
//	    	}
//			this.cache = new HashMap<String, String>(tempCacheSize);
		} catch (EnvironmentLockedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		this.searcher = new Searcher(path);
//		System.out.println("Loading Cache...");
		loadCache(cacheSize);
//		System.out.println("Done Cache...");
	}
	
//	public BerkeleyDBHashTable getTable(){
//		return this.persistentTable;
//	}
	
	private void loadCache(int cacheSize) throws Exception {
		try {
			Tuple[] tuples = persistentTable.listElements();
			int count = 0;
			while(count < tuples.length && count < cacheSize){
//				cache.put(new StringCacheKey(tuples[count].getKey()), new StringCacheKey(tuples[count].getValue()));
//				System.out.println(tuples[count].getKey() + ":" + tuples[count].getValue());
				cache.put(tuples[count].getKey(), tuples[count].getValue());
				count++;
			}
//			System.out.println("TOTAL:" + count);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
/*
		TermEnum terms = searcher.listElements(path);
		if(terms != null){
			int count = 0;
			while(terms.next() && count < cacheSize){
				String term = terms.term().text();
				cache.put(new StringCacheKey(term), new StringCacheKey(10000+""));
				count++;
			}
		}
	*/
	}
	
	public Tuple[] getTable() throws Exception{
		return persistentTable.listElements();
	}
	
	public List<String> getCache() throws Exception
	{
		List<String> pages = new ArrayList<String>();
    /*
		for (HashMap.Entry<String, String> entry : (Set<HashMap.Entry<String, String>>)cache.entrySet())
		{
			pages.add(URLDecoder.decode(entry.getKey(), "UTF-8"));
		}*/
    for (String key: (Set<String>)cache.keySet())
    {
			pages.add(URLDecoder.decode(key, "UTF-8"));
    }
		return pages;
	}

	public synchronized void updateCache(HashMap newCache){
		this.cache.clear();
		this.cache = null;
		this.cache = newCache;
		tempList = null; 
		tempList = new Tuple[tempCacheSize];
	}

	
	public synchronized String get(String key){
		String obj = null;
		try {
//			System.out.println("GET:" + cache.size());
			key = URLEncoder.encode(key);
//			StringCacheKey tempObj = ((StringCacheKey)cache.get(new StringCacheKey(key)));
			String tempObj = (String) cache.get(key);
			if(tempObj == null){
				obj = persistentTable.get(key);
			}else{
				obj = tempObj.toString();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	public synchronized boolean put(String key, String value){
		try {
			key = URLEncoder.encode(key);
//			if(!value.equals("-1")){
				cache.put(key, value);	
//			}else{
//				cache.remove(key);
//			}
//			System.out.println(cache);
//			if(size <= tempCacheSize){
////				persistentTable.put(key,value);
//				cache.put(key, value);
//			}
			
			tempList[size] = new Tuple(key,value);
			size++;
//			System.out.println("#####SIZE:" + size);
//			System.out.println("#####CACHE_SIZE:" + cache.size());
			if(size%10000 == 0){
				System.out.println("#####CACHE_SIZE:" + cache.size());
			}
			if(size == tempCacheSize){
				size = 0;
				persistentTable.put(tempList);
				tempList = null; 
				tempList = new Tuple[tempCacheSize];
				
			}
			return true;
		} catch (Exception e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void commit() {
		try {
			persistentTable.put(tempList);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			persistentTable.put(tempList);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized Vector<VSMElement> orderedSet() throws CacheException{
		Vector<VSMElement> result = new Vector<VSMElement>();
		Iterator<String> iterator = cache.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String)cache.get(key);
			VSMElement elem = new VSMElement(key,Double.parseDouble(value));
			result.add(elem);
		}
		Collections.sort(result,new VSMElementComparator());
		return result;
	}
	
	public synchronized Iterator getKeys() throws CacheException{
		return cache.keySet().iterator();
//		return cache.keySet().iterator();
	}
	
	public int size(){
		return this.size;
	}
	
//	public boolean optimize() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
//		return this.indexer.optimize();
//	}
	
	public static void main(String[] args) {
		try {
			PersistentHashtable ph = new PersistentHashtable(args[0], 100000);
			String key = ph.get(args[1]);
			System.out.println("RES:" + key);
			key = ph.get(key);
			System.out.println("RES:" + key);
			key = ph.get(key);
			System.out.println("RES:" + key);
			key = ph.get(key);
			System.out.println("RES:" + key);
			key = ph.get(key);
			System.out.println("RES:" + key);
			
			
//			ph.loadCache(1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
