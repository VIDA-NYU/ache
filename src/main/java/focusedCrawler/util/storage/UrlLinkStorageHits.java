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
package focusedCrawler.util.storage;





import java.net.URL;

import java.net.URLEncoder;

import java.util.Vector;

import java.util.StringTokenizer;



public class UrlLinkStorageHits {



  private String origem;

  private String prefix;

  private URL [] urls;

  private Vector urlsString;



  /**

   * Quando esta variavel eh true significa que os links do documento

   * origem estao na forma de objetos URL!

   */

  private boolean objUrl;



  public UrlLinkStorageHits(String origem, URL [] urls, String prefix) {

    this.origem = origem;

    this.urls = urls;

    objUrl = true;

    this.prefix = prefix;

  }



  public UrlLinkStorageHits(String origem, Vector urlsString) {

    this.origem = origem;

    this.urlsString = urlsString;

    objUrl = false;

  }



//  public String toString(){

//

//      StringBuffer urlsSB = new StringBuffer();

//      int size =0;

//      System.out.println("(@) PREFIXX : "+this.prefix);

//      urlsSB.append(this.prefix);

//

//      if(objUrl){

//        size =  urls.length;

//        urlsSB.append(origem);

//        for(int i=0; i<size; i++){

//           urlsSB.append("|");

//           urlsSB.append(urls[i].toString());

//        }

//      }else{

//        size = urlsString.size();

//        for(int i=0; i<size; i++){

//           urlsSB.append("|");

//           urlsSB.append(urlsString.elementAt(i));

//        }

//      }

//      return urlsSB.toString();

//  }



public String toStringEncoder(){



      StringBuffer urlsSB = new StringBuffer();

      int size = 0;

      //System.out.println("(@) PREFIXX : "+this.prefix);

      //System.out.println("(@) ORIGEM : "+this.origem);

      urlsSB.append(this.prefix);

      urlsSB.append(origem);

      if(objUrl){

        size = urls.length;

        for(int i=0; i<size; i++){

           urlsSB.append("|");

           urlsSB.append(superTrim(URLEncoder.encode(urls[i].toString())));

        }

      }else{

        size = urlsString.size();

        for(int i=0; i<size; i++){

           urlsSB.append("|");

           urlsSB.append(superTrim(URLEncoder.encode((String)urlsString.elementAt(i))));

        }



      }

        return urlsSB.toString();

  }



  private String superTrim(String entrada){



    if(entrada.indexOf('\n') != -1 ){

      StringTokenizer saida = new StringTokenizer(entrada, "\n\t\r");

      StringBuffer sb = new StringBuffer();

      while(saida.hasMoreTokens()){

         sb.append(saida.nextToken());

      }

      return sb.toString();

    }else{

      return entrada;

    }



  }



}