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
package achecrawler.link.classifier;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;

/**
 *
 * <p>
 * Description:This class implements a baseline crawler setting the link relevance according to the
 * page relevance given by the form classsifier.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 *
 * <p>
 * </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class LinkClassifierBaseline implements LinkClassifier {

    private Random randomGenerator;

    public LinkClassifierBaseline() {
        this.randomGenerator = new Random();
    }

    /**
     * This method classifies pages according to its relevance given by the form.
     *
     * @param page Page
     * @return LinkRelevance[]
     */
    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        
        List<LinkRelevance> linkRelevances = new ArrayList<>();
        URL[] links = page.getParsedData().getLinks();
        if (links != null && links.length > 0) {
            for (URL link : links) {
                double relevance = page.getTargetRelevance().getRelevance() * 100;
                if (relevance == 100) {
                    relevance = relevance + randomGenerator.nextInt(100);
                }
                linkRelevances.add(new LinkRelevance(link, relevance));
            }
        }
        return (LinkRelevance[]) linkRelevances.toArray(new LinkRelevance[linkRelevances.size()]);
    }

    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        return null;
    }

}
