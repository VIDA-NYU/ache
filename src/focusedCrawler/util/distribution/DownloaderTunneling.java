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
package focusedCrawler.util.distribution;



import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.io.IOException;

import java.net.URL;

import focusedCrawler.util.Log;
import focusedCrawler.util.download.Downloader;
import focusedCrawler.util.download.DownloaderException;
import focusedCrawler.util.download.DownloaderSocket;









public class DownloaderTunneling {



    private String nick;

    private URL url;

    private Downloader downloader = null;



    public DownloaderTunneling(String _nick, URL _url) throws DownloaderException {

        downloader = new DownloaderSocket();

        downloader.setShowNormalLog(true);

        downloader.setShowErrorLog(true);

        downloader.setProtocol(Downloader.PROTOCOL_0);

        downloader.setMethod(Downloader.METHOD_POST);

        downloader.setId(_nick);

        downloader.setTimeout(Integer.MAX_VALUE);

        downloader.setFollowRedirects(false);

        downloader.setUrlTarget(_url);

        this.nick = _nick;

        this.url = _url;

    }





    public void send(String data) throws DownloaderException {

        send(data.getBytes());

    } //send



    public synchronized void send(byte[] data) throws DownloaderException {

        try {

            //Ajusta para escrita

            System.out.println("DEBUG");

            downloader.setRequestProperty("Content-Length",""+data.length);

            downloader.connect();

            downloader.getOutputStream().write(data, 0, data.length);

            downloader.getOutputStream().flush();

            Log.log("TUN", nick, "request done.");



            //Le a resposta.

            BufferedReader in = new BufferedReader(new InputStreamReader(downloader.getInputStream()));

            String s;

            System.out.println("+------- POST RESPONSE --------+");

            while ( (s = in.readLine()) != null ) {

                System.out.println(s);

            }

            System.out.println("+------------------------------+");

            Log.log("TUN", nick, "response readed.");



            System.out.println(downloader.toString());

            if( downloader != null ) {

                downloader.close();

            }

            Log.log("TUN", nick, "downloader cleaned.");

        }

        catch(IOException exc) {

            throw new DownloaderException("Error:"+exc.getMessage(),exc);

        }

    } //send



    public static void main(String [] args) throws Exception {

        URL url = new URL(args[0]);

        String data = args[1];

        DownloaderTunneling tunel = new DownloaderTunneling("tunnel", url);



        System.out.println ("abriu tunel");

        if (data.equals ("file")) {

            System.out.println("lendo arquivo" + args [2]);

            java.io.BufferedInputStream bis = new java.io.BufferedInputStream(new java.io.FileInputStream(args[2]));

            byte[] bytes = new byte[bis.available()];

            bis.read(bytes, 0, bytes.length);

            tunel.send(bytes);

        } //if

        else {

            tunel.send(data);

        } //else

        System.out.println ("enviou dados");

    } //main

}



