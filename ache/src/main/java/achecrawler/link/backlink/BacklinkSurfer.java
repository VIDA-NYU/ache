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
package achecrawler.link.backlink;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.link.LinkStorageConfig.BackSurferConfig;
import achecrawler.util.parser.BackLinkNeighborhood;

public class BacklinkSurfer {
    
    private static Logger logger = LoggerFactory.getLogger(BacklinkSurfer.class);
    
    private BacklinkApi backlinkApi;

    public BacklinkSurfer(BackSurferConfig config) {
        if(config.getMozAccessId() != null && config.getMozKey() != null) {
            logger.info("Using backlinks from Moz API.");
            this.backlinkApi = new MozBacklinkApi(config.getMozAccessId(), config.getMozKey());
        }
        else {
            logger.info("Using backlinks from Google.");
            this.backlinkApi = new GoogleBacklinkApi();
        }
    }

    public BackLinkNeighborhood[] getLNBacklinks(URL url) throws MalformedURLException, IOException {
        
        waitTimeLimitIfNecessary();
        
        String urlNoProtocol = URLEncoder.encode(url.toString().substring(7), "UTF-8");
        
        BackLinkNeighborhood[] links = backlinkApi.downloadBacklinks(urlNoProtocol);

        if (links != null) {
            logger.info("Found {} backlinks.", links.length);
            for (int i = 0; i < links.length; i++)
                logger.info(links[i].getLink());
        }
        return links;
    }

    private long lastVisit = 0;
    private void waitTimeLimitIfNecessary() {
        if (lastVisit == 0) {
            lastVisit = System.currentTimeMillis();
        } else {
            long diffTime = System.currentTimeMillis() - lastVisit;
            if (diffTime < 10000) {
                try {
                    logger.info("Waiting time limit to download backlinks.");
                    Thread.sleep(diffTime);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for time limit");
                }
            }
        }
        lastVisit = System.currentTimeMillis();
    }

}
