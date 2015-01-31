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

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.cache.StringCacheKey;
import focusedCrawler.util.persistence.PersistentHashtable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.net.UnknownHostException;


public class FrontierFormRepository extends FrontierTargetRepositoryBaseline{

  private HashMap<String, Integer> hostPages;

  private int limitOfPagesPerSite = 250;
  
  public FrontierFormRepository(PersistentHashtable urlRelevance)  {
	  super(urlRelevance,null);
    

    this.hostPages = new HashMap<String, Integer>();
////    try {
////      loadHash();
////    }
////    catch (ClassNotFoundException ex) {
////      ex.printStackTrace();
////    }
//    catch (IOException ex) {
//      ex.printStackTrace();
//    }
  }

  
//  public void saveHash() throws FileNotFoundException, IOException {
//     System.out.println(">>>>>SAVING HOST_PAGES:"+urlRelevance.getDirectory() + "/hostPages.pers" );
//     FileOutputStream fout = new FileOutputStream( urlRelevance.getDirectory() + "/hostPages.pers" );
//     ObjectOutputStream oous = new ObjectOutputStream(fout);
//     oous.writeObject(hostPages);
//     fout.close();
//     oous.close();
//  }

//  public void loadHash() throws IOException, ClassNotFoundException {
//    File hashPers = new File(urlRelevance.getDirectory() + "/hostPages.pers");
//    System.out.println("HOST_PAGE EXIST:" + hashPers.exists());
//    if(hashPers.exists()){
//      FileInputStream fin = new FileInputStream( hashPers );
//      ObjectInputStream ois = new ObjectInputStream(fin);
//      hostPages = (HashMap)ois.readObject();
//    }
//  }

//  public Integer exist(LinkRelevance linkRelev) throws FrontierPersistentException {
//		String url = linkRelev.getURL().toString();
//		boolean result = false;
//		if(urlRelevance.get(url) != null || reachLimit(linkRelev.getURL())|| url.indexOf("porn") != -1 || 
//				url.indexOf("sex") != -1 || url.indexOf("adult") != -1 || url.indexOf("xxx") != -1){
//			result = true;
//		}
//  		return result;
//	}
//
//  int count = 0;
//  long existTime = 0;
//  long insertTime = 0;
//  
//  
//	public boolean insert(LinkRelevance linkRelev) throws FrontierPersistentException {
//
//		boolean inserted = false;
//		String url = linkRelev.getURL().toString();
//		long tempExist = System.currentTimeMillis();
//		boolean exist = exist(linkRelev);
//		existTime = existTime + (tempExist - System.currentTimeMillis());
//		if (!exist && url.toString().length() < 210) {
//			String host = filterServer(linkRelev.getURL().getHost());
////			String ip = null;
////        	try {
////				ip = InetAddress.getByName(linkRelev.getURL().getHost()).toString();
////				ip = ip.substring(ip.indexOf("/")+1,ip.lastIndexOf("."));
////        	} catch (UnknownHostException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//
//			Object obj = hostPages.get(host);
//			if (obj == null) {
//				hostPages.put(host, new Integer(0));
//			}else {
//				int numPages = ( (Integer) (obj)).intValue();
////					System.out.println(">>> TOTAL:" + numPages);
//				if(numPages > limitOfPagesPerSite){
//					return false;
//				}else{
//					hostPages.put(host, new Integer(numPages+1));
//				}
//			}
////			Object objIP = hostPages.get(ip);
////			if (objIP == null) {
////				hostPages.put(ip, new Integer(0));
////			}else {
////				int numPages = ( (Integer) (objIP)).intValue();
//////					System.out.println(">>> TOTAL:" + numPages);
////				if(numPages > limitOfPagesPerSite*3){
////					return false;
////				}else{
////					hostPages.put(ip, new Integer(numPages+1));
////				}
////			}
//			
//			double relevance = linkRelev.getRelevance();
//			int relevInt = (int) (relevance);
//			long tempInsert = System.currentTimeMillis();
//			urlRelevance.put(url.toString(), relevInt+"");
//			insertTime = insertTime + (tempInsert - System.currentTimeMillis());
//			if ((relevInt >= 700 && count < 50) || relevInt == 10000) { //to avoid insert bad links in queue
//				inserted = true;
//				count++;
//			}	
//		}
//		return inserted;
//	}
  
    
  
  public LinkRelevance[] select(int numberOfLinks) throws
      FrontierPersistentException {

    HashMap<Integer, Integer> queue = new HashMap<Integer, Integer>();
    HashMap<String, Integer> hosts = new HashMap<String, Integer>();
    LinkRelevance[] result = null;
      try {
    	  Iterator keys = urlRelevance.getKeys();
    	  Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
    	  int count = 0;
    	  for (int i = 0; count < numberOfLinks && keys.hasNext(); i++) {
//    		  String key = ((StringCacheKey)keys.next()).toString();
    		  String key = ((String)keys.next()).toString();
    		  String url = URLDecoder.decode(key);
    		  if (url != null){
//    			  System.out.println(url);
    			  Integer relevInt = new Integer((String)urlRelevance.get(url));
    			  if(relevInt != null){
    				  int relev = relevInt.intValue();
    				  if(relev > 0){
    					  URL urlTemp = new URL(url);
    					  String host =  filterServer(urlTemp.getHost());
//                    System.out.println(">>> HOST" + host);
    					  Integer numPag = hosts.get(host);
    					  if(numPag == null){
    						  hosts.put(host, new Integer(1));
//                		System.out.println(">>> PAGES = 1 ");
    					  }else{
    						  hosts.put(host,  new Integer(numPag.intValue() + 1));
//                		System.out.println(">>> PAGES = " + numPag.intValue());
    					  }
    					  if(numPag == null || numPag.intValue() < 10){
    						  Integer numOccur = ((Integer)queue.get(relevInt));
    						  int numOccurInt = 0;
    						  if(numOccur != null){
    							  if(relev % 100 == 99 && relev > 200){
    								  numOccurInt = numOccur.intValue() + 1;
    							  }else{
    								  numOccurInt = numOccur.intValue() + 5;
    							  }
    						  }else{
    							  numOccurInt = 1;
    						  }
    						  queue.put(relevInt,new Integer(numOccurInt));
    						  if(numOccurInt < 50 || relev > 1000){
    							  System.out.println(">>> INSERTING:" + url);
    							  LinkRelevance linkRel = new LinkRelevance(new URL(url),relev);
    							  tempList.add(linkRel);
    							  count++;
    						  }
    					  }
    				  }
    			  }
    		  }
    	  }
    	  hosts.clear();
    	  System.out.println("Total loaded:"+count);
    	  result = new LinkRelevance[tempList.size()];
    	  tempList.toArray(result);
    	  queue.clear();
      }catch (CacheException ex) {
    	  ex.printStackTrace();
      }catch (IOException ex) {
    	  ex.printStackTrace();
      }
    
    return result;
  }

  public boolean reachLimit(URL url){
    boolean result = false;
    String host = filterServer(url.getHost());
    Object obj = hostPages.get(host);
    if(obj != null){
      int numPages = ( (Integer) (obj)).intValue();
//      if(numPages >= limitOfPagesPerSite){
//        result = true;
//      }
    }
    return result;
  }

  private String filterServer(String server){
	  if(server.lastIndexOf(".") != -1){
		  String serverTemp = server.substring(0,server.lastIndexOf("."));
		  int index = serverTemp.lastIndexOf(".");
		  if(index != -1){
			  server = server.substring(index+1);
		  }
	  }
	  return server;
  }

  
  public static void main(String[] args) {
//    try {
//    	focusedCrawler.util.ParameterFile config = new focusedCrawler.util.ParameterFile(args[0]);
//    	String dir = config.getParam("LINK_DIRECTORY");
//    	PersistentHashtable urls = new PersistentHashtable(dir,1000);
//    	FrontierFormRepository frontier = new FrontierFormRepository(urls);
//    	int count = 0;
//    	if(args.length > 1){
//    		   BufferedReader input = new BufferedReader(new FileReader(args[1]));
//               for (String line1 = input.readLine(); line1 != null; line1 = input.readLine()) {
//            	   LinkRelevance linkRel = new LinkRelevance(new URL(line1), 299);
//            	   frontier.insert(linkRel);
//            	   count++;
//               }
//    	}else{
//        	String[] seeds = config.getParam("SEEDS"," ");
//        	for (int i = 0; i < seeds.length; i++) {
//        		LinkRelevance linkRel = new LinkRelevance(new URL(seeds[i]), 299);
//        		frontier.insert(linkRel);
//        		count++;
//        	}
//    	}
//    	System.out.println("# SEEDS:" + count);
//    	frontier.close();
//    }
//    catch (MalformedURLException ex) {
//      ex.printStackTrace();
//    }
//    catch (IOException ex) {
//      ex.printStackTrace();
//    }
//    catch (FrontierPersistentException ex) {
//      ex.printStackTrace();
//    } catch (CacheException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
  }
}

