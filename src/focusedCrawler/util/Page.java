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

import java.net.URL;
import java.io.Serializable;

import focusedCrawler.util.parser.PaginaURL;

/**
 * <p>Description: </p>
 * @author Luciano Barbosa
 * @version 1.0
 */

public class Page implements Serializable, Target{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean auth = false;
	
	private boolean hub = false;
	
	private URL url;

    private String content;
    
    private String cleanContent;

    private double relevance;

    private PaginaURL pageURL;
    
    private String encoding;
    
    public Page() {

    }

    public Page(URL url, String cont) {
        this.url = url;
        this.content = cont;
    }


    public URL getURL(){
        return url;
    }

    public String getDomainName(){
        String domain = url.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public String getContent(){
        return content;
    }

    public String getCleanContent(){
        return cleanContent;
    }
    
    public void setContent(String cont){
        this.content = cont.toLowerCase();
    }

    public void setCleanContent(String cont){
        this.cleanContent = cont.toLowerCase();
    }

    public void setPageURL(PaginaURL page){
        this.pageURL = page;
    }

    public void setRelevance(double rel){
        this.relevance = rel;
    }

    public double getRelevance(){
        return this.relevance;
    }

    public String getIdentifier() {
    	return this.url.toString();
    }

    public String getSource() {
    	return this.content;
    }

    public PaginaURL getPageURL(){
        return this.pageURL;
    }

    public void setEncoding(String enc){
        this.encoding = enc;
    }

    public String getEncoding(){
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

    
}

