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
package focusedCrawler.util.url.naming;



import java.net.URL;

import java.net.MalformedURLException;



public class URL_MAKER {

        protected static final char   COD = '#';

        protected static final char   TIP = '#';

        protected static final String END = "##";



    public static URL make( URL base,int codigo,int tipo ) throws MalformedURLException {

        if( base == null )

            return null;

        return new URL(base,""+COD+codigo+TIP+tipo);

//        return new URL(base,""+COD+codigo+TIP+tipo+END);

    }



    public static String make( String base,int codigo,int tipo ) {

        return base+COD+codigo+TIP+tipo;

//        return base+COD+codigo+TIP+tipo+END;

    }



    public static URL getURL(URL url) throws MalformedURLException {

        if( url == null )

            return null;

        String str = url.toString();

        int pos = str.indexOf(END);

        if( pos > 0 ) {

            return new URL( str.substring(0,pos) );

        }

        else {

            return new URL(url.getProtocol()+"://"+url.getHost()+url.getFile());

        }

    }



    public static int[] getCodigoETipo(URL url) {

        if( url == null )

            return null;

        String ref = url.getRef();



        int pos_tip = ref.indexOf(TIP);

        int pos_end = ref.indexOf(END,pos_tip+1);

        if( pos_tip > 0 )

          {

            int x[] = null;

            try {

                  String cod = ref.substring(0,pos_tip);

                  String tip = ref.substring(pos_tip+1,ref.length());

/*

                  String tip;

                  if( pos_end > 0 )

                      tip = ref.substring(pos_tip+1,pos_end);

                  else

                      tip = ref.substring(pos_tip+1,ref.length());

*/

                  x = new int[]{ Integer.valueOf(cod).intValue(),Integer.valueOf(tip).intValue() };

                }

            catch(Exception exc)

                {

                  x = null;

                }

            return x;

          }

        return null;

    }



    public static void main(String args[]) {

        try {

              long time = System.currentTimeMillis();

              URL url = new URL(args[0].trim());

              System.out.println("parser_url_time = "+(System.currentTimeMillis()-time));

              long total_io = 0;

              long time_io  = System.currentTimeMillis();

              System.out.println("1."+url);

              total_io += (System.currentTimeMillis()-time_io);



              time = System.currentTimeMillis();

              url = URL_MAKER.make(url,Integer.valueOf(args[1].trim()).intValue(),Integer.valueOf(args[2].trim()).intValue());

              time    = (System.currentTimeMillis()-time);

              time_io = System.currentTimeMillis();

              System.out.println("2."+url+", time = "+time);

              total_io += (System.currentTimeMillis()-time_io);



              time = System.currentTimeMillis();

              int[] ct = URL_MAKER.getCodigoETipo(url);

              time = (System.currentTimeMillis()-time);

              if( ct!=null )

                {

                  time_io = System.currentTimeMillis();

                  System.out.println("2.cod = "+ct[0]+", tip = "+ct[1]+", time = "+time);

                  total_io += (System.currentTimeMillis()-time_io);

                }

              else

                {

                  time_io = System.currentTimeMillis();

                  System.out.println("2.cod e tip = null");

                  total_io += (System.currentTimeMillis()-time_io);

                }



              time = System.currentTimeMillis();

              url = URL_MAKER.getURL(url);

              time = (System.currentTimeMillis()-time);

              time_io = System.currentTimeMillis();

              System.out.println("3."+url+", time = "+time);

              total_io += (System.currentTimeMillis()-time_io);

              System.out.println("4.total_io = "+total_io);

            }

        catch( MalformedURLException mfue )

            {

              mfue.printStackTrace();

            }

    }



}
