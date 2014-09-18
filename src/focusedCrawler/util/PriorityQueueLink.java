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


public class PriorityQueueLink extends PriorityQueue{


  public PriorityQueueLink(int maxSize) {
    initialize(maxSize);
  }

  /**
   * lessThan
   *
   * @param object Object
   * @param object1 Object
   * @return boolean
   */
  protected boolean lessThan(Object object, Object object1) {
    LinkRelevance link1 = (LinkRelevance)object;
    LinkRelevance link2 = (LinkRelevance)object1;
    boolean less = false;
    if(link1 == null || link2 == null){
      less = true;
    }else{
      if(link1.getRelevance() > link2.getRelevance()){
        less = true;
      }
    }
    return less;
  }

  public static void main(String[] args) {
    PriorityQueue queue = new PriorityQueueLink(1);
    System.out.println("SIZE_A:"+queue.size());
    System.out.println("SIZE_A:"+queue.top());
    try {
    LinkRelevance linkRelev = new LinkRelevance(new java.net.URL("http://"),0);
    queue.insert(linkRelev);
    System.out.println("SIZE_B:"+queue.size());
    linkRelev = (LinkRelevance)queue.top();
    System.out.println("SIZE_A:"+linkRelev.getRelevance());
    for (int i = 0; i < 5; i++) {
        linkRelev = new LinkRelevance(new java.net.URL("http://"),1);
        queue.insert(linkRelev);
    }
    System.out.println("SIZE_C:"+queue.size());
    linkRelev = (LinkRelevance)queue.top();
    System.out.println("SIZE_A:"+linkRelev.getRelevance());

    linkRelev = new LinkRelevance(new java.net.URL("http://"),1000);
    queue.insert(linkRelev);
    System.out.println("SIZE_D:"+queue.size());
    linkRelev = (LinkRelevance)queue.pop();
    System.out.println("SIZE_A:"+linkRelev.getRelevance());

    linkRelev = new LinkRelevance(new java.net.URL("http://"),2);
    queue.insert(linkRelev);
    System.out.println("SIZE_E:"+queue.size());
    linkRelev = (LinkRelevance)queue.pop();
    System.out.println("SIZE_A:"+linkRelev.getRelevance());
    queue.adjustTop();
  }
  catch (java.net.MalformedURLException ex) {
    ex.printStackTrace();
  }

  }

}
