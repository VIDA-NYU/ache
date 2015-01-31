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
package focusedCrawler.util.url;



import focusedCrawler.util.string.FuncoesString;



public class UrlValidator{

	/**

  	 * httpAddress:Bnf

     * httpAddress = http://hostport[/path][?search]

     * hostport = host[:port]

     * path = void | segment [ / path ]

     * void =

     * segment = xpalphas

     * search = xalphas [+search]

     * host = hostname | hostnumber

     * port = digits

     * hostname = ialpha [.hostname]

     * ialpha = alpha [ xalphas ]

     * alpha = a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z | A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z

     * xalphas = xalpha [ xalphas ]

     * xalpha = alpha | digit | other

     * other = % | $ | - | _ | @ | . | & | + | - | ! | * | " | ' | ( | ) | ,

     * hex = digit | a | b | c | d | e | f | A | B | C | D | E | F

     * hostnumber = digits.digits.digits.digits

     * digits = digit[digits]

     * digit = 0 |1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

  	 */

	public static boolean isHttpAddress(String url){

		if((url.length()>8) && url.startsWith("http://")){

  			char[] 	chars = url.substring(7).toCharArray(),

	     			other 	= {'%','$','-','_','@','.','&','+','-','!','*','"','\'','(',')'};

  			int curChar = 0;

  			if(Character.isLetter(chars[0])){ //hostname

     			++curChar;

        		while((curChar < chars.length) && (chars[curChar] != ':') && (chars[curChar] !='/') && (chars[curChar] !='?')){

          			if(Character.isLetterOrDigit(chars[curChar]) || FuncoesString.in(other,chars[curChar]))

             			++curChar;

            		else return false;

          		}

     		}else if(Character.isDigit(chars[0])){//hostnumber

       			int pointCount = 0;

    			++curChar;

        		while((curChar < chars.length) && (chars[curChar] != ':') && (chars[curChar] !='/') && (chars[curChar] !='?')){

          			if(Character.isDigit(chars[curChar])){

             			++curChar;

            		} else if(chars[curChar] == '.'){

              			++curChar;

       					++pointCount;

					} else return false;

          		}

            	if(pointCount != 3)

             		return false;

         	}else return false;



          	if((curChar < chars.length) && (chars[curChar] == ':')){ //port

           		++curChar;

	        	do{

          			if((curChar < chars.length) && (Character.isDigit(chars[curChar]))){

             			++curChar;

            		} else return false;

          		}while((curChar < chars.length) && (chars[curChar] !='/') && (chars[curChar] !='?'));

            }



           	if((curChar < chars.length) && (chars[curChar] == '/')){//path

            	++curChar;

             	if((curChar < chars.length) && (chars[curChar] =='~'))

              		++curChar;

        		while((curChar < chars.length) && (chars[curChar] !='?')){

          			if(Character.isLetterOrDigit(chars[curChar]) || FuncoesString.in(other,chars[curChar]) || chars[curChar]=='/')

             			++curChar;

            		else return false;

          		}

            }



           	if((curChar < chars.length) && (chars[curChar] == '?')){//search

            	++curChar;

        		while((curChar < chars.length) && (chars[curChar] !='?')){

          			if(Character.isLetterOrDigit(chars[curChar]) || FuncoesString.in(other,chars[curChar]))

             			++curChar;

            		else return false;

          		}

            }

            return true;

    	}

  		return false;

 	}

}

