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

import java.net.URLDecoder;

import java.net.URLEncoder;

import java.net.MalformedURLException;

import java.util.Hashtable;

import java.util.Vector;

import java.util.Enumeration;

import focusedCrawler.util.ParameterFile;






public class URL_Resolver {



//--- separadores utilizados na URL modificada

        public static final String SEPARADOR_INICIO = "##";

        public static final char   SEPARADOR_PARES  = '#';

        public static final char   SEPARADOR_TIPOS  = '^';



//--- modo de construcao da URL, MODO_ID para URLs com identificadores e MODO_NAME para o nome do parametros.

        public static final int    MODO_ID          = 1;

        public static final int    MODO_NAME        = MODO_ID + 1;



//--- Mapeamento utlizado para a construcao dos elementos.

        protected Map       map;



//--- Modo utilizado

        protected int       modo;

        protected Hashtable hash_ids;   // mapeamento entre os identificadores e o objeto correpondente.

        protected Hashtable hash_names; // mapeamento entre os nome e o objeto correpondente.



    public URL_Resolver(ParameterFile config) {

        this(new Map_Geral(config),MODO_NAME);

    }



    public URL_Resolver(Map map) {

        this(map,MODO_NAME);

    }



    public URL_Resolver(Map map,int modo) {

        this.map  = map;

        this.modo = modo;

    }



    public void setMap(Map map) {

        this.map = map;

    }

    public Map getMap() {

        return map;

    }



    public int getModo() {

        return modo;

    }

    public void setModo(int modo) {

        this.modo = modo;

    }



    /**

     * Constroi a nova URL agregando os dados que estao no URL_Resolver

     */

    public URL make(URL base) throws MalformedURLException {

        resolve(base,false);

        Vector items = new Vector();

        String[] loop = map.getNames();

        for (int i = 0; i < loop.length; i++) {

            items.add(hash_names.get(loop[i]));

        }

        Object[] objs = new Object[items.size()];

        items.copyInto(objs);

        return make( getURL(base), objs);

    }



    /**

    *   Constroi a nova URL agregando os dados complementares do array de objetos.

    *   @param base URL que servira do partida.

    *   @param values Objetos complementares que deverao esr agregados a url.

    *   @return Uma URL modificada com os novos parametros.

    */

    public URL make( URL base,Object[] values ) throws MalformedURLException {

//        System.out.println("BASE::"+base.toString());

        String[] prefixos = null;

        if( modo == MODO_ID )

            prefixos = map.getIds();

        else

            prefixos = map.getNames();

        URL resultado = base;

        if( values != null )

          {

            StringBuffer complemento = new StringBuffer(6 * values.length);

            for( int i = 0; i < values.length; i++ )

               {

                 if( values[i] != null )

                   {

                     if( i == 0 )

                          complemento.append(SEPARADOR_INICIO).append(prefixos[i]).append(SEPARADOR_TIPOS).append(map.formatString(values[i]));

                     else

                          complemento.append(SEPARADOR_PARES).append(prefixos[i]).append(SEPARADOR_TIPOS).append(map.formatString(values[i]));

                   }

               }

            complemento.append(SEPARADOR_PARES);

            String urlTemp = base.toString();

//            System.out.println("BASE::"+urlTemp);

            int end = urlTemp.indexOf("#");

            if(end != -1){

                urlTemp = urlTemp.substring(0,end);

//                System.out.println("urlTemp::"+urlTemp);

//                System.out.println("REF::"+base.getRef());

//                complemento.append(URLEncoder.encode(base.getRef()));

                complemento.append(base.getRef());

            }

            resultado = new URL( urlTemp + complemento.toString());

            //resultado = new URL( base, complemento.toString() );

          }

        //com.radix.util.Log.log("URL_Resolver","make","result = '"+resultado+"'");

        return resultado;

    }



    /**

    *   Retorna o URL original que foi utilizada como base.

    *   @param url A URL modificada como a agregacao de informacoes.

    *   @return A URL original, antes de terem sido agregadas novas informacoes.

    */

    public URL getURL(URL url) throws MalformedURLException {

        URL result = null;

        String str = getStringURL(url);

        if( str != null ) {

            result = new URL(str);

        } //if

        return result;

    } //getURL



    public String getStringURL(URL url) {

        if( url == null )

            return null;

        String str = url.toString();

        int pos = str.indexOf(SEPARADOR_INICIO);

        if( pos > 0 ) {

            return str.substring(0,pos);

        }

        else {

            return url.getProtocol()+"://"+url.getHost()+url.getFile();

        }

    } //getURL



    /**

    *   Identifica os parametros agregados para disponibiliza-los aos metodos,

    *   <code>getId(<identificador>)</code> e <code>getName(<nome do parametro>)</code>.

    */

    public synchronized void resolve( URL url ) {

        resolve(url,true);

    }

    private synchronized void resolve( URL url, boolean clear ) {

        if( url == null )

          {

            hash_ids   = null;

            hash_names = null;

            return;

          }

        String urlOri = url.toString();

//        System.out.println("URL1::"+urlOri);

        String str_url = URLDecoder.decode(urlOri);

//        System.out.println("URL2::"+str_url);

        int pos_inicio = str_url.indexOf(SEPARADOR_INICIO);

        if( pos_inicio < 0 )

            return;

        //System.out.println("URL" + str_url);

        pos_inicio = pos_inicio + SEPARADOR_INICIO.length();

        int pos_tip = str_url.indexOf(SEPARADOR_TIPOS,pos_inicio);

        if( pos_tip < 0 )

            return;

        //System.out.println("DEBUG1");

        int pos_par = str_url.indexOf(SEPARADOR_PARES,pos_tip+1);

        if( pos_par < 0 )

            return;

        //System.out.println("DEBUG2");

        if( clear ) {

            hash_ids   = null;

            hash_names = null;

            hash_ids   = new Hashtable(9);

            hash_names = new Hashtable(9);

        }



        int pos_atual = pos_inicio;

        String key,valor;

        Object aux  = null;

        while( pos_tip > 0 && pos_par > 0 )

             {

               key   = str_url.substring(pos_atual,pos_tip);

               valor = str_url.substring(pos_tip+1,pos_par);

//               System.out.println("KEY = "+key+", VALOR = "+valor);

               if( modo == MODO_ID )

                 {

                   aux = map.parseForId(key,valor);

                   if( aux != null )

                     {

                       hash_ids.put(key,aux);

                       hash_names.put(map.getNameForId(key),aux);

                     }

                 }

               else

                 {

                   aux = map.parseForName(key,valor);

                   if( aux != null )

                     {

                       hash_ids.put(map.getIdForName(key),aux);

                       hash_names.put(key,aux);

                     }

                 }

               pos_atual = pos_par + 1;

               pos_tip   = str_url.indexOf(SEPARADOR_TIPOS,pos_atual);

               pos_par   = str_url.indexOf(SEPARADOR_PARES,pos_tip+1);

             }

        //com.radix.util.Log.log("URL_Resolver","resolve","hash_ids = '"+hash_ids+"', hash_names = "+hash_names);

    }



    /**

    *   Dado o numero de identificacao da informacao agregada, retorna o objeto correspondente.

    *   Obs:primeiro deve-se chamar o metodo <code>resolve(<URL desejada>)</code>.

    *   @param id Identificador do paramentro.

    */

    public Object getId(String id) {

        return hash_ids.get(id);

    }



    /**

    *   Dado o numero de identificacao da informacao agregada, retoran o objeto correspondente.

    *   Obs:primeiro deve-se chamar o metodo <code>resolve(<URL desejada>)</code>.

    *   @param id Identificador do paramentro.

    */

    public Object getName(String name) {

        return hash_names.get(name);

    }



    public java.util.Enumeration getNames() {

        return hash_names.keys();

    }



    private int  ERRO_INT  = -1;

    private long ERRO_LONG = -1;

    public int getInt(String name) {

        return ( getName(name) != null ? ((Integer)getName(name)).intValue() : ERRO_INT );

    }



    public long getLong(String name) {

        return ( getName(name) != null ? ((Long)getName(name)).longValue() : ERRO_LONG );

    }



    public String getQuery(URL url) {

        String str = url.toString();

        return ( str.indexOf("##") > 0 ? str.substring(0,str.indexOf("##")) : "");

    }



    public void clear() {

        if( hash_ids != null ) {

            hash_ids.clear();

        }

        if( hash_names != null ) {

            hash_names.clear();

        }

    }



    public static void main(String args[]) {

        try {

              ParameterFile config = new ParameterFile (args [0]);

              URL_Resolver resolver = new URL_Resolver(config);

              URL url = new URL(args[1].trim());

              System.out.println("1."+url);

              resolver.clear();

              resolver.resolve(url);

              java.util.Enumeration keys = resolver.getNames();

              String name;

              while (keys.hasMoreElements()) {

                  name = (String)keys.nextElement();

                  System.out.println("getName("+name+")="+resolver.getName(name));

              } //while



            }

        catch( MalformedURLException mfue )

            {

              mfue.printStackTrace();

            }

    }

}
