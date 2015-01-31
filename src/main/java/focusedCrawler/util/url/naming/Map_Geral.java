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



import java.util.Vector;

import focusedCrawler.util.ParameterFile;






public class Map_Geral extends Map {



    /**

     Mapeamento utilizado pela indexacao para a colocacao na url de informa��es como:

     codigo da url, tipo da url, data de modificacao, data de visitada, contador de atualizacaoes, nota do link para imagem,nota do link para Lingua Portuguesa

     Ex.: "http://www.di.ufpe.br##cod^123#tip^2"



    */

    public Map_Geral () {

    } //Map_Geral



    public Map_Geral(ParameterFile config) {

        String[] types = config.getParam("MAP_TYPES"," ");

        String[] names = config.getParam("MAP_NAMES"," ");

        String[] ids = new String [names.length];

        for (int i = 0; i < names.length; i++) {

            ids [i] = new Integer (i).toString();

        } //for

        setIds(ids);

        setNames(names);

        setTypes(types);

    } //Map_Geral



    public static void main(String args[]) {

        try {

              URL_Resolver resolver = new URL_Resolver(new ParameterFile(args [0]));

              java.net.URL url = new java.net.URL(args[1].trim());

              Object objs[] = new Object[]{

                                            Integer.valueOf(args[2].trim()),

                                            Integer.valueOf(args[3].trim()),

                                            null,//Long.valueOf(args[3].trim()),

                                            null,//Long.valueOf(args[4].trim()),

                                            null,//Integer.valueOf(args[5].trim()),

                                            null,//Integer.valueOf(args[6].trim()),

                                            null,//Integer.valueOf(args[7].trim()),

                                            null,

                                            null

                                          };



              Map map = resolver.getMap();

              String ids[]   = map.getIds();

              String names[] = map.getNames();



              url = resolver.make(url,objs);

              System.out.println("1.MAKE = "+url);

              System.out.println("1.GET  = "+resolver.getURL(url));

              System.out.println("1.GETURL OK");

/*

              resolver.resolve(url);

              System.out.println("1.RESOLVE OK");

              for( int i = 0; i < names.length; i++ )

                   System.out.println("getName("+names[i]+") = "+resolver.getName(names[i]));

              for( int i = 0; i < names.length; i++ )

                   System.out.println("getId("+ids[i]+") = "+resolver.getId(ids[i]));

*/



              System.out.println("1.RESOLVE OK");

              resolver.setModo(URL_Resolver.MODO_NAME);

              System.out.println("1.RESOLVE OK");



              url = resolver.make(url,objs);

              System.out.println("2.MAKE = "+url);

              System.out.println("2.GET  = "+resolver.getURL(url));

              System.out.println("2.RESOLVE OK");

              resolver.resolve(url);

              for( int i = 0; i < names.length; i++ )

                   System.out.println("getName("+names[i]+") = "+resolver.getName(names[i]));

              for( int i = 0; i < names.length; i++ )

                   System.out.println("getId("+ids[i]+") = "+resolver.getId(ids[i]));

            }

        catch( Exception exc )

            { exc.printStackTrace();

            }

    }

}
