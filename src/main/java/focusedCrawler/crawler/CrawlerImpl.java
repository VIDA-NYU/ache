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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.Html2Txt;
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

public class CrawlerImpl extends Crawler{

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
		  de.printStackTrace();
		  throw new CrawlerException("Max file size reached", de );
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
		  exc.printStackTrace();
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

          long t1 = 0;
          try {
              //getLog().writeLog("LOG>"+getName()+"selectUrl() linkServer start(0)");
              t1 = System.currentTimeMillis();
              setMessage("selectUrl() linkStorage.");
              LinkRelevance lr = ((LinkRelevance)linkStorage.select(null));
              initialUrl = lr.getURL();
              String host = lr.getURL().getHost();
              host = host.substring(0,host.indexOf("."));
              boolean number = false;
              System.out.println(host);
              try{Integer.parseInt(host); number = true;}catch(Exception ex){ }
              relevance = lr.getRelevance();
              t1 = System.currentTimeMillis()-t1;
              System.out.println("LOG>"+getName()+">selectUrl() linkServer end("+t1+"):"+initialUrl);
              if( initialUrl == null || number || lr.getURL().getHost().contains("fc2.com")) {
                  throw new CrawlerException(getName()+": LinkStorage sent null!");
              }
              setSelectedLinks(getSelectedLinks() +1);
              currentUrl = initialUrl;
              setUrl(currentUrl);
              t1 = System.currentTimeMillis();
              System.out.println("LOG>"+getName()+">update() linkStorage end("+t1+")");
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
          try {            
            urlFinal = getUrl();
            URLConnection conn = urlFinal.openConnection();
            InputStream in = conn.getInputStream();
            StringBuffer   buffer = new StringBuffer();
            BufferedReader bin = new BufferedReader(new InputStreamReader(in));
            String inputLine;

            try {
                while ((inputLine = bin.readLine()) != null) {
                	buffer.append(inputLine).append("\n");
                }
            } catch (IOException ioe) {
                bin.close();

                throw ioe;
            }

            bin.close();
            source = buffer.toString();

//            downloader.clearResponseProperties();
//            downloader.clearBuffer();
//            downloader.setUrlTarget(getUrl());
//            downloader.connect();
//            int responseCode = HttpURLConnection.HTTP_OK;
//            try {
//              responseCode = downloader.getResponseCode();
//            }
//            catch (Exception exc) {
//              System.out.println("R>" + getName() + ">" + exc.getMessage());
//            }
//            String mimeType = "text/html";
//            try {
//              mimeType = downloader.getContentType();
//            }
//            catch (Exception exc) {
//              System.out.println("R>" + getName() + ">" + exc.getMessage());
//            }
//            urlFinal = downloader.getUrlTarget();
//            System.out.println("R>" + getName() + ">" + initialUrl + " -> " +
//                              responseCode + "," + mimeType + " -> " + urlFinal);
//            if (responseCode != HttpURLConnection.HTTP_OK &&
//                     responseCode != HttpURLConnection.HTTP_PARTIAL) {
//              handleNotFound();
//            }
//            if (!isJump()) {
//              setUrl(urlFinal);
//              mimeType = mimeType.toLowerCase();
//              if (! (mimeType.startsWith("text/html") ||
//                     mimeType.startsWith("text/plain"))) {
//                throw new CrawlerException(getName() + ":" + "MIME_TYPE " + mimeType);
//              }
//              else {
//                downloader.getInputStream();
//                buffer = downloader.getBuffer();
//                bufferSize = downloader.getBufferSize();
//                try {
//                  length = downloader.getBufferSize();
//                }
//                catch (DownloaderException de) {
//                  length = 0;
//                }
//              }
//            }
          }
          catch (MalformedURLException exc) {
            throw new CrawlerException(getName() + ":" + exc.getMessage(), exc);
          }
          catch (SocketException exc) {
            throw new CrawlerException(getName() + ":" + exc.getMessage(), exc);
          }
          catch (IOException exc) {
            throw new CrawlerException(getName() + ":" + exc.getMessage(), exc);
          }
//          catch (StorageException exc) {
//            throw new CrawlerException(getName() + ":" + exc.getMessage(), exc);
//          }
//          catch (CommunicationException exc) {
//            throw new CrawlerException(getName() + ":" + exc.getMessage(), exc);
//          }
          catch (Exception exc) {
            throw new CrawlerException(getName() + ":" + exc.getMessage(), exc);
          }
        }



        protected void handleNotFound() throws Exception {
          setJump(true,"R>" + getName() + "> Url(insert) '" + getUrl() + "' not found.");
        }

        protected void handleRedirect() throws Exception {
          System.out.println("R>" + getName() + ">" + getUrl() + " redirected to " +
                             urlFinal + ".");
        }

        protected void processData() throws CrawlerException {
            setMessage("URL "+getUrl());
			try {
				page = new Page(getUrl(),source);
				PaginaURL pageParser = new PaginaURL(page.getURL(), 0, 0,page.getContent().length(),page.getContent(), null);
				page.setPageURL(pageParser);
				if(relevance > LinkRelevance.DEFAULT_HUB_RELEVANCE && relevance < LinkRelevance.DEFAULT_AUTH_RELEVANCE){
					page.setHub(true);
				}
//				page.setRelevance(relevance);
			}catch (Exception e) {
				e.printStackTrace();
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

                System.out.println("R>"+getName()+" send page with "+page.getURL()+" to TargetStorage .");

                targetStorage.insert(page);

//                linkStorage.insert(page);

            }

            catch( StorageException se ) {

                se.printStackTrace();

                throw new CrawlerException(getName()+":"+se.getMessage(),se);

            }

            catch(CommunicationException ce) {
                ce.printStackTrace();
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
                exc.printStackTrace();
            }
            length = 0;
            //file.setLength(0);
            page = null;
            for(int i = getStatus(); i < STATES.length; i++) {
                setPartitionTime(i,0);
            }
        }

}

