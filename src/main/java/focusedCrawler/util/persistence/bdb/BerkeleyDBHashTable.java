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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

import focusedCrawler.util.persistence.Tuple;

public class BerkeleyDBHashTable {

	private Environment exampleEnv;
	
	private Database exampleDb;
	
	public BerkeleyDBHashTable(File path) throws EnvironmentLockedException, DatabaseException{
		 EnvironmentConfig envConfig = new EnvironmentConfig();
		 envConfig.setTransactional(true);
		 envConfig.setAllowCreate(true);
		 exampleEnv = new Environment(path, envConfig);

		 /*
		  * Make a database within that environment
		  *
		  * Notice that we use an explicit transaction to
		  * perform this database open, and that we
		  * immediately commit the transaction once the
		  * database is opened. This is required if we
		  * want transactional support for the database.
		  * However, we could have used autocommit to
		  * perform the same thing by simply passing a
		  * null txn handle to openDatabase().
		  */
		 Transaction txn;
		 txn = exampleEnv.beginTransaction(null, null);
		 DatabaseConfig dbConfig = new DatabaseConfig();
		 dbConfig.setTransactional(true);
		 dbConfig.setAllowCreate(true);
		 dbConfig.setSortedDuplicates(false);
		 exampleDb = exampleEnv.openDatabase(txn, "simpleDb", dbConfig);
		 txn.commit();
	}

	public synchronized void put(List<Tuple> tuples) throws DatabaseException {
	    if(tuples.isEmpty())
	        return;
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		Transaction txn;
		TransactionConfig txnConfig = new TransactionConfig();
	    txnConfig.setReadUncommitted(true);          // Use uncommitted reads 
	                                                 // for this transaction.
		txn = exampleEnv.beginTransaction(null, txnConfig);
		txn.setLockTimeout(0);

		for (int i = 0; i < tuples.size(); i++) {
		     Tuple tuple = tuples.get(i);
		     if(tuple == null)
		         continue;
		     StringBinding.stringToEntry(tuple.getKey(), keyEntry);
		     StringBinding.stringToEntry(tuple.getValue(), dataEntry);
	         OperationStatus status = exampleDb.put(txn, keyEntry, dataEntry);
	         if (status != OperationStatus.SUCCESS) {
	             throw new DatabaseException("Data insertion got status " + status);
	         }
		}
		txn.commit();
	}

	
	public synchronized void put(String key, String value) throws DatabaseException{
	     DatabaseEntry keyEntry = new DatabaseEntry();
	     DatabaseEntry dataEntry = new DatabaseEntry();
	     Transaction txn;
	     txn = exampleEnv.beginTransaction(null, null);
	     StringBinding.stringToEntry(key, keyEntry);
	     StringBinding.stringToEntry(value, dataEntry);
         OperationStatus status = exampleDb.put(txn, keyEntry, dataEntry);
         if (status != OperationStatus.SUCCESS) {
             throw new DatabaseException("Data insertion got status " + status);
         }
         txn.commit();
	}
	
	public synchronized List<Tuple> listElements() throws DatabaseException {
		Cursor cursor = exampleDb.openCursor(null, null);
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		List<Tuple> tempList = new ArrayList<Tuple>();
		while (cursor.getNext(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
        	Tuple tuple = new Tuple(StringBinding.entryToString(keyEntry),StringBinding.entryToString(dataEntry));
        	tempList.add(tuple);
		}
        cursor.close();
        return tempList;
	}
	

	public synchronized String get(String key) throws DatabaseException{
	     DatabaseEntry keyEntry = new DatabaseEntry();
	     DatabaseEntry dataEntry = new DatabaseEntry();
	     StringBinding.stringToEntry(key, keyEntry);
//	     TransactionConfig txnConfig = new TransactionConfig();
//	     txnConfig.setReadUncommitted(true);          // Use uncommitted reads 
//		                                                 // for this transaction.
//
//	     Transaction txn;
//	     txn = exampleEnv.beginTransaction(null, txnConfig);
	     exampleDb.get(null, keyEntry, dataEntry, LockMode.READ_UNCOMMITTED);
	     if(dataEntry.getData() == null){
	    	 return null;
	     }else{
	    	 return StringBinding.entryToString(dataEntry);
	     }
	}

}
