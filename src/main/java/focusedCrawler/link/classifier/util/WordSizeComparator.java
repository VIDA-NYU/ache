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

import java.util.Comparator;

public class WordSizeComparator implements Comparator{
  public WordSizeComparator() {
  }

  /**
   * equals
   *
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj) {
    return false;
  }

  /**
   * compare
   *
   * @param o1 Object
   * @param o2 Object
   * @return int
   */
  public int compare(Object o1, Object o2) {
    if(((WordFrequency)o1).getWord().length() < ((WordFrequency)o2).getWord().length())
      return -1;
    else
          if(((WordFrequency)o1).getWord().length() == ((WordFrequency)o2).getWord().length())
            return 0;
          else
            return 1;

  }
}
