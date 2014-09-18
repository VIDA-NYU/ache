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

import java.text.DateFormat;

import java.text.ParseException;

import java.util.Iterator;

import java.util.HashMap;

import java.util.Vector;

import focusedCrawler.util.Log;
import focusedCrawler.util.ParameterFile;









public abstract class DownloaderAbstract implements Downloader {



        private Log normalLog;

        private boolean showNormalLog;

        private Log errorLog;

        private boolean showErrorLog;



        protected Vector requestNames;

        protected Vector requestValues;

        private DateFormat dateFormater;

        protected HashMap response;



        private String protocol;

        private String method;

        private int timeout;

        private boolean followRedirects;

        private int followRedirectsTolerance;



        private URL urlTarget;

        private int status;

        private String id;

        private boolean shutdown;



    public DownloaderAbstract() {

        showNormalLog = false;

        showErrorLog = false;

        requestNames = new Vector();

        requestValues = new Vector();

        dateFormater = DateFormat.getInstance();

        response = new HashMap();

    }

    public DownloaderAbstract(ParameterFile paramFile) throws DownloaderException {

        this();

        try {

            String str = paramFile.getParam("DOWNLOADER_ID");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_ID' parameter");

            }

            setId(str.trim());



            str = paramFile.getParam("DOWNLOADER_SHOW_NORMAL_LOG");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_SHOW_NORMAL_LOG' parameter");

            }

            setShowNormalLog(Boolean.valueOf(str.trim()).booleanValue());



            str = paramFile.getParam("DOWNLOADER_SHOW_ERROR_LOG");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_SHOW_ERROR_LOG' parameter");

            }

            setShowErrorLog(Boolean.valueOf(str.trim()).booleanValue());

            str = paramFile.getParam("DOWNLOADER_PROTOCOL");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_PROTOCOL' parameter");

            }

            setProtocol(str.trim());



            str = paramFile.getParam("DOWNLOADER_METHOD");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_METHOD' parameter");

            }

            setMethod(str.trim());



            str = paramFile.getParam("DOWNLOADER_TIMEOUT");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_TIMEOUT' parameter");

            }

            setTimeout(Integer.valueOf(str.trim()).intValue());



            str = paramFile.getParam("DOWNLOADER_FOLLOW_REDIRECT");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_FOLLOW_REDIRECT' parameter");

            }

            setFollowRedirects(Boolean.valueOf(str.trim()).booleanValue());



            str = paramFile.getParam("DOWNLOADER_FOLLOW_REDIRECT_TOLERANCE");

            if( str == null ) {

                throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_FOLLOW_REDIRECT_TOLERANCE' parameter");

            }

            setFollowRedirectsTolerance(Integer.valueOf(str.trim()).intValue());



            String token = paramFile.getParam("DOWNLOADER_TOKEN");

            Iterator requests = paramFile.getParameters();

            while( requests.hasNext() ) {

                str = (String)requests.next();

                if( str.startsWith("DOWNLOADER_REQUEST") ) {

                    str = paramFile.getParam(str);

                    int pos = str.indexOf(token);

                    setRequestProperty(str.substring(0,pos).trim(),str.substring(pos+token.length()).trim());

                }

            }

        }

        catch(Exception exc) {

            throw new DownloaderException(exc.getMessage(),exc);

        }

    }



    protected void logAll(String message) {

        logNormal(message);

        logError(message);

    }



    public void setNormalLog(Log newNormalLog) {

        normalLog = newNormalLog;

    }

    public Log getNormalLog() {

        return normalLog;

    }

    public void setShowNormalLog(boolean newShowNormalLog) {

        showNormalLog = newShowNormalLog;

    }

    public boolean isShowNormalLog() {

        return showNormalLog;

    }



    protected void logNormal(String message) {

        if( showNormalLog ) {

            Log.log("Downloader",getId()+".ok",message);

        }

        if( normalLog != null ) {

            normalLog.logMessage("Downloader",getId()+".ok",message);

        }

    }



    public void setErrorLog(Log newErrorLog) {

        errorLog = newErrorLog;

    }

    public Log getErrorLog() {

        return errorLog;

    }

    public void setShowErrorLog(boolean newShowErrorLog) {

        showErrorLog = newShowErrorLog;

    }

    public boolean isShowErrorLog() {

        return showErrorLog;

    }



    protected void logError(String message) {

        if( showErrorLog ) {

            try {

                Log.log("Downloader",getId()+".err","'"+getUrlTarget()+"'-"+message);

            }

            catch(DownloaderException exc) {

                exc.printStackTrace();

            }

        }

        if( errorLog != null ) {

            try {

                errorLog.logMessage("Downloader",getId()+".err","'"+getUrlTarget()+"'-"+message);

            }

            catch(DownloaderException exc) {

                exc.printStackTrace();

            }

        }

    }



    public void setId(String newId) {

        id = newId;

    }

    public String getId() {

        return id;

    }



    public void clearRequestProperties() throws DownloaderException {

        requestNames.clear();

        requestValues.clear();

    }



    public void setRequestProperty(String name, String value) throws DownloaderException {

        int index = requestNames.indexOf(name.trim());

        if( index != -1 ) {

            requestNames.remove(index);

            requestValues.remove(index);

            requestNames.add(index,name.trim());

            requestValues.add(index,value.trim());

        }

        else {

            requestNames.add(name.trim());

            requestValues.add(value.trim());

        }

    }



    public void setProtocol(String newProtocol) throws DownloaderException {

        protocol = newProtocol;

    }

    public String getProtocol() throws DownloaderException {

        return protocol;

    }



    public void setMethod(String newMethod) throws DownloaderException {

        method = newMethod;

    }

    public String getMethod() throws DownloaderException {

        return method;

    }



    public void setTimeout(int newTimeout) throws DownloaderException {

        timeout = newTimeout;

    }

    public int getTimeout() throws DownloaderException {

        return timeout;

    }



    public void setFollowRedirects(boolean newFollowRedirects) throws DownloaderException {

        followRedirects = newFollowRedirects;

    }

    public boolean isFollowRedirects() throws DownloaderException {

        return followRedirects;

    }



    public void setFollowRedirectsTolerance(int newFollowRedirectsTolerance) throws DownloaderException {

        followRedirectsTolerance = newFollowRedirectsTolerance;

    }

    public int getFollowRedirectsTolerance() throws DownloaderException {

        return followRedirectsTolerance;

    }



    public void setUrlTarget(URL newUrlTarget) throws DownloaderException {

        urlTarget = newUrlTarget;

        setStatus(Downloader.UNKNOWN);

        clearResponseProperties();

    }

    public URL getUrlTarget() throws DownloaderException {

        return urlTarget;

    }



    protected void setStatus(int newStatus) {

        status = newStatus;

    }



    public int getStatus() {

        return status;

    }



    public void clearResponseProperties() throws DownloaderException {

        response.clear();

    }



    public String getResponseProtocol() throws DownloaderException {

        check();

        String result = getResponseProperty(Downloader.RESPONSE_PROTOCOL);

        if( result == null ) {

            String message = "Missing data '"+Downloader.RESPONSE_PROTOCOL+"'.";

            logError(message);

            throw new DownloaderException(message);

        }

        return result;

    }



    public int getResponseCode() throws DownloaderException {

        check();

        try {

            return Integer.valueOf(getResponseProperty(Downloader.RESPONSE_CODE)).intValue();

        }

        catch(NumberFormatException exc) {

            String message = "Missing data '"+Downloader.RESPONSE_CODE+"'.";

            logError(message);

            throw new DownloaderException(message);

        }

    }



    public int getContentLength() throws DownloaderException {

        check();

        try {

            String contentLenght = getResponseProperty("Content-Length",false);

            return Integer.valueOf(contentLenght).intValue();

        }

        catch(NumberFormatException exc) {

            //String message = "Missing data Content-Length.";

            //logError(message);

            //throw new DownloaderException(message);

            return 0;

        }

    }



    public String getResponseMessage() throws DownloaderException {

        check();

        String result = getResponseProperty(Downloader.RESPONSE_MESSAGE);

        if( result == null ) {

            String message = "Missing data '"+Downloader.RESPONSE_MESSAGE+"'.";

            logError(message);

            throw new DownloaderException(message);

        }

        return result;

    }



    public String getContentType() throws DownloaderException {

        check();

        String result = getResponseProperty("Content-Type",false);

        if( result == null ) {

            String message = "Missing data 'Content-Type'.";

            logError(message);

            throw new DownloaderException(message);

        }

        return result;

    }



    public long getLastModified() throws DownloaderException {

        check();

        String result = getResponseProperty("Last-Modified",false);

        if( result == null ) {

            String message = "Missing data 'Last-Modified'.";

            logError(message);

            throw new DownloaderException(message);

        }

        try {

            //return dateFormater.parse(result).getTime();

            return java.util.Date.parse(result);

        }

//        catch(ParseException exc) {

//            throw new DownloaderException("Could not convert '"+result+"'."+exc.getMessage());

//        }

        catch(IllegalArgumentException exc) {

            String message = "Could not convert '"+result+"'."+exc.getMessage();

            logError(message);

            throw new DownloaderException(message,exc);

        }

    }



    protected void setResponseProperty(String name, String value) throws DownloaderException {

        response.put(name,value);

    }



    public String getResponseProperty(String name) throws DownloaderException {

        check();

        return (String)response.get(name);

    }



    public String getResponseProperty(String name, boolean caseSensitive) throws DownloaderException {

        check();

        String result = null;

        if( caseSensitive ) {

            result = getResponseProperty(name);

        }

        else {

            Iterator i = listResponse();

            while( i.hasNext() ) {

                result = (String)i.next();

                if( name.equalsIgnoreCase(result) ) {

                    result = getResponseProperty(result);

                    break;

                }

                result = null;

            }

        }

        return result;

    }



    public Iterator listResponse() throws DownloaderException {

        check();

        return response.keySet().iterator();

    }



    public void setShutdown(boolean newShutdown) {

        shutdown = newShutdown;

    }



    public boolean isShutdown() throws DownloaderException {

        return shutdown;

    }





    protected void check() throws DownloaderException {

        if( getStatus() == Downloader.UNKNOWN ) {

            if( Downloader.METHOD_POST.equals(getMethod()) ) {

                String message = "In a POST request you must call getInputStream() before call this method.";

                logError(message);

                throw new DownloaderException(message);

            }

            String message = "Unknown downloader state.";

            logError(message);

            throw new DownloaderException(message);

        }

    }



    public String toString() {

        String result = null;

        try {

            result = "Request>'"+getUrlTarget()+"', "+getProtocol()+", "+getMethod()+", "+getTimeout()+", "+isFollowRedirects()+", "+getFollowRedirectsTolerance();

            if( getStatus() == Downloader.OK ) {

                try {

                    result += "\nResponse>"+getResponseProtocol()+", "+getResponseCode()+", "+getResponseMessage()+ ", content-type="+getContentType()+", lastModified="+getLastModified();

                }

                catch(DownloaderException exc) {

                    result += "\nResponse>"+exc.getMessage();

                }

            }

            else {

                result += "\nResponse> statusFAIL="+getStatus();

            }

            result += "\nResponse>"+response.toString();

        }

        catch(DownloaderException exc) {

            result = "FAIL>"+exc.getMessage();

        }

        return result;

    }

}
