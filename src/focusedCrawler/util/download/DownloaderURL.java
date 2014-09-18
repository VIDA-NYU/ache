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

import java.io.InputStream;

import java.io.InterruptedIOException;

import java.io.IOException;

import java.io.OutputStream;

import java.io.PrintWriter;

import java.io.PushbackInputStream;

import java.net.MalformedURLException;

import java.net.URL;

import focusedCrawler.util.ParameterFile;


public class DownloaderURL extends DownloaderAbstract {

	private int maxBlockedCount;

    private int blockedCount;

    private OutputStream out;

    private PushbackInputStream in;

    private ThreadDownload threadDown;

    public DownloaderURL() throws DownloaderException {
        super();
        maxBlockedCount = Integer.MAX_VALUE;
        blockedCount = 0;
    }

    public DownloaderURL(ParameterFile paramFile) throws DownloaderException {
        super(paramFile);
        String str = paramFile.getParam("DOWNLOADER_MAX_BLOCKED_THREADS");
        if( str == null ) {
            throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_MAX_BLOCKED_THREADS' parameter");
        }
        maxBlockedCount = Integer.valueOf(str.trim()).intValue();
        blockedCount = 0;
    }

    public void connect() throws DownloaderException {
        connect(0);
    }

    protected void connect(int depth) throws DownloaderException {
        try {
            if( depth > getFollowRedirectsTolerance() ) {
                setStatus(Downloader.LOOP);
                String message = "Too many redirects.";
                logError(message);
                throw new DownloaderException(message);
            }
            setStatus(Downloader.UNKNOWN);
            connect(getUrlTarget(),depth);
        }catch(MalformedURLException exc) {
            setStatus(Downloader.FAIL_REDIRECT);
            String message = "Malformed."+exc.getMessage();
            logError(message);
            throw new DownloaderException(message,exc);
        }catch(InterruptedIOException exc) {
            setStatus(Downloader.FAIL_TIMEOUT);
            String message = "Timeout expired."+exc.getMessage();
            logError(message);
            throw new DownloaderException(message,exc);
        }catch(IOException exc) {
            setStatus(Downloader.FAIL_IO);
            String message = "IO Error."+exc.getMessage();
            logError(message);
            throw new DownloaderException(message,exc);
        }

    }

    
    protected void connect(URL url,int depth) throws IOException, InterruptedIOException, MalformedURLException, DownloaderException {
        logAll("connect("+depth+")>"+url);
        if( (!"http".equals(url.getProtocol())) && (!"https".equals(url.getProtocol())) ) {
            setStatus(Downloader.FAIL_PROTOCOL);
            String message = "Invalid protocol. "+url.getProtocol();
            logError(message);
            throw new DownloaderException(message);
        }
        threadDown = new ThreadDownloadURL(url.toString(),getTimeout());
        threadDown.start();
//        SocketCreator sc = new SocketCreator(getId()+"_call_"+(count++));
//        sc.setTarget(host,port);
//        sc.start();
        try {
        	threadDown.join(getTimeout());
            logNormal("join("+threadDown.getName()+") finished");
        }
        catch(InterruptedException exc) {
            blockedCount++;
            threadDown.finalizar();
            String message = "SocketCreator join().(num="+blockedCount+",max="+maxBlockedCount+")";
            logError(message);
            throw new DownloaderException(message);
        }
//        out = threadDown.getOutPutStream();
//        PrintWriter pout = new PrintWriter(out);
//        String file = url.getFile();
//        if( file.length() == 0 ) {
//            file = "/";
//        }
//        System.out.println(">>>>>URL"+url.toString());
//        String str = getMethod()+" "+file+" "+getProtocol();
//        logNormal("Request>"+str);
//        pout.println( str );
//        str = "Host: "+host+":"+port;
//        logNormal("Request>"+str);
//        pout.println( str );
//        for (int i = 0; i < requestNames.size(); i++) {
//            str = requestNames.elementAt(i)+": "+requestValues.elementAt(i);
//            logNormal("Request>"+str);
//            pout.println( str );
//        }
//        pout.println();
//        pout.flush();
        
        
        in = new PushbackInputStream(threadDown.getInputStream(),1024);
        setStatus(Downloader.OK);
//        if( Downloader.METHOD_GET.equals(getMethod()) ) {
//            readInput(depth);
//        }
//        else {
//            setStatus(Downloader.UNKNOWN);
//        }
    }



    


    protected void processResponse(String str) throws DownloaderException {

        if( str.indexOf(Downloader.PROTOCOL) >= 0 ) {

            int pos = str.indexOf(' ');

            setResponseProperty(Downloader.RESPONSE_PROTOCOL,str.substring(0,pos).trim());

            setResponseProperty(Downloader.RESPONSE_CODE,

                str.substring(pos+1,pos = (str.indexOf(' ',pos+1) > 0 ? str.indexOf(' ',pos+1) : str.length())).trim()

            );
            if( pos < str.length() ) {
                setResponseProperty(Downloader.RESPONSE_MESSAGE,str.substring(pos+1).trim());
            }
            else {
                setResponseProperty(Downloader.RESPONSE_MESSAGE,"UNKNOWN");
            }
        }else {
            int pos = str.indexOf(':');
            if ( pos > 0 ) {
                setResponseProperty(str.substring(0,pos).trim(),str.substring(pos+1).trim());
            }
        }
    }



    public OutputStream getOutputStream() throws DownloaderException {
        return out;
    }



    public InputStream getInputStream() throws DownloaderException {
        return in;
    }



    public void close() throws DownloaderException {

        try {

            if( out != null ) {

                out.close();

                out = null;

            }

            if( in != null ) {

                in.close();

                in = null;

            }

            if( threadDown != null ) {

            	threadDown.finalizar();
                
                threadDown = null;

            }

        }

        catch(IOException ioe) {
          ioe.printStackTrace();
           // throw new DownloaderException(ioe.getMessage(),ioe);

        }

    }



    public boolean isShutdown() throws DownloaderException {

        return blockedCount > maxBlockedCount;

    }


    public static void main(String[] args) {

        try {
            Downloader down = new DownloaderURL(new ParameterFile(args[0]));
            for (int i = 1; i < args.length; i++) {
                try {
                    //down.clearResponseProperties();
                    down.setUrlTarget(new URL(args[i]));
                    down.connect();
                    System.out.println(down);
                    if( down.getStatus() == Downloader.OK ) {
                        System.out.println("+------- CONTENT -------+");
                        InputStream in = new java.io.BufferedInputStream(down.getInputStream());
                        int c;
                        while( (c = in.read()) != -1 ) {
                            System.out.print((char)c);
                        }
                        System.out.println("\n+-----------------------+");
                    }
                    else {
                        System.out.println("FAIL="+down.getStatus()+":"+down.getUrlTarget());
                    }
                }
                catch(Exception exc) {
                    exc.printStackTrace();
                }
                finally {
                    down.close();
                }
            }
        }
        catch(Exception exc) {
            exc.printStackTrace();
        }
        System.exit(0);
    }
}
