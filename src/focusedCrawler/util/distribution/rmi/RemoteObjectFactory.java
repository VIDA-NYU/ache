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
package focusedCrawler.util.distribution.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import focusedCrawler.util.cache.CacheKey;
import focusedCrawler.util.cache.FactoryException;
import focusedCrawler.util.cache.ObjectFactory;


public class RemoteObjectFactory implements ObjectFactory {



    private long timeout = 5 * 1000; // 5seg;

    private int counterLookup = 0;



    public RemoteObjectFactory() {

    } //RemoteObjectFactory



    public RemoteObjectFactory(long timeout) {

        this.timeout = timeout;

    } //RemoteObjectFactory



    public synchronized Object produce(CacheKey key) throws FactoryException {

        if (key instanceof RemoteCacheKey) {

            RemoteCacheKey remoteKey = (RemoteCacheKey) key;

            try {

                Registry registry = LocateRegistry.getRegistry(remoteKey.getHostname (), remoteKey.getPort ());



                LookupThread lookup = new LookupThread (registry,remoteKey.getObjectname ());

                lookup.start();

                long INICIO = System.currentTimeMillis();



                while( !lookup.getFinished() && (System.currentTimeMillis()-INICIO) < timeout ) {

                    System.out.println("Esperando(" + counterLookup + "): "+ key.toString() + " " + System.currentTimeMillis());

                    try {

                        Thread.sleep(500);

                    } //try

                    catch(InterruptedException exc) {

                        exc.printStackTrace();

                    } //catch

                } //while

                if (lookup.getFound()) {

                    counterLookup++;

                    return lookup.getRemoteObject();

                } //if

                else {

                    throw new FactoryException ("Não conseguiu acessar o objeto: " + key.hashKey(),lookup.getError());

                } //else

            } //try

            catch (RemoteException erro) {

                erro.printStackTrace ();

                throw new FactoryException ("Não conseguiu acessar o objeto: " + erro.getMessage(),erro);

            } //catch

        } //if

        else {

            throw new FactoryException ("Cache inválida: " + key.toString());

        } //else

    }



    public Object[] produce(CacheKey[] keys) throws FactoryException {

        Object[] result = new Object[keys.length];

        for (int counter=0; counter < keys.length; counter++) {

            result[counter] = produce(keys[counter]);

        } //for

        return result;

    } //produce



}