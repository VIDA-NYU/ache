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



import java.io.*;

import java.net.*;

public class ThreadDownloadURL extends ThreadDownload {

    protected URL            url;

    protected URLConnection  con;

    protected BufferedReader bin;

    protected StringBuffer   buffer;

    private long lastModified;

    public static final int  BUFFER_SIZE = 8 * 1024;    // 8k bytes.

    public static boolean debug = false;

    public void debug(Object obj) {
        if( debug ) {
            System.out.println(obj);
        }
    }

    public ThreadDownloadURL(String alvo) {
        super(alvo);
    }

    /**
     * Constroi um thread com alvo ja definido, no caso, uma URL.
     * @param alvo URL que sera o alvo do thread.
     */
    public ThreadDownloadURL(String alvo,int timeout) {
        super(alvo,timeout);
    }

    /**
     * @see ThreadDownload#setAlvo().
     */
    public void setAlvo(String alvo) {
        try {
            this.alvo = alvo;
            this.url = new URL(alvo);
            this.buffer = new StringBuffer(BUFFER_SIZE);
            //debug("TD> URL " + url + " OK!");
        } catch (MalformedURLException mfue) {
            mfue.printStackTrace();
        }
    }

    /**
     * O demais atributos do thread so devem ser incializados no momento em que e dado um start() para permitir que
     * o processamento das conexoes nao "trave" a criacao do thread.
     */

    public void run() {
        String nome = getName();
        nome += " " + url.getHost() + ">";
        long INICIO = System.currentTimeMillis();
        try {
            setReady(false);
            System.out.println("TD>"+nome+" DOWNLOADING "+url);
            long TIME = System.currentTimeMillis();
            con = url.openConnection();
            debug("TD>"+nome+" CONEXAO "+(System.currentTimeMillis()-TIME));

            // MUITO IMPORTANTE
            System.out.println("TD>"+nome+" Colocando TIMEOUT P/"+timeout+"mls");
            con.setRequestProperty("Timeout",""+timeout);
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            TIME = System.currentTimeMillis();
            input = con.getInputStream();
            debug("TD>"+nome+" INPUT "+(System.currentTimeMillis()-TIME));
            lastModified = con.getLastModified();
            debug("TD>"+nome+" Finalizando normalmente. Com ready = "+ready());
            debug("TD>"+nome+" Download_URL OK!");
        }catch (Exception exc) {
            exc.printStackTrace();
            debug("TD>"+nome+" Finalizando devido a excecao no run().");
            finalizar();
        }
        System.out.println("TD>"+nome+" TEMPO TOTAL = "+(System.currentTimeMillis()-INICIO)+"mls");
    }



    /**
     * @see ThreadDownload#getDados().
     */
    public String getDados() {
        return (buffer != null ? buffer.toString() : null);
    }

    public long getLastModified(){
        return lastModified;
    }

    /**
     * @see ThreadDownload#finalizar().
     */

    public void finalizar() {
        setReady(true);
        String nome = getName();
        try {
            url = null;
            //buffer = null;
            if (input != null) {
                debug("TD>"+nome+" FECHANDO InputStream!");
                input.close();
            }
            input = null;
            if (out != null) {
                debug("TD>"+nome+" FECHANDO OutputStream!");
                out.close();
            }
            out = null;
            if( con != null ) {
                debug("TD>"+nome+" FECHANDO Connection!");
                ((HttpURLConnection)con).disconnect();
            }
            con = null;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        System.out.println("TD>"+nome+" FINALIZADO!");
    }

    /**
     * Metodo de validacao da classe.
     */
    public static void main(String args[]) throws Throwable {

        //Socket.setSoTimeout(timeout);

//    	URL url = new URL(args[0]);
//    	URLConnection yc = url.openConnection();
//    System.out.println("SETTING TIMEUOT...");
////    yc.setConnectTimeout(connectTimeout);
////    yc.setReadTimeout(readTimeout);
//
//    StringBuffer buffer = new StringBuffer();
////    try{
//      BufferedReader inCon = new BufferedReader(new InputStreamReader(yc.
//          getInputStream()));
//      String inputLine;
//      while ((inputLine = inCon.readLine()) != null) {
//        buffer.append(inputLine);
//        buffer.append("\n");
//      }
//      inCon.close();
//      System.out.println(buffer.toString());
      
        ThreadDownload thread = new ThreadDownloadURL("http://www.uralprofi.ru/");

        int cont = 0;

        int maximo = (args.length > 1 ? Integer.valueOf(args[1].trim()).intValue() : 4);

        thread.start();

        while( !thread.ready() && cont < maximo ) {

            System.out.println("SLEEP(" + (cont++) + ")");

            Thread.sleep(2500);

        }

        System.out.println("DADOS = " + thread.getDados());

        thread.finalizar();

        //thread.stop();

    }

}



/*

    class InputGenerator {

        public synchronized InputStreamReader getInput(URLConnection con) throws Exception {

            return new InputStreamReader(con.getInputStream());

        }

    }

*/
