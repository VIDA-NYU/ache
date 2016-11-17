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
package focusedCrawler.target.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.metadata.Metadata;

import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.classifier.TargetClassifier.TargetRelevance;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author Luciano Barbosa
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Page implements Serializable {

    private URL url;
    private URL redirectedURL;
    private String content;
    private Map<String, List<String>> responseHeaders;
    private long fetchTime;
    private boolean auth = false;
    private LinkRelevance linkRelevance;
    private ParsedData parsedData;
    private TargetRelevance targetRelevance;

    public Page(URL url, String content) {
        this(url, content, null, null);
    }

    public Page(URL url, String content, Map<String, List<String>> responseHeaders) {
        this(url, content, responseHeaders, null);
    }

    public Page(URL url, String content, Map<String, List<String>> responseHeaders, URL redirectedURL) {
        this.url = url;
        this.content = content;
        this.responseHeaders = responseHeaders;
        this.redirectedURL = redirectedURL;
    }
    
    public Page(TargetModelCbor target) throws MalformedURLException {
        this.url = new URL(target.url);
        this.content = (String) target.response.get("body");
        this.fetchTime = target.timestamp * 1000;
    }
    
    public Page(FetchedResult fetchedResult) throws MalformedURLException {
        this.url = new URL(fetchedResult.getBaseUrl());
        this.content =  new String(fetchedResult.getContent());
        this.fetchTime = fetchedResult.getFetchTime();
        this.responseHeaders = parseResponseHeaders(fetchedResult.getHeaders());
        if (fetchedResult.getNumRedirects() > 0) {
            this.redirectedURL = new URL(fetchedResult.getFetchedUrl());
        }
    }
    
    private Map<String, List<String>> parseResponseHeaders(Metadata headerAsMetadata) {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        String[] names = headerAsMetadata.names();
        if(names != null && names.length > 0) {
            for(String name : names) {
                responseHeaders.put(name, Arrays.asList(headerAsMetadata.getValues(name)));
            }
        }
        return responseHeaders;
    }

    public String getDomainName() {
        String domain = url.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public boolean isHub() {
        if (linkRelevance != null) {
            double relevance = linkRelevance.getRelevance();
            return relevance > LinkRelevance.DEFAULT_HUB_RELEVANCE &&
                   relevance < LinkRelevance.DEFAULT_AUTH_RELEVANCE;
        }
        return false;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getIdentifier() {
        return this.url.toString();
    }

    public URL getURL() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content.toLowerCase();
    }

    public URL getRedirectedURL() {
        return redirectedURL;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public LinkRelevance getLinkRelevance() {
        return linkRelevance;
    }

    public void setLinkRelevance(LinkRelevance linkRelevance) {
        this.linkRelevance = linkRelevance;
    }

    public ParsedData getParsedData() {
        return parsedData;
    }

    public void setParsedData(ParsedData parsedData) {
        this.parsedData = parsedData;
    }

    public TargetRelevance getTargetRelevance() {
        return targetRelevance;
    }

    public void setTargetRelevance(TargetRelevance targetRelevance) {
        this.targetRelevance = targetRelevance;
    }

}
