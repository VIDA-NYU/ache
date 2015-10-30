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
import java.net.URL;
import java.util.List;
import java.util.Map;

import focusedCrawler.util.parser.PaginaURL;

/**
 * <p>
 * Description:
 * </p>
 * 
 * @author Luciano Barbosa
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Page implements Serializable, Target {

    private boolean auth = false;

    private boolean hub = false;

    private URL url;

    private String content;

    private String cleanContent;

    private double relevance;

    private PaginaURL pageURL;

    private String encoding;

    private URL redirectedURL;

    private Map<String, List<String>> responseHeaders;
    
    public Page(URL url, String cont) {
        this(url, cont, null, null);
    }
    
    public Page(URL url, String cont, Map<String, List<String>> responseHeaders) {
        this(url, cont, responseHeaders, null);
    }

    public Page(URL url, String cont, Map<String, List<String>> responseHeaders, URL redirectedURL) {
        this.url = url;
        this.content = cont;
        this.responseHeaders = responseHeaders;
        this.redirectedURL = redirectedURL;
    }

    public URL getURL() {
        return url;
    }

    public String getDomainName() {
        String domain = url.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public String getContent() {
        return content;
    }

    public String getCleanContent() {
        return cleanContent;
    }

    public void setContent(String cont) {
        this.content = cont.toLowerCase();
    }

    public void setCleanContent(String cont) {
        this.cleanContent = cont.toLowerCase();
    }

    public void setPageURL(PaginaURL page) {
        this.pageURL = page;
        this.setCleanContent(page.palavras_to_string());
    }

    public void setRelevance(double rel) {
        this.relevance = rel;
    }

    public double getRelevance() {
        return this.relevance;
    }

    public String getIdentifier() {
        return this.url.toString();
    }

    public String getSource() {
        return this.content;
    }

    public PaginaURL getPageURL() {
        return this.pageURL;
    }

    public void setEncoding(String enc) {
        this.encoding = enc;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isHub() {
        return hub;
    }

    public void setHub(boolean hub) {
        this.hub = hub;
    }
    
    public URL getRedirectedURL() {
        return redirectedURL;
    }
    
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

}
