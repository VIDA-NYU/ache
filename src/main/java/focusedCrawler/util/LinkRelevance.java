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
package focusedCrawler.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.net.InternetDomainName;

@SuppressWarnings("serial")
public class LinkRelevance implements Serializable {

    public static double DEFAULT_RELEVANCE = 299;
    public static double DEFAULT_HUB_RELEVANCE = 100;
    public static double DEFAULT_AUTH_RELEVANCE = 200;

    private final URL url;
    private final double relevance;

    public LinkRelevance(URL url, double relevance) {
        this.url = url;
        this.relevance = relevance;
    }

    public LinkRelevance(String string, int relevance) throws MalformedURLException {
        this(new URL(string), relevance);
    }

    public URL getURL() {
        return url;
    }

    public double getRelevance() {
        return relevance;
    }
    
    public InternetDomainName getDomainName() {
        String host = url.getHost();
        InternetDomainName domain = InternetDomainName.from(host);
        if(host.startsWith("www.")) {
            return InternetDomainName.from(host.substring(4));
        } else {
            return domain;
        }
    }
    
    public String getTopLevelDomainName() {
        InternetDomainName domain = this.getDomainName();
        try {
            if(domain.isUnderPublicSuffix()) {
                return domain.topPrivateDomain().toString();
            } else {
                // if the domain is a public suffix, just use it as top level domain
                return domain.toString();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Invalid top private domain name=["+domain+"] in URL=["+url+"]", e);
        }
    }

}
