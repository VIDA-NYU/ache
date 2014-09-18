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
package focusedCrawler.link.classifier.builder.wrapper;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;

import focusedCrawler.link.classifier.util.FilterData;
import focusedCrawler.link.classifier.util.WordField;
import focusedCrawler.link.classifier.util.WordFrequency;
import focusedCrawler.link.classifier.util.WordFrequencyComparator;
import focusedCrawler.link.classifier.util.WordFrequencyGenerator;



public class WrapperLevelWordsFile {

  public WrapperLevelWordsFile() {

  }

  public Vector wrapperFile (String nameFile) throws FileNotFoundException,
      IOException {

    BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream(nameFile)));
    Vector lines = new Vector();
    HashSet usedUrls = new HashSet();
    for (String line = bin.readLine(); line != null; line = bin.readLine()) {
      Vector fields = new Vector();
      for (int i = 0; i < WordField.FIELD_NAMES.length; i++) {
        fields.add(new Vector());
      }
      String url = line.substring(line.indexOf(":"),line.indexOf(" "));
      int index = line.indexOf("Level:");
      line = line.substring(index + "Level:".length() + 2);
      index = line.indexOf("field:");
      while(index != -1){
        index = index + "field:".length();
        line = line.substring(index);
//        System.out.println("line : " + line);
        int indexEnd = line.indexOf(" ") + 1;
        String field = line.substring(0,indexEnd);
        line = line.substring(indexEnd);
        indexEnd = line.indexOf(" ");
        if(indexEnd == -1){
          indexEnd = line.length();
        }
        index = line.indexOf("word:")+"word:".length();
        String word = "";
        try {
          word = line.substring(index, indexEnd);
        }
        catch (StringIndexOutOfBoundsException e) {
          index = line.indexOf("field:");
          continue;
        }
        int fieldInt = new Integer(field.trim()).intValue();
        if(!(fieldInt == WordField.URLFIELD && usedUrls.contains(url))){
          Vector wf = (Vector)fields.elementAt(fieldInt);
          wf.add(word);
        }
        index = line.indexOf("field:");
      }
      usedUrls.add(url);
      lines.add(fields);
    }
//    System.out.println("LINES"+lines);
    return lines;
  }

  public void filteringFeatures(File[] levels) throws FileNotFoundException,
      IOException {

    WordFrequencyGenerator sort = new WordFrequencyGenerator();
    FilterData filterData = new FilterData(50,2);
    Vector lines = new Vector();
//    HashSet
    for (int i = 0; i < levels.length; i++) {
      lines =  wrapperFile(levels[i].toString());
      sort.storeWordsFromFile(lines,levels.length-i);

    }
    Vector[] lists = sort.sort();
    Vector[] finalLists = new Vector[lists.length];
    boolean stem = true;
    for (int i = 0; i < lists.length; i++) {
      if(lists[i].size() == 0){
        continue;
      }
      Vector result = filterData.filter(lists[i],new Vector());
      stem = false;
      Collections.sort(result,new WordFrequencyComparator());
      finalLists[i] = new Vector();
      for (int  j = 0; j < result.size() ; j++) {
        WordFrequency wf = (WordFrequency)result.elementAt(j);
        if((i == 0 && j < 5) || ((i == 3 && j < 5)) || (i == 2 && j < 5)){
          System.out.println(wf.getWord() + ":" + wf.getFrequency());
          finalLists[i].add(wf.getWord());
        }
      }
    }
    Vector featStr = new Vector();
    System.out.println("@relation linkClassifier");
    for (int i = 0; i < finalLists.length; i++) {
      Vector fieldWords = finalLists[i];
      if(fieldWords == null){
        continue;
      }
//      System.out.println(WordField.FIELD_NAMES[i] + ":" + fieldWords);
      for (int j = 0; j < fieldWords.size(); j++) {
        System.out.println("@attribute " + WordField.FIELD_NAMES[i].toLowerCase() + "_" +
                         fieldWords.elementAt(j) + " real");
        featStr.add(WordField.FIELD_NAMES[i] + "_" + fieldWords.elementAt(j));
      }
    }
      System.out.print("@attribute level {0");
      for (int i = 1; i < levels.length; i++) {
        System.out.print(",");
        System.out.print(i);
      }
      System.out.print("}");
      System.out.println("");
      System.out.println("@data");
      int minSize = Integer.MAX_VALUE;
      Vector[] levelsVector = new Vector[levels.length];
      for (int i = 0; i < levels.length; i++) {
        Vector result = wrapperFile(levels[i].toString());
        levelsVector[i] = result;
        if(result.size() < minSize && result.size() > 0){
          minSize = result.size();
        }
      }

      int emptyLines = 0;
      for (int i = 0; i < levels.length; i++) {
        Vector result = levelsVector[i];
        int level = Integer.parseInt(levels[i].toString().substring(levels[i].
            toString().length() - 1));
        for (int j = 0; j < result.size() && j < minSize; j++) {
          //System.out.println("LINE");
          Vector line = (Vector) result.elementAt(j);
          int[] wekaLine = createWekaLine(featStr, line);
          int count = 0;
          boolean good = true;
          for (int l = 0; l < wekaLine.length && good; l++) {
            if(wekaLine[l] > 0){
              count++;
            }
          }
          if ((count >= 2 && level == 0) || (count >= 1 && level == 1) || (count >= 0 && level == 2)){
            if((count == 0 && level == 2 && emptyLines <= 20)){
              emptyLines++;
            }else{
              if((count == 0 && level == 2 && emptyLines > 20)){
                continue;
              }
            }
            for (int l = 0; l < wekaLine.length; l++) {
              System.out.print(wekaLine[l] + ",");
            }
            System.out.print(level);
            System.out.print("\n");
          }
        }
      //     System.out.println(WordField.FIELD_NAMES[i] +":" + finalLists[i]);
      }

  }


  public void createWekaFile(File[] levels) throws FileNotFoundException,
      IOException {

    HashMap features = new HashMap();
    for (int i = 0; i < levels.length; i++) {
      Vector result = wrapperFile(levels[i].toString());
      for (int j = 0; j < result.size(); j++) {
        //System.out.println("LINE");
        Vector line = (Vector) result.elementAt(j);
        for (int l = 0; l < line.size(); l++) {
          Vector words = (Vector) line.elementAt(l);
//          System.out.print("FIELD:"+WordField.FIELD_NAMES[j] + " ");
          for (int k = 0; k < words.size(); k++) {
//            buf.append(WordField.FIELD_NAMES[l] + "_" + words.elementAt(k));
//            buf.append(",");
              String wordTemp = (String)words.elementAt(k);
              if(wordTemp.length() < 3){
                continue;
              }
              Integer freq = (Integer)features.get(WordField.FIELD_NAMES[l] + "_" + words.elementAt(k));
            if(freq == null){
              features.put(WordField.FIELD_NAMES[l] + "_" + words.elementAt(k), new Integer(1));
            }else{
              features.put(WordField.FIELD_NAMES[l] + "_" + words.elementAt(k),new Integer(freq.intValue()+1));
            }
          }
        }
//        buf.append(level);
//        buf.append("\n");
      }
    }
    System.out.println("@relation linkClassifier");
    java.util.Iterator iter = features.keySet().iterator();
    Vector featStr = new Vector();
    int count = 0;
    while(iter.hasNext()){
      String feature = (String)iter.next();
      Integer freq = (Integer)features.get(feature);
      if(freq != null && freq.intValue() > 50){
        featStr.add(feature);
        System.out.println("@attribute " + feature + " real");
      }
    }

    System.out.print("@attribute level {0");
    for (int i = 1; i < levels.length; i++) {
      System.out.print(",");
      System.out.print(i);
    }
    System.out.print("}");
    System.out.println("");
    System.out.println("@data");
//    int level0Size = 0;
    for (int i = 0; i < levels.length; i++) {
      Vector result = wrapperFile(levels[i].toString());
      int level = Integer.parseInt(levels[i].toString().substring(levels[i].toString().length()-1));
//      if(level == 0){
//        level0Size = result.size();
//      }
      for (int j = 0; j < result.size(); j++) {
        //System.out.println("LINE");
        Vector line = (Vector) result.elementAt(j);
        int[] wekaLine = createWekaLine(featStr,line);
        for (int l = 0; l < wekaLine.length; l++) {
          System.out.print(wekaLine[l] + ",");
        }
        System.out.print(level);
        System.out.print("\n");
      }
    }
  }

  private int[] createWekaLine(Vector features, Vector line){
    int[] wekaLine = new int[features.size()];
    for (int i = 0; i < features.size(); i++) {
      String feat = (String)features.elementAt(i);
      String featNoTag = feat.substring(feat.indexOf("_")+1);
      for (int j = 0; j < line.size(); j++) {
        Vector words = (Vector) line.elementAt(j);
        for (int k = 0; k < words.size(); k++) {
          String wordTemp = (String)words.elementAt(k);
          String featTemp = WordField.FIELD_NAMES[j] + "_" + wordTemp;
          if(feat.equals(featTemp) || (j==0 && wordTemp.indexOf(featNoTag) != -1)){
            wekaLine[i] = wekaLine[i] + 1;
          }
        }
      }
    }
    return wekaLine;
  }


  public static void main(String[] args) {

    WrapperLevelWordsFile wrapper = new WrapperLevelWordsFile();

    Vector result = null;

    try {

//      result = wrapper.wrapperFile(args[0]);
//
//      for (int i = 0; i < result.size(); i++) {
//
//        //System.out.println("LINE");
//
//        Vector line = (Vector)result.elementAt(i);
//
//        for(int j = 0; j < line.size(); j++){
//
//          Vector words = (Vector)line.elementAt(j);
//
////          System.out.print("FIELD:"+WordField.FIELD_NAMES[j] + " ");
//
//          for(int l=0; l < words.size();l++){
//
//            System.out.print(WordField.FIELD_NAMES[j] + "_" +words.elementAt(l) + " ");
//
//          }
//
//
//
//        }
//        System.out.println("\n");
//
//      }
//
        java.io.File files = new java.io.File(args[0]);
        wrapper.filteringFeatures(files.listFiles());
    }

    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }

    catch (IOException ex) {
      ex.printStackTrace();
    }

  }



}

