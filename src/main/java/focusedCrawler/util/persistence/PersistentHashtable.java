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
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import focusedCrawler.util.persistence.bdb.BerkeleyDBHashTable;
import focusedCrawler.util.persistence.rocksdb.RocksDBHashtable;

public class PersistentHashtable<T> {
    
    public enum DB {
        BERKELEYDB, ROCKSDB
    }
    
    private static Logger logger = LoggerFactory.getLogger(PersistentHashtable.class);
	
	private HashtableDb<T> persistentTable;
	
	private int tempMaxSize = 1000;
	private List<Tuple<T>> tempList = new ArrayList<>(tempMaxSize);

    private Cache<String, T> cache;
	
	public PersistentHashtable(String path, int cacheSize, Class<T> contentClass) {
	    this(path, cacheSize, contentClass, DB.ROCKSDB);
	}
	
	public PersistentHashtable(String path, int cacheSize, Class<T> contentClass, DB backend) {
	    File file = new File(path);
	    if(!file.exists()) {
	        file.mkdirs();
	    }
	    this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
	    if(backend == DB.BERKELEYDB) {
            try {
                this.persistentTable = new BerkeleyDBHashTable<T>(file, contentClass);
            } catch (Exception e) {
                throw new RuntimeException("Failed to open BerkeleyDB database at "+path, e);
            }
        } else {
            this.persistentTable = new RocksDBHashtable<>(file.getPath(), contentClass);
        }
	}
	
	public List<Tuple<T>> getTable() {
        try {
            return persistentTable.listElements();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get hashtable values.", e);
        }
	}

	/**
	 * Use method {@link #getTable()} instead.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public Tuple<T>[] getTableAsArray() {
		List<Tuple<T>> table = getTable();
		return table.toArray((Tuple<T>[]) Array.newInstance(Tuple.class, table.size()));
	}
	
	public synchronized T get(String key){
		try {
			key = URLEncoder.encode(key, "UTF-8");
			
			T obj = cache.getIfPresent(key);
			if(obj == null){
				obj = persistentTable.get(key);
			}
			return obj;
		} catch (Exception e) {
			logger.error("Failed to get key from hashtable.", e);
			return null;
		}
	}
	
    public synchronized boolean put(String key, T value) {
        try {
            key = URLEncoder.encode(key, "UTF-8");
            cache.put(key, value);
            tempList.add(new Tuple<T>(key, value));
            if (tempList.size() == tempMaxSize) {
                commit();
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to store item in persitent hashtable.", e);
            return false;
        }
    }

    public synchronized void commit() {
        if(tempList.size() == 0)
            return;
		try {
		    persistentTable.put(tempList);
		    tempList = new ArrayList<>();
		} catch (Exception e) {
			throw new RuntimeException("Failed to commit persistent hashtable.", e);
		}
	}
	
	public synchronized void close() {
		this.commit();
		persistentTable.close();
	}
	
	public synchronized List<Tuple<T>> orderedSet(final Comparator<T> valueComparator) {
        try {
            List<Tuple<T>> elements = persistentTable.listElements();
            Collections.sort(elements, new Comparator<Tuple<T>>() {
                @Override
                public int compare(Tuple<T> o1, Tuple<T> o2) {
                    return valueComparator.compare(o1.getValue(), o2.getValue());
                }
            });
            return elements;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list elements from hashtable.", e);
        }
    }

    public TupleIterator<T> iterator() {
        try {
            this.commit();
            return persistentTable.iterator();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open hashtable iterator.", e);
        }
    }
    
   
}
