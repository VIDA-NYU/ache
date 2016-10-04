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
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.DownloadScheduler;
import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.LogFile;
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

    private final Frontier frontier;
    private final int linksToLoad;
    private final LinkFilter linkFilter;
    private final LinkSelector linkSelector;
    private final HostManager hostsManager;
    private final boolean downloadRobots;
    private final DownloadScheduler scheduler;
    private final LogFile schedulerLog;

    private boolean linksRejectedDuringLastLoad;


    public FrontierManager(Frontier frontier, String dataPath, boolean downloadRobots,
                           int linksToLoad, int schedulerMaxLinks, int schdulerMinAccessInterval,
                           LinkSelector linkSelector, LinkFilter linkFilter) {
        this.frontier = frontier;
        this.hostsManager = new HostManager(Paths.get(dataPath, "data_hosts"));;
        this.downloadRobots = downloadRobots;
        this.linksToLoad = linksToLoad;
        this.linkSelector = linkSelector;
        this.linkFilter = linkFilter;
        this.scheduler = new DownloadScheduler(schdulerMinAccessInterval, schedulerMaxLinks);
        this.loadQueue(linksToLoad);
        this.schedulerLog = new LogFile(Paths.get(dataPath, "data_monitor", "scheduledlinks.csv"));
    }

    public Frontier getFrontierPersistent() {
        return this.frontier;
    }

    public void clearFrontier() {
        logger.info("Cleaning frontier... current queue size: " + scheduler.numberOfLinks());
        scheduler.clear();
        logger.info("# Queue size:" + scheduler.numberOfLinks());
    }

    private void loadQueue(int numberOfLinks) {
        logger.info("Loading more links from frontier into the scheduler...");
        scheduler.clear();
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
            
            linksRejectedDuringLastLoad = false;
            int linksAdded = 0;
            List<LinkRelevance> selectedLinks = linkSelector.getSelectedLinks();
            for (LinkRelevance link : selectedLinks) {
                boolean addedLink = scheduler.addLink(link);
                if(addedLink) {
                    linksAdded++;
                } else {
                    linksRejectedDuringLastLoad = true;
                }
            }
            logger.info("Loaded {} links.", linksAdded);
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

    public LinkRelevance nextURL() throws FrontierPersistentException, DataNotFoundException {

        if(!scheduler.hasLinksAvailable()) {
            loadQueue(linksToLoad);
        }
        
        LinkRelevance link = scheduler.nextLink();
        if (link == null) {
            if(scheduler.hasPendingLinks() || linksRejectedDuringLastLoad) {
                throw new DataNotFoundException(false, "No links available for selection right now.");
            } else {
                throw new DataNotFoundException(true, "Frontier run out of links.");
            }
        }
        
        frontier.delete(link);
            
        schedulerLog.printf("%d\t%.5f\t%s\n", System.currentTimeMillis(),
                            link.getRelevance(), link.getURL().toString());

        return link;
    }

    public void close() {
        frontier.commit();
        frontier.close();
        hostsManager.close();
        schedulerLog.close();
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
