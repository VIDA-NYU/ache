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
package focusedCrawler.link.classifier.util;

import java.io.Serializable;

/**
 *
 * <p>Description: Represents the tuple: field,word</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */

public class WordField implements Serializable {

  private String word;

  private int field;

  public static final int URLFIELD = 0;

  public static final int ALT = 1;

  public static final int AROUND = 2;

  public static final int ANCHOR = 3;

  public static final int SRC = 4;

  public static final String[] FIELD_NAMES = new String[]{"URL","ALT","AROUND","ANCHOR","SRC"};

  public WordField(int field, String word) {
    this.field = field;
    this.word = word;
   }

  public void setWord(String word){
    this.word = word;
  }

  public void setField(int field){
    this.field = field;
  }

  public String getWord(){
    return word;
  }

  public int getField(){
    return field;
  }

  public String toString(){
    return "field:"+ field + " word:" + word;
  }

  public boolean equals(WordField wordField){
    boolean ret = false;
    if(wordField.getWord().equals(word)){
      ret = true;
    }
    return ret;
  }

}

