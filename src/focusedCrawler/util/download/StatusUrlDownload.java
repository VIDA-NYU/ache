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


import java.net.URL;

import java.net.HttpURLConnection;

import java.io.IOException;

import java.io.BufferedReader;

import java.io.InputStreamReader;



public class StatusUrlDownload {



    private URL statusUrl;



    public StatusUrlDownload () {

    } //StatusUrlDownload



    public StatusUrlDownload (URL statusUrl) {

        this.statusUrl = statusUrl;

    } //StatusUrlDownload



    public URL getStatusUrl () {

        return this.statusUrl;

    } //getStatusUrl



    public void setStatusUrl (URL statusUrl) {

        this.statusUrl = statusUrl;

    } //setStatusUrl



    public String getStatus () throws IOException {

        HttpURLConnection con = null;

        con = (HttpURLConnection) statusUrl.openConnection();



        String resultado = "";

        try {

            BufferedReader bis = new BufferedReader(new InputStreamReader (con.getInputStream()));

            String input = null;

            while( (input = bis.readLine()) != null ) {

                resultado += input + "\n";

            }

            bis.close ();

        } //try

        finally {

            con.disconnect();

            if( resultado.length() == 0 ) {

                resultado = null;

            }

        } //finally

        return resultado;

    }

}