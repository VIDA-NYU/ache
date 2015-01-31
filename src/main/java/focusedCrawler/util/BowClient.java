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

import focusedCrawler.util.Page;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.net.UnknownHostException;
import java.io.IOException;

public class BowClient {

        private Socket socket;
        private InputStream streamFromSocket;
        private OutputStream streamToSocket;
        private final String endString = "\r\n.\r\n";
        private String host;
        private int port;


        public BowClient(String host, int port) throws UnknownHostException,
            IOException {
          this.host = host;
          this.port = port;
          open();
        }

        public BowClient() {

        }

        public void open() throws UnknownHostException, IOException {
          socket = new Socket(host,port);
        }

        public double classify(Page page) throws IOException {
             //return 0.6;
                ///*
                 String resultBow;
                double probability = 0;
                try {
                  boolean result = true;
                  int dotIndex = 0;
                  String content = page.getContent();
         //                System.out.println("BOW CLIENT : " + page.getURL());
//                System.out.println("BOW CLIENT : " + content);
                content = content + endString;
                streamToSocket = socket.getOutputStream();
                streamToSocket.write(content.getBytes());
                //byte[] buffer = new byte[128000];
                byte[] buffer = new byte[90];
                int cont  = 0;
                streamFromSocket = socket.getInputStream();
                int avail = streamFromSocket.read(buffer);
                resultBow = new String(buffer);
                resultBow = resultBow.substring(0, resultBow.lastIndexOf(".")+1);
//                System.out.println("RESULT TT " + resultBow);
                probability = getRelevance(resultBow);
              }catch(java.net.SocketException ex){
                close();
                open();
                return 0;
              }
              return probability;
          //*/
        }


        private void close(){
          try {
            socket.close();
            streamToSocket = null;
            streamFromSocket = null;
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }


        private double getRelevance(String resultBow) {

                //resultBow = "biology 0.5000893474\nnon_biology 0.4999106526\n.biology 0";
                System.out.println("RESULT BOW : " + resultBow);
                int index = resultBow.indexOf(" ");
                if (resultBow .indexOf(".") == 0) {
                     System.out.println("zero");
                     return 0;
                }
                String temp = null;

                double relevance  = 0;

                StringTokenizer st = new StringTokenizer(resultBow, " \n");

                while (st.hasMoreTokens()) {

                     temp = new String(st.nextToken());
                     //System.out.println(temp);
                     if (temp.indexOf("non") != -1) {
                          temp = new String(st.nextToken());
                          relevance = 1 - Double.parseDouble(temp);
                     }
                }


                return relevance;


        }

        public static void main(String[] args) throws IOException {


                Integer port = new Integer(args[1]);
                BowClient bow = null;
                try {
                        bow = new BowClient(args[0], port.intValue());
                }
                catch (UnknownHostException ex1) {
                }
                catch (IOException ex1) {
                }
//                java.io.BufferedReader stdin = new java.io.BufferedReader( new java.io.InputStreamReader(System.in));
                StringBuffer buf = new StringBuffer();

                try {
                        java.io.BufferedReader stdin = new java.io.BufferedReader(new java.io.FileReader(args[2]));
                        for (String line = stdin.readLine(); line != null; line=stdin.readLine()) {
                                buf.append(line);
                                buf.append("\n");
                        }
                }

                catch (java.io.FileNotFoundException ex) {
                        System.out.println(ex.getMessage());
                }
                catch (IOException ex) {
                        System.out.println(ex.getMessage());
                }

                Page page = null;
                try {
                        page = new Page(new java.net.URL("http://"), buf.toString());
                }
                catch (java.net.MalformedURLException ex2) {
                }
                double relevance = 0.0;
                try {
                        relevance = bow.classify(page);
                }
                catch (IOException ex3) {
                }


                if (relevance > 0.51)
                        System.out.println("GOOD");
                System.out.println("relevance : " + relevance);

        }


}





