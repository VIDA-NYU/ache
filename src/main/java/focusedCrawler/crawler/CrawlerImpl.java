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
package focusedCrawler.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.download.DownloaderBuffered;
import focusedCrawler.util.download.DownloaderException;
import focusedCrawler.util.Page;

/**
 * This class implements a crawler
 * @author lbarbosa
 *
 */

public class CrawlerImpl extends Crawler {
    
    private static final Logger logger = LoggerFactory.getLogger(CrawlerImpl.class);

    private Storage linkStorage;

    private Storage targetStorage;

    protected DownloaderBuffered downloader;

    protected URL initialUrl;

    protected URL currentUrl;

    protected URL urlFinal;

    protected static long DATE_UNKNOWN = -1;

    protected int length;

    protected double relevance;
    
    protected Page page;

    protected byte[] buffer;

    protected int bufferSize;

    protected String source;
    
    protected Downloader urlDownloader;
    
    protected CrawlerImpl(ThreadGroup tg, String name) {
    	super(tg, name);
    }

    public CrawlerImpl(){
    }


    public CrawlerImpl(ThreadGroup tg, String name, Storage linkStorage, Storage formStorage) {
    	super(tg,name);
    	setLinkStorage(linkStorage);
    	setFormStorage(formStorage);
    }
    
    public void setLinkStorage(Storage newLinkStorage) {
	  linkStorage = newLinkStorage;
    }

  public Storage getLinkStorage() {
	  return linkStorage;
  }

 
  public void setFormStorage(Storage newFormStorage) {
	  targetStorage = newFormStorage;
  }

  public Storage getFormStorage() {
	  return targetStorage;
  }

  public void setDownloader(DownloaderBuffered newDownloader) {
	  downloader = newDownloader;
  }

  public DownloaderBuffered getDownloader() {
	  return downloader;
  }

  public void setMaxFileSize(long newMaxFileSize) throws CrawlerException {
	  try {
		  downloader.setMaxBufferSize((int)newMaxFileSize);
	  }
	  catch (DownloaderException de) {
		  logger.error("Max file size reached", de);
		  throw new CrawlerException("Max file size reached", de);
	  }
  }

  public long getMaxFileSize() throws CrawlerException {
	  try {
		  return downloader.getMaxBufferSize();
	  }
	  catch (DownloaderException de) {
		  throw new CrawlerException("Could not return maxFileSize", de );
	  }
  }

  public void setBuffer(byte[] buffer){
	  this.buffer = buffer;
  }

  public byte[] getBuffer(){
	  return buffer;
  }

  public Page getPage() {
	  return page;
  }

  public void setPage(Page page) {
	  this.page = page;
  }


  public URL getInitialURL() {
	  return initialUrl;
  }

  public boolean isShutdown() {
	  try {
		  return downloader.isShutdown();
	  }catch (DownloaderException exc) {
		  logger.warn("Problem while verifying if crawler is shutdown.", exc);
		  return false;
	  }
  }

  public String getHost(String prehost) {

	  if (prehost.startsWith("http://www")) {
		  prehost = prehost.substring(11, prehost.length());
	  }else if (prehost.startsWith("http://")) {
		  prehost = prehost.substring(7, prehost.length());
	  }
//       System.out.println("prehost : " + prehost);
	  String result = "";
	  int last, begin;
	  last = prehost.lastIndexOf(".");
	  String tmp = prehost.substring(0, last);
	  begin = tmp.lastIndexOf(".");

	  if (begin != -1) {
		  result = prehost.substring(begin + 1, prehost.length());
	  }else {
		  last = prehost.lastIndexOf("-");
		  if (last != -1) {
			  tmp = prehost.substring(0, last);
			  begin = tmp.lastIndexOf("-");
			  if (begin != -1) {
				  result = prehost.substring(begin, prehost.length());
			  }else {
				  result = prehost;
			  }
		  }else {
			  result = prehost;
		  }
       	}
	  return result;
  }

     /**
      * This method selects the next URL to be downloaded by the crawler 
      */
     protected void selectUrl() throws CrawlerException {

          try {
              long t1 = System.currentTimeMillis();
              setMessage("selectUrl() linkStorage.");
              
              LinkRelevance lr = ((LinkRelevance)linkStorage.select(null));
              
              initialUrl = lr.getURL();
              String host = lr.getURL().getHost();
              
              host = host.substring(0,host.indexOf("."));
              boolean number = false;
              
              try{
                  Integer.parseInt(host);
                  number = true;
              } catch(Exception ex) { }
              
              relevance = lr.getRelevance();
              
              t1 = System.currentTimeMillis()-t1;
              
              logger.info("Selected next URL to download (time: "+t1+"): "+initialUrl);
              
              if( initialUrl == null || number || lr.getURL().getHost().contains("fc2.com")) {
                  throw new CrawlerException(getName()+": LinkStorage sent null!");
              }
              
              setSelectedLinks(getSelectedLinks() +1);
              currentUrl = initialUrl;
              setUrl(currentUrl);
              
              setMessage("");
          }
          catch(DataNotFoundException dnfe) {
              throw new CrawlerException(getName()+":"+dnfe.getMessage(),dnfe.detail);
          }
          catch(StorageException se) {
              throw new CrawlerException(getName()+":"+se.getMessage(),se.detail);
          }
          catch(CommunicationException ce) {
              throw new CrawlerException(getName()+":"+ce.getMessage(),ce.detail);
          }
     }

     protected void checkUrl() throws CrawlerException {
    	 
     }

    /**
     * This method downloads the URL selected in the selectURL method.
     */
    protected void downloadUrl() throws CrawlerException {
        urlFinal = getUrl();
        urlDownloader = new Downloader(urlFinal);
        source = urlDownloader.getContent();
    }

        protected void handleNotFound() throws Exception {
          setJump(true,"Url(insert) '" + getUrl() + "' not found.");
        }

        protected void handleRedirect() throws Exception {
          logger.info(getUrl() + " redirected to " + urlFinal + ".");
        }

        protected void processData() throws CrawlerException {
            setMessage("URL "+getUrl());
			try {
			    
			    if(urlDownloader.isRedirection()) {
			        page = new Page(getUrl(), source, 
			                        urlDownloader.getResponseHeaders(),
			                        urlDownloader.getRedirectionUrl());
			    } else {
			        page = new Page(getUrl(), source, urlDownloader.getResponseHeaders());
			    }
			    
				PaginaURL pageParser = new PaginaURL(page.getURL(),page.getContent());
				page.setPageURL(pageParser);
				if(relevance > LinkRelevance.DEFAULT_HUB_RELEVANCE && relevance < LinkRelevance.DEFAULT_AUTH_RELEVANCE){
					page.setHub(true);
				}
//				page.setRelevance(relevance);
			} catch (Exception e) {
				logger.error("Problem while processing data.", e);
			}

            setMessage(null);
        }
        
        
        protected void checkData() throws CrawlerException {

        }

        /**
         * In this method, the crawler sends a downloaded page to the Form Storage.
         */
        protected void sendData() throws CrawlerException {

            try {

                logger.info("Sending page [ "+page.getURL()+" ] to TargetStorage.");

                targetStorage.insert(page);

//                linkStorage.insert(page);

            }
            catch( StorageException se ) {
                logger.error("Problem while sending page to storage.", se);
                throw new CrawlerException(getName()+":"+se.getMessage(),se);
            }
            catch(CommunicationException ce) {
                logger.error("Communication problem while sending page to storage.", ce);
                throw new CrawlerException(getName()+":"+ce.getMessage(),ce);
            }

        }

        /**
         * This cleans all the temporary variables. 
         */
        
        protected synchronized void cleanup() throws CrawlerException {
            setUrl(null);
            initialUrl = null;
            currentUrl = null;
            urlFinal = null;
            try {
                if( downloader != null ) {
                    downloader.close();
                }
            }
            catch( Exception exc ) {
                logger.error("Problem while closing downloader.", exc);
            }
            length = 0;
            //file.setLength(0);
            page = null;
            for(int i = getStatus(); i < STATES.length; i++) {
                setPartitionTime(i,0);
            }
        }

}

