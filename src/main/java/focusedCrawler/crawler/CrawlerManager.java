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

import focusedCrawler.util.download.Downloader;
import focusedCrawler.util.download.DownloaderBuffered;
import focusedCrawler.util.download.DownloaderURL;
import focusedCrawler.util.download.ExtractorProxyDownloader;
import focusedCrawler.util.download.DownloaderException;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.distribution.StorageCreator;
import focusedCrawler.util.storage.*;


import java.io.*;


/**
 * <p>Description: This class manages the crawlers </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class CrawlerManager extends Thread{

    private ParameterFile configManager;

    private ParameterFile configLinkStorage;

    private Storage linkStorage;

    private ParameterFile configFormStorage;

    private Storage formStorage;

    private boolean stop;

    private ThreadGroup crawlerThreadGroup;

    private Crawler[] crawlers;

    private int[] life;

    private long restingTime;

    private long sleepCheckTime;

    private long maxCrawlerLifeTime;

    private long sleepErrorTime;

    private int permitedThreadsFactor;
    
    public CrawlerManager(ParameterFile configManager,
                                Storage linkStorage, ParameterFile configLinkStorage,
                                Storage formStorage, ParameterFile configFormStorage,
                                ThreadGroup crawlerThreadGroup, int crawlersNumber,
                                long sleepCheckTime,long maxCrawlerLifeTime) throws CrawlerManagerException {

        setConfigManager(configManager);
        setLinkStorage(linkStorage);
        setConfigLinkStorage(configLinkStorage);
        setFormStorage(formStorage);
        setConfigFormStorage(configFormStorage);
        setCrawlerThreadGroup(crawlerThreadGroup);
        setCrawlers(new Crawler[crawlersNumber]);
        setSleepCheckTime(sleepCheckTime);
        setMaxCrawlerLifeTime(maxCrawlerLifeTime);
   }

    
    
   public void createCrawlers() throws CrawlerManagerException {
        for(int i = 0; i < crawlers.length; i++) {
            crawlers[i] = createCrawler(crawlerThreadGroup,i,life[0]);
            crawlers[i].setRestingTime(getRestingTime());
        }
    }

    public void setConfigManager(ParameterFile newConfigManager) {

        configManager = newConfigManager;

    }

    public ParameterFile getConfigManager() {

        return configManager;

    }

    public void setFormStorage(Storage newFormStorage) {
        formStorage = newFormStorage;
    }



    public Storage getFormStorage() {
        return formStorage;
    }

    public void setConfigLinkStorage(ParameterFile newConfigLinkStorage) {

        configLinkStorage = newConfigLinkStorage;

    }

    public ParameterFile getConfigLinkStorage() {

        return configLinkStorage;

    }



    public void setLinkStorage(Storage newLinkStorage) {

        linkStorage = newLinkStorage;

    }

    public Storage getLinkStorage() {

        return linkStorage;

    }

    public void setConfigFormStorage(ParameterFile newConfigFormStorage) {

        configFormStorage = newConfigFormStorage;

    }

    public ParameterFile getConfigFormStorage() {

        return configFormStorage;

    }



    public void setCrawlerThreadGroup(ThreadGroup newCrawlerThreadGroup) {

        crawlerThreadGroup = newCrawlerThreadGroup;

    }

    public ThreadGroup getCrawlerThreadGroup() {

        return crawlerThreadGroup;

    }

    public void setCrawlers(Crawler[] newCrawlers) {

        crawlers = newCrawlers;

        setLife(new int[crawlers.length]);

    }

    public Crawler[] getCrawlers() {

        return crawlers;

    }

    public void setLife(int[] newLife) {

        life = newLife;

    }

    public int[] getLife() {

        return life;

    }



    public void setRestingTime(long newRestingTime) {

        restingTime = newRestingTime;

    }

    public long getRestingTime() {

        return restingTime;

    }

    public void setSleepCheckTime(long newSleepCheckTime) {

        sleepCheckTime = newSleepCheckTime;

    }

    public long getSleepCheckTime() {

        return sleepCheckTime;

    }

    public void setMaxCrawlerLifeTime(long newMaxCrawlerLifeTime) {

        maxCrawlerLifeTime = newMaxCrawlerLifeTime;

    }

    public long getMaxCrawlerLifeTime() {

        return maxCrawlerLifeTime;

    }

    public void setSleepErrorTime(long newSleepErrorTime) {

        sleepErrorTime = newSleepErrorTime;

        for(int i = 0; i < crawlersNumber(); i++) {

            getCrawler(i).setSleepTime(sleepErrorTime);

        }

    }

    public long getSleepErrorTime() {

        return sleepErrorTime;

    }

    public void setFatorPermitedThreads(int newpermitedThreadsFactor) {

        permitedThreadsFactor = newpermitedThreadsFactor;

    }

    public int getPermitedThreadsFactor() {

        return permitedThreadsFactor;

    }



    /**

     * This method monitors the crawlers' behavior

     */



    public void run() {

        setPriority( getPriority() + 1 );

        //Start the Robots

        for( int i = 0; i < crawlersNumber(); i++ ) {

            getCrawler(i).start();

        }

        Crawler crawler;

        stop = false;

        while(!stop) {

            boolean isShutdown = false;

            for ( int i = 0 ; i < crawlersNumber() ; i++ ) {

                try {

                    crawler = getCrawler(i);

                    if( crawler.isShutdown() ) {

                        isShutdown = true;

                        System.out.println("RM>"+crawlerThreadGroup.getName()+">"+crawler.getName()+">In shutdown mode.");

                    }

                    System.out.println("RM>"+crawlerThreadGroup.getName()+">"+crawler.getName()+">Time("+crawler.getCicleTime()+"):"+statusCrawlerString(crawler)+(crawler.getMessage()==null?"":":"+crawler.getMessage()));

                    if(crawler.getCicleTime() > maxCrawlerLifeTime ) {

                        stopCrawler(i);

                        life[i]++;

                        crawlers[i] = createCrawler(crawlerThreadGroup,i,life[i]);

                        crawlers[i].setPriority(Thread.NORM_PRIORITY);

                        crawlers[i].setSleepTime(sleepErrorTime);

                        crawlers[i].start();

                    }

                }

                catch(CrawlerManagerException rme) {

                    System.out.println("Problem:"+rme.getMessage());

                    rme.printStackTrace();

                }

                catch(Exception e) {

                    System.out.println("Problem:"+e.getMessage());

                    e.printStackTrace();

                }

                int numThreadsEst = crawlerThreadGroup.activeCount();

                Thread[] threads = new Thread[numThreadsEst * 2];

                int numThreads = crawlerThreadGroup.enumerate(threads, false);

                if( numThreads > (permitedThreadsFactor * crawlersNumber()) ) {

                  System.out.println("Threads limit "+(permitedThreadsFactor * crawlersNumber())+" exceeded '"+numThreads+"'");

                  stopManager();

                  sleepExit(1 * 60 * 1000);

                }

                else if ( isShutdown ) {

                  System.out.println("Shutdown fired.");

                  stopManager();

                  sleepExit(1 * 60 * 1000);

                }

            }



            try {

                sleep(sleepCheckTime);

            }

            catch(InterruptedException ie) {

                ie.printStackTrace();

            }

        }

        System.out.println("RM>"+getName()+">Stop requested. Giving a max of 60s to stop all robots before halt.");

        stopManagerExit(1 * 60 * 1000);

    }



    private void sleepExit(long time) {
        try {
          System.out.println("Waiting "+time+" mls to die.");
          sleep(time);
          System.out.println("System.exit()");
          System.exit(1);
        }
        catch( InterruptedException exc ) {
            exc.printStackTrace();
        }
    }


    public Crawler createCrawler(ThreadGroup tg, int index, int life) throws CrawlerManagerException{
      try {
           DownloaderBuffered downloader = createDownloader(tg.getName()+"_"+index+"_"+life );
           CrawlerImpl crawler = new CrawlerImpl( tg, tg.getName()+"_"+index+"_"+life,
                                                getLinkStorage(), getFormStorage());
//           PC_LangDetector ld = createPCDetector(pcConfig);
//           crawler.setPC_Lang(ld);
           crawler.setDownloader(downloader);
           return crawler;
       }
       catch(Exception exc) {
           throw new CrawlerManagerException(exc.getMessage(),exc);
       }
    }



    public int crawlersNumber() {
        return crawlers.length;
    }



  /**

   * This method gets the crawler by the index

   *

   * @param index int

   * @return Crawler

   */

  public Crawler getCrawler(int index) {

        return crawlers[index];

    }



  /**

   * This method stops the crawler n

   *

   * @param n int

   */

  public void stopCrawler(int n) {

        Crawler r = getCrawler(n);

        System.out.println("Killing crawler"+r+" : "+statusCrawlerString(r));

        r.setStop(true);

    }



    /**

     * Stop all crawlers

     */

    public void stopAllCrawlers() {

        for(int i = 0; i < crawlersNumber(); i++) {

            getCrawler(i).setStop(true);

        }

    }



    /**

     * This method stops all the robots and the manager as well.

     */

    public void stopManager() {

        stopAllCrawlers();

        stop = true;

    }



    public void stopManagerExit(long time) {

        stopManager();

        try {

            long MAX_JOIN = 5 * 60 * 1000; //5minutos cada robot.

            for (int i = 0; i < crawlersNumber(); i++) {

                Crawler crawler = getCrawler(i);

                System.out.println("RM> Waiting "+crawler.getName()+". Max join time is "+MAX_JOIN);

                crawler.join(MAX_JOIN);

                System.out.println("RM>"+crawler.getName()+" joined.");

            }

            for (int i = 0; i < crawlersNumber(); i++) {

                Crawler crawler = getCrawler(i);

                System.out.println("RM>"+crawler.getName()+">Time("+crawler.getCicleTime()+"):"+statusCrawlerString(crawler));

            }

        }

        catch (InterruptedException ex) {

            ex.printStackTrace();

        }

        System.exit(0);

    }



    public int statusCrawler(int number) {

        return getCrawler(number).getStatus();

    }



    public String statusCrawlerString(Crawler crawler) {

        String result = null;

        switch( crawler.getStatus() ) {

            case Crawler.INIT :

                result = "At the begin of the main loop.";

                break;

            case Crawler.SELECT_URL :

                result = "Asking for a new url to work on.";

                break;

            case Crawler.DOWNLOAD_URL :

                result = "Downloading "+crawler.getUrl()+".";

                break;

            case Crawler.PROCESS_DATA :

                result = "Processing data retrieved. URL:"+crawler.getUrl();

                break;

            case Crawler.CHECK_DATA :

                result = "Checking the processed data. URL:"+crawler.getUrl();

                break;

            case Crawler.SEND_DATA :

                result = "Sending data of '"+crawler.getUrl()+"' to the page storage.";

                break;

            case Crawler.END :

                result = "At the end of the main loop";

                break;

            case Crawler.SLEEPING :

                result = "Sleeping as a consequence of this problem: '"+crawler.getLastException().getMessage()+"'";

                break;

            case Crawler.DEAD :

                result = "Dead.";

                break;

            default :

                result = "Unknown.";

        }

        return result;

    }



    public long cicleTimeCrawler(int number) {
        return getCrawler(number).getCicleTime();
    }

    public URL getUrlCrawler(int number) {
        return getCrawler(number).getUrl();
    }



    protected DownloaderBuffered createDownloader(String id) throws
        DownloaderException {

      DownloaderBuffered downloader;
      System.out.print("Create Downloader Start,  ");
      Downloader inter_downloader = new DownloaderURL(getConfigManager());
      downloader = new ExtractorProxyDownloader(inter_downloader);
      System.out.println("Tipo do Downloader : "+ downloader.getClass().getName());
      downloader.setId(id);
      return downloader;
     }

     public static void main(String[] args) throws IOException,
         NumberFormatException {

       String crawlerConfigFile = args[0] + "/crawler/crawler.cfg";
       String linkConfigFile = args[0] + "/link_storage/link_storage.cfg";
       String formConfigFile = args[0] + "/target_storage/target_storage.cfg";
       ParameterFile config = new ParameterFile(crawlerConfigFile);
       ThreadGroup tg = new ThreadGroup(config.getParam("ROBOT_THREAD_GROUP"));
       int numberRobots = Integer.valueOf(config.getParam("ROBOT_QUANTITY")).intValue();
       long restingTime = 0;
       try {
           restingTime = Long.valueOf(config.getParam("ROBOT_MANAGER_RESTINGTIME")).longValue();
       }
       catch(NumberFormatException nfe) {
           System.out.println("Resting time not found. RestingTime bound to '0'");
       }

       try{
         long sleepCheckTime = Long.valueOf(config.getParam(
             "ROBOT_MANAGER_CHECKTIME")).longValue();
         long maxRobotLifeTime = Long.valueOf(config.getParam(
             "ROBOT_MANAGER_MAXTIME")).longValue();
         ParameterFile configLinkStorage = new ParameterFile(linkConfigFile);
         Storage linkStorage = null;
         linkStorage = new StorageCreator(configLinkStorage).produce();
         ParameterFile configFormStorage = new ParameterFile(formConfigFile);
         Storage formStorage = new StorageCreator(configFormStorage).produce();
         CrawlerManager manager = new CrawlerManager(config, linkStorage,
                                                     configLinkStorage,
                                                     formStorage,
                                                     configFormStorage,
                                                     tg, numberRobots,
                                                     sleepCheckTime,
                                                     maxRobotLifeTime);
         manager.setRestingTime(restingTime);
         manager.createCrawlers();
         long sleepErrorTimeCheck = Long.valueOf(config.getParam("ROBOT_MANAGER_ROBOT_ERROR_SLEEP_TIME")).longValue();
         manager.setSleepErrorTime(sleepErrorTimeCheck);
         int fatorPermitedThreads = Long.valueOf(config.getParam("ROBOT_MANAGER_ROBOT_THREAD_FACTOR")).intValue();
         manager.setFatorPermitedThreads(fatorPermitedThreads);
         manager.start();
     }catch (CrawlerManagerException ex) {
        ex.printStackTrace();
      }
      catch (StorageFactoryException ex) {
    	  ex.printStackTrace();  
      } 
     }
   }

