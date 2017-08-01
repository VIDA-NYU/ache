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

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.UrlValidator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

@SuppressWarnings("serial")
public class LinkRelevance implements Serializable {

    public static double DEFAULT_RELEVANCE = 299;
    public static double DEFAULT_HUB_RELEVANCE = 100;
    public static double DEFAULT_AUTH_RELEVANCE = 200;
    
    private static final UrlValidator validator = new UrlValidator(new String[] {"http","https"});
    // .onion links aren't accepted by the validator
    // Regex ".[^.]+" --> any string of at least 1 char without dot
    private static final Pattern onionPattern = Pattern.compile("https?://.[^.]+\\.onion.*");

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

    @JsonDeserialize(using = UrlDeseralizer.class)
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
    
    public static LinkRelevance create(String url) throws MalformedURLException {
        return new LinkRelevance(new URL(url), LinkRelevance.DEFAULT_RELEVANCE);
    }
    
    @Override
    public String toString() {
        return "LinkRelevance[url=" + url + ", relevance=" + relevance + "]";
    }

    public static class UrlDeseralizer extends JsonDeserializer<URL> {
        @Override
        public URL deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = parser.getCodec().readTree(parser);
            return new URL(node.asText());
        }
    }

    public static LinkRelevance createForward(String url, double relevance) {
        try {
            if (isValid(url)) {
                return new LinkRelevance(url, relevance, Type.FORWARD);
            }
        } catch (MalformedURLException e) {
        }
        return null;
    }

    public static LinkRelevance createSitemap(String url, double relevance) {
        try {
            if (isValid(url)) {
                return new LinkRelevance(url, relevance, Type.SITEMAP);
            }
        } catch (MalformedURLException e) {
        }
        return null;
    }

    public static LinkRelevance createRobots(String url, double relevance) {
        try {
            if (isValid(url)) {
                return new LinkRelevance(url, relevance, Type.ROBOTS);
            }
        } catch (MalformedURLException e) {
        }
        return null;
    }

    private static boolean isValid(String url) {
        return validator.isValid(url) || onionPattern.matcher(url).matches();
    }

}
