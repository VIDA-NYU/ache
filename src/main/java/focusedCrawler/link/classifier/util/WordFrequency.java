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





public class WordFrequency {

  private String word;

  private int frequency;

  public WordFrequency(String word, int frequency) {
    this.word = word;
    this.frequency = frequency;
  }

  public String getWord(){
    return word;
  }

  public int getFrequency(){
    return frequency;
  }

  public void setFrequency(int freq){
    this.frequency = freq;
  }



  public void setWord(String newWord){
    word = newWord;
  }

  public void incrementFrequncy(int freq){
    frequency = frequency + freq;
  }

  public boolean equals(WordFrequency wordFrequency){
    return this.getWord().equals(wordFrequency.getWord());
  }

  public String toString(){
    return getWord() + ":" + getFrequency();
  }

  public static void main(String[] args) {
    java.util.Set set = new java.util.HashSet();
    set.add(new WordFrequency("test",1));
    boolean exist = set.contains(new WordFrequency("test",64));
    System.out.println("EXIST:"+exist);
  }
}

