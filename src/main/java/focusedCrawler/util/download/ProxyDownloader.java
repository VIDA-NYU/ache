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
package focusedCrawler.util.download;





import java.io.InputStream;

import java.io.OutputStream;

import java.net.URL;

import java.util.Iterator;

import focusedCrawler.util.Log;






public class ProxyDownloader implements Downloader {



  private Downloader downloader;



  public ProxyDownloader( Downloader _downloader) {

    this.downloader = _downloader;

  }



  public void setShowNormalLog(boolean newNormalLog) throws DownloaderException {

    downloader.setShowNormalLog( newNormalLog);

  }

  public void setNormalLog(Log newNormalLog) throws DownloaderException {

    downloader.setNormalLog(newNormalLog);

  }

  public void setShowErrorLog(boolean newErrorLog) throws DownloaderException {

    downloader.setShowErrorLog(newErrorLog);

  }

  public void setErrorLog(Log newErrorLog) throws DownloaderException {

    downloader.setErrorLog(newErrorLog) ;

  }

  public void setId(String newId) throws DownloaderException {

    downloader.setId(newId);

  }

  public String getId() throws DownloaderException {

    return downloader.getId();



  }

  public void clearRequestProperties() throws DownloaderException {

    downloader.clearRequestProperties() ;

  }

  public void setRequestProperty(String name, String value) throws DownloaderException {

    downloader.setRequestProperty(name, value) ;

  }

  public void setProtocol(String newProtocol) throws DownloaderException {

    downloader.setProtocol(newProtocol);

  }

  public String getProtocol() throws DownloaderException {

    return downloader.getProtocol();

  }

  public void setMethod(String newMethod) throws DownloaderException {

    downloader.setMethod(newMethod);

  }

  public String getMethod() throws DownloaderException {

    return downloader.getMethod();

  }

  public void setTimeout(int timeout) throws DownloaderException {

    downloader.setTimeout(timeout);

  }

  public int getTimeout() throws DownloaderException {

    return downloader.getTimeout();

  }

  public void setUrlTarget(URL newUrlTarget) throws DownloaderException {

    downloader.setUrlTarget(newUrlTarget);

  }

  public URL getUrlTarget() throws DownloaderException {

    return downloader.getUrlTarget();

  }

  public void setFollowRedirects(boolean newFollowRedirects) throws DownloaderException {

    downloader.setFollowRedirects( newFollowRedirects);

  }

  public boolean isFollowRedirects() throws DownloaderException {

    return downloader.isFollowRedirects();

  }

  public void setFollowRedirectsTolerance(int newFollowRedirectsTolerance) throws DownloaderException {

    downloader.setFollowRedirectsTolerance(newFollowRedirectsTolerance);

  }

  public int getFollowRedirectsTolerance() throws DownloaderException {

    return downloader.getFollowRedirectsTolerance();

  }

  public void clearResponseProperties() throws DownloaderException {

    downloader.clearResponseProperties();

  }

  public void connect() throws DownloaderException {

    downloader.connect();

  }

  public int getStatus() throws DownloaderException {

    return downloader.getStatus();

  }

  public String getResponseProtocol() throws DownloaderException {

    return downloader.getResponseProtocol();

  }

  public int getResponseCode() throws DownloaderException {

    return downloader.getResponseCode();

  }

  public String getResponseMessage() throws DownloaderException {

    return downloader.getResponseMessage();

  }

  public String getContentType() throws DownloaderException {

    return downloader.getContentType();

  }

  public long getLastModified() throws DownloaderException {

    return downloader.getLastModified();

  }

  public int getContentLength() throws DownloaderException{

    return downloader.getContentLength() ;

  }

  public String getResponseProperty(String name) throws DownloaderException {

    return downloader.getResponseProperty(name);

  }

  public String getResponseProperty(String name, boolean caseSensitive) throws DownloaderException {

    return downloader.getResponseProperty(name, caseSensitive);

  }

  public Iterator listResponse() throws DownloaderException {

    return downloader.listResponse();

  }

  public OutputStream getOutputStream() throws DownloaderException {

    return downloader.getOutputStream();

  }

  public InputStream getInputStream() throws DownloaderException {

    return downloader.getInputStream();

  }

  public void close() throws DownloaderException {

    downloader.close();

  }

  public boolean isShutdown() throws DownloaderException {

    return downloader.isShutdown();

  }

}