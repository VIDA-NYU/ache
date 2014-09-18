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

import java.util.Enumeration;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;



public class StorageSelectedCollection extends StorageDefault {



    private int storageCounter;

    private Vector vStorage;

    private Long pingTime;



    public StorageSelectedCollection() {

        vStorage = new Vector();

        storageCounter = 0;

        pingTime = new Long(System.currentTimeMillis());

    } //StorageSelectedCollection



    private synchronized Storage next() {

        Storage resultado = null;

        if (size () > 0) {

            storageCounter = (storageCounter +1) % size();

            resultado = getStorage(storageCounter);

        } //if

        return resultado;

    } //next



    public int size () {

        return vStorage.size();

    } //size



    public Storage getStorage(int index) {

        return (Storage) vStorage.elementAt(index);

    } //getStorage



    public Object addResource(Object obj) throws StorageException, CommunicationException {

        if (! (obj instanceof Storage)) {

            throw new StorageException ("Tipo invalido: " + obj.getClass(), new Throwable ());

        } //if

        vStorage.addElement(obj);

        return obj;

    } //addResource



    public Object[] addResourceArray(Object[] objs) throws StorageException, CommunicationException {

        Storage storage = next();

        if (storage != null) {

           return storage.addResourceArray(objs);

        } //if

        return null;

    } //addResourceArray



    public Object removeResource(Object obj) throws StorageException, CommunicationException {

        vStorage.remove(obj);

        return obj;

    } //removeResource



    public Object[] removeResourceArray(Object[] objs) throws StorageException, CommunicationException {

        Object[] result = new Object[objs.length];

        for (int counter=0; counter < objs.length; counter++) {

            result[counter] = removeResourceArray(objs);

        } //for

        return null;

    } //removeResourceArray



    public Object select(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        Storage storage = next();

        if (storage != null) {

           return storage.select(obj);

        } //if

        return null;

    } //select



    public Object[] selectArray(Object[] objs) throws StorageException,DataNotFoundException,CommunicationException {

        Storage storage = next();

        if (storage != null) {

           return storage.selectArray(objs);

        } //if

        return null;

    } //selectArray



    public Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        Storage storage = next();

        if (storage != null) {

            return storage.selectEnumeration(obj);

        } //if

        return null;

    } //selectEnumeration



    public Object insert(Object obj) throws StorageException, CommunicationException {

        Storage storage = next();

        if (storage != null) {

            return storage.insert(obj);

        } //if

        return null;

    } //insert



    public Object[] insertArray(Object[] objs) throws StorageException, CommunicationException {

        Storage storage = next();

        if (storage != null) {

            return storage.insertArray(objs);

        } //if

        return null;

    } //insertArray



    public Object remove(Object obj) throws StorageException, CommunicationException {

        Storage storage = next();

        if (storage != null) {

            return storage.remove(obj);

        } //if

        return null;

    } //remove



    public Object[] removeArray(Object[] objs) throws StorageException, CommunicationException {

        Storage storage = next();

        if (storage != null) {

            return storage.removeArray(objs);

        } //if

        return null;

    } //removeArray



    public Object update(Object obj) throws StorageException, CommunicationException {

        Storage storage = next();

        if (storage != null) {

            return storage.update(obj);

        } //if

        return null;

    } //update



    public Object[] updateArray(Object[] objs) throws StorageException, CommunicationException {

        Storage storage = next();

        if (storage != null) {

            return storage.updateArray(objs);

        } //if

        return null;

    } //updateArray



    public Object commit (Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).commit(obj);

        } //for

        pingTime = new Long(System.currentTimeMillis());

        //System.out.println("@@@@@@@@@@@@@ pingTime commit("+storageCounter+","+pingTime+") @@@@@@@@@@@");

        return null;

    } //commit



    public Object rollback (Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).rollback(obj);

        } //for

        pingTime = new Long(System.currentTimeMillis());

        //System.out.println("@@@@@@@@@@@@@ pingTime rollback("+storageCounter+","+pingTime+") @@@@@@@@@@@");

        return null;

    } //rollback



    public Object finalize (Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).finalize(obj);

        } //for

        pingTime = new Long(System.currentTimeMillis());

        //System.out.println("@@@@@@@@@@@@@ pingTime finalize("+storageCounter+","+pingTime+") @@@@@@@@@@@");

        return null;

    } //finalize



    public Object ping(Object obj) throws StorageException, CommunicationException {

        Object result = null;

        int size = vStorage.size();

        for (int counter = 0; counter < size; counter++) {

            result = getStorage(counter).ping(obj);

        } //for

        //System.out.println("@@@@@@@@@@@@@ pingTime ping("+storageCounter+","+pingTime+") @@@@@@@@@@@");

        pingTime = new Long(System.currentTimeMillis());

        return pingTime;

    } //ping

} //class

