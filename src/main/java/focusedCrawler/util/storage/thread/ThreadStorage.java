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
package focusedCrawler.util.storage.thread;



import java.util.Vector;

import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;

public class ThreadStorage extends Thread {



    // variaveis para controle da execucao da thread

    private boolean toRun = true;

    private Exception lastError;



    // storage

    private Storage storage;

    private Vector vAddObjects;

    private Vector vRemoveObjects;



    public ThreadStorage (Storage _storage) {

        this.storage = _storage;

        vAddObjects = new Vector();

        vRemoveObjects = new Vector();

    } //ThreadStorage



    public synchronized void addResourceArray(Object[] objs) {

        for (int counter=0; counter < objs.length; counter++) {

            addResource(objs[counter]);

        } //for

    } //addResourceArray



    public synchronized void addResource(Object obj) {

        this.vAddObjects.add(obj);

    } //addResource



    public synchronized void removeResourceArray(Object[] objs) {

        for (int counter=0; counter < objs.length; counter++) {

            removeResource(objs[counter]);

        } //for

    } //removeResourceArray



    public synchronized void removeResource(Object obj) {

        this.vRemoveObjects.add(obj);

    } //removeResource



    public void commit() {

        wakeUp();

    } //commit



    public synchronized void wakeUp() {

        this.notify();

    } //acordar



    public synchronized void sleep() {

       try {

           wait();

       } //try

       catch ( InterruptedException e) {

           e.printStackTrace();

       } //catch

    } //dormir



    public Exception getLastError() {

        return this.lastError;

    } //getLastError



    public void setToRun(boolean toRun) {

        this.toRun = toRun;

    } //setToRun



    private Object[] getObjects(Vector vObjects) {

        synchronized (vObjects) {

            Object[] objs = new Object[vObjects.size()];

            vObjects.copyInto(objs);

            vObjects.removeAllElements();

            return objs;

        } //synchronized

    } //getObjects



    public void run() {

        boolean toCommit;

        while (this.toRun) {

            toCommit = false;

            sleep();

            try {

                System.out.println("Enviando paginas...");

                Object[] objs = null;

                objs = getObjects(vAddObjects);

                if (objs.length > 0) {

                    toCommit = true;

                    storage.addResourceArray(objs);

                } //if

                objs = getObjects(vRemoveObjects);

                if (objs.length > 0) {

                    toCommit = true;

                    storage.removeResourceArray(objs);

                } //if

                if (toCommit) {

                    storage.commit(null);

                } //if

            } //try

            catch (StorageException error) {

                lastError = error;

                error.printStackTrace();

            } //catch

            catch (CommunicationException error) {

                lastError = error;

                error.printStackTrace();

            } //catch

        } //while

    } //run

}

