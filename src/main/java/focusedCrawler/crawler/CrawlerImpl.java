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

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.Page;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;

/**
 * This class implements a crawler
 * @author lbarbosa
 *
 */

public class CrawlerImpl extends Crawler {
    
    private static final Logger logger = LoggerFactory.getLogger(CrawlerImpl.class);

    private Storage linkStorage;

    private Storage targetStorage;

    protected URL initialUrl;

    protected URL currentUrl;

    protected URL urlFinal;

    protected static long DATE_UNKNOWN = -1;

    protected int length;

    protected double relevance;
    
    protected Page page;

    protected int bufferSize;

    protected String source;
    
    protected Downloader urlDownloader;
    
    public CrawlerImpl(ThreadGroup tg, String name, Storage linkStorage, Storage targetStorage) {
    	super(tg,name);
    	this.linkStorage = linkStorage;
    	this.targetStorage = targetStorage;
        
    }

     /**
      * This method selects the next URL to be downloaded by the crawler 
      */
     @Override
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

    @Override
    protected void checkUrl() throws CrawlerException {

    }

    /**
     * This method downloads the URL selected in the selectURL method.
     */
    @Override
    protected void downloadUrl() throws CrawlerException {
        urlFinal = getUrl();
        urlDownloader = new Downloader(urlFinal);
        source = urlDownloader.getContent();
    }

    protected void handleNotFound() throws Exception {
        setJump(true, "Url(insert) '" + getUrl() + "' not found.");
    }

    protected void handleRedirect() throws Exception {
        logger.info(getUrl() + " redirected to " + urlFinal + ".");
    }
    
        @Override
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
			    page.setFetchTime(System.currentTimeMillis());
			    
				PaginaURL pageParser = new PaginaURL(page.getURL(),page.getContent());
				page.setPageURL(pageParser);
				if(relevance > LinkRelevance.DEFAULT_HUB_RELEVANCE && relevance < LinkRelevance.DEFAULT_AUTH_RELEVANCE){
					page.setHub(true);
				}
				page.setRelevance(relevance);
			} catch (Exception e) {
				logger.error("Problem while processing data.", e);
			}

            setMessage(null);
        }
        
        @Override
        protected void checkData() throws CrawlerException {

        }

        /**
         * In this method, the crawler sends a downloaded page to the Form Storage.
         */
        @Override
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
        @Override
        protected synchronized void cleanup() throws CrawlerException {
            setUrl(null);
            initialUrl = null;
            currentUrl = null;
            urlFinal = null;
            length = 0;
            //file.setLength(0);
            page = null;
            for(int i = getStatus(); i < STATES.length; i++) {
                setPartitionTime(i,0);
            }
        }

}

