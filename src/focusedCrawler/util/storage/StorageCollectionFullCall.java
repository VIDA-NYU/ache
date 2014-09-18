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
package focusedCrawler.util.storage;



import java.util.Vector;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.Log;
import focusedCrawler.util.SelfLoggable;
import focusedCrawler.util.distribution.CommunicationException;



public class StorageCollectionFullCall extends SelfLoggable implements Storage {



    private static int serialNumber = 0;

    private int number;

    private int itemCounter = 0;



    private long idleTime;

    private Long pingTime;



    private Vector vStorage;



    public StorageCollectionFullCall() {

        vStorage = new Vector();

        this.number = serialNumber;

        serialNumber++;

        idleTime = System.currentTimeMillis();

        pingTime = new Long(System.currentTimeMillis());

    } //StorageCollectionFullCall



    public int size() {

        return vStorage.size();

    } //size



    public Storage getStorage (int index) {

        return (Storage) vStorage.elementAt(index);

    } //getStorage



    public int getNumber() {

        return number;

    } //getNumber



    public int getItemCounter() {

        return this.itemCounter;

    } //getItemCounter



    public void setItemCounter(int newItemCounter) {

        this.itemCounter = newItemCounter;

    } //setItemCounter









    public Object addElement (Object obj) throws StorageException, CommunicationException {

        if (! (obj instanceof Storage)) {

            throw new StorageException ("Tipo invalido: " + obj.getClass(), new Throwable ());

        } //if

        vStorage.addElement(obj);

        return obj;

    } //addElement



    public Object[] addElementArray (Object[] objs) throws StorageException, CommunicationException {

        Object[] result = null;

        for (int counter=0; counter < objs.length; counter++) {

            result = addResourceArray(objs);

        } //for

        return objs;

    } //addElementArray



    public Object removeElement (Object obj) throws StorageException, CommunicationException {

        vStorage.remove(obj);

        return obj;

    } //removeElement



    public Object[] removeElementArray (Object[] objs) throws StorageException, CommunicationException {

        Object[] result = new Object[objs.length];

        for (int counter=0; counter < objs.length; counter++) {

            result[counter] = removeElement(objs[counter]);

        } //for

        return null;

    } //removeElementArray





    public Object addResource (Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        //adicionando elemento

        //System.out.println("Linha na colecao: "+(String)obj);

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).addResource(obj);

        } //for

        return result;

    } //addResource



    public Object[] addResourceArray (Object[] objs) throws StorageException, CommunicationException {

        Object[] result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

//            System.out.println("@@@ Chama: "+counter);

            result = getStorage(counter).addResourceArray(objs);

        } //for

        return result;

    } //addResourceArray



    public Object removeResource (Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).removeResource(obj);

        } //for

        return result;

    } //removeResource



    public Object[] removeResourceArray (Object[] objs) throws StorageException, CommunicationException {

        Object[] result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).removeResourceArray(objs);

        } //for

        return result;

    } //removeResourceArray



    public Object select(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).select(obj);

        } //for

        return result;

    } //select



    public Object[] selectArray(Object[] objs) throws StorageException,DataNotFoundException,CommunicationException {

        Object[] result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).selectArray(objs);

        } //for

        return result;

    } //selectArray



    public java.util.Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        java.util.Enumeration result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).selectEnumeration(obj);

        } //for

        return result;

    } //selectEnumeration



    public Object insert(Object obj) throws StorageException, CommunicationException {

        idleTime = System.currentTimeMillis() - idleTime;

        int size = vStorage.size();

        long time;

        long total = 0;

        StringBuffer sTimes = new StringBuffer("insert(");

        sTimes.append(getItemCounter());

        sTimes.append("):idle="); sTimes.append(idleTime); sTimes.append(",");

        for (int counter = 0; counter < size; counter++) {

            time = System.currentTimeMillis();

            getStorage(counter).insert(obj);

            time = System.currentTimeMillis() - time;

            sTimes.append("T("); sTimes.append(counter);

            sTimes.append(")="); sTimes.append(time);

            sTimes.append(",");

            total += time;

        } //for

        sTimes.append("Total="); sTimes.append(total);



        Log.log("Collection", Integer.toString(getNumber()), sTimes.toString());



        incrementCounter();

        idleTime = System.currentTimeMillis();

        return null;

    } //insert



    public Object[] insertArray(Object[] objs) throws StorageException, CommunicationException {

        Object[] result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).insertArray(objs);

        } //for

        return null;

    } //insertArray



    public Object remove(Object obj) throws StorageException, CommunicationException {

        idleTime = System.currentTimeMillis() - idleTime;

        int size = vStorage.size();

        long time;

        long total = 0;

        StringBuffer sTimes = new StringBuffer("remove(");

        sTimes.append(getItemCounter());

        sTimes.append("):idle="); sTimes.append(idleTime); sTimes.append(",");

        for (int counter = 0; counter < size; counter++) {

            time = System.currentTimeMillis();

            getStorage(counter).remove(obj);

            time = System.currentTimeMillis() - time;

            sTimes.append("T("); sTimes.append(counter);

            sTimes.append(")="); sTimes.append(time);

            sTimes.append(",");

            total += time;

        } //for

        sTimes.append("Total="); sTimes.append(total);



//        Log.log("Collection", Integer.toString(getNumber()), sTimes.toString());



        incrementCounter();

        idleTime = System.currentTimeMillis();

        return null;

    } //remove



    public Object[] removeArray(Object[] objs) throws StorageException, CommunicationException {

        Object[] result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).removeArray(objs);

        } //for

        return null;

    } //removeArray



    public Object update(Object obj) throws StorageException, CommunicationException {

        idleTime = System.currentTimeMillis() - idleTime;

        int size = vStorage.size();

        long time;

        long total = 0;

        StringBuffer sTimes = new StringBuffer("update(");

        sTimes.append(getItemCounter());

        sTimes.append("):idle="); sTimes.append(idleTime); sTimes.append(",");

        for (int counter = 0; counter < size; counter++) {

            time = System.currentTimeMillis();

            getStorage(counter).update(obj);

            time = System.currentTimeMillis() - time;

            sTimes.append("T("); sTimes.append(counter);

            sTimes.append(")="); sTimes.append(time);

            sTimes.append(",");

            total += time;

        } //for

        sTimes.append("Total="); sTimes.append(total);



//        Log.log("Collection", Integer.toString(getNumber()), sTimes.toString());



        incrementCounter();

        idleTime = System.currentTimeMillis();

        return null;

    } //remove



    public Object[] updateArray(Object[] objs) throws StorageException, CommunicationException {

        Object[] result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).updateArray(objs);

        } //for

        return null;

    } //removeArray



    public Object commit(Object obj) throws StorageException, CommunicationException {



//        System.out.println(" ******** COLLECTION - COMMIT *********");

//        System.out.println(" ******** COLLECTION - COMMIT *********");

//        System.out.println(" ******** COLLECTION - COMMIT *********");

        idleTime = System.currentTimeMillis() - idleTime;

        int size = vStorage.size();

        long time;

        long total = 0;

        StringBuffer sTimes = new StringBuffer("commit(");

        sTimes.append(getItemCounter());

        sTimes.append("):idle="); sTimes.append(idleTime); sTimes.append(",");

        for (int counter = 0; counter < size; counter++) {

            time = System.currentTimeMillis();

            getStorage(counter).commit(obj);

            time = System.currentTimeMillis() - time;

            sTimes.append("T("); sTimes.append(counter);

            sTimes.append(")="); sTimes.append(time);

            sTimes.append(",");

            total += time;

        } //for

        sTimes.append("Total="); sTimes.append(total);



        Log.log("Collection", Integer.toString(getNumber()), sTimes.toString());

        idleTime = System.currentTimeMillis();

        pingTime = new Long(System.currentTimeMillis());

        return null;

    } //commit



    public Object rollback(Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).rollback(obj);

        } //for

        pingTime = new Long(System.currentTimeMillis());

        return null;

    } //rollback



    public Object finalize(Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).finalize(obj);

        } //for

        pingTime = new Long(System.currentTimeMillis());

        return null;

    } //finalize



    public Object ping(Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).ping(obj);

        } //for

        //System.out.println("SC @@@@@@@@@@@@@ pingTime ping("+pingTime+") @@@@@@@@@@@");

        pingTime = new Long(System.currentTimeMillis());

        return pingTime;

    } //ping



    public void incrementCounter() throws StorageException, CommunicationException {

        setItemCounter(getItemCounter() +1);

    }

}

