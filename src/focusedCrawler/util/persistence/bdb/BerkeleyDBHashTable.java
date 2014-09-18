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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Vector;

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

	public synchronized void put(Tuple[] tuples) throws DatabaseException{
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		Transaction txn;
		TransactionConfig txnConfig = new TransactionConfig();
	    txnConfig.setReadUncommitted(true);          // Use uncommitted reads 
	                                                 // for this transaction.
		txn = exampleEnv.beginTransaction(null, txnConfig);
		txn.setLockTimeout(0);

		for (int i = 0; i < tuples.length && tuples[i] != null; i++) {
		     StringBinding.stringToEntry(tuples[i].getKey(), keyEntry);
		     StringBinding.stringToEntry(tuples[i].getValue(), dataEntry);
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
	
	public synchronized Tuple[] listElements() throws DatabaseException{
		Cursor cursor = exampleDb.openCursor(null, null);
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		Vector<Tuple> tempList = new Vector<Tuple>();
		while (cursor.getNext(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
        	Tuple tuple = new Tuple(StringBinding.entryToString(keyEntry),StringBinding.entryToString(dataEntry));
        	tempList.add(tuple);
		}
        cursor.close();
        Tuple[] result = new Tuple[tempList.size()];
        tempList.toArray(result);
        return result;
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
	
	public static void main(String[] args) {
		try {
			System.out.println("LOADING...");
			BerkeleyDBHashTable hash = new BerkeleyDBHashTable(new File(args[0]));
			Tuple[] tuples = hash.listElements();
			for (int i = 0; i < tuples.length; i++) {
//				String[] links = tuples[i].getValue().split("###");
//				System.out.println("##" + links.length + "##");
				System.out.println(tuples[i].getKey() + ":" + tuples[i].getValue());
			}
//			String value = hash.get(args[1]);
//			System.out.println(value);
//			BerkeleyDBHashTable hash = new BerkeleyDBHashTable(new File(args[0]));
//			BerkeleyDBHashTable hashGraph = new BerkeleyDBHashTable(new File(args[1]));
////			Tuple[] tuples = hashGraph.listElements();
//////			for (int i = 0; i < tuples.length; i++) {
//////				System.out.println(tuples[i].getKey() + ":" + tuples[i].getValue());
//////			}
//			BerkeleyDBHashTable hashId = new BerkeleyDBHashTable(new File(args[2]));
////			Tuple[] tuples1 = hashId.listElements();
////			for (int i = 0; i < tuples1.length; i++) {
////				System.out.println(tuples1[i].getKey() + ":" + tuples1[i].getValue());
////			}
//			BerkeleyDBHashTable hashURL = new BerkeleyDBHashTable(new File(args[3]));
////			Tuple[] tuples2 = hashURL.listElements();
//			int id = 0;
//			Tuple[] tuples = hash.listElements();
//			for (int i = 0; i < tuples.length; i++) {
//				String keyIdStr = hashURL.get(tuples[i].getKey());
//				if(keyIdStr == null){
//					keyIdStr = id+"";
//					hashURL.put(tuples[i].getKey(),keyIdStr);	
//					id++;
//					String url_title = hashId.get(keyIdStr);
//					if(url_title == null){
//						hashId.put(keyIdStr, tuples[i].getKey());	
//					}
//				}
//				String[] values = tuples[i].getValue().split("###");
//				HashSet<String> usedURLs = new HashSet<String>();
//				StringBuffer buffer = new StringBuffer();
//				for (int j = 0; j < values.length; j++) {
//					String[] link = values[j].split(":::");
//					if(usedURLs.contains(link[0])){
//						continue;
//					}
//					usedURLs.add(link[0]);
//					String strId = hashURL.get(link[0]);
//					if(strId == null){
//						strId = id+"";
//						hashURL.put(link[0],strId);	
//						id++;
//						String url_title = hashId.get(strId);
//						if(url_title == null){
//							hashId.put(strId, values[j]);	
//						}
//					}
//					buffer.append(strId);
//					buffer.append("###");
//				}
////				if(tuples[i].getKey().equals("www.qualityinn.com")){
////					System.out.println(id + ":" + buffer.toString() + ":" + values.length);
////				}
//				hashGraph.put(keyIdStr,buffer.toString());
//			}
//			hashURL.put("MAX",id+"");
//			System.out.println("FINISHED");

		} catch (EnvironmentLockedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		
		
	}
}
