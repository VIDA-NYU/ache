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



import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.SelfLoggable;
import focusedCrawler.util.distribution.CommunicationException;





/**

 * Implementacao basica de um Storage. Para reduzir a codificacao na criacao de

 * um Storage pode-se estender esta classe de forma a re-implementar apenas os

 * metodos realmente necessarios.

 */

public class StorageDefault extends SelfLoggable implements Storage {



    public StorageDefault() {}



    public synchronized Object insert(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method insert() not yet implemented.");

    }

    public synchronized Object[] insertArray(Object[] objs) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method insert() not yet implemented.");

    }

    public synchronized Object select(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method select() not yet implemented.");

    }

    public synchronized Object[] selectArray(Object[] objs) throws StorageException,DataNotFoundException,CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method select() not yet implemented.");

    }

    public synchronized java.util.Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException,CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method selectEnumeration() not yet implemented.");

    }

    public synchronized Object update(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method update() not yet implemented.");

    }

    public synchronized Object[] updateArray(Object[] objs) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method update() not yet implemented.");

    }

    public synchronized Object remove(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");

    }

    public synchronized Object[] removeArray(Object[] objs) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");

    }

    public synchronized Object addResource(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method addResource() not yet implemented.");

    }

    public synchronized Object[] addResourceArray(Object[] objs) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method addResource() not yet implemented.");

    }

    public synchronized Object removeResource(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method removeResource() not yet implemented.");

    }

    public synchronized Object[] removeResourceArray(Object[] objs) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method removeResource() not yet implemented.");

    }

    public synchronized Object commit(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method commit() not yet implemented.");

    }

    public synchronized Object rollback(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method rollback() not yet implemented.");

    }

    public synchronized Object finalize(Object obj) throws StorageException, CommunicationException {

        /**@todo: Implement this util.storage.Storage method*/

        throw new java.lang.UnsupportedOperationException("Method finalize() not yet implemented.");

    }

    public Object ping(Object obj) throws StorageException, CommunicationException {

        return new Long(System.currentTimeMillis());

    }

}