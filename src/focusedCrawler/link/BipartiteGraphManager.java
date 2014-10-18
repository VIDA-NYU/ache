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
package focusedCrawler.link;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;
import java.util.ArrayList;


import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierException;
import focusedCrawler.link.classifier.builder.BacklinkSurfer;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.FrontierPersistentException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.Page;
import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

/**
 * This class is responsible to manage the info in the graph (backlinks and outlinks).
 * @author lbarbosa
 *
 */

public class BipartiteGraphManager {

	private FrontierManager frontierManager;
	
	private BacklinkSurfer surfer;
	
	private LinkClassifier backlinkClassifier;
	
	private LinkClassifier outlinkClassifier;

	private BipartiteGraphRep rep;
	
	private int count = 0;

  //Data structure for stop conditions //////////////////////////
  private int maxPages = 100; //Maximum number of pages per each domain
  private HashMap<String, Integer> domainCounter;//Count number of pages for each domain
  ///////////////////////////////////////////////////////////////
	
	private final int pagesToCommit = 100;
	
	public BipartiteGraphManager(FrontierManager frontierManager, BipartiteGraphRep rep, LinkClassifier outlinkClassifier) throws IOException, ClassNotFoundException{
		this.frontierManager = frontierManager;
		this.outlinkClassifier = outlinkClassifier;
		this.rep = rep;
    this.domainCounter = new HashMap<String, Integer>();
	}
	
	public BipartiteGraphManager(FrontierManager frontierManager, BipartiteGraphRep rep, LinkClassifier outlinkClassifier, LinkClassifier backlinkClassifier) throws IOException, ClassNotFoundException{
		this.frontierManager = frontierManager;
		this.outlinkClassifier = outlinkClassifier;
		this.backlinkClassifier = backlinkClassifier;
		this.rep = rep;
    this.domainCounter = new HashMap<String, Integer>();
	}

  public void setMaxPages(int max){
    this.maxPages = max;
  }

	public void setBacklinkSurfer(BacklinkSurfer surfer){
		this.surfer = surfer;
	}
	
	public void setBacklinkClassifier(LinkClassifier classifier){
		this.backlinkClassifier = classifier;
	}

	public void setOutlinkClassifier(LinkClassifier classifier){
		this.outlinkClassifier = classifier;
	}

	
	public BipartiteGraphRep getRepository(){
		return this.rep;
	}
	
	public String insertOutlinks(Page page) throws IOException, FrontierPersistentException, LinkClassifierException{
		String outLinks = page.getIdentifier() + "\t1.0\t" + String.valueOf(System.currentTimeMillis() / 1000L);
		PaginaURL parsedPage = page.getPageURL();
		parsedPage.setRelevance(page.getRelevance());
		LinkRelevance[] linksRelevance = outlinkClassifier.classify(parsedPage);
    ArrayList<LinkRelevance> temp = new ArrayList<LinkRelevance>();
		HashSet<String> relevantURLs = new HashSet<String>();
		for (int i = 0; i < linksRelevance.length; i++) {
			if(frontierManager.isRelevant(linksRelevance[i])){
				String url = linksRelevance[i].getURL().toString();
        if(!relevantURLs.contains(url)){
          String domain = linksRelevance[i].getTopLevelDomainName();
          Integer domainCount = domainCounter.get(domain);
          if (domainCount == null)
            domainCount = 0;
          if (domainCount < maxPages){//Stop Condition
            domainCount++;
            domainCounter.put(domain, domainCount);
				    relevantURLs.add(url);
            temp.add(linksRelevance[i]);
				    outLinks += "\t" + url;
          }
        }
			}
		}
    
    LinkRelevance[] filteredLinksRelevance = temp.toArray(new LinkRelevance[relevantURLs.size()]);
		LinkNeighborhood[] lns = parsedPage.getLinkNeighboor();
		for (int i = 0; i < lns.length; i++) {
			if(!relevantURLs.contains(lns[i].getLink().toString())){
				lns[i] = null;
			}
		}
		rep.insertOutlinks(page.getURL(), lns);
		frontierManager.insert(filteredLinksRelevance);
		if(count == pagesToCommit){
			rep.commit();
			count = 0;
		}
		count++;
		return outLinks;
	}
	
	public void insertBacklinks(Page page) throws IOException, FrontierPersistentException, LinkClassifierException{
		URL url = page.getURL();
		BackLinkNeighborhood[] links = rep.getBacklinks(url);
		if(links == null || (links != null && links.length < 10)){
			links = surfer.getLNBacklinks(url);	
		}
		if(links != null && links.length > 0){
			LinkRelevance[] linksRelevance = new LinkRelevance[links.length];
			for (int i = 0; i < links.length; i++){
				if(links[i] != null){
					LinkNeighborhood ln = new LinkNeighborhood(new URL(links[i].getLink()));
					String title = links[i].getTitle();
					if(title != null){
						StringTokenizer tokenizer = new StringTokenizer(title," ");
						Vector<String> anchorTemp = new Vector<String>();
						while(tokenizer.hasMoreTokens()){
							 anchorTemp.add(tokenizer.nextToken());
			   		  	}
			   		  	String[] aroundArray = new String[anchorTemp.size()];
			   		  	anchorTemp.toArray(aroundArray);
			   		  	ln.setAround(aroundArray);
					}
					linksRelevance[i] = backlinkClassifier.classify(ln);
				}
			}
			frontierManager.insert(linksRelevance);
		}
		URL normalizedURL = new URL(url.getProtocol(), url.getHost(), "/"); 
		rep.insertBacklinks(normalizedURL, links);
		if(count == pagesToCommit){
			rep.commit();
			count = 0;
		}
		count++;
	}

}
