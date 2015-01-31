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
import focusedCrawler.util.PriorityQueueLink;

import java.net.URL;

/**
 * This class manages the crawler frontier
 * @author Luciano Barbosa
 * @version 1.0
 */

public class FrontierManager {

	private PriorityQueueLink priorityQueue;

	private FrontierTargetRepositoryBaseline frontier;

	private int linksToLoad;

	public FrontierManager(PriorityQueueLink priorityQueue, FrontierTargetRepositoryBaseline frontier, int linksToLoad) throws
		FrontierPersistentException {

		this.priorityQueue = priorityQueue;
		this.frontier = frontier;
		this.linksToLoad = linksToLoad;
		this.loadQueue(linksToLoad);
	}

	public void setPolicy(boolean random){
		this.frontier.setPolicy(random);
	}
  
	public FrontierTargetRepositoryBaseline getFrontierPersistent(){
		return this.frontier;
	}

	public void loadQueue() throws FrontierPersistentException {
		loadQueue(linksToLoad);
	}

	public void clearFrontier(){
		priorityQueue.clear();
		System.out.println("###QUEUE:" + priorityQueue.size());
	}

	private void loadQueue(int numberOfLinks) throws FrontierPersistentException {
		priorityQueue.clear();
		LinkRelevance[] links = frontier.select(numberOfLinks);
		for (int i = 0; i < links.length; i++) {
			priorityQueue.insert(links[i]);
		}
	}

	public boolean isRelevant(LinkRelevance elem) throws FrontierPersistentException{
		boolean result = false;
		Integer value = frontier.exist(elem);
		if(value == null  && elem.getRelevance() > 0 && !elem.getURL().toString().endsWith("pdf") &&
				!elem.getURL().toString().endsWith("jpg") && !elem.getURL().toString().endsWith("gif") &&
				!elem.getURL().toString().endsWith("ps") && !elem.getURL().toString().endsWith("css") ){
			result = true;
		}
		return result;
	}
  
	public void insert(LinkRelevance[] linkRelevance) throws
		FrontierPersistentException {
		for (int i = 0; i < linkRelevance.length; i++) {
			LinkRelevance elem = linkRelevance[i];
			boolean insert = isRelevant(elem);
			if (insert) {
				frontier.insert(elem);
			}
		}
	}

	public boolean insert(LinkRelevance linkRelevance) throws  FrontierPersistentException {
		boolean insert = isRelevant(linkRelevance);
		if (insert) {
			insert = frontier.insert(linkRelevance);
		}
		return insert;
	}

  
	public LinkRelevance nextURL() throws FrontierPersistentException {

		URL url = null;
		LinkRelevance linkRelev = (LinkRelevance)priorityQueue.pop();
    
		if(linkRelev != null){
			boolean limit =  false;
			do{
				limit = frontier.reachLimit(linkRelev.getURL());
				if(!limit){
					url = linkRelev.getURL();
					frontier.delete(linkRelev);
				}else{
					frontier.delete(linkRelev);
					linkRelev = (LinkRelevance)priorityQueue.pop();
				}
			}while(limit && priorityQueue.size() > 0);
			int value = (int)linkRelev.getRelevance()/100;
			System.out.println(">>>>>URL:" + linkRelev.getURL() + " REL:" + value);
			System.out.println(">>>RELEV:" + linkRelev.getRelevance());
		}
		else{
			System.out.println("LOADED: " + linksToLoad);
			loadQueue(linksToLoad);
		}
		return linkRelev;
	}

}

