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



/**

 * Lista Duplamente Ligada.

 * @author Oscar Miranda

 * @date   19/05/1999

 */

public class DoubleLinkedListNode {



    private Object data;

    private DoubleLinkedList list;

    private DoubleLinkedListNode next;

    private DoubleLinkedListNode previous;



    public DoubleLinkedListNode(Object data, DoubleLinkedListNode ant, DoubleLinkedListNode prox, DoubleLinkedList list) {

        this.data = data;

        this.list = list;

        previous = ant;

        next = prox;

    }



    public DoubleLinkedListNode(Object data,  DoubleLinkedList list) {

        this(data, null, null, list);

    }



    public DoubleLinkedListNode(Object data, DoubleLinkedListNode ant, DoubleLinkedListNode prox) {

        this(data, ant, prox, null);

    }

    

    public DoubleLinkedListNode(Object data) {

        this(data, null, null);

    }



    public DoubleLinkedListNode() {

        this(null);

    }



    /************************************/

    /************************************/



    public DoubleLinkedListNode next() {

        return next;

    }



    public DoubleLinkedListNode previous() {

        return previous;

    }



    public DoubleLinkedList list() {

        return list;

    }



    public Object data() {

        return data;

    }

    /**************************/

    /**************************/



    public void setData(Object data) {

        this.data = data;

    }



    public void setNext(DoubleLinkedListNode p) {

        next = p;

    }



    public void setPrevious(DoubleLinkedListNode ant) {

        previous = ant;

    }



    public void setList(DoubleLinkedList list) {

        this.list = list;

    }



    public String toString() {

        if (data==null) return null;

        else return data.toString();

    }



    /**

     * Remove o no da lista.

     * Retorna o head da lista.

     */

    public void removeNode() {

        if (list!=null) list.removeNode(this);

    }



    /**

     * Move este no para o inicio da lista

     */

    public void moveFirst() {

        if (list!=null) {

            list.moveFirst(this);

        }

    }



    /**

     * Move este no para o final da lista

     */

    public void moveLast() {

        if (list!=null) {

            list.moveLast(this);

        }

    }



    public static void main(String args[]) {

/**

        DoubleLinkedList list = new DoubleLinkedList();

        System.out.println("lista vazia : "+list);

        list.setData(new Integer(1));

        System.out.println("lista um : "+list);

        list.insertLast(new Integer(10));

        System.out.println("lista 2 : "+list);

        list.insertFirst(new Integer(-10));

        System.out.println("lista 3 : "+list);

**/

    }

}

