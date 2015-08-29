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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.PriorityQueueLink;

/**
 * This class manages the crawler frontier
 * 
 * @author Luciano Barbosa
 * @version 1.0
 */

public class FrontierManager {

    private static final Logger logger = LoggerFactory.getLogger(FrontierManager.class);

    private PriorityQueueLink priorityQueue;

    private Frontier frontier;

    private int linksToLoad;

    private LinkFilter linkFilter;

    public FrontierManager(Frontier frontier,
                           int maxSizeLinkQueue,
                           int linksToLoad,
                           LinkFilter linkFilter)
                           throws FrontierPersistentException {
        this.priorityQueue = new PriorityQueueLink(maxSizeLinkQueue);
        this.frontier = frontier;
        this.linksToLoad = linksToLoad;
        this.loadQueue(linksToLoad);
        this.linkFilter = linkFilter;
    }

    public Frontier getFrontierPersistent() {
        return this.frontier;
    }

    public void clearFrontier() {
        logger.info("Cleaning frontier... current queue size: " + priorityQueue.size());
        priorityQueue.clear();
        logger.info("# Queue size:" + priorityQueue.size());
    }

    private void loadQueue(int numberOfLinks) throws FrontierPersistentException {
        priorityQueue.clear();
        LinkRelevance[] links = frontier.select(numberOfLinks);
        for (int i = 0; i < links.length; i++) {
            priorityQueue.insert(links[i]);
        }
    }

    public boolean isRelevant(LinkRelevance elem) throws FrontierPersistentException {
        
        if (elem.getRelevance() <= 0) {
            return false;
        }

        Integer value = frontier.exist(elem);
        if (value != null) {
            return false;
        }

        String url = elem.getURL().toString();
        if (url.endsWith("pdf") ||
            url.endsWith("jpg") ||
            url.endsWith("gif") ||
            url.endsWith("ps")  ||
            url.endsWith("css") ||
            linkFilter.accept(url) == false) {
            return false;
        }
        
        return true;
    }

    public void insert(LinkRelevance[] linkRelevance) throws FrontierPersistentException {
        for (int i = 0; i < linkRelevance.length; i++) {
            LinkRelevance elem = linkRelevance[i];
            this.insert(elem);
        }
    }

    public boolean insert(LinkRelevance linkRelevance) throws FrontierPersistentException {
        boolean insert = isRelevant(linkRelevance);
        if (insert) {
            insert = frontier.insert(linkRelevance);
        }
        return insert;
    }

    public LinkRelevance nextURL() throws FrontierPersistentException {

        if(priorityQueue.size() == 0) {
            // Load more links from frontier into the priority queue
            loadQueue(linksToLoad);
        }
        
        LinkRelevance linkRelev = (LinkRelevance) priorityQueue.pop();
        if (linkRelev == null) {
            return null;
        }

        frontier.delete(linkRelev);
            
        logger.info("\n> URL:" + linkRelev.getURL() +
                    "\n> REL:" + ((int) linkRelev.getRelevance() / 100) +
                    "\n> RELEV:" + linkRelev.getRelevance());

        return linkRelev;
    }
    
    public void close() {
        frontier.commit();
        frontier.close();
    }

}
