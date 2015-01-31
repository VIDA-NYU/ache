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
package focusedCrawler.target;


import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.StorageBinderException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.distribution.StorageBinder;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.StorageFactoryException;
import focusedCrawler.util.storage.distribution.StorageCreator;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;

import focusedCrawler.util.BowClient;
import focusedCrawler.util.Page;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.string.StopList;



/**
 * <p> </p>
 *
 * <p>Description: </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class TargetPerplexityStorage extends StorageDefault {

  private TargetClassifier targetClassifier;

  private String fileLocation;

  private TargetRepository targetRepository;

  private int totalOfPages;

  private int totalOfTargets;

  private int totalOnTopicPages;

  private int limitOfPages;

  private Storage linkStorage;
  
  private StringBuffer urls = new StringBuffer();
  
  public TargetPerplexityStorage(TargetClassifier targetClassifier, String fileLocation, TargetRepository targetRepository, Storage linkStorage) {
    this.targetClassifier = targetClassifier;
    this.fileLocation = fileLocation;
    this.targetRepository = targetRepository;
    this.linkStorage = linkStorage;
  }

  public synchronized Object insert(Object obj) throws StorageException {
      
      Page page = (Page)obj;
      System.out.println(">>>INSERTING: " + page.getIdentifier());
//      System.out.println(">>>INSERTING: " + page.getContent());
      urls.append(fileLocation + "/" + page.getURL().getHost()+"/" +URLEncoder.encode(page.getIdentifier()));
      urls.append("\n");
      totalOfPages++;
      try {
    	  linkStorage.insert(page);
//    	  page.setContent(page.getCleanContent());
    	  targetRepository.insert(page);
          totalOnTopicPages++;
          System.out.println(getClass() + "TOTAL_PAGES=" + totalOfPages
                           + ": PAGE:" + page.getURL() + " RELEVANT:" +
                           totalOfTargets );
          System.out.println("---------------------------");
//          executePerpScript();
          if(totalOfPages > limitOfPages){
        	  System.exit(0);
          }

      }
      catch (CommunicationException ex) {
        ex.printStackTrace();
        throw new StorageException(ex.getMessage());
      } 
//      catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
      return null;
    }

  	private void executePerpScript() throws IOException{
  		if(totalOfPages % 500 == 0){
  			String fileName = "/home/lbarbosa/perp_crawler/focused_crawler/perp_input/urls_" + totalOfPages;
  			FileOutputStream fout = new FileOutputStream(fileName);
  	    	DataOutputStream dout = new DataOutputStream(fout);
  	    	dout.writeBytes(urls.toString());
  	    	dout.close();
  	    	System.out.println(">>FILE+" + fileName);
  	    	Runtime.getRuntime().exec("sh /home/lbarbosa/perp_crawler/focused_crawler/script/runPerplexity.sh urls_" + totalOfPages);
  	    	urls = new StringBuffer();
  		}
  	}
  
  
    public void setLimitPages(int limit){
      limitOfPages = limit;
    }

    public static void main(String[] args) {

      try{
        ParameterFile config = new ParameterFile(args[0]);
        StopList stoplist = new StopListArquivo(config.getParam("STOPLIST_FILES"));
        String targetDirectory = config.getParam("TARGET_STORAGE_DIRECTORY");
        TargetRepository targetRepository = new TargetFileRepository(targetDirectory);
        ParameterFile linkStorageConfig = new ParameterFile(config.getParam(
            "LINK_STORAGE_FILE"));
        Storage linkStorage = new StorageCreator(linkStorageConfig).produce();
        BowClient bow;
        if (config.getParamBoolean("USE_BOW")) {
          String bowServer = config.getParam("BOW_HOST");
          int bowPort = config.getParamInt("BOW_PORT");
          bow = new BowClient(bowServer, bowPort);
        }
        else {
          bow = null;
        }
        Storage targetStorage = new TargetPerplexityStorage(null,targetDirectory,targetRepository,linkStorage);
        ((TargetPerplexityStorage) targetStorage).setLimitPages(config.getParamInt("VISITED_PAGE_LIMIT"));
        StorageBinder binder = new StorageBinder(config);
        binder.bind(targetStorage);
      }
      catch (java.io.IOException ex) {
        ex.printStackTrace();
      }
//      catch (ClassNotFoundException ex) {
//        ex.printStackTrace();
//      }
      catch (StorageBinderException ex) {
        ex.printStackTrace();
      }
      catch (StorageFactoryException ex) {
        ex.printStackTrace();
      }
    }

  }
