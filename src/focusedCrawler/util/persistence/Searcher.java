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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Searcher {
  private File indexDir;
  
  public Searcher(String indexPath) {
	  this.indexDir = new File(indexPath);
	  if (!indexDir.isDirectory() || !indexDir.exists())
		try {
			throw new IOException(indexPath + " does not exist or is not a directory");
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	  
  }
  
  public String get(String key) throws Exception {
//	System.out.println("indexDir: " + this.indexDir.getAbsolutePath());
	  Directory fsDir = FSDirectory.open(indexDir);
	  IndexSearcher is = new IndexSearcher(fsDir);
	  ScoreDoc[] hits = search(this.indexDir,key,is);
	  if(hits.length != 0){
		  Document hitDoc = is.doc(hits[0].doc);
		  return hitDoc.get("value");
	  }else{
		  return null;
	  }
  }

  public TermEnum listElements(String indexDir) throws IOException{
	  try{
		  Directory fsDir = FSDirectory.open(new File(indexDir));
		  IndexSearcher is = new IndexSearcher(fsDir);
		  IndexReader reader = is.getIndexReader();
		  return reader.terms();
	  }catch(java.io.FileNotFoundException ex){
		  return null;
	  }
  }
  
  private static ScoreDoc[] search(File indexDir, String q, IndexSearcher is) throws Exception {
    Analyzer analyzer = new KeywordAnalyzer();
    QueryParser parser = new QueryParser(null, "key", analyzer);
    Query query = parser.parse(q);
	TopDocs topDocs = is.search(query, 20);
	ScoreDoc[] hits = topDocs.scoreDocs;
	return hits;
  }
  
  public static void main(String[] args) {
	  try {
		  Searcher searcher = new Searcher(args[0]);
		  long t0 = System.currentTimeMillis();
		  System.out.println(searcher.get(URLEncoder.encode((args[1]))));
		  System.out.println(System.currentTimeMillis()-t0);
	  } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
  }
  
}









