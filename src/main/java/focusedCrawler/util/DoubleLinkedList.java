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



public class DoubleLinkedList {



    private int size;

    private DoubleLinkedListNode head;

    private DoubleLinkedListNode last;



    public DoubleLinkedList() {

        size=0;

        head=last=null;

    }



    public boolean isEmpty() { 

        return size == 0;

    }



    public DoubleLinkedListNode head() {

        return head;

    }



    public DoubleLinkedListNode last() {

        return last;

    }



    public void setHead(DoubleLinkedListNode head) {

        this.head = head;

    }



    public void setLast(DoubleLinkedListNode last) {

        this.last = last;

    }



    /** cria um novo no para o objeto dado e insere no final */

    public DoubleLinkedListNode insertLast(Object obj) {

        DoubleLinkedListNode newNode = new DoubleLinkedListNode(obj);

        insertNodeAtTheEnd(newNode);

        return newNode;

    }



    /** insere o no dado no final da lista */

    public synchronized void insertNodeAtTheEnd(DoubleLinkedListNode newNode) {

        if (size==0) {

            head = last = newNode;

        } else {

            last.setNext(newNode);

            newNode.setPrevious(last);

            newNode.setNext(null);

            last = newNode;

        }

        newNode.setList(this);

        size++;

    }



    /** cria um novo no para o objeto dado e insere no inicio */

    public DoubleLinkedListNode insertFirst(Object obj) {

        DoubleLinkedListNode newNode = new DoubleLinkedListNode(obj);

        insertNodeFirst(newNode);

        return newNode;

    }



    /** insere o no dado no inicio */

    public synchronized void insertNodeFirst(DoubleLinkedListNode newNode) {

        if (size == 0) {

            head = last = newNode;

            newNode.setNext(null);

            newNode.setPrevious(null);

        } else {

            head.setPrevious(newNode);

            newNode.setNext(head);

            newNode.setPrevious(null);

            head=newNode;

        }

        newNode.setList(this);

        size++;

    }



    /** Remove o no dado desta lista, se o no nao for desta lista nao faz nada */

    public synchronized void removeNode(DoubleLinkedListNode oldNode) {

        if (oldNode.list()==this) {

            if (!isEmpty()) {

                if (head==last) {

                    head = last = null;

                } else if (oldNode==head) {

                    head = oldNode.next();

                    head.setPrevious(null);

                } else if (oldNode==last) {

                    last = oldNode.previous();

                    last.setNext(null);
                    
                } else {

                    oldNode.previous().setNext(oldNode.next());

                    oldNode.next().setPrevious(oldNode.previous());

                }

                size--;

            }

            oldNode.setList(null);

            oldNode.setNext(null);

            oldNode.setPrevious(null);

        }

    }



    public synchronized void moveFirst(DoubleLinkedListNode node) {

        removeNode(node);

        insertNodeFirst(node);

    }



    /** move o no dado para o final da lista */

    public synchronized void moveLast(DoubleLinkedListNode node) {

        removeNode(node);

        insertNodeAtTheEnd(node);

    }



    /** Remove a cabeca da lista e retorna o no removido */

    public synchronized DoubleLinkedListNode removeHead() {

        DoubleLinkedListNode ret = head;

        if (!isEmpty()) {

            removeNode(ret);

        }

        return ret;

    }



    /** Remove o ultimo no da lista e retorna o no removido */

    public synchronized DoubleLinkedListNode removeLast() {

        DoubleLinkedListNode ret = last;

        if (!isEmpty()) {

            removeNode(ret);

        }

        return ret;

    }



    /** representacao em string da lista */

    public String toString() {

        StringBuffer buf = new StringBuffer(size+10);

        DoubleLinkedListNode walker = head;

        buf.append("[");

        boolean first = true;

        while(walker!=null) {

            if (first) {

                buf.append(walker);

                first = false;

            } else  buf.append(",").append(walker);

            walker = walker.next();

        }

        buf.append("]");

        return buf.toString();

    }



    /** Tamanho da lista */

    public int size() {

        return size;

    }



    public static void main(String args[]) {

        DoubleLinkedList list = new DoubleLinkedList();

        DoubleLinkedListNode no1,no2,no3; 

        System.out.println("lista vazia : "+list);

        no1 = list.insertFirst(new Integer(1));

        System.out.println("lista um : "+list);

        no2 = list.insertLast(new Integer(10));

        System.out.println("lista 2 : "+list);

        no3 = list.insertFirst(new Integer(-10));

        System.out.println("lista 3 : "+list);

        no1.removeNode();

        System.out.println("lista 4 : "+list);

        no2.removeNode();

        System.out.println("lista 5 : "+list);

        no3.removeNode();

        System.out.println("lista 6 : "+list);

        list.insertNodeFirst(no3);

        System.out.println("lista 7 : "+list);

    }



}

