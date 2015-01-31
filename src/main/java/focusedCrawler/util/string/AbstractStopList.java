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
/*

 * @(#)AbstractStopList.java

 *

 * Copyright (c) 1997-1999 Departamento de Inform�tica - UFPE

 *    Grupo:

 *      Luciano de A. Barbosa           (lab)

 *      Oscar G. de Miranda             (ogm)

 *      Thiago L.V.L. Santos          (tlvls)

 *      Flavio Couto                   (frco)

 */



package focusedCrawler.util.string;



public abstract class AbstractStopList implements StopList {



        protected String excecoes[]       = null;

        protected String irrelevantes[]   = null;

        protected String complementares[] = null;

        protected String prefixos[]       = null;

        protected String sufixos[]        = null;



        protected static final int MIN_LENGTH = 2;

        protected static final int MAX_LENGTH = 30;



        protected static final int MIN_NUM_LENGTH = 2;

        protected static final int MAX_NUM_LENGTH = 30;



    public AbstractStopList( ) {

        this( new String[0],new String[0],new String[0],new String[0],new String[0],true );

    }



    public AbstractStopList( String excecoes[],String irrelevantes[] ) {

        this( excecoes,irrelevantes,new String[0],new String[0],new String[0],true );

    }



    public AbstractStopList( String excecoes[],String irrelevantes[],String complementares[] ) {

        this( excecoes,irrelevantes,complementares,new String[0],new String[0],true );

    }



    public AbstractStopList( String excecoes[],String irrelevantes[],String complementares[],String prefixos[],String sufixos[] ) {

        this( excecoes,irrelevantes,complementares,prefixos,sufixos,true );

    }



    public AbstractStopList( String excecoes[],String irrelevantes[],String complementares[],String prefixos[],String sufixos[],boolean realizarQuickSort ) {

        this.excecoes       = excecoes;

        this.irrelevantes   = irrelevantes;

        this.complementares = complementares;

        this.prefixos       = prefixos;

        this.sufixos        = sufixos;

        if( realizarQuickSort ) realizarQuickSort();

    }



    public synchronized String[] getExcecoes() {

        return excecoes;

    }

    public synchronized void setExcecoes( String array[] ) {

        excecoes = array;

        if( excecoes != null )

            quickSortString( excecoes );

    }



    public synchronized String[] getIrrelevantes() {

        return irrelevantes;

    }

    public synchronized void setIrrelevantes( String array[] ) {

        irrelevantes = array;

        if( irrelevantes != null )

            quickSortString( irrelevantes );

    }



    public synchronized String[] getComplementares() {

        return complementares;

    }

    public synchronized void setComplementares( String array[] ) {

        complementares = array;

        if( complementares != null )

            quickSortString( complementares );

    }



    public synchronized String[] getPrefixos() {

        return prefixos;

    }

    public synchronized void setPrefixos( String array[] ) {

        prefixos = array;

        if( prefixos != null )

            quickSortString( prefixos );

    }



    public synchronized String[] getSufixos() {

        return sufixos;

    }

    public synchronized void setSufixos( String array[] ) {

        sufixos = array;

        if( sufixos != null )

            quickSortString( sufixos );

    }



    protected synchronized void realizarQuickSort() {

        if( excecoes != null )

            quickSortString( excecoes );

        if( irrelevantes != null )

            quickSortString( irrelevantes );

        if( complementares != null )

            quickSortString( complementares );

        if( prefixos != null )

            quickSortString( prefixos );

        if( sufixos != null )

            quickSortString( sufixos );

    }



    protected synchronized void quickSortString( String str[] ) {

        quicksort_str( str,0,str.length-1 );

    }

    protected synchronized void quicksort_str( String str[],int left,int right ) {

        String pivot;

        int l = left;

        int r = right;

        if(left<right)

          {

            pivot = str[(left+right)/2];

            while(l<=r)

                 {

                   while( str[l].compareTo( pivot ) < 0 & l < right ) l++;

                   while( str[r].compareTo( pivot ) > 0 & r > left  ) r--;

                   if(l<=r)

                     {

                       troque( str,l,r );

                       l++;

                       r--;

                     }

                 }

            if(left<r)

               quicksort_str( str,left,r );

            if(l<right)

               quicksort_str( str,l,right );

          }

    }

    // Funcao auxiliar do quicksort

    protected synchronized void troque( String str[],int l,int r ) {

        String temp;

        temp   = str[l];

        str[l] = str[r];

        str[r] = temp;

    }


    public boolean isStopWord(String word){
    	boolean isStop = false;
    	word = word.trim();
        int size = word.length();
        if( size < MIN_LENGTH || size > MAX_LENGTH ){
        	return true;
        }
   	   	if(pertenceAoArray(word,irrelevantes)){
   	   		return true;
   	   	}
   	   	char first = word.charAt(0);
   	   	if(eNumero(first) || first < 48 || (first > 57 && first < 65) || (first > 90 && first < 97) || (first > 123 && first < 128)){
           return true;
   	   	}
   	   	return isStop;
    }

    public synchronized boolean eIrrelevante( String palavra) {

        if( palavra == null )

            return true;

        palavra = palavra.trim();

        int size = palavra.length();

        if( pertenceAoArray( palavra,excecoes ))
            return false;

        if( size < MIN_LENGTH || size > MAX_LENGTH )
            return true;

        char first = palavra.charAt(0);

//        if( !(eNumero(first) || eLetra(first)) )
//
//            return true;
//
//        char last  = palavra.charAt(size-1);
//
//        if( !(eNumero(last) || eLetra(last)) )
//
//            return true;

        if( apenasNumero(palavra) && (size < MIN_NUM_LENGTH || size > MAX_NUM_LENGTH) )

            return true;

        if( apenasHifen(palavra) )

            return true;

        if( apenasNumeroEHifen(palavra) )

            return true;

        if( possuiCaracteresIrrelevantes( palavra ) )

            return true;

        if( pertenceAoArray( palavra,irrelevantes ) )

            return true;

        if( pertenceAoArray( palavra,complementares ) )

            return true;

        if( possuiPrefixos( palavra,prefixos ) )

            return true;

        if( possuiSufixos( palavra,sufixos ) )

            return true;

        return false;

    }



    /**

    *   Indica se uma string e formada apenas por numeros.

    */

    public synchronized boolean apenasNumero( String palavra ) {

        boolean numero = true;

        int i = 0; char c;

        int size = palavra.length();

        while( i < size && numero ) {

            c = palavra.charAt(i);

            numero = numero && eNumero(c);

            i++;

        }

        return numero;

    }



    /**

    *   Indica se uma string e formada apenas por hifens.

    */

    public synchronized boolean apenasHifen( String palavra ) {

        boolean hifen = true;

        int i = 0; char c;

        int size = palavra.length();

        while( i < size && hifen ) {

            c = palavra.charAt(i);

            hifen = hifen && (c == '-');

            i++;

        }

        return hifen;

    }



    /**

    *   Tenta identificar um conjunto de caracteres, no caso os numeros.

    *   >48 e <57   para 0..9

    */

    public synchronized boolean eNumero( char c ) {

        int value = (int) c;

        return ( 48 <= value && value <= 57 );

    }



    /**

    *   Tenta identificar um conjunto de caracteres, no caso as letras do alfabeto.

    *   >65  e <90   para a..z

    *   >97  e <122  para A..Z

    */

    public synchronized boolean eLetra( char c ) {

        int value = (int)(Acentos.retirarAcentosANSI(""+c).charAt(0));

        return ( 65 <= value && value <= 90 ) || ( 97 <= value && value <= 122 );

    }



    /**

    *   Indica se a palavra e formada apenas por numeros e hifens, sendo assim capaz

    *   de identificar os numero de telefone, CPF, CEP, etc.

    */

    public synchronized boolean apenasNumeroEHifen( String palavra ) {

        boolean numero = false,hifen  = false,outro  = false;

        int i = 0; char c;

        int size = palavra.length();

        while( i < size )

             {

               c = palavra.charAt(i);

               if( eNumero(c) )

                   numero = !outro;

               else if( c == '-' )

                   hifen  = !outro;

               else

                 {

                   numero = false;

                   hifen  = false;

                   outro  = true;

                 }

               i++;

             }

        return numero || hifen;

    }



    /**

    *   Tenta identifica palavras com caracteres "malucos" deixando apenas as letras, numeros e hifens.

    *   !=45        para ignorar os hifens

    *   <48         para caracteres n�o visualiz�veis e !"#$%&'()*+,./

    *   >57  e <65  para :;<=>?@

    *   >90  e <97  para [\]^_`

    *   >122 e <192 para {|}~�������������������������������� al�m de alguns caracteres n�o visualiz�veis

    */

    public synchronized boolean possuiCaracteresIrrelevantes( String palavra ) {

        int codigoANSI;

        int size = palavra.length(); // este size evita que o metodo length() seja chamado a cada loop.

        for( int i = 0; i < size; i++ )

           {

             codigoANSI = (int)palavra.charAt(i);

             if(

                 codigoANSI != 45 && ( codigoANSI < 48    ||

                 ( codigoANSI > 57  && codigoANSI < 65  ) ||

                 ( codigoANSI > 90  && codigoANSI < 97  ) ||

                 ( codigoANSI > 122 && codigoANSI < 192 ) )

               )

                 return true;

           }

        return false;

    }



    /** Busca bin�ria em um array de Strings */

    protected synchronized boolean pertenceAoArray( String palavra,String array[] ) {

        boolean achou = false;

        if( array!=null )

          {

            int pos;

            int left = 0,right = array.length-1;

            while( !achou && left <= right )

                 {

                   pos = ((right+left)/2);

                   if( array[pos].compareTo( palavra ) == 0 )

                       achou = true;

                   else if( array[pos].compareTo( palavra ) < 0 )

                       left = pos+1;

                   else

                       right = pos-1;

                 }

          }

        return achou;

    }



    /** Verifica os prefixos. */

    protected synchronized boolean possuiPrefixos( String palavra, String term[] ) {

        boolean achou = false;

        if( term != null )

          {

            for( int i = 0; i < term.length && !achou; i++)

                 if( palavra.startsWith( term[i] ) )

                     achou = true;

          }

        return achou;

    }



    /** Verifica os sufixos. */

    protected synchronized boolean possuiSufixos( String palavra, String term[] ) {

        boolean achou = false;

        if( term != null )

          {

            for( int i = 0; i < term.length && !achou; i++)

                 if( palavra.endsWith( term[i] ) )

                     achou = true;

          }

        return achou;

    }



    /** Imprime os dados dispon�veis para cada conjunto de palavras */

    public synchronized void printListas( ) {

        debug( "Excecoes"      ,excecoes       );

        debug( "Irrelevantes"  ,irrelevantes   );

        debug( "Complementares",complementares );

        debug( "Prefixos"      ,prefixos       );

        debug( "Sufixos"       ,sufixos        );

    }



    protected synchronized void debug( String message,String array[] ) {

        System.out.println("---> "+message+" <---");

        if( array != null )

          {

            int size = array.length;



            StringBuffer buffer = new StringBuffer(15 * size);

            buffer.append("[");

            for( int i = 0; i < array.length; i++ )

                 buffer.append( i == 0 ? array[i] : ","+array[i] );

            buffer.append("]");



            System.out.println(buffer.toString());

          }

        else

            System.out.println("Nada dispon�vel.");

    }

}