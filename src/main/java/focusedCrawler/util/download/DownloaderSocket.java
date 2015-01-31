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

import java.net.Socket;

import java.net.URL;

import focusedCrawler.util.ParameterFile;





public class DownloaderSocket extends DownloaderAbstract {



    private int maxBlockedCount;

    private int blockedCount;

    private int count;

    private Socket socket;

    private OutputStream out;

    private PushbackInputStream in;



    public DownloaderSocket() throws DownloaderException {

        super();

        maxBlockedCount = Integer.MAX_VALUE;

        blockedCount = 0;

        count = 0;

    }



    public DownloaderSocket(ParameterFile paramFile) throws DownloaderException {

        super(paramFile);

        String str = paramFile.getParam("DOWNLOADER_MAX_BLOCKED_THREADS");

        if( str == null ) {

            throw new DownloaderException(paramFile.getCfgFile()+"> Missing 'DOWNLOADER_MAX_BLOCKED_THREADS' parameter");

        }

        maxBlockedCount = Integer.valueOf(str.trim()).intValue();

        blockedCount = 0;

        count = 0;

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

        }

        catch(MalformedURLException exc) {

            setStatus(Downloader.FAIL_REDIRECT);

            String message = "Malformed."+exc.getMessage();

            logError(message);

            throw new DownloaderException(message,exc);

        }

        catch(InterruptedIOException exc) {

            setStatus(Downloader.FAIL_TIMEOUT);

            String message = "Timeout expired."+exc.getMessage();

            logError(message);

            throw new DownloaderException(message,exc);

        }

        catch(IOException exc) {

            setStatus(Downloader.FAIL_IO);

            String message = "IO Error."+exc.getMessage();

            logError(message);

            throw new DownloaderException(message,exc);

        }

    }



    protected void connect(URL url,int depth) throws IOException, InterruptedIOException, MalformedURLException, DownloaderException {

        logAll("connect("+depth+")>"+url);
        if( !"http".equals(url.getProtocol()) ) {
            setStatus(Downloader.FAIL_PROTOCOL);
            String message = "Invalid protocol. "+url.getProtocol();
            logError(message);
            throw new DownloaderException(message);
        }
        String host = url.getHost();
        int port = url.getPort() == -1 ? 80 : url.getPort();
        SocketCreator sc = new SocketCreator(getId()+"_call_"+(count++));
        sc.setTarget(host,port);
        sc.start();
        try {
            sc.join(getTimeout());
            logNormal("join("+sc.getName()+") finished");
        }
        catch(InterruptedException exc) {
            blockedCount++;
            sc.close = true;
            String message = "SocketCreator join().(num="+blockedCount+",max="+maxBlockedCount+")";
            logError(message);
            throw new DownloaderException(message);
        }
        if( !sc.available ) {
            blockedCount++;
            sc.close = true;
            String message = "SocketCreator blocked.(num="+blockedCount+",max="+maxBlockedCount+")";
            logError(message);
            throw new DownloaderException(message);
        }
        if( sc.exc != null ) {
            blockedCount++;
            sc.close = true;
            String message = "SocketCreator error.(num="+blockedCount+",max="+maxBlockedCount+")";
            logError(message);
            throw new DownloaderException(message,sc.exc);
        }
        socket = sc.socket;
        socket.setSoTimeout(getTimeout());
        out = socket.getOutputStream();
        PrintWriter pout = new PrintWriter(out);
        String file = url.getFile();
        if( file.length() == 0 ) {
            file = "/";
        }
        System.out.println(">>>>>URL"+url.toString());
        String str = getMethod()+" "+file+" "+getProtocol();
        logNormal("Request>"+str);
        pout.println( str );
        str = "Host: "+host+":"+port;
        logNormal("Request>"+str);
        pout.println( str );
        for (int i = 0; i < requestNames.size(); i++) {
            str = requestNames.elementAt(i)+": "+requestValues.elementAt(i);
            logNormal("Request>"+str);
            pout.println( str );
        }
        pout.println();
        pout.flush();
        in = new PushbackInputStream(socket.getInputStream(),1024);
        if( Downloader.METHOD_GET.equals(getMethod()) ) {
            readInput(depth);
        }
        else {
            setStatus(Downloader.UNKNOWN);
        }
    }



    protected void readInput(int depth) throws DownloaderException, IOException {

        boolean first = true;

        boolean stop = false;

        StringBuffer input = new StringBuffer();

        while( !stop ) {

            input.setLength(0);

            int c;

            int i = 0;

            while(  (c = in.read()) != '\n' && c != -1 ) {

                input.append((char)c);

                i++;

            }

            String header = input.toString().trim();

            logNormal("Response>"+header);
            if( header.indexOf(Downloader.PROTOCOL) < 0 && header.indexOf(":") < 0 ) { // respondem corretamente tivemos que colocar este if.
                if( first ) { // caso não haja cabeçário deve-se retornar o InputStream.
                    logNormal("unread>"+input);
                    in.unread(input.append('\n').toString().getBytes());
                }
                stop = true;
            }
            else {
                first = false;
                processResponse(header);
            }
        }

        logNormal("End of header!");

        setStatus(Downloader.OK);

        if( (""+java.net.HttpURLConnection.HTTP_MOVED_PERM).equals(getResponseProperty(Downloader.RESPONSE_CODE)) ||

            (""+java.net.HttpURLConnection.HTTP_MOVED_TEMP).equals(getResponseProperty(Downloader.RESPONSE_CODE)) ) {

            logNormal("Redirected!");

            if( isFollowRedirects() ) {


                close();
                String redirect = getResponseProperty("Location",false);

                if( redirect == null ) {

                    setStatus(Downloader.FAIL_REDIRECT);

                    throw new DownloaderException(getUrlTarget()+" redirected to nowhere.");

                }

                if( redirect.indexOf("http://") >= 0 ) { // redirecionamento absoluto

                    setUrlTarget(new URL(redirect));

                }

                else { // redirecionamento relativo.

                    if( !redirect.startsWith("/") ) {

                        redirect = "/"+redirect;

                    }

                    setUrlTarget(new URL(getUrlTarget().getProtocol()+"://"+getUrlTarget().getHost()+redirect));

                }

                clearResponseProperties();

                connect(depth+1);

            }

        }

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

        }

        else {

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

        if( getStatus() == Downloader.UNKNOWN && Downloader.METHOD_POST.equals(getMethod()) ) {

            try {

                readInput(0);

            }

            catch(Exception exc) {

                throw new DownloaderException("Error:"+exc.getMessage(),exc);

            }

        }

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

            if( socket != null ) {

                socket.shutdownOutput();

                socket.shutdownInput();

                socket.close();

                socket = null;

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



    class SocketCreator extends Thread {

        public String host;

        public int port;

        public Socket socket;



        public boolean available;

        public boolean close;

        public Exception exc;



        public SocketCreator(String id) {

            super(id);

            this.close = false;

            this.available = true;

            this.exc = null;

        }



        public void setTarget(String host,int port) {

            this.host = host;

            this.port = port;

        }



        public void run() {

            long t = System.currentTimeMillis();

            try {

                available = false;

                logNormal(getName()+" before socket.");

                socket = new Socket(host,port);

                logNormal(getName()+" after socket. "+(System.currentTimeMillis()-t)+" mls");

                available = true;

                if( close ) {

                    logError(getName()+" dead creator closing socket.");

                    socket.close();

                    logError(getName()+" dead creator socket closed. "+(System.currentTimeMillis()-t)+" mls");

                }

            }

            catch(Exception exc) {

                if( close ) {

                    logError(getName()+" dead creator exception. "+(System.currentTimeMillis()-t)+" mls. msg '"+exc.getMessage()+"'");

                }

                this.exc = exc;

            }

        }

    }



    public static void main(String[] args) {

        try {

            if(args[0].equals("host")) {

                java.net.ServerSocket server = new java.net.ServerSocket(8080);

                while(true) {

                    java.net.Socket soc = server.accept();

                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(soc.getInputStream()));

                    int c;

                    while(true) {

                        String ln = in.readLine();

                        if (ln == null) break;

                        if (ln.length() == 0) break;

                        System.out.println("request> " + ln);

                    }



                    java.io.PrintWriter out = new java.io.PrintWriter(soc.getOutputStream());

                    out.println("HTTP/1.1 200 OK");

                    out.println("Date: Wed, 03 Oct 2001 18:47:53 GMT");

                    out.println("Server: Apache/1.3.20 (Unix) mod_gzip/1.3.19.1a ApacheJServ/1.1.2");

                    out.println("Set-Cookie: Apache=200.249.104.8.12942100213487366; path=/");

                    out.println("Last-Modified: Tue, 12 Jun 2001 06:52:08 GMT");

                    out.println("ETag: \"1bb1a-1fa-3b25bc18\"");

                    out.println("Accept-Ranges: bytes");

                    out.println("Content-Length: 0");

                    out.println("Connection: close");

                    out.println("Content-Type: text/html");

                    System.out.println("END");

                    out.println("\n");

                    out.flush();

                    int p;

                    System.out.print("data> '");

                    while( (p = in.read()) != -1 ) {

                        System.out.print("["+p+"]"+(char)p);

                    }

                    System.out.println("'");



                    soc.close();

                }

            }

            Downloader down = new DownloaderSocket(new ParameterFile(args));

            for (int i = 1; i < args.length; i++) {

                try {

                    down.clearResponseProperties();

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
