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

//crawler
import java.net.URL;

/**
 * <p>Description: This abstract class implements the partial behavior
 * of a webcrawler</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */


public abstract class Crawler extends Thread {

  //possible states
  public static final int INIT         = 0;
  public static final int SELECT_URL   = INIT + 1;
  public static final int CHECK_URL    = SELECT_URL + 1;
  public static final int DOWNLOAD_URL = CHECK_URL + 1;
  public static final int PROCESS_DATA = DOWNLOAD_URL + 1;
  public static final int CHECK_DATA   = PROCESS_DATA + 1;
  public static final int SEND_DATA    = CHECK_DATA + 1;
  public static final int END          = SEND_DATA + 1;
  public static final int SLEEPING     = END + 1;
  public static final int[] STATES = new int[]{INIT,
                                         SELECT_URL,CHECK_URL,DOWNLOAD_URL,
                                         PROCESS_DATA,CHECK_DATA,SEND_DATA,
                                         END,SLEEPING};
  public static final int DEAD         = SLEEPING + 1;

  private int status;

  private boolean stop;

  private long restingTime;

  private long sleepTime;

  private boolean jump;

  private long startCicleTime;

  private URL url;

  private long cicleTime;

  private long totalCicleTime;

  private long[] partitionTime = new long[STATES.length];

  private String message;

  private int selectedLinks = 0;

  private boolean shutdown;

  private CrawlerException lastException;

  public Crawler() {
  }


  public Crawler(String name) {
         super(name);
         defaults();
     }
     public Crawler(ThreadGroup g, String name) {
         super(g,name);
         defaults();
     }
     protected void defaults() {
         setShutdown(false);
         setStop(false);
         setRestingTime(0);
         setSleepTime(0);
         setUrl(null);
         setJump(false);
         setStartCicleTime(System.currentTimeMillis());
     }

     public void setStatus(int newStatus) {
         status = newStatus;
     }
     public int getStatus() {
         return status;
     }
     public void setStop(boolean newStop) {
         stop = newStop;
     }
     public boolean isStop() {
         return stop;
     }
     public void setRestingTime(long newRestingTime) {
         restingTime = newRestingTime;
     }
     public long getRestingTime() {
         return restingTime;
     }
     public void setSleepTime(long newSleepTime) {
         sleepTime = newSleepTime;
     }
     public long getSleepTime() {
         return sleepTime;
     }
     public void setJump(boolean newJump) {
         jump = newJump;
     }
     public boolean isJump() {
         return jump;
     }
     public void setJump(boolean newJump,String message) {
         System.out.println(message);
         setJump(newJump);
     }

     public void setStartCicleTime(long newStartCicleTime) {
         startCicleTime = newStartCicleTime;
     }
     public long getStartCicleTime() {
         return startCicleTime;
     }
     public void setUrl(URL newUrl) {
         url = newUrl;
     }
     public URL getUrl() {
         return url;
     }
     public void setCicleTime(long newCicleTime) {
         cicleTime = newCicleTime;
     }
     public long getCicleTime() {
         return System.currentTimeMillis()-getStartCicleTime();
     }
     public void setTotalCicleTime(long newTotalCicleTime) {
         totalCicleTime = newTotalCicleTime;
     }
     public long getTotalCicleTime() {
         return totalCicleTime;
     }
     public void setPartitionTime(int index, long time) {
         partitionTime[index] = time;
     }
     public long getPartitionTime(int index) {
         return partitionTime[index];
     }
     public void setMessage(String newMessage) {
         message = newMessage;
     }
     public String getMessage() {
         return message;
     }

     /** Getter for property selectedLinks.
      * @return Value of property selectedLinks.
      */
     public int getSelectedLinks() {
         return selectedLinks;
     }

     /** Setter for property selectedLinks.
      * @param selectedLinks New value of property selectedLinks.
      */
     public void setSelectedLinks(int selectedLinks) {
         this.selectedLinks = selectedLinks;
     }

     public void setShutdown(boolean newShutdown) {
         shutdown = newShutdown;
     }
     public boolean isShutdown() {
         return shutdown;
     }

     public void setLastException(CrawlerException newLastException) {
       lastException = newLastException;
     }

     public CrawlerException getLastException() {
       return lastException;
     }
     /**
      * This method implements the main loop of the crawler, where the crawler
      * accomplishes all the steps needed to retrieve Web pages.
      */

     public void run() {
         long time = System.currentTimeMillis();
         while(!stop) {
             setStartCicleTime(System.currentTimeMillis());
             try {
                 setStatus(INIT);
                 setPartitionTime(INIT,System.currentTimeMillis()-time);
                 if( jump ) {
                     setJump(false);
                     cleanup();
                     continue;
                 }

                 setStatus(SELECT_URL);
                 time = System.currentTimeMillis();
                 selectUrl();
                 setPartitionTime(SELECT_URL,System.currentTimeMillis()-time);
                 if( jump ) {
                     setJump(false);
                     cleanup();
                     continue;
                 }
                  System.out.println(getName()+">after request url");

                 setStatus(CHECK_URL);
                 time = System.currentTimeMillis();
                 checkUrl();
                 setPartitionTime(CHECK_URL,System.currentTimeMillis()-time);
                 if( jump ) {
                     setJump(false);
                     cleanup();
                     continue;
                 }
                 System.out.println(getName()+">after check url");

                 setStatus(DOWNLOAD_URL);
                 time = System.currentTimeMillis();
                 downloadUrl();
                 setPartitionTime(DOWNLOAD_URL,System.currentTimeMillis()-time);
                 if( jump ) {
                     setJump(false);
                     cleanup();
                     continue;
                 }
                 System.out.println(getName()+">after download data");

                 setStatus(PROCESS_DATA);
                 time = System.currentTimeMillis();
                 processData();
                 setPartitionTime(PROCESS_DATA,System.currentTimeMillis()-time);
                 if( jump ) {
                     setJump(false);
                     cleanup();
                     continue;
                 }
                 System.out.println(getName()+">after process data");

                 setStatus(CHECK_DATA);
                 time = System.currentTimeMillis();
                 checkData();
                 setPartitionTime(CHECK_DATA,System.currentTimeMillis()-time);
                 if( jump ) {
                     setJump(false);
                     cleanup();
                     continue;
                 }
                 System.out.println(getName()+">after check data");

                 setStatus(SEND_DATA);
                 time = System.currentTimeMillis();
                 sendData();
                 setPartitionTime(SEND_DATA,System.currentTimeMillis()-time);
                 System.out.println(getName()+">after send data");

                 setLastException(null);
                 setStatus(END);
                 time = System.currentTimeMillis();
                 restingSleep();
             }
             catch(CrawlerException re) {
                 System.out.println(re.getMessage());
                 re.printStackTrace();
                 if( re.detail != null ) {
                     re.detail.printStackTrace();
                 }
                 setLastException(re);
                 try {
                     setStatus(SLEEPING);
                     time = System.currentTimeMillis();
                     if( !stop ) {
                         System.out.println("R>"+getName()+">Sleeping "+sleepTime+" mls.");
                         sleep(sleepTime);
                     }
                     setPartitionTime(SLEEPING,System.currentTimeMillis()-time);
                 }
                 catch( InterruptedException ie ) {
                     ie.printStackTrace();
                 }
             }
             finally {
                 try {
                     cleanup();
                 }
                 catch(Exception exc) {
                     exc.printStackTrace();
                 }
                 setPartitionTime(END,System.currentTimeMillis()-time);
                 setTotalCicleTime(System.currentTimeMillis() - getStartCicleTime());
             }
             String parts = "";
             for(int i = 0; i < STATES.length; i++) {
                 parts += (i==0?""+getPartitionTime(i):","+getPartitionTime(i));
             }
             System.out.println("R>"+getName()+">Total time is "+getTotalCicleTime()+" mls ["+parts+"]");
         }
         try {
             System.out.println("R>"+getName()+">Thread dead, calling cleanup().");
             setStatus(DEAD);
             cleanup();
             System.out.println("R>"+getName()+">Thread dead cleanup() done.");
         }
         catch(Exception exc) {
             exc.printStackTrace();
         }
     }
     public void restingSleep() {
         try {
             sleep(restingTime);
         }
         catch(InterruptedException exc) {
             exc.printStackTrace();
         }
     }

  /**
   * This method gets the next URL to be processed.
   * @throws CrawlerException
   */
  abstract protected void selectUrl() throws CrawlerException;

  /**
   * It checks if there is any constraint about the given URL
   *
   * @throws CrawlerException
   */
  abstract protected void checkUrl() throws CrawlerException;

  /**
   * This method dowloads the given URL.
   *
   * @throws CrawlerException
   */
  abstract protected void downloadUrl() throws CrawlerException;

  /**
   * This method processes the URL content
   *
   * @throws CrawlerException
   */
  abstract protected void processData() throws CrawlerException;

  /**
   * It checks if there is any constraint about the processed data.
   *
   * @throws CrawlerException
   */
  abstract protected void checkData() throws CrawlerException;

  /**
   * This method sends data already processed.
   *
   * @throws CrawlerException
   */
  abstract protected void sendData() throws CrawlerException;

  /**
   * This method cleans up any temporary attribute/variable
   *
   * @throws CrawlerException
   */
  abstract protected void cleanup() throws CrawlerException;


}
