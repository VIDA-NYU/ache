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
package achecrawler.link.frontier;

import static achecrawler.util.Urls.isValid;
import static achecrawler.util.Urls.normalize;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

import achecrawler.util.Urls.UrlDeserializer;

@SuppressWarnings("serial")
public class LinkRelevance implements Serializable {

    public static double DEFAULT_RELEVANCE = 299;
    public static double DEFAULT_HUB_RELEVANCE = 100;
    public static double DEFAULT_AUTH_RELEVANCE = 200;
    
    public enum Type {
        FORWARD, ROBOTS, SITEMAP
    }
    
    public static Comparator<LinkRelevance> DESC_ORDER_COMPARATOR = new Comparator<LinkRelevance>() {
        @Override
        public int compare(LinkRelevance o1, LinkRelevance o2) {
            return Double.compare(o2.getRelevance(), o1.getRelevance());
        }
    };
    
    public static Comparator<LinkRelevance> DESC_ABS_ORDER_COMPARATOR = new Comparator<LinkRelevance>() {
        @Override
        public int compare(LinkRelevance o1, LinkRelevance o2) {
            return Double.compare(Math.abs(o2.getRelevance()), Math.abs(o1.getRelevance()));
        }
    };

    @JsonDeserialize(using = UrlDeserializer.class)
    private URL url;
    private double relevance;
    private Type type;
    
    public LinkRelevance() {
        // required for JSON serialization
    }

    public LinkRelevance(String url, double relevance) throws MalformedURLException {
        this(new URL(url), relevance);
    }

    public LinkRelevance(URL url, double relevance) {
        this(url, relevance, Type.FORWARD);
    }

    private LinkRelevance(String url, double relevance, Type type) throws MalformedURLException {
        this(new URL(url), relevance, type);
    }

    public LinkRelevance(URL url, double relevance, Type type) {
        this.url = url;
        this.relevance = relevance;
        this.type = type;
    }

    public URL getURL() {
        return url;
    }

    public double getRelevance() {
        return relevance;
    }
    
    public Type getType() {
        return type;
    }
    
    @JsonIgnore
    private static InternetDomainName getDomainName(String host) {
        InternetDomainName domain = InternetDomainName.from(host);
        if(host.startsWith("www.")) {
            return InternetDomainName.from(host.substring(4));
        } else {
            return domain;
        }
    }
    
    @JsonIgnore
    public String getTopLevelDomainName() {
        return getTopLevelDomain(url.getHost());
    }

    public static String getTopLevelDomain(String host) {
        InternetDomainName domain = null;
        try {
            domain = getDomainName(host);
            if(domain.isUnderPublicSuffix()) {
                return domain.topPrivateDomain().toString();
            } else {
                // if the domain is a public suffix, just use it as top level domain
                return domain.toString();
            }
        } catch (IllegalArgumentException e) {
            // when host is an IP address, use it as TLD
            if(InetAddresses.isInetAddress(host)) {
                return host;
            }
            throw new IllegalStateException("Invalid top private domain name=["+domain+"] in URL=["+host+"]", e);
        }
    }
    
    @Override
    public String toString() {
        return "LinkRelevance[url=" + url + ", relevance=" + relevance + "]";
    }

    public static LinkRelevance create(String url) {
        return createForward(url, LinkRelevance.DEFAULT_RELEVANCE);
    }

    public static LinkRelevance createForward(String url, double relevance) {
        if (isValid(url)) {
            return create(url, relevance, Type.FORWARD);
        }
        return null;
    }

    public static LinkRelevance createSitemap(String url, double relevance) {
        if (isValid(url)) {
            return create(url, relevance, Type.SITEMAP);
        }
        return null;
    }

    public static LinkRelevance createRobots(String url, double relevance) {
        if (isValid(url)) {
            return create(normalize(url), relevance, Type.ROBOTS);
        }
        return null;
    }

    private static LinkRelevance create(String url, double relevance, Type type) {
        try {
            return new LinkRelevance(normalize(url), relevance, type);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
