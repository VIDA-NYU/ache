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




import java.util.HashMap;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.io.FileWriter;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import focusedCrawler.util.ParameterFile;



/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AttributeSelector {

  HashMap url;
  HashMap around;
  HashMap anchor;

  ArrayList urlV;
  ArrayList aroundV;
  ArrayList anchorV;
  String wekaInput;
  String domain;
  String selected;
  int level;
  int maxAtt;



  class PairComparator implements Comparator {

    public int compare(Object a, Object b) {
      int a1 = ((Pair)a).getValue();
      int b1 = ((Pair)b).getValue();
      return (a1 < b1 ? 1 : (a1 == b1 ? 0 : -1));
    }

  }



  class Pair {

    private String key;
    private int value;

    public Pair(String key, int value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public int getValue() {
      return value;
    }

  }




  public AttributeSelector() {

    url = new HashMap();
    around = new HashMap();
    anchor = new HashMap();

    urlV = new ArrayList();
    aroundV = new ArrayList();
    anchorV = new ArrayList();

  }

  public AttributeSelector(String weka, String domain, int level, int maxAtt, String selected) {

    url = new HashMap();
    around = new HashMap();
    anchor = new HashMap();

    urlV = new ArrayList();
    aroundV = new ArrayList();
    anchorV = new ArrayList();
    this.wekaInput = weka;
    this.domain = domain;
    this.level = level;
    this.maxAtt = maxAtt;
    this.selected = selected;

  }




  public void addWordsFromFile(String fileName) throws IOException {

    FileReader in = new FileReader(fileName);
    BufferedReader reader = new BufferedReader(in);

    StringTokenizer st;
    String key, value;
    Integer fre;
    HashMap cur = new HashMap();

    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
      if (inputLine.equals("")) {
        continue;
      }
      if (-1 != inputLine.indexOf("URL")) {
        cur = this.url;
        continue;
      }
      if (-1 != inputLine.indexOf("AROUND")) {
        cur = this.around;
        continue;
      }
      if (-1 != inputLine.indexOf("ANCHOR")) {
        cur = this.anchor;
        continue;
      }
      if (-1 != inputLine.indexOf("TITLE")) {
        continue;
      }
      if (-1 != inputLine.indexOf("TEXT")) {
        continue;
      }

      st = new StringTokenizer(inputLine);
      key = st.nextToken();
      value = st.nextToken();
      fre = new Integer(value);
      System.out.println(key + "  " + value);
      Integer inValue = (Integer)cur.get(key);
      if (inValue == null) {
        cur.put(key, fre);
      }
      else {
        int total = inValue.intValue() + fre.intValue();
        fre = new Integer(total);
        cur.put(key, fre);
      }

    }


  }


  public void sort() {

    Set keys = url.keySet();
    Iterator itr = keys.iterator();
    String key;
    Pair p;
    PairComparator comp = new PairComparator();

    while (itr.hasNext()) {
      key = (String)itr.next();
      p = new Pair(key , ((Integer) url.get(key)).intValue());
      urlV.add(p);
    }

    Collections.sort(urlV, comp);

    keys = around.keySet();
    itr = keys.iterator();

    while (itr.hasNext()) {
      key = (String)itr.next();
      p = new Pair(key , ((Integer) around.get(key)).intValue());
      aroundV.add(p);
    }

    Collections.sort(aroundV, comp);

    keys = anchor.keySet();
    itr = keys.iterator();

    while (itr.hasNext()) {
      key = (String)itr.next();
      p = new Pair(key , ((Integer) anchor.get(key)).intValue());
      anchorV.add(p);
    }

    Collections.sort(anchorV, comp);

  }




  public void getTopList(String fileName) throws IOException {

    FileWriter out = new FileWriter(fileName);
    FileWriter weka = new FileWriter(this.wekaInput);
    FileWriter selected = new FileWriter(this.selected);
    int size = urlV.size();

    weka.write("@relation " + domain + "\r\r");
    selected.write("ATTRIBUTES ");

    out.write("FIELD:URL\n\n");
    for (int i = 0; i < size && i < this.maxAtt; i++) {
      Pair p = (Pair) urlV.get(i);
      out.write(p.getKey() + " " +  p.value + "\n");
      weka.write("@attribute url_" + p.getKey() + " {0,1}" + "\r");
      selected.write("url_" + p.getKey() + " ");
    }
    System.out.println("url : " + size);

    size = aroundV.size();
    out.write("\nFIELD:AROUND\n\n");
    for (int i = 0; i < size && i < maxAtt; i++) {
      Pair p = (Pair) aroundV.get(i);
      out.write(p.getKey() + " " +  p.value + "\n");
      weka.write("@attribute around_" + p.getKey() + " {0,1}" + "\r");
      selected.write("around_" + p.getKey() + " ");
    }
    System.out.println("around : " + size);

    size = anchorV.size();
    out.write("\nFIELD:ANCHOR\n\n");
    for (int i = 0; i < size && i < maxAtt; i++) {
      Pair p = (Pair) anchorV.get(i);
      out.write(p.getKey() + " " +  p.value + "\n");
      weka.write("@attribute anchor_" + p.getKey() + " {0,1}" + "\r");
      selected.write("anchor_" + p.getKey() + " ");
    }
    System.out.println("anchor : " + size);

    if (level == 2) {
      weka.write("@attribute level {0,1}\r\r");
    }
    else if (level == 3) {
      weka.write("@attribute level {0,1,2}\r\r");
    }
    weka.write("@data\r");

    out.close();
    weka.close();
    selected.close();

  }




  public static void main(String[] args) throws IOException {


    ParameterFile config = new ParameterFile(args[0]);

    int level = config.getParamInt("LEVEL");
    int maxAtt = config.getParamInt("MAX_NUM_ATTRIBUTE");
    String domain = config.getParam("DOMAIN");
    String wekaFile = config.getParam("WEKA_OUTPUT");
    String mergedOut = config.getParam("MERGED_OUTPUT");
    String selected = config.getParam("SELECTED_ATTRIBUTES");

    AttributeSelector as = new AttributeSelector(wekaFile, domain, level, maxAtt, selected);

    int length = config.getParamInt("NUM_BACKLINK_OUT");
    String backOut = config.getParam("BACKLINK_OUT_FILE");

    for (int i = 0; i < length; i++) {
      as.addWordsFromFile(backOut+i);
    }

    as.sort();

    as.getTopList(mergedOut);


  }


}




