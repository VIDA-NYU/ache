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
package focusedCrawler.util;

import focusedCrawler.util.string.StopList;
import focusedCrawler.util.vsm.VSMVector;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Vector;
import java.io.File;
import org.xml.sax.SAXException;
import java.util.Collections;
import java.util.Iterator;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VocabularyHomogeneity {
    public VocabularyHomogeneity() {
    }

    public static void main(String[] args) {
        VocabularyHomogeneity vocabularyhomogeneity = new VocabularyHomogeneity();
        try {
           StopList st = new focusedCrawler.util.string.StopListArquivo(args[0]);
           File[] labeledFiles = new File(args[1]).listFiles();
           System.out.println("FILE:"+new File(args[1]).toString());

           VSMVector vsmFormLabeled = new VSMVector();
           Vector temp = new Vector();
           double count = 0;
           for (int i = 0; i < labeledFiles.length; i++) {
               VSMVector vsmFormTemp = new VSMVector(labeledFiles[i].toString(), true, st);
               count = count + vsmFormTemp.getArrayElements().length;
               vsmFormLabeled.addVector(vsmFormTemp);
               temp.add(vsmFormTemp);
           }
           System.out.println(count/(double)labeledFiles.length);

            VSMVector[] forms = new VSMVector[temp.size()];
            temp.toArray(forms);
            HashMap idfLabel = vsmFormLabeled.calculateWordOccurence(forms);
            double size = idfLabel.size();
            Iterator iter = idfLabel.keySet().iterator();
            double homogen = 0;
            int total = 0;
            temp = new Vector();
            while(iter.hasNext()){
              String word= (String)iter.next();
                int occur = ((Integer)(idfLabel.get(word))).intValue();
                temp.add(new VSMElement(word,occur));
              }
              Collections.sort(temp,new VSMElementComparator());
              for (int i = 0; i < 500; i++) {
                VSMElement elem = (VSMElement)temp.elementAt(i);
                System.out.print(elem.getWeight());
                System.out.print("\n");
                total = total + (int)elem.getWeight();
              }
              for (int i = 0; i < 500; i++) {
                VSMElement elem = (VSMElement)temp.elementAt(i);
                 homogen = homogen + ((elem.getWeight()/total)*(elem.getWeight()/total));
              }

              iter = idfLabel.keySet().iterator();
            while(iter.hasNext()){
                double occur = ((Integer)(idfLabel.get(iter.next()))).doubleValue();
                total = total + (int)occur;
                if(occur > 5){
                  homogen = homogen + ((occur/total)*(occur/total));
                }
            }
            System.out.println("HOMOG:"+homogen);
              System.out.println("DISTINCT:"+size);
              System.out.println("TOTAL:"+total);
              System.out.println("VALUE:"+total/size);
       } catch (SAXException ex) {
           ex.printStackTrace();
       } catch (MalformedURLException ex) {
           ex.printStackTrace();
       } catch (IOException ex) {
           ex.printStackTrace();
       }

    }
}
