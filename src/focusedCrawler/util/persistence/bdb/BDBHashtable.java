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
package focusedCrawler.util.persistence.bdb;

import focusedCrawler.util.persistence.Tuple;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

public class BDBHashtable {

	private EntityStore store;
	private DataAccessor accessor; 
	private MyDbEnv myEnv;
	
	public BDBHashtable(File dir) throws DatabaseException{
		myEnv = new MyDbEnv(dir,false);
		store = myEnv.getEntityStore();
		accessor = new DataAccessor(store);
	}
	
	public void put(Tuple[] tuples) throws DatabaseException{
		for (int i = 0; i < tuples.length; i++) {
			if(tuples[i] != null){
				accessor.registerByKey.put(tuples[i]);	
			}
		}
	}
		
	public void put(String key, String data) throws DatabaseException{
		Tuple tuple = new Tuple(key,data);
		accessor.registerByKey.put(tuple);
	}
	
	public String get(String key) throws DatabaseException{
		Tuple tuple = accessor.registerByKey.get(key);
		if(tuple != null){
			return tuple.getValue();	
		}else{
			return null;
		}
		
	}
	
	public Tuple[] listElements() throws DatabaseException{
		Vector<Tuple> tempList = new Vector<Tuple>();
		PrimaryIndex<String,Tuple> pi =
		    store.getPrimaryIndex(String.class, Tuple.class);
		EntityCursor<Tuple> pi_cursor = pi.entities();
		try {
		    Iterator<Tuple> i = pi_cursor.iterator();
		    while (i.hasNext()) {
		    	tempList.add(i.next());
		    }
	        Tuple[] result = new Tuple[tempList.size()];
	        tempList.toArray(result);
	        return result;
		} finally {
		    // Always close the cursor
		    pi_cursor.close();
		} 
	}
	
	public void shutdown() throws DatabaseException {
		myEnv.close();
	} 

	public static void main(String[] args) {
		try {
			BDBHashtable bdb = new BDBHashtable(new File(args[0]));
			for (int i = 0; i < args.length; i++) {
				bdb.put("http"+i, "100");	
			}
			
			
//			Tuple[] t = bdb.listElements();
//			System.out.println("----" + t.length);
//			for (int i = 0; i < t.length; i++) {
//				System.out.println("VALUE:" + t[i].getKey());	
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		
	}
}
