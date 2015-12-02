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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentLockedException;

import focusedCrawler.util.persistence.bdb.BerkeleyDBHashTable;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

public class PersistentHashtable {
    
    private static Logger logger = LoggerFactory.getLogger(PersistentHashtable.class);
	
	private BerkeleyDBHashTable persistentTable;
	
	private int tempMaxSize = 1000;
	private List<Tuple> tempList = new ArrayList<>(tempMaxSize);

    private Cache<String, String> cache; 
	
	public PersistentHashtable(String path, int cacheSize) {
	    File file = new File(path);
	    if(!file.exists()) {
	        file.mkdirs();
	    }
		this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
		try {
			this.persistentTable = new BerkeleyDBHashTable(file);
		} catch (EnvironmentLockedException e) {
			throw new RuntimeException(e);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<Tuple> getTable() {
		try {
            return persistentTable.listElements();
        } catch (DatabaseException e) {
            throw new RuntimeException("Failed to get hashtable values.", e);
        }
	}

    public Tuple[] getTableAsArray() {
        List<Tuple> table = getTable();
        return (Tuple[]) table.toArray(new Tuple[table.size()]);
    }
    
	public synchronized String get(String key){
		try {
			key = URLEncoder.encode(key, "UTF-8");
			
			String obj = cache.getIfPresent(key);
			if(obj == null){
				obj = persistentTable.get(key);
			}
			return obj;

		} catch (Exception e) {
			logger.error("Failed to get key from hashtable.", e);
			return null;
		}
	}
	
	public synchronized boolean put(String key, String value){
		try {
			key = URLEncoder.encode(key, "UTF-8");
			cache.put(key, value);	
			
			tempList.add(new Tuple(key, value));
			
			if(tempList.size() == tempMaxSize){
                commit();
            }
			
			return true;
		} catch (Exception e) {
			logger.error("Failed to store item in persitent hashtable.", e);
			return false;
		}
	}

    public void commit() {
		try {
		    persistentTable.put(tempList);
		    tempList = new ArrayList<>();
		    
		} catch (DatabaseException e) {
			throw new RuntimeException("Failed to commit persistent hashtable.", e);
		}
	}
	
	public void close() {
		this.commit();
	}
	
	public synchronized Vector<VSMElement> orderedSet() {
        try {
            List<Tuple> elements = persistentTable.listElements();
            Vector<VSMElement> result = new Vector<VSMElement>();
            for (Tuple tuple : elements) {
                result.add(new VSMElement(
                    tuple.getKey(),
                    Double.parseDouble(tuple.getValue())
                ));
            }
            Collections.sort(result, new VSMElementComparator());
            return result;
        } catch (DatabaseException e) {
            throw new RuntimeException("Failed to list elements from hashtable.", e);
        }
	    
	}

}
