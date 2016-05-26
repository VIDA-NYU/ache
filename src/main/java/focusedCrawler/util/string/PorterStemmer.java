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

/*
***************************************************************
    PorterStemmer.java
    Programmer: John Keyes (keyeg@ils.unc.edu)
    Last Modified: January 3, 1998

    This class implements the PORTER stemming algorithm, which
    is fully described in "An algorithm for suffix stripping",
    M.F. Porter (1980), _Program_, Vol. 14, No. 3, pp. 130-137

***************************************************************
*/

public class PorterStemmer {


  public String stem(String str) {
    String result = str;
    try {
      result = stemLocal(str);
      if(result.length() < 3){
        result = str;
      }
    }
    catch (java.lang.StringIndexOutOfBoundsException ex) {

    }
    return result;
  }

    private String stemLocal(String str) {
        // check for zero length
        if (str.length() > 0) {
            // all characters must be letters
            char[] c = str.toCharArray();
            for (int i = 0; i < c.length; i++) {
                if (!Character.isLetter(c[i]))
                  return str.substring(0,i);
//                    return "Invalid term";
            }
        } else {
            return "No term entered";
        }
        str = step1a(str);
        str = step1b(str);
        str = step1c(str);
        str = step2(str);
        str = step3(str);
        str = step4(str);
        str = step5a(str);
        str = step5b(str);
        return str;
    } // end stem

    protected String step1a (String str) {
        // SSES -> SS
        if (str.endsWith("sses")) {
            return str.substring(0, str.length() - 2);
        // IES -> I
        } else if (str.endsWith("ies")) {
            return str.substring(0, str.length() - 2);
        // SS -> S
        } else if (str.endsWith("ss")) {
            return str;
        // S ->
        } else if (str.endsWith("s")) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    } // end step1a

    protected String step1b (String str) {
        // (m > 0) EED -> EE
        if (str.endsWith("eed")) {
            if (stringMeasure(str.substring(0, str.length() - 3)) > 0)
                return str.substring(0, str.length() - 1);
            else
                return str;
        // (*v*) ED ->
        } else if ((str.endsWith("ed")) &&
                   (containsVowel(str.substring(0, str.length() - 2)))) {
            return step1b2(str.substring(0, str.length() - 2));
        // (*v*) ING ->
        } else if ((str.endsWith("ing")) &&
                   (containsVowel(str.substring(0, str.length() - 3)))) {
            return step1b2(str.substring(0, str.length() - 3));
        } // end if
        return str;
    } // end step1b

    protected String step1b2 (String str) {
        // AT -> ATE
        if (str.endsWith("at") ||
            str.endsWith("bl") ||
            str.endsWith("iz")) {
            return str + "e";
        } else if ((endsWithDoubleConsonent(str)) &&
                   (!(str.endsWith("l") || str.endsWith("s") || str.endsWith("z")))) {
            return str.substring(0, str.length() - 1);
        } else if ((stringMeasure(str) == 1) &&
                   (endsWithCVC(str))) {
            return str + "e";
        } else {
            return str;
        }
    } // end step1b2

    protected String step1c(String str) {
        // (*v*) Y -> I
        if (str.endsWith("y")) {
            if (containsVowel(str.substring(0, str.length() - 1)))
                return str.substring(0, str.length() - 1) + "i";
        } // end if
        return str;
    } // end step1c

    protected String step2 (String str) {
        // (m > 0) ATIONAL -> ATE
        if ((str.endsWith("ational")) &&
            (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5) + "e";
        // (m > 0) TIONAL -> TION
        } else if ((str.endsWith("tional")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
        // (m > 0) ENCI -> ENCE
        } else if ((str.endsWith("enci")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
        // (m > 0) ANCI -> ANCE
        } else if ((str.endsWith("anci")) &&
            (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
            return str.substring(0, str.length() - 1) + "e";
        // (m > 0) IZER -> IZE
        } else if ((str.endsWith("izer")) &&
            (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
            return str.substring(0, str.length() - 1);
        // (m > 0) ABLI -> ABLE
        } else if ((str.endsWith("abli")) &&
            (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
            return str.substring(0, str.length() - 1) + "e";
        // (m > 0) ENTLI -> ENT
        } else if ((str.endsWith("alli")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
        // (m > 0) ELI -> E
        } else if ((str.endsWith("entli")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
        // (m > 0) OUSLI -> OUS
        } else if ((str.endsWith("eli")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
        // (m > 0) IZATION -> IZE
        } else if ((str.endsWith("ousli")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
        // (m > 0) IZATION -> IZE
        } else if ((str.endsWith("ization")) &&
            (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5) + "e";
        // (m > 0) ATION -> ATE
        } else if ((str.endsWith("ation")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3) + "e";
        // (m > 0) ATOR -> ATE
        } else if ((str.endsWith("ator")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2) + "e";
        // (m > 0) ALISM -> AL
        } else if ((str.endsWith("alism")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
           return str.substring(0, str.length() - 3);
        // (m > 0) IVENESS -> IVE
        } else if ((str.endsWith("iveness")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
        // (m > 0) FULNESS -> FUL
        } else if ((str.endsWith("fulness")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
        // (m > 0) OUSNESS -> OUS
        } else if ((str.endsWith("ousness")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
        // (m > 0) ALITII -> AL
        } else if ((str.endsWith("aliti")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
        // (m > 0) IVITI -> IVE
        } else if ((str.endsWith("iviti")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3) + "e";
        // (m > 0) BILITI -> BLE
        } else if ((str.endsWith("biliti")) &&
            (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5) + "le";
        } // end if
        return str;
    } // end step2


    protected String step3 (String str) {
        // (m > 0) ICATE -> IC
        if ((str.endsWith("icate")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
        // (m > 0) ATIVE ->
        } else if ((str.endsWith("ative")) &&
            (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5);
        // (m > 0) ALIZE -> AL
        } else if ((str.endsWith("alize")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
        // (m > 0) ICITI -> IC
        } else if ((str.endsWith("iciti")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
        // (m > 0) ICAL -> IC
        } else if ((str.endsWith("ical")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
        // (m > 0) FUL ->
        } else if ((str.endsWith("ful")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
        // (m > 0) NESS ->
        } else if ((str.endsWith("ness")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
        } // end if
        return str;
    } // end step3


    protected String step4 (String str) {
        if ((str.endsWith("al")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
            // (m > 1) ANCE ->
        } else if ((str.endsWith("ance")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
        // (m > 1) ENCE ->
        } else if ((str.endsWith("ence")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
        // (m > 1) ER ->
        } else if ((str.endsWith("er")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
        // (m > 1) IC ->
        } else if ((str.endsWith("ic")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
        // (m > 1) ABLE ->
        } else if ((str.endsWith("able")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
        // (m > 1) IBLE ->
        } else if ((str.endsWith("ible")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
        // (m > 1) ANT ->
        } else if ((str.endsWith("ant")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) EMENT ->
        } else if ((str.endsWith("ement")) &&
            (stringMeasure(str.substring(0, str.length() - 5)) > 1)) {
            return str.substring(0, str.length() - 5);
        // (m > 1) MENT ->
        } else if ((str.endsWith("ment")) &&
            (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
        // (m > 1) ENT ->
        } else if ((str.endsWith("ent")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) and (*S or *T) ION ->
        } else if ((str.endsWith("sion") || str.endsWith("tion")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) OU ->
        } else if ((str.endsWith("ou")) &&
            (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
        // (m > 1) ISM ->
        } else if ((str.endsWith("ism")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) ATE ->
        } else if ((str.endsWith("ate")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) ITI ->
        } else if ((str.endsWith("iti")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) OUS ->
        } else if ((str.endsWith("ous")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) IVE ->
        } else if ((str.endsWith("ive")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        // (m > 1) IZE ->
        } else if ((str.endsWith("ize")) &&
            (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        } // end if
        return str;
    } // end step4


    protected String step5a (String str) {
        // (m > 1) E ->
        if ((stringMeasure(str.substring(0, str.length() - 1)) > 1) &&
            str.endsWith("e"))
            return str.substring(0, str.length() -1);
        // (m = 1 and not *0) E ->
        else if ((stringMeasure(str.substring(0, str.length() - 1)) == 1) &&
                 (!endsWithCVC(str.substring(0, str.length() - 1))) &&
                 (str.endsWith("e")))
            return str.substring(0, str.length() - 1);
        else
            return str;
    } // end step5a


    protected String step5b (String str) {
        // (m > 1 and *d and *L) ->
        if (str.endsWith("l") &&
            endsWithDoubleConsonent(str) &&
            (stringMeasure(str.substring(0, str.length() - 1)) > 1)) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    } // end step5b


    /*
       -------------------------------------------------------
       The following are functions to help compute steps 1 - 5
       -------------------------------------------------------
    */

    // does string end with 's'?
    protected boolean endsWithS(String str) {
        return str.endsWith("s");
    } // end function

    // does string contain a vowel?
    protected boolean containsVowel(String str) {
        char[] strchars = str.toCharArray();
        for (int i = 0; i < strchars.length; i++) {
            if (isVowel(strchars[i]))
                return true;
        }
        // no aeiou but there is y
        if (str.indexOf('y') > -1)
            return true;
        else
            return false;
    } // end function

    // is char a vowel?
    public boolean isVowel(char c) {
        if ((c == 'a') ||
            (c == 'e') ||
            (c == 'i') ||
            (c == 'o') ||
            (c == 'u'))
            return true;
        else
            return false;
    } // end function

    // does string end with a double consonent?
    protected boolean endsWithDoubleConsonent(String str) {

        char c = str.charAt(str.length() - 1);
        if (c == str.charAt(str.length() - 2))
          if (!containsVowel(str.substring(str.length() - 2))) {
            return true;
          }
        return false;
    } // end function

    // returns a CVC measure for the string
    protected int stringMeasure(String str) {
        int count = 0;
        boolean vowelSeen = false;
        char[] strchars = str.toCharArray();

        for (int i = 0; i < strchars.length; i++) {
            if (isVowel(strchars[i])) {
                vowelSeen = true;
            } else if (vowelSeen) {
                count++;
                vowelSeen = false;
            }
        } // end for
        return count;
    } // end function

    // does stem end with CVC?
    protected boolean endsWithCVC (String str) {
        char c, v, c2;
        if (str.length() >= 3) {
            c = str.charAt(str.length() - 1);
            v = str.charAt(str.length() - 2);
            c2 = str.charAt(str.length() - 3);
        } else {
            return false;
        }

        if ((c == 'w') || (c == 'x') || (c == 'y')) {
            return false;
        } else if (isVowel(c)) {
            return false;
        } else if (!isVowel(v)) {
            return false;
        } else if (isVowel(c2)) {
            return false;
        } else {
            return true;
        }
    } // end function
} // end class
