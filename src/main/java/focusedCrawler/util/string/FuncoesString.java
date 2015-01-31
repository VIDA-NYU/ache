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
package focusedCrawler.util.string;



import java.util.*;



/**

 * Conjunto de funcoes comuns usadas na manipulação de strings

 *

 * @author Marcelo Rômulo

 * @version 1.0, 29/07/99

 */

public class FuncoesString {



    // retorna true se o caracter eh de palavra(letra, digito, hifen ou "*")

    public static boolean char_ok(char ch) {

        boolean result;

	    if (Character.isLetterOrDigit(ch)) {

	        result = true;

	    }

	    else if (ch == '-') {

	        result = true;

	    }

	    else if (ch == '*') {

	        result = true;

	    }

	    else {

	        result = false;

	    }

	    return result;

    }



   /**

     * @param str       String original

     * @param strOld    A String que deve ser substituida

     * @param strNew    A nova string

     * @return          Retorna a string original substituido strOld por strNew

     */

    public static String replace(String str,String strOld,String strNew){

        int iAnt=0,iPos=0;

        StringBuffer  strChanged=new StringBuffer();

        iPos =  str.indexOf(strOld,iAnt);

        while(iPos!=-1){

            strChanged.append(str.substring(iAnt,iPos)+strNew);

            iAnt=iPos+strOld.length();

            iPos =  str.indexOf(strOld,iAnt);

        }

        return  strChanged.append(str.substring(iAnt,str.length())).toString();

    }



    public static boolean in(char[] array, char c){

    	for(int i=array.length-1;i > -1 ; --i){

     		if(array[i]==c)

       			return true;

     	}

      	return false;

    }

}

