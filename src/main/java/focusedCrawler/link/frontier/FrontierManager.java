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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.persistence.TupleIterator;

/**
 * This class manages the crawler frontier
 * 
 * @author Luciano Barbosa
 * @version 1.0
 */

public class FrontierManager {

    private static final Logger logger = LoggerFactory.getLogger(FrontierManager.class);

    private final PriorityQueueLink priorityQueue;
    private final Frontier frontier;
    private final int linksToLoad;
    private final LinkFilter linkFilter;
    private final LinkSelector linkSelector;
    private final HostManager hostsManager;
    private final boolean downloadRobots;

    public FrontierManager(Frontier frontier, HostManager hostsManager, boolean downloadRobots,
                           int maxSizeLinkQueue, int linksToLoad,
                           LinkSelector linkSelector, LinkFilter linkFilter) {
        this.frontier = frontier;
        this.hostsManager = hostsManager;
        this.downloadRobots = downloadRobots;
        this.linksToLoad = linksToLoad;
        this.linkSelector = linkSelector;
        this.linkFilter = linkFilter;
        this.priorityQueue = new PriorityQueueLink(maxSizeLinkQueue);
        this.loadQueue(linksToLoad);
    }

    public Frontier getFrontierPersistent() {
        return this.frontier;
    }

    public void clearFrontier() {
        logger.info("Cleaning frontier... current queue size: " + priorityQueue.size());
        priorityQueue.clear();
        logger.info("# Queue size:" + priorityQueue.size());
    }

    private void loadQueue(int numberOfLinks) {
        priorityQueue.clear();
        frontier.commit();

        try(TupleIterator<LinkRelevance> it = frontier.iterator()) {
            
            linkSelector.startSelection(numberOfLinks);
            while(it.hasNext()) {
                Tuple<LinkRelevance> tuple = it.next();
                LinkRelevance link = tuple.getValue();
                if (link.getRelevance() > 0) {
                    linkSelector.evaluateLink(link);
                }
            }

            List<LinkRelevance> selectedLinks = linkSelector.getSelectedLinks();
            for (LinkRelevance link : selectedLinks) {
                priorityQueue.insert(link);
            }
            
        } catch (Exception e) {
            logger.error("Failed to read items from the frontier.", e);
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
        if (linkFilter.accept(url) == false) {
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
            if(downloadRobots) {
                URL url = linkRelevance.getURL();
                String hostName = url.getHost();
                if(!hostsManager.isKnown(hostName)) {
                    hostsManager.insert(hostName);
                    try {
                        URL robotUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/robots.txt");
                        LinkRelevance sitemap = new LinkRelevance(robotUrl, 299, LinkRelevance.Type.ROBOTS);
                        frontier.insert(sitemap);
                    } catch (Exception e) {
                        logger.warn("Failed to insert robots.txt for host: "+hostName, e);
                    } 
                }
            }
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
        hostsManager.close();
    }

    public Frontier getFrontier() {
        return frontier;
    }

    public void addSeeds(String[] seeds) {
        if (seeds != null && seeds.length > 0) {
            int count = 0;
            for (String seed : seeds) {
                logger.info("Adding seed URL: " + seed);

                URL seedUrl;
                try {
                    seedUrl = new URL(seed);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Invalid seed URL provided: " + seed, e);
                }
                LinkRelevance link = new LinkRelevance(seedUrl, LinkRelevance.DEFAULT_RELEVANCE);
                try {
                    boolean inserted = insert(link);
                    if (inserted)
                        count++;
                } catch (FrontierPersistentException e) {
                    throw new RuntimeException("Failed to insert seed URL: " + seed, e);
                }
            }
            logger.info("Number of seeds added: " + count);
        }
    }

}
