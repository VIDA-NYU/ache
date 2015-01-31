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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import focusedCrawler.link.classifier.builder.wrapper.WrapperLevelWordsFile;

public class WordFrequencyGenerator {

  HashMap[] mapping = new HashMap[WordField.FIELD_NAMES.length];

  public WordFrequencyGenerator() {
    for (int i = 0; i < mapping.length; i++) {
      mapping[i] = new HashMap();
    }
  }

  public void storeWordsFromFile(Vector lines, int weight) {
    weight = 1;
      for (int i = 0; i < lines.size(); i++) {
        Vector line = (Vector)lines.elementAt(i);
        for(int j = 0; j < line.size(); j++){

          Vector words = (Vector)line.elementAt(j);
          HashMap hashmap = mapping[j];
          for(int l=0; l < words.size();l++){
            String word = (String)words.elementAt(l);
            if(word.equals("www") || word.equals("com") || word.equals("htm") || word.equals("the")){
              continue;
            }
            if(!hashmap.containsKey(word)){
              hashmap.put(word, new WordFrequency(word,1*weight));
            }else{
              WordFrequency tempWF = (WordFrequency) hashmap.get(word);
              hashmap.put(word, new WordFrequency(word, (tempWF.getFrequency() + 1)*weight));
            }
         }
      }
    }
  }

  public Vector[] sort(){
    Vector[] sortLists = new Vector[WordField.FIELD_NAMES.length];
    for (int i = 0; i < mapping.length; i++) {
      HashMap hashmap = mapping[i];
      sortLists[i] = new Vector(hashmap.values());
      Collections.sort(sortLists[i],new WordFrequencyComparator());
//      System.out.println("VECTOR"+sortLists[i]);
      for (int j = 0; j < sortLists[i].size(); j++) {
        WordFrequency temp = (WordFrequency)sortLists[i].elementAt(j);
//        System.out.println("FIELD:" + WordField.FIELD_NAMES[i] + " " + temp.getWord() + " " +  temp.getFrequency() );
      }
    }
    return sortLists;
  }



  public static void main(String[] args) {

    WordFrequencyGenerator sort = new WordFrequencyGenerator();

    WrapperLevelWordsFile wrapper = new WrapperLevelWordsFile();

    try {

      Vector lines = wrapper.wrapperFile(args[0]);

      FilterData filterData = new FilterData(50,2);

      sort.storeWordsFromFile(lines,1);

      Vector[] lists = sort.sort();

//          Vector[] lists = sort.storeWordsFromFile(lines);

      for (int i = 0; i < lists.length; i++) {

        System.out.println("FIELD:" + WordField.FIELD_NAMES[i]);

        Vector result = filterData.filter(lists[i]);

        Collections.sort(result,new WordFrequencyComparator());

        System.out.println("");

        for (int  j = 0; j < result.size() && j < 15; j++) {

          WordFrequency temp = (WordFrequency)result.elementAt(j);

//          if(temp.getFrequency() > 10){

            System.out.println(temp.getWord() + " " + temp.getFrequency() + " ");

//          }

        }

        System.out.println("");

     }



    }

    catch (FileNotFoundException ex) {

    }

    catch (IOException ex) {

    }

  }



}





