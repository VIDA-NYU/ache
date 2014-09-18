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



import java.net.URL;

import java.net.HttpURLConnection;

import java.net.URLConnection;

import java.io.BufferedInputStream;

import java.io.BufferedReader;

import java.io.FileInputStream;

import java.io.InputStreamReader;

import java.io.InputStream;

import java.io.OutputStream;

import java.io.IOException;

import java.io.FileOutputStream;

import java.net.Socket;

import focusedCrawler.util.Log;






public class Tunneling {



    private URL url;

    private String nick;



    private URLConnection connection = null;

    private OutputStream out = null;



    public Tunneling(String _nick, URL _url) {

        this.nick = _nick;

        this.url = _url;

    } //Tunneling



    public OutputStream openTunnel() throws IOException {

        connection = url.openConnection();

        if (connection instanceof HttpURLConnection) {

            ((HttpURLConnection)connection).setRequestMethod("POST");

        } //if

        connection.setDoInput(true);

        connection.setDoOutput(true);

        connection.connect();

        out = connection.getOutputStream();

        return out;

    } //openTunnel



    public void closeTunnel() throws IOException {

        if (connection != null) {

            if (out != null) {

                out.close();

                Log.log("TUN", nick, "output closed.");

            } //if

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String s = null;

            while (true) {

                s = in.readLine();

                if (s == null) {

                    break;

                } //if

                else {

                    System.out.println(s);

                } //else

            } //while

            in.close();

            Log.log("TUN", nick, "input closed.");



            if (connection instanceof HttpURLConnection) {

                ((HttpURLConnection) connection).disconnect();

                Log.log("TUN", nick, "disconnected.");

            } //if

        } //if

    } //closeTunmel



    public void send(String data) throws IOException{

        send(data.getBytes());

    } //send



    public synchronized void send(byte[] data) throws IOException{

        out.write(data, 0, data.length);

    } //send



    public static void main(String [] args) throws Exception {

        URL url = new URL(args[0]);

        String data = args[1];

        Tunneling tunel = new Tunneling("0", url);

        tunel.openTunnel();

        System.out.println ("abriu tunel");

        if (data.equals ("file")) {

            System.out.println("lendo arquivo" + args [2]);

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream (args [2]));

            byte[] bytes = new byte[bis.available()];

            bis.read(bytes, 0, bytes.length);

            tunel.send(bytes);

        } //if

        else {

            tunel.send(data);

        } //else

        tunel.closeTunnel();

        System.out.println("fechou tunel");

    } //main

}



