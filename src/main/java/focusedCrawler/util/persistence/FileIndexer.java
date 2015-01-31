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

import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.analysis.Analyzer;

import java.net.URLEncoder;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A File Indexer capable of recursively indexing a directory tree.
 */
public class FileIndexer
{
  private String indexPath = "";
  private Analyzer analyzer;
  private Directory dir;
  private File indexFile;
  private IndexWriter writer;
  private HashMap<String, String> tempHash;
  
  
  public FileIndexer(String indexPath) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
	    this.indexPath = indexPath;
	    this.dir = FSDirectory.open(new File(this.indexPath));
	    this.indexFile = new File(this.indexPath + File.separator + "segments.gen");
	    String textSearchAnalyzer = "org.apache.lucene.analysis.StopAnalyzer";
	    Class analyzerClass = Class.forName(textSearchAnalyzer);
	    this.analyzer = (Analyzer)analyzerClass.newInstance();
	    this.tempHash = new HashMap<String, String>(10000);
	    if(!indexFile.exists()) {
		    this.writer = new IndexWriter(dir, analyzer, true, null);
		  	this.writer.setMergeFactor(50);
		  	this.writer.setRAMBufferSizeMB(100);
		    this.index(writer, URLEncoder.encode("http://"), "-1");
			writer.close();
	    }
  }//

  public boolean index(IndexWriter writer, String key, String value){
	 
	boolean success = true;
//    System.out.println("Indexing " + key);
    try {
    	org.apache.lucene.document.Document doc =
  	      new org.apache.lucene.document.Document();
  	  
    	doc.add(new Field("key", key, Field.Store.YES, Field.Index.NOT_ANALYZED));
      	doc.add(new Field("value", value, Field.Store.YES, Field.Index.NO));

        if (doc != null) {
//        	long startLS = System.currentTimeMillis();
        	writer.addDocument(doc);
//        	System.out.println(">>>>INDEX TIME:" + (System.currentTimeMillis() - startLS));
        }
        else {
        	System.err.println("Cannot handle " + key + "; ERROR");
        	success = false;
        }
    }
    catch (Exception e)
    {
    	success = false;
    	System.err.println("Cannot index " + key + "; ERROR (" + e.getMessage() + ")");
    	e.printStackTrace();
    }
    
    return success;
  }

  public boolean put(String key, String value) throws Exception {

	tempHash.put(key, value); 
	
	if(tempHash.size() == 50000){
		System.out.println(">>>>>CLOSING..");
		this.close();
	}

//    System.out.println("indexFile: " + indexFile.getAbsolutePath() + " exists: " + indexFile.exists());
//    long time1 = System.currentTimeMillis();
//
//    System.out.println(">>>>INDEX TIME 1:" + (System.currentTimeMillis() - time1));
    //get the analyzer

    
    
//    if(!createNewIndex && (search.get(key) != null)){
////    	System.out.println("key: " + key + " already exists! Deleting existing value!");
//    	IndexWriter writer = new IndexWriter(dir, analyzer, false);
//    	Term term = new Term("key", key);
//    	writer.deleteDocuments(term);   
////    	System.out.println("deletion complete!");
//    	writer.close();
//    }
    
    
//    writer.setMaxMergeDocs(10000);
//    long time2 = System.currentTimeMillis();
//    
//    System.out.println(">>>>INDEX TIME 2 :" + (System.currentTimeMillis() - time2));
//    if(optimize){
//    	writer.optimize();
//    }
//    long time3 = System.currentTimeMillis();
//    
//    System.out.println(">>>>INDEX TIME 3:" + (System.currentTimeMillis() - time3));
    return true;
  }
  
  public void close() throws CorruptIndexException, LockObtainFailedException, IOException{
	  if(indexFile.exists()) {
	        this.writer = new IndexWriter(dir, analyzer, false, null);
	    }else{
	    	this.writer = new IndexWriter(dir, analyzer, true, null);
	    }
	  	this.writer.setMergeFactor(50);
	  	this.writer.setRAMBufferSizeMB(100);
		Iterator iter = tempHash.keySet().iterator();
		while(iter.hasNext()){
			String tempKey = (String)iter.next();
			String tempValue = tempHash.get(tempKey);
//			System.out.println("INSERTING:" + tempKey + ":" + tempValue);
			this.index(writer, tempKey, tempValue);	
		}
		writer.optimize();
		writer.close();
		tempHash.clear();
  }
  
  public void flush()throws CorruptIndexException, LockObtainFailedException, IOException{
//	  if(indexFile.exists()) {
//      this.writer = new IndexWriter(dir, analyzer, false);
//  }else{
//  	this.writer = new IndexWriter(dir, analyzer, true);
//  }


	Iterator iter = tempHash.keySet().iterator();
	while(iter.hasNext()){
		String tempKey = (String)iter.next();
		String tempValue = tempHash.get(tempKey);
//		System.out.println("INSERTING:" + tempKey + ":" + tempValue);
		this.index(writer, tempKey, tempValue);	
	}
//	writer.optimize();
	
	tempHash.clear();
	  
  }
  
  public static void main(String[] args) {
	  try {
		  FileIndexer indexer = new FileIndexer(args[0]);
		  long t0 = System.currentTimeMillis();
		  indexer.put(java.net.URLEncoder.encode(args[1]),args[2]);
		  indexer.close();
		  System.out.println(System.currentTimeMillis()-t0);
	  } catch (Exception e) {
		// TODO: handle exception
	  }
	  
  }
  
//  public boolean optimize() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
//	  Directory dir = FSDirectory.getDirectory(this.indexPath);
//	  File indexFile = new File(this.indexPath + File.separator + "segments.gen");
////	  System.out.println("indexFile: " + indexFile.getAbsolutePath() + " exists: " + indexFile.exists());
//	  if(!indexFile.exists()) {
////	  	System.out.println("Unable to find index files! Aborting ..");
//	  	return false;
//	  }
//	  
//	 String textSearchAnalyzer = "org.apache.lucene.analysis.StopAnalyzer";
//	 Class analyzerClass = Class.forName(textSearchAnalyzer);
//	 Analyzer analyzer = (Analyzer)analyzerClass.newInstance();
//
//	 IndexWriter writer = new IndexWriter(dir, analyzer, false);
//	 writer.optimize();
//	 writer.close();
//	 return true;	    
//  }
}
