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
package focusedCrawler.util.vsm;

import java.util.Comparator;

public class VSMElement {

    /**
     * Comparator to be used when one wants to order VSMElements in descendant order.
     */
    public static Comparator<VSMElement> DESC_ORDER_COMPARATOR = new Comparator<VSMElement>() {
        @Override
        public int compare(VSMElement o1, VSMElement o2) {
            return Double.compare(o2.getWeight(), o1.getWeight());
        }
    };

    private String word;
    private double weight;

    public VSMElement(String word, double weight) {
        this.word = word;
        this.weight = weight;
    }

    public String getWord() {
        return word;
    }

    public double getWeight() {
        return weight;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String toString() {
        return word + " " + weight;
    }

}
