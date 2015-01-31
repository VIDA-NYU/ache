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
package focusedCrawler.link;

import focusedCrawler.util.persistence.PersistentHashtable;

import java.net.URL;
import java.io.IOException;

/**
 * <p>Description: This class genrates code for URLs and stores it</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * @author Luciano Barbosa
 * @version 1.0
 */
public class URLRepository {

  private PersistentHashtable codeToURL;
  private PersistentHashtable urlToCode;
  private int nextCode;

  public URLRepository(PersistentHashtable codeToURL, PersistentHashtable urlHash) throws URLRepositoryException {
    this.codeToURL = codeToURL;
    nextCode = codeToURL.size() + 1;
  }

  /**
   * Inserts an URL in the repository and return its code
   * @param url URL
   * @return int
   * @throws URLRepositoryException
   */
  public int insert(URL url) throws URLRepositoryException {
    int code = -1;
    String urlStr = url.toString();
    Object obj = urlToCode.get(urlStr);
    if(obj == null){
    	codeToURL.put("" + nextCode, urlStr);
        urlToCode.put(urlStr,nextCode+"");
        nextCode++;
        code = nextCode;
    }else{
    	throw new URLRepositoryException("URL:"+url+ " ALREADY EXISTS");
    }
    
    return code;
  }
  /**
   * This method updates an URL in the repository
   * @param url URL
   * @throws URLRepositoryException
   */
  public void update(URL url) throws URLRepositoryException {
	  String urlStr = url.toString();
      Object obj = urlToCode.get(urlStr);
      if(obj != null){
        codeToURL.put("" + nextCode, urlStr);
        urlToCode.put(urlStr, nextCode+"");
      }
      else {
        throw new URLRepositoryException("URL:" + url + " DOESNT EXIST");
      }

    }
    /**
     * This method gets the code from a given URL
     * @param url URL
     * @return int
     * @throws URLRepositoryException
     */

  public int select(URL url)throws URLRepositoryException {
    int code = -1;
    Object object = urlToCode.get(url.toString());
    if(object != null){
    	code = ((Integer)object).intValue();
    }
    return code;
  }

  /**
   * This method returns an URL gidev its code
   * @param code int
   * @return URL
   * @throws URLRepositoryException
   */

  public URL select(int code)throws URLRepositoryException {
    URL url = null;
    try {
      Object object = codeToURL.get(code + "");
      if(object != null){
        String urlStr = (String)object;
        url = new URL(urlStr);
      }
    }
    catch (IOException ex) {
      throw new URLRepositoryException(ex.getMessage());
    }
    return url;
  }

}
