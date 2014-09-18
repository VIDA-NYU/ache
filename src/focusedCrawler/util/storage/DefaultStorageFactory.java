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
/*

 * StorageFactory.java

 *

 */



package focusedCrawler.util.storage;



import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.AbstractStorageFactory;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageFactory;
import focusedCrawler.util.storage.StorageFactoryException;







/**

 *

 * @author  Marcelo Fernandes

 * @version

 */

public class DefaultStorageFactory extends AbstractStorageFactory {



    private StorageFactory factory;

    private String factoryClassName;



    public DefaultStorageFactory() {

        super();

    } //StorageFactory



    public DefaultStorageFactory(ParameterFile config) {

        super(config);

    } //StorageFactory



    public DefaultStorageFactory(ParameterFile config, String newFactoryClassName) {

        super(config);

        factoryClassName = newFactoryClassName;

    } //StorageFactory



    public StorageFactory getFactory() {

        return factory;

    } //main



    public void checkFactory() throws StorageFactoryException {

        if (factory == null) {

            if ((getConfig() == null) && (factoryClassName == null)) {

                throw new StorageFactoryException("config not set!");

            } //if

            if ((getConfig() != null) && (factoryClassName == null)) {

                factoryClassName = getConfig().getParam("STORAGE_FACTORY_CLASSNAME");

            } //if

            try {

                factory = (StorageFactory) Class.forName(factoryClassName).newInstance();

                factory.setConfig(getConfig());

            } //try

            catch(ClassNotFoundException error) {

                throw new StorageFactoryException(error);

            } //catch

            catch(IllegalAccessException error) {

                throw new StorageFactoryException(error);

            } //catch

            catch(InstantiationException error) {

                throw new StorageFactoryException(error);

            } //catch

        } //if

    }



    public Storage produce() throws StorageFactoryException {

        checkFactory();

        return factory.produce();

    }



    /**

     * metodo main executavel

     * @param args os argumentos

     */

    public static void main(String args[]) throws focusedCrawler.util.distribution.CommunicationException,

                                                focusedCrawler.util.DetailException, StorageException, StorageFactoryException {

        ParameterFile config = new ParameterFile (args);

        StorageFactory run = new DefaultStorageFactory(config);

        Storage storage = run.produce();

        System.out.println ("storage: " + storage);

        if (args.length > 1) {

            String command = args[1];

            if (command.endsWith("commit")) {

                storage.commit(null);

            } //if

            else if (command.equals("rollback")) {

                storage.rollback(null);

            } //else

            else if (command.equals("finalize")) {

                storage.finalize(null);

            } //else

            else if (command.equals("ping")) {

                storage.ping(null);

            } //else

            else if (command.equals("select")) {

                System.out.println("Select " + args[2] + "=" + storage.select(args[2]));

            } //else

        } //if

    }

}

