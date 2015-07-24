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
import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentLockedException;

import focusedCrawler.util.persistence.bdb.BerkeleyDBHashTable;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

public class PersistentHashtable {
	
	private BerkeleyDBHashTable persistentTable;
	private Map<String, String> cache;
	private int size = 0;
	private int tempCacheSize = 1000;
	private Tuple[] tempList = new Tuple[tempCacheSize]; 
	
	public PersistentHashtable(String path, int cacheSize) {
		this.cache = new HashMap<String, String>(cacheSize);
		try {
			File file = new File(path);
			if(!file.exists()) {
				file.mkdirs();
			}
			this.persistentTable = new BerkeleyDBHashTable(file);
		} catch (EnvironmentLockedException e) {
			throw new RuntimeException(e);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		loadCache(cacheSize);
	}
	
	private void loadCache(int cacheSize) {
		try {
			Tuple[] tuples = persistentTable.listElements();
			int count = 0;
			while(count < tuples.length && count < cacheSize){
				cache.put(tuples[count].getKey(), tuples[count].getValue());
				count++;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public Tuple[] getTable() throws Exception{
		return persistentTable.listElements();
	}
	
	public List<String> getCache() throws Exception {
		List<String> pages = new ArrayList<String>();
		for (String key : (Set<String>) cache.keySet()) {
			pages.add(URLDecoder.decode(key, "UTF-8"));
		}
		return pages;
	}

	public synchronized void updateCache(Map<String, String> newCache){
		this.cache.clear();
		this.cache = null;
		this.cache = newCache;
		this.tempList = new Tuple[tempCacheSize];
	}

	@SuppressWarnings("deprecation")
	public synchronized String get(String key){
		String obj = null;
		try {
			key = URLEncoder.encode(key);
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
	
	@SuppressWarnings("deprecation")
	public synchronized boolean put(String key, String value){
		try {
			key = URLEncoder.encode(key);
			cache.put(key, value);	
			
			tempList[size] = new Tuple(key,value);
			size++;
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
	
	public synchronized Vector<VSMElement> orderedSet() {
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
	
	public synchronized Iterator<String> getKeys() {
		return cache.keySet().iterator();
	}
	
	public int size(){
		return this.size;
	}
	
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
