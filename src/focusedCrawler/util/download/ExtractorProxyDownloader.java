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
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

public class ExtractorProxyDownloader extends ProxyDownloader implements DownloaderBuffered {

  private byte[] buffer;

  private int length;

  private int max_length;

  private static final int DEFAULT_MAX_LENGHT = 64000;

  public ExtractorProxyDownloader( Downloader _downloader) throws DownloaderException{
    this( _downloader, DEFAULT_MAX_LENGHT );
  }

  public ExtractorProxyDownloader( Downloader _downloader, int _max) throws DownloaderException{
    super( _downloader);
    setMaxBufferSize(_max);
  }

  public int getMaxBufferSize() throws DownloaderException {
    return this.max_length;
  }

  public void setMaxBufferSize(int max) throws DownloaderException {
    if (max > 0){
      this.max_length = max;
      this.buffer= new byte[ max_length];
    } else {
      throw new DownloaderException ("Tamanho maximo do conteudo de uma conexao invalido "+ max);
    }
  }

  public void clearBuffer()  throws DownloaderException {
      setMaxBufferSize(max_length);
  }

  public InputStream getInputStream () throws DownloaderException{

    try {
      return extract(super.getInputStream());
    } catch (IOException ioe){
      throw new DownloaderException ("Problema ao realizar o download do inputstream", ioe);
    }
  }

  public InputStream extract(InputStream input) throws IOException{
     this.length = extractBytes( new BufferedInputStream(input), buffer);
     return new ByteArrayInputStream(buffer, 0, length);
   }

  protected int extractBytes( InputStream input, byte[] buffer) throws IOException{
    int ch, cont = 0;
    for (;(ch = input.read()) > -1 && cont < buffer.length  ;cont++) {
      buffer[cont] = (byte)ch;
    }
    input.close();
    return cont;
  }





  public byte[] getBuffer() throws DownloaderException {


    return buffer;


  }





  public int getBufferSize() throws DownloaderException {


    return length;


  }





  /**


   *


   */


  public static  void main(String[] args) {

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
            Downloader down = new ExtractorProxyDownloader( new DownloaderSocket(new focusedCrawler.util.ParameterFile(args)));
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
