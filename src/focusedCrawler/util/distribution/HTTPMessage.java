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



import java.io.*;

import java.util.*;

import java.net.*;



public class HTTPMessage{



    public HTTPMessage() {



    }



    public String sendGET(Hashtable prop) throws MalformedURLException,IOException{

        String url = (String)prop.get("url");

        String queryString = toQueryString(prop);

        String urlS = url + (queryString != null ? "?"+queryString : "");

        URLConnection connection=createConnection(urlS);

        String result;

        connection.setDoInput(true);

        sendHeader(connection,prop);

        result = readInput(connection);

		if (connection != null && connection instanceof HttpURLConnection) {

		    ((HttpURLConnection) connection).disconnect ();

		}

        return result;

    }



    public String sendPOST(Hashtable prop) throws MalformedURLException,IOException {

        String url = (String)prop.get("url");

        String contentType = (String)prop.get("content-type");

        String result="";

        URLConnection connection=null;

        if ("application/x-www-form-urlencoded".equals(contentType)) {

            connection = createConnection(url);

            String queryString = toQueryString(prop);

            connection.setDoInput(true);

            connection.setDoOutput(true);

            sendHeader(connection,prop);

            writeOutput(queryString,connection);

            result = readInput(connection);

        }

		if (connection != null && connection instanceof HttpURLConnection) {

		    ((HttpURLConnection) connection).disconnect ();

		}

        return result;

    }





    public String toQueryString(Hashtable prop) {

        Enumeration e = (Enumeration)prop.get("parameterNames");

        String result="";

        boolean first = true;

        while(e.hasMoreElements()) {

            String paramName = (String)e.nextElement();

            String[] param = (String[])prop.get(paramName);

            for (int i=0;i<param.length;i++) {

                if (first) {

                    result = paramName+"="+param[i];

                    first = false;

                }

                else result = result+"&"+paramName+"="+param[i];

            }

        }

        return result;

    }



    protected void sendHeader(URLConnection connection, Hashtable prop) {

        Enumeration e = (Enumeration)prop.get("headerNames");

        while(e.hasMoreElements()) {

            String headerName = (String)e.nextElement();

            String header = (String)prop.get(headerName);

            connection.setRequestProperty(headerName,header);

        }

    }



    protected URLConnection createConnection(String url) throws MalformedURLException,IOException {

        URL urlU = new URL(url);

        return urlU.openConnection();

    }



    protected String readInput(URLConnection connection) throws IOException {

        String result="";

        BufferedReader d = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line = d.readLine();

        boolean first=true;

        while (line!=null) {

            if (first) {

                result = line;

                first = false;

            }

            else result = result+"\n"+line;

            line = d.readLine();

        }

        d.close();

        return result;

    }



    protected void writeOutput (String data, URLConnection connection) throws IOException {

        byte[] dataBytes = data.getBytes();

        OutputStream out = connection.getOutputStream ();

        out.write (dataBytes, 0, dataBytes.length);

        out.flush ();

        out.close ();

    }



}