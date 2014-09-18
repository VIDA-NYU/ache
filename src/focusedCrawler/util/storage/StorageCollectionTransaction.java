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

import focusedCrawler.util.Log;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageCollection;
import focusedCrawler.util.storage.StorageException;

public class StorageCollectionTransaction extends StorageCollection {
    private int transactionCounter = 0;
    private int transactionSize = 25;
    private long OCIOSO;
    private long TEMPO_TOTAL;
    private DataChecker checker;
    public StorageCollectionTransaction(int transactionSize) {
        super();
        this.transactionSize = transactionSize;
    } //StorageCollectionTransaction
    public DataChecker getChecker() {
        return checker;
    } //getChecker
    public void setChecker(DataChecker checker) {
        this.checker = checker;
    } //setChecker
    public Object addResource(Object obj) throws StorageException, CommunicationException {
        if (obj instanceof Storage) {
            return super.addResource(obj);
        } //if
        else {
            throw new StorageException("Objeto nao eh storage: " + obj.getClass());
        } //else
    } //addStorage


    public synchronized Object insert(Object obj) throws StorageException, CommunicationException{

        if (checker != null) {

            if (!getChecker().verify(obj, getNumber())) {

                throw new StorageException("Objeto nao verificado: " + getChecker().getReason());

            } //if

        } //if

        try {

            super.insert(obj);

        } //try

        catch (StorageException error) {

            treatError("insert",error);

            throw new StorageException(error);

        } //catch

        catch (CommunicationException error) {

            treatError("insert",error);

            throw new StorageException(error);

        } //catch

        catch (Exception error) {

            treatError("insert",error);

            error.printStackTrace();

            throw new StorageException(error);

        } //catch

        return null;

    } //insert


    public synchronized Object update(Object obj) throws StorageException, CommunicationException{

        if (checker != null) {

            if (!getChecker().verify(obj, getNumber())) {

                throw new StorageException("Objeto nao verificado: " + getChecker().getReason());

            } //if

        } //if

        try {

            super.update(obj);

        } //try

        catch (StorageException error) {

            treatError("update",error);

            throw new StorageException(error);

        } //catch

        catch (CommunicationException error) {

            treatError("update",error);

            throw new StorageException(error);

        } //catch

        catch (Exception error) {

            treatError("update",error);

            error.printStackTrace();

            throw new StorageException(error);

        } //catch

        return null;

    } //update


    public synchronized Object remove(Object obj) throws StorageException, CommunicationException {

        if (checker != null) {

            if (!getChecker().verify(obj, getNumber())) {

                throw new StorageException("Objeto nao verificado: " + getChecker().getReason());

            } //if

        } //if

        try {

            super.remove(obj);

        } //try

        catch (StorageException error) {

            treatError("remove",error);

            throw new StorageException(error);

        } //catch

        catch (CommunicationException error) {

            treatError("remove",error);

            throw new StorageException(error);

        } //catch

        catch (Exception error) {

            treatError("remove",error);

            error.printStackTrace();

            throw new StorageException(error);

        } //catch

        return null;

    } //remove


    public synchronized Object finalize(Object obj) throws StorageException, CommunicationException {

        return commit(obj);

    } //finalize


    /**

     * Fecha a transacao

     */

    public synchronized Object commit(Object obj) throws StorageException, CommunicationException {

        try {

            super.commit(obj);

            checker.remove(this.getNumber());

        } //try

        catch (CommunicationException error) {

            treatError("commit",error);

        } //catch

        catch (StorageException error) {

            treatError("commit",error);

        } //catch

        transactionCounter++;

        Log.log("Transaction", Integer.toString(getNumber()), "closed transaction " + transactionCounter);

        return null;

    } //commit


    private void treatError(String reason, Exception exc) throws StorageException {

        for (int counter =0; counter < size(); counter++) {

            try {

                getStorage(counter).rollback(null);

            } //try

            catch (CommunicationException error) {

                error.printStackTrace();

            } //catch

            catch (StorageException error) {

                error.printStackTrace();

            } //catch

        } //for

        checker.remove(this.getNumber());

        setItemCounter(0);

        exc.printStackTrace();

        Log.log("Transaction", Integer.toString(getNumber()), "rollback: treat exception " + reason + "("+exc.getMessage()+") on transaction " + transactionCounter);

    } //treatError


    // Testando se eh para realizar commit

    // O Rollback eh AUTOMATICO

    public void incrementCounter() throws StorageException, CommunicationException {

        super.incrementCounter();

        if (getItemCounter() >= this.transactionSize) {

            try {

                this.commit(null);

            } //try

            finally {

                setItemCounter(0);

            } //finally

        } //if

    }

}

