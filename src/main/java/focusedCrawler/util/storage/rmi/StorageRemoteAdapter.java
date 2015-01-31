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
package focusedCrawler.util.storage.rmi;


import java.rmi.RemoteException;

import java.util.Enumeration;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageException;









public class StorageRemoteAdapter implements Storage {



    private StorageRemote storage;



    public StorageRemoteAdapter(StorageRemote storage) {

        setStorage(storage);

    }



    public void setStorage(StorageRemote newStorage) {

        storage = newStorage;

    }



    public StorageRemote getStorage() {

        return storage;

    }



    public Object insert(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.insert(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object[] insertArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return storage.insertArray(objs);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object select(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        try {

            return storage.select(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object[] selectArray(Object[] objs) throws StorageException,DataNotFoundException,CommunicationException {

        try {

            return storage.selectArray(objs);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        try {

            return storage.selectEnumeration(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object update(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.update(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object[] updateArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return storage.updateArray(objs);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object remove(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.remove(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object[] removeArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return storage.removeArray(objs);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object addResource(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.addResource(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object[] addResourceArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return storage.addResourceArray(objs);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object removeResource(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.removeResource(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object[] removeResourceArray(Object[] objs) throws StorageException,CommunicationException {

        try {

            return storage.removeResourceArray(objs);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object commit(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.commit(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object rollback(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.rollback(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object finalize(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.finalize(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }



    public Object ping(Object obj) throws StorageException,CommunicationException {

        try {

            return storage.ping(obj);

        }

        catch( RemoteException exc ) {

            exc.printStackTrace();

            throw new CommunicationException(exc.getMessage(),exc.detail );

        }

    }

}