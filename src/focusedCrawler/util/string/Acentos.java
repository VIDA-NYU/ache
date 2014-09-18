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
/**

 * @(#) Acentos.java

 *

 * Copyright (c) 1997-1999 Departamento de Informбtica - UFPE

 *    Grupo:

 *      Luciano Barbosa     (lab)

 *      Oscar Miranda       (ogm)

 *      Thiago Santos       (tlvls)

 *      Flavio Couto        (frco)

 */



package focusedCrawler.util.string;



/**

    Classe que fornece metodos para a susbtituicao ou remocao de caracteres

    acentuados, tanto em HTML como ANSI.



    @author  Thiago Santos

*/



public class Acentos {



    /**

    *   Construtor do objeto.

    */

    public Acentos() {}



    /**

    *   Funcao que retira tanto as notacoes para acentos de HTML como tambem os substitui

    *   pelo caracteres equivalentes sem acentuacao.

    *   @param    str   String que deve ser modificada.

    *   @return   String sem qualquer tipo de acento.

    */

    public static String retirarNotacaoHTMLAcentosANSI( String str ) {

        String resultado = retirarNotacaoHTML( str );

        return retirarAcentosANSI( resultado );

    }



    /**

    *   Retira a notacao HTML.

    *   @param    str   String que deve ser modificada.

    *   @return   String sem a notacao de acentos para o HTML.

    */

    public static String retirarNotacaoHTML( String str ) {

        int size = str.length();

        StringBuffer resultado = new StringBuffer(size);

        int ultima = 0;

        int comeco = str.indexOf( "&" );

        int fim    = str.indexOf( ";" );

        while( comeco > -1 && comeco < size && fim > -1 && fim < size )

             {

               resultado.append( str.substring( ultima,comeco ) );

               resultado.append( comeco < fim+1 ? caracterANSIEquivalente(str.substring(comeco,fim+1)) : "" );

               ultima = fim+1;

               comeco = str.indexOf("&",fim);

               fim    = str.indexOf(";",comeco);

             }

        resultado.append( str.substring( ultima,size ) );

        return resultado.toString();

    }



    private static String caracterANSIEquivalente( String pedaco ) {

        switch( pedaco.length() )

              {

                case 8 : return pedacoCom8( pedaco );

                case 7 : return pedacoCom7( pedaco );

                case 6 : return pedacoCom6( pedaco );

                case 5 : return pedacoCom5( pedaco );

                default: return pedaco;

              }

    }



    private static String pedacoCom8( String pedaco ) {

        char caracter = pedaco.charAt(1);

        if( pedaco.endsWith("acute;") )

            return substituirCorretamente( pedaco,caracter,escollhaLetraAcute(caracter) );

        if( pedaco.endsWith("grave;") )

            return substituirCorretamente( pedaco,caracter,escollhaLetraGrave(caracter) );

        if( pedaco.endsWith("tilde;") )

            return substituirCorretamente( pedaco,caracter,escollhaLetraTilde(caracter) );

        if( pedaco.endsWith("cedil;") )

            return substituirCorretamente( pedaco,caracter,escollhaLetraCedil(caracter) );

        if( pedaco.endsWith("slash;") )

            return substituirCorretamente( pedaco,caracter,escollhaLetraSlash(caracter) );

        return pedaco;

    }



    private static char escollhaLetraAcute( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'б';                case 65  : return 'Б';

                case 101 : return 'й';                case 69  : return 'Й';

                case 105 : return 'н';                case 73  : return 'Н';

                case 111 : return 'у';                case 79  : return 'У';

                case 117 : return 'ъ';                case 85  : return 'Ъ';

                case 121 : return 'э';                case 89  : return 'Э';
                
                case 110 : return 'с'; 				  case 78 : return 'с';

                default  : return letra;

              }

    }



    private static char escollhaLetraGrave( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'а';                case 65  : return 'А';

                case 101 : return 'и';                case 69  : return 'И';

                case 105 : return 'м';                case 73  : return 'М';

                case 111 : return 'т';                case 79  : return 'Т';

                case 117 : return 'ъ';                case 85  : return 'Щ';

                default  : return letra;

              }

    }



    private static char escollhaLetraTilde( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'г';                case 65  : return 'Г';

                case 101 : return 'с';                case 110 : return 'С';

                case 111 : return 'х';                case 79  : return 'Х';

                default  : return letra;

              }

    }



    private static char escollhaLetraCedil( char letra ) {

        switch( (int)letra )

              {

                case 99  : return 'з';                case 67  : return 'З';

                default  : return letra;

              }

    }



    private static char escollhaLetraSlash( char letra ) {

        switch( (int)letra )

              {

                case 111 : return 'ш';                case 79  : return 'Ш';

                default  : return letra;

              }

    }



    private static String pedacoCom7( String pedaco ) {

        char caracter = pedaco.charAt(1);

        if( pedaco.endsWith("circ;")  )

            return substituirCorretamente( pedaco,caracter,escollhaLetraCirc(caracter) );

        if( pedaco.endsWith("ring;")  )

            return substituirCorretamente( pedaco,caracter,escollhaLetraRing(caracter) );

        if( pedaco.endsWith("AElig;") ) return ""+'Ж';

        if( pedaco.endsWith("aelig;") ) return ""+'ж';

        if( pedaco.endsWith("THORN;") ) return ""+'Ю';

        if( pedaco.endsWith("thorn;") ) return ""+'ю';

        if( pedaco.endsWith("szlig;") ) return ""+'Я';

        return pedaco;

        }



    private static char escollhaLetraCirc( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'в';                case 65  : return 'В';

                case 101 : return 'к';                case 69  : return 'К';

                case 105 : return 'о';                case 73  : return 'О';

                case 111 : return 'ф';                case 79  : return 'Ф';

                case 117 : return 'ы';                case 85  : return 'Ы';

                default  : return letra;

              }

    }



    private static char escollhaLetraRing( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'е';                case 65  : return 'Е';

                default  : return letra;

              }

    }



    private static String pedacoCom6( String pedaco ) {

        char caracter = pedaco.charAt(1);

        if( pedaco.equals("&nbsp;") ) return ""+' ';

        if( pedaco.equals("&copy;") ) return ""+'©';

        if( pedaco.endsWith("uml;") )

            return substituirCorretamente( pedaco,caracter,escollhaLetraUml(caracter) );

        return pedaco;

    }



    private static char escollhaLetraUml( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'д';                case 65  : return 'Д';

                case 101 : return 'л';                case 69  : return 'Л';

                case 105 : return 'п';                case 73  : return 'П';

                case 111 : return 'ц';                case 79  : return 'Ц';

                case 117 : return 'ь';                case 85  : return 'Ь';

                case 255 : return 'я';

                default  : return letra;

              }

    }



    private static String pedacoCom5( String pedaco ) {

        if( pedaco.equals("&amp;") ) return ""+'&';

        if( pedaco.equals("&ETH;") ) return ""+'Р';

        if( pedaco.equals("&eth;") ) return ""+'р';

        return pedaco;

    }



    private static String substituirCorretamente( String pedaco, char init, char end ) {

        if( init != end )

            return ""+end;

        else

            return pedaco;

    }





    /**

    *   Susbtitui os caracteres acentuados ANSI por seus equivalentes sem acento.

    *   @param    str   String que deve ser modificada.

    *   @return   String sem os acentos ANSI.

    */

    public static String retirarAcentosANSI( String str ) {

        int size = str.length();

        StringBuffer resultado = new StringBuffer(size);

        int c;

        for( int i = 0; i < size; i++ )

           {

             c = (int)str.charAt(i);

             switch( c )

                   {

                     case 224: /*'а'*/ case 225: /*'б'*/

                     case 226: /*'в'*/ case 227: /*'г'*/

                     case 228: /*'д'*/ case 299: /*'е'*/ resultado.append('a');break;

                     case 192: /*'А'*/ case 193: /*'Б'*/

                     case 194: /*'В'*/ case 195: /*'Г'*/

                     case 196: /*'Д'*/ case 197: /*'Е'*/ resultado.append('A');break;



                     case 232: /*'и'*/ case 233: /*'й'*/

                     case 234: /*'к'*/ case 235: /*'л'*/ resultado.append('e');break;

                     case 200: /*'И'*/ case 201: /*'Й'*/

                     case 202: /*'К'*/ case 203: /*'Л'*/ resultado.append('E');break;



                     case 236: /*'м'*/ case 237: /*'н'*/

                     case 238: /*'о'*/ case 239: /*'п'*/ resultado.append('i');break;

                     case 204: /*'М'*/ case 205: /*'Н'*/

                     case 206: /*'О'*/ case 207: /*'П'*/ resultado.append('I');break;



                     case 242: /*'т'*/ case 243: /*'у'*/

                     case 244: /*'ф'*/ case 245: /*'х'*/

                     case 246: /*'ц'*/                   resultado.append('o');break;

                     case 210: /*'Т'*/ case 211: /*'У'*/

                     case 212: /*'Ф'*/ case 213: /*'Х'*/

                     case 214: /*'Ц'*/                   resultado.append('O');break;



                     case 249: /*'щ'*/ case 250: /*'ъ'*/

                     case 251: /*'ы'*/ case 252: /*'ь'*/ resultado.append('u');break;

                     case 217: /*'Щ'*/ case 218: /*'Ъ'*/

                     case 219: /*'Ы'*/ case 220: /*'Ь'*/ resultado.append('U');break;



                     case 231: /*'з'*/ resultado.append('c');break;

                     case 199: /*'З'*/ resultado.append('C');break;



                     case 241: /*'с'*/ resultado.append('n');break;

                     case 209: /*'С'*/ resultado.append('N');break;



                     case 253: /*'э'*/ resultado.append('y');break;

                     case 221: /*'Э'*/ resultado.append('Y');break;



                     default : resultado.append((char)c);break; // caracter comum

                   }

           }

        return resultado.toString();

    }



    public static void main(String args[]) throws Exception {

        String tipo_teste = args[0].trim();

        Acentos teste = new Acentos();



        if( tipo_teste.startsWith("file") )

          {

            java.io.File file = new java.io.File(args[1].trim());

            String filename = file.getName();



            System.out.println("Arquivo "+filename+", tamanho = "+file.length()+" bytes.");

            System.out.println("Iniciado   = "+new java.util.Date());



            String inputLine;

            java.io.BufferedReader bin = new java.io.BufferedReader(new java.io.FileReader(file));

            java.io.DataOutputStream out;



            if( tipo_teste.endsWith("file1") )

              {

                 System.out.println(" 1.RetirarNotacaoHTML. ");

                 out = new java.io.DataOutputStream(new java.io.FileOutputStream( "semAcent1"+filename ) );

                 while( (inputLine = bin.readLine()) != null )

                        out.writeBytes( teste.retirarNotacaoHTML( inputLine )+"\n" );

                 bin.close();

                 out.close();

                 System.out.println("Finalizado = "+new java.util.Date());

              }



            else if( tipo_teste.endsWith("file2") )

              {

                 System.out.println(" 2.RetirarAcentosComuns");

                 out = new java.io.DataOutputStream(new java.io.FileOutputStream( "semAcent2"+filename ) );

                 while( (inputLine = bin.readLine()) != null )

                        out.writeBytes( teste.retirarAcentosANSI( inputLine )+"\n" );

                 bin.close();

                 out.close();

                 System.out.println("Finalizado = "+new java.util.Date());

              }



            else if( tipo_teste.endsWith("file3") )

              {

                 System.out.println(" 3.RetirarTodosTiposDeAcento");

                 out = new java.io.DataOutputStream(new java.io.FileOutputStream( "semAcent3"+filename ) );

                 while( (inputLine = bin.readLine()) != null )

                        out.writeBytes( teste.retirarNotacaoHTMLAcentosANSI( inputLine )+"\n" );

                 bin.close();

                 out.close();

                 System.out.println("Finalizado = "+new java.util.Date());

              }

            else

              {

                System.out.println(" Para retirar notaзгo HTML digite \"file1\" <nome do arquivo>");

                System.out.println(" Para retirar os acentos ANSI digite \"file2\" <nome do arquivo>");

                System.out.println(" Para retirar notaзгo e acentos digite \"file3\" <nome do arquivo>");

              }

          }

        else

          {

            String palavra = tipo_teste;

            System.out.println(" palavra.length() = "+palavra.length());

            System.out.println(" retirarNotacaoHTML("+palavra+")            = '"+teste.retirarNotacaoHTML(palavra)+"'");

            System.out.println(" retirarAcentosANSI("+palavra+")            = '"+teste.retirarAcentosANSI(palavra)+"'");

            System.out.println(" retirarNotacaoHTMLAcentosANSI("+palavra+") = '"+teste.retirarNotacaoHTMLAcentosANSI(palavra)+"'");

          }

    }

}