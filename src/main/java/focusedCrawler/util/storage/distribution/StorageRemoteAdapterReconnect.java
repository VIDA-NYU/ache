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
package focusedCrawler.util.storage.distribution;




import java.rmi.RemoteException;

import java.util.Enumeration;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.StorageFactory;
import focusedCrawler.util.storage.StorageFactoryException;

public class StorageRemoteAdapterReconnect extends StorageDefault {



    private int tryNumber = 1;

    private long delayAfterException = 2000;

    private StorageFactory factory;

    private Storage storage;



    public StorageRemoteAdapterReconnect() {

    } //StorageRemoteAdapterReconnect



    /**

     * Retorna o numero de tentativas de reconexao

     */

    public int getTryNumber() {

        return this.tryNumber;

    } //getTryNumber



    /**

     * Altera o numero de tentativas de reconexao

     */

    public void setTryNumber(int _tryNumber) {

        this.tryNumber = _tryNumber;

    } //setTryNumber



    /**

     * Retorna o tempo de sleep do processo apos um erro de comunicacao

     */

    public long getDelayAfterException() {

        return this.delayAfterException;

    } //getDelayAfterException



    /**

     * Altera o tempo de sleep do processo apos um erro de comunicacao

     */

    public void setDelayAfterException(long _delayAfterException) {

        this.delayAfterException = _delayAfterException;

    } //setDelayAfterException



    /**

     * Retorna a fabrica de storage

     */

    public StorageFactory getStorageFactory() {

        return this.factory;

    } //getStorageFactory



    /**

     * Altera a fabrica de storage

     */

    public void setStorageFactory(StorageFactory _factory) {

        this.factory = _factory;

    } //setStorageFactory



    /**

     * Metodo auxiliar para dormir a thread

     */

    private void sleep() {

        try {

            System.out.println("Dormindo "+ getDelayAfterException() + " mls");

            Thread.sleep(getDelayAfterException());

        } //try

        catch (InterruptedException error) {

            error.printStackTrace();

        } //catch

    } //sleep



    /**

     * Cria o storage

     */

    private synchronized void testStorage() throws CommunicationException {

        if (storage == null) {

            createStorage();

        } //synchronized

    } //testStorage



    private synchronized void createStorage() throws CommunicationException {

        try {

            this.storage = factory.produce();

        } //try

        catch(StorageFactoryException error) {

            throw new CommunicationException("Nao conseguiu criar objeto: " + error.getMessage());

        } //catch

    } //createStorage



    public Object insert(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.insert(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.insert(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object[] insertArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.insertArray(objs);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.insertArray(objs);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object select(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        try {

            testStorage();

            return storage.select(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.select(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object[] selectArray(Object[] objs) throws StorageException,DataNotFoundException,CommunicationException {

        try {

            testStorage();

            return storage.selectArray(objs);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.selectArray(objs);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        try {

            testStorage();

            return storage.selectEnumeration(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.selectEnumeration(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object update(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.update(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.update(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object[] updateArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.updateArray(objs);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    testStorage();

                    return storage.updateArray(objs);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object remove(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.remove(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.remove(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object[] removeArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.removeArray(objs);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.removeArray(objs);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object addResource(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.addResource(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.addResource(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object[] addResourceArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.addResourceArray(objs);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.addResourceArray(objs);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object removeResource(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.removeResource(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.removeResource(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object[] removeResourceArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.removeResourceArray(objs);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.removeResourceArray(objs);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object commit(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.commit(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.commit(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object rollback(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.rollback(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.rollback(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object finalize(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.finalize(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.finalize(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }



    public Object ping(Object obj) throws StorageException,CommunicationException {

        try {

            testStorage();

            return storage.ping(obj);

        } //try

        catch (CommunicationException error) {

            writeLog("Erro de comunicacao: " + error.getMessage());

            for (int counter=0; counter < getTryNumber(); counter++) {

                try {

                    sleep();

                    createStorage();

                    return storage.ping(obj);

                } //try

                catch(CommunicationException error1) {

                    writeLog("Erro de comunicacao: " + error.getMessage());

                } //catch

            } //for

            throw error;

        } //catch

    }

}