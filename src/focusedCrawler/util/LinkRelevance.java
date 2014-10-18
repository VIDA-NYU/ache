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
import com.google.common.net.InternetDomainName;

public class LinkRelevance implements Serializable{

  /**
	 * 
	 */
	private static final long serialVersionUID = 1349218562528024956L;

	private URL url;

	private double relevance;
	
	public static double DEFAULT_HUB_RELEVANCE = 100;
	
	public static double DEFAULT_AUTH_RELEVANCE = 200;

	public LinkRelevance(URL url, double relevance) {
		this.url = url;
		this.relevance = relevance;
	}

	public URL getURL(){
		return url;
	}

  public String getDomainName(){
    String domain = url.getHost();
    return domain.startsWith("www.") ? domain.substring(4) : domain;
  }

  public String getTopLevelDomainName(){
    String domain = this.getDomainName();
    InternetDomainName topPrivateDomain = InternetDomainName.from(domain).topPrivateDomain();
    return topPrivateDomain.toString();  
  }


	public double getRelevance(){
		return relevance;
	}
	
	public void setRelevance(double rel){
		this.relevance = rel;
	}

}
