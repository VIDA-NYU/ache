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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageFactoryException;
import focusedCrawler.util.storage.distribution.StorageCreator;


/**
 * <p>Description: This class manages the crawlers </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class CrawlerManager extends Thread {
	
	public static final Logger logger = LoggerFactory.getLogger(CrawlerManager.class);

    private CrawlerManagerConfig config;

    private Storage linkStorage;

    private Storage formStorage;

    private boolean stop;

    private ThreadGroup crawlerThreadGroup;

    private Crawler[] crawlers;

    private int[] life;

    
    public CrawlerManager(CrawlerManagerConfig config,
                          Storage linkStorage,
                          Storage formStorage) throws CrawlerManagerException {

        this.config = config;
        this.linkStorage = linkStorage;
        this.formStorage = formStorage;
        
        this.crawlerThreadGroup = new ThreadGroup(config.getRobotThreadGroup());
        this.crawlers = new Crawler[config.getRobotQuantity()];
        this.life = new int[crawlers.length];

        for (int i = 0; i < this.crawlers.length; i++) {
            this.crawlers[i] = createCrawler(this.crawlerThreadGroup, i, this.life[0]);
        }
        
    }

    /**
     * This method monitors the crawlers' behavior
     */
    public void run() {

        setPriority( getPriority() + 1 );

        //Start the Robots

        for( int i = 0; i < crawlers.length; i++ ) {

            crawlers[i].start();

        }


        stop = false;

        while(!stop) {

            boolean isShutdown = false;

            for ( int i = 0 ; i < crawlers.length ; i++ ) {

                try {

                    Crawler crawler = crawlers[i];
                    
                    logger.info("RM>"+crawlerThreadGroup.getName()+">"+crawler.getName()+">Time("+crawler.getCicleTime()+"):"+statusCrawlerString(crawler)+(crawler.getMessage()==null?"":":"+crawler.getMessage()));

                    if(crawler.getCicleTime() > config.getRobotManagerMaxTime() ) {

                        stopCrawler(i);

                        life[i]++;

                        crawlers[i] = createCrawler(crawlerThreadGroup, i, life[i]);
                        crawlers[i].start();
                    }
                }
                
                catch(CrawlerManagerException rme) {
                    logger.error("Problem:"+rme.getMessage(), rme);
                }
                catch(Exception e) {
                    logger.error("Problem:"+e.getMessage(), e);
                }

                int numThreadsEst = crawlerThreadGroup.activeCount();

                Thread[] threads = new Thread[numThreadsEst * 2];

                int numThreads = crawlerThreadGroup.enumerate(threads, false);

                int maxThreads = 2 * config.getRobotManagerRobotThreadFactor() * crawlers.length;
                
                if (numThreads > maxThreads) {

                    logger.warn("Threads limit " + maxThreads + " exceeded '" + numThreads + "'");
                    
//                    stopManager();
                    
                    sleepExit(1 * 60 * 1000);
                    
                } else if ( isShutdown ) {

                  logger.info("Shutdown fired.");

                  stopManager();

                  sleepExit(1 * 60 * 1000);

                }
            }

            try {
                sleep(config.getRobotManagerSleepCheckTime());
            }
            catch(InterruptedException ie) {
                logger.error("Sleeping for "+config.getRobotManagerSleepCheckTime()+" interrupted.", ie);
            }

        }

        logger.info("RM>"+getName()+">Stop requested. Giving a max of 60s to stop all robots before halt.");

        stopManagerExit(1 * 60 * 1000);

    }

    private void sleepExit(long time) {
        try {
            logger.info("Waiting " + time + " mls to die.");
            sleep(time);
            logger.info("System.exit()");
//            System.exit(1);
        } catch (InterruptedException exc) {
            logger.error("Interruped while waiting mls to exit.", exc);
        }
    }

    public Crawler createCrawler(ThreadGroup tg, int index, int life) throws CrawlerManagerException {
        try {
            String name = tg.getName() + "_" + index + "_" + life;
            
            CrawlerImpl crawler = new CrawlerImpl(tg, name, linkStorage, formStorage);
            crawler.setRestingTime(config.getRobotManagerRestingTime());
            crawler.setSleepTime(config.getRobotManagerRobotErrorTime());
            crawler.setPriority(Thread.NORM_PRIORITY);
            
            return crawler;
        } catch (Exception exc) {
            throw new CrawlerManagerException(exc.getMessage(), exc);
        }
    }

    /**
     * This method stops the crawler n
     */
    public void stopCrawler(int n) {
        Crawler r = crawlers[n];
        logger.info("Killing crawler" + r + " : " + statusCrawlerString(r));
        r.setStop(true);
    }

    /**
     * Stop all crawlers
     */
    public void stopAllCrawlers() {
        for(int i = 0; i < crawlers.length; i++) {
            crawlers[i].setStop(true);
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

            for (int i = 0; i < crawlers.length; i++) {

                Crawler crawler = crawlers[i];

                logger.info("RM> Waiting "+crawler.getName()+". Max join time is "+MAX_JOIN);

                crawler.join(MAX_JOIN);

                logger.info("RM>"+crawler.getName()+" joined.");

            }

            for (int i = 0; i < crawlers.length; i++) {

                Crawler crawler = crawlers[i];

                logger.info("RM>"+crawler.getName()+">Time("+crawler.getCicleTime()+"):"+statusCrawlerString(crawler));

            }

        }
        catch (InterruptedException ex) {
            logger.error("Error while stoping manager. ", ex);
        }

        System.exit(0);

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

    public static CrawlerManager createCrawlerManager(String crawlerConfigFile,
                                                      Storage linkStorage,
                                                      Storage formStorage)
                                                      throws CrawlerManagerException {
        
        return new CrawlerManager(new CrawlerManagerConfig(crawlerConfigFile),
                                  linkStorage,
                                  formStorage);
    }

    public static void main(String[] args) throws IOException, NumberFormatException {

        logger.info("Starting CrawlerManager...");

        String crawlerConfigFile = args[0] + "/crawler/crawler.cfg";
        String linkConfigFile = args[0] + "/link_storage/link_storage.cfg";
        String formConfigFile = args[0] + "/target_storage/target_storage.cfg";

        try {

            ParameterFile configLinkStorage = new ParameterFile(linkConfigFile);
            Storage linkStorage = new StorageCreator(configLinkStorage).produce();

            ParameterFile configFormStorage = new ParameterFile(formConfigFile);
            Storage formStorage = new StorageCreator(configFormStorage).produce();

            CrawlerManager manager = createCrawlerManager(crawlerConfigFile, linkStorage, formStorage);

            manager.start();

        } catch (CrawlerManagerException ex) {
            logger.error("An error occurred while starting CrawlerManager. ", ex);
        } catch (StorageFactoryException ex) {
            logger.error("An error occurred while starting CrawlerManager. ", ex);
        }
    }
    
}

