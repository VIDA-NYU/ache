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
package focusedCrawler.util.cache;



import java.util.Enumeration;

import focusedCrawler.util.DoubleLinkedList;
import focusedCrawler.util.DoubleLinkedListNode;







/**

 * Classe que representa uma enumeração de chaves de cache, criada a partir de uma enumeração de Strings (valores das chaves)

 *

 * @see CacheKey

 * @see ObjectCacheKey

 * @see java.util.Enumeration

*/

public class KeyEnumeration implements Enumeration {



    private DoubleLinkedListNode node;



//    Enumeration enum; // Armazena a enumeração de Strings (valores das chaves)



    public KeyEnumeration(DoubleLinkedListNode head) {

        this.node = head;

    }



    public boolean hasMoreElements() {

        return node != null &&  node.next() != null;

    }



    public Object nextElement() {

        if (node == null) return null;

        CacheEntry ce = (CacheEntry) node.data();

        node = node.next();

        return ce.getKey();

    }

}



