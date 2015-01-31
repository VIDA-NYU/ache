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



import java.rmi.Remote;

import java.rmi.RemoteException;

import java.util.Enumeration;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.StorageException;








public interface StorageRemote extends Remote {



    /**

     * Insercao de elementos.

     */

    Object insert(Object obj) throws StorageException,CommunicationException,RemoteException;

    Object[] insertArray(Object[] objs) throws StorageException,CommunicationException,RemoteException;



    /**

     * Selecao de elementos.

     */

    Object select(Object obj) throws StorageException,DataNotFoundException,CommunicationException,RemoteException;

    Object[] selectArray(Object[] objs) throws StorageException,DataNotFoundException,CommunicationException,RemoteException;

    Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException,CommunicationException,RemoteException;



    /**

     * Atualizacao de elementos.

     */

    Object update(Object obj) throws StorageException,CommunicationException,RemoteException;

    Object[] updateArray(Object[] objs) throws StorageException,CommunicationException,RemoteException;



    /**

     * Remocao de elementos.

     */

    Object remove(Object obj) throws StorageException,CommunicationException,RemoteException;

    Object[] removeArray(Object[] objs) throws StorageException,CommunicationException,RemoteException;



    /**

     * Adiciona os recursos.

     */

    Object addResource(Object obj) throws StorageException,CommunicationException,RemoteException;

    Object[] addResourceArray(Object[] objs) throws StorageException,CommunicationException,RemoteException;



    /**

     * Remove recursos.

     */

    Object removeResource(Object obj) throws StorageException,CommunicationException,RemoteException;

    Object[] removeResourceArray(Object[] objs) throws StorageException,CommunicationException,RemoteException;



    /**

     * Realiza o commit de um estado.

     */

    Object commit(Object obj) throws StorageException,CommunicationException,RemoteException;



    /**

     * Realiza o rollback em caso de problemas.

     */

    Object rollback(Object obj) throws StorageException,CommunicationException,RemoteException;



    /**

     * Finaliza um Storage.

     */

    Object finalize(Object obj) throws StorageException,CommunicationException,RemoteException;



    /**

     * Utilizado para acessar o objeto

     */

    Object ping(Object obj) throws StorageException,CommunicationException,RemoteException;

}