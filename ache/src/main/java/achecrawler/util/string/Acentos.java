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

 * Copyright (c) 1997-1999 Departamento de Informática - UFPE

 *    Grupo:

 *      Luciano Barbosa     (lab)

 *      Oscar Miranda       (ogm)

 *      Thiago Santos       (tlvls)

 *      Flavio Couto        (frco)

 */



package achecrawler.util.string;



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

                case 97  : return 'á';                case 65  : return 'Á';

                case 101 : return 'é';                case 69  : return 'É';

                case 105 : return 'í';                case 73  : return 'Í';

                case 111 : return 'ó';                case 79  : return 'Ó';

                case 117 : return 'ú';                case 85  : return 'Ú';

                case 121 : return 'ý';                case 89  : return 'Ý';
                
                case 110 : return 'ñ'; 				  case 78 : return 'ñ';

                default  : return letra;

              }

    }



    private static char escollhaLetraGrave( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'à';                case 65  : return 'À';

                case 101 : return 'è';                case 69  : return 'È';

                case 105 : return 'ì';                case 73  : return 'Ì';

                case 111 : return 'ò';                case 79  : return 'Ò';

                case 117 : return 'ú';                case 85  : return 'Ù';

                default  : return letra;

              }

    }



    private static char escollhaLetraTilde( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'ã';                case 65  : return 'Ã';

                case 101 : return 'ñ';                case 110 : return 'Ñ';

                case 111 : return 'õ';                case 79  : return 'Õ';

                default  : return letra;

              }

    }



    private static char escollhaLetraCedil( char letra ) {

        switch( (int)letra )

              {

                case 99  : return 'ç';                case 67  : return 'Ç';

                default  : return letra;

              }

    }



    private static char escollhaLetraSlash( char letra ) {

        switch( (int)letra )

              {

                case 111 : return 'ø';                case 79  : return 'Ø';

                default  : return letra;

              }

    }



    private static String pedacoCom7( String pedaco ) {

        char caracter = pedaco.charAt(1);

        if( pedaco.endsWith("circ;")  )

            return substituirCorretamente( pedaco,caracter,escollhaLetraCirc(caracter) );

        if( pedaco.endsWith("ring;")  )

            return substituirCorretamente( pedaco,caracter,escollhaLetraRing(caracter) );

        if( pedaco.endsWith("AElig;") ) return ""+'Æ';

        if( pedaco.endsWith("aelig;") ) return ""+'æ';

        if( pedaco.endsWith("THORN;") ) return ""+'Þ';

        if( pedaco.endsWith("thorn;") ) return ""+'þ';

        if( pedaco.endsWith("szlig;") ) return ""+'ß';

        return pedaco;

        }



    private static char escollhaLetraCirc( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'â';                case 65  : return 'Â';

                case 101 : return 'ê';                case 69  : return 'Ê';

                case 105 : return 'î';                case 73  : return 'Î';

                case 111 : return 'ô';                case 79  : return 'Ô';

                case 117 : return 'û';                case 85  : return 'Û';

                default  : return letra;

              }

    }



    private static char escollhaLetraRing( char letra ) {

        switch( (int)letra )

              {

                case 97  : return 'å';                case 65  : return 'Å';

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

                case 97  : return 'ä';                case 65  : return 'Ä';

                case 101 : return 'ë';                case 69  : return 'Ë';

                case 105 : return 'ï';                case 73  : return 'Ï';

                case 111 : return 'ö';                case 79  : return 'Ö';

                case 117 : return 'ü';                case 85  : return 'Ü';

                case 255 : return 'ÿ';

                default  : return letra;

              }

    }



    private static String pedacoCom5( String pedaco ) {

        if( pedaco.equals("&amp;") ) return ""+'&';

        if( pedaco.equals("&ETH;") ) return ""+'Ð';

        if( pedaco.equals("&eth;") ) return ""+'ð';

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

                     case 224: /*'à'*/ case 225: /*'á'*/

                     case 226: /*'â'*/ case 227: /*'ã'*/

                     case 228: /*'ä'*/ case 299: /*'å'*/ resultado.append('a');break;

                     case 192: /*'À'*/ case 193: /*'Á'*/

                     case 194: /*'Â'*/ case 195: /*'Ã'*/

                     case 196: /*'Ä'*/ case 197: /*'Å'*/ resultado.append('A');break;



                     case 232: /*'è'*/ case 233: /*'é'*/

                     case 234: /*'ê'*/ case 235: /*'ë'*/ resultado.append('e');break;

                     case 200: /*'È'*/ case 201: /*'É'*/

                     case 202: /*'Ê'*/ case 203: /*'Ë'*/ resultado.append('E');break;



                     case 236: /*'ì'*/ case 237: /*'í'*/

                     case 238: /*'î'*/ case 239: /*'ï'*/ resultado.append('i');break;

                     case 204: /*'Ì'*/ case 205: /*'Í'*/

                     case 206: /*'Î'*/ case 207: /*'Ï'*/ resultado.append('I');break;



                     case 242: /*'ò'*/ case 243: /*'ó'*/

                     case 244: /*'ô'*/ case 245: /*'õ'*/

                     case 246: /*'ö'*/                   resultado.append('o');break;

                     case 210: /*'Ò'*/ case 211: /*'Ó'*/

                     case 212: /*'Ô'*/ case 213: /*'Õ'*/

                     case 214: /*'Ö'*/                   resultado.append('O');break;



                     case 249: /*'ù'*/ case 250: /*'ú'*/

                     case 251: /*'û'*/ case 252: /*'ü'*/ resultado.append('u');break;

                     case 217: /*'Ù'*/ case 218: /*'Ú'*/

                     case 219: /*'Û'*/ case 220: /*'Ü'*/ resultado.append('U');break;



                     case 231: /*'ç'*/ resultado.append('c');break;

                     case 199: /*'Ç'*/ resultado.append('C');break;



                     case 241: /*'ñ'*/ resultado.append('n');break;

                     case 209: /*'Ñ'*/ resultado.append('N');break;



                     case 253: /*'ý'*/ resultado.append('y');break;

                     case 221: /*'Ý'*/ resultado.append('Y');break;



                     default : resultado.append((char)c);break; // caracter comum

                   }

           }

        return resultado.toString();

    }



    public static void main(String args[]) throws Exception {

        String tipo_teste = args[0].trim();

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

                        out.writeBytes( Acentos.retirarNotacaoHTML( inputLine )+"\n" );

                 bin.close();

                 out.close();

                 System.out.println("Finalizado = "+new java.util.Date());

              }



            else if( tipo_teste.endsWith("file2") )

              {

                 System.out.println(" 2.RetirarAcentosComuns");

                 out = new java.io.DataOutputStream(new java.io.FileOutputStream( "semAcent2"+filename ) );

                 while( (inputLine = bin.readLine()) != null )

                        out.writeBytes( Acentos.retirarAcentosANSI( inputLine )+"\n" );

                 bin.close();

                 out.close();

                 System.out.println("Finalizado = "+new java.util.Date());

              }



            else if( tipo_teste.endsWith("file3") )

              {

                 System.out.println(" 3.RetirarTodosTiposDeAcento");

                 out = new java.io.DataOutputStream(new java.io.FileOutputStream( "semAcent3"+filename ) );

                 while( (inputLine = bin.readLine()) != null )

                        out.writeBytes( Acentos.retirarNotacaoHTMLAcentosANSI( inputLine )+"\n" );

                 bin.close();

                 out.close();

                 System.out.println("Finalizado = "+new java.util.Date());

              }

            else

              {

                System.out.println(" Para retirar notação HTML digite \"file1\" <nome do arquivo>");

                System.out.println(" Para retirar os acentos ANSI digite \"file2\" <nome do arquivo>");

                System.out.println(" Para retirar notação e acentos digite \"file3\" <nome do arquivo>");

              }

          }

        else

          {

            String palavra = tipo_teste;

            System.out.println(" palavra.length() = "+palavra.length());

            System.out.println(" retirarNotacaoHTML("+palavra+")            = '"+Acentos.retirarNotacaoHTML(palavra)+"'");

            System.out.println(" retirarAcentosANSI("+palavra+")            = '"+Acentos.retirarAcentosANSI(palavra)+"'");

            System.out.println(" retirarNotacaoHTMLAcentosANSI("+palavra+") = '"+Acentos.retirarNotacaoHTMLAcentosANSI(palavra)+"'");

          }

    }

}