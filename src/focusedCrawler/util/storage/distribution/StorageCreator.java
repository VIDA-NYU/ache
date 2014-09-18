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

import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.DefaultStorageFactory;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageFactory;
import focusedCrawler.util.storage.StorageFactoryException;
import focusedCrawler.util.storage.distribution.StorageRemoteAdapterReconnect;

public class StorageCreator extends DefaultStorageFactory {

    public StorageCreator() {
        super();
    } //StorageCreator

    public StorageCreator(ParameterFile config) {
        super(config);
    } //StorageCreator

    public StorageCreator(ParameterFile config, String newFactoryClassName) {
        super(config, newFactoryClassName);
    } //

    public Storage produce() throws StorageFactoryException {
        checkFactory();
        // Criando o storage capaz de reconectar
        StorageRemoteAdapterReconnect result = new StorageRemoteAdapterReconnect();
        // Setando a fabrica de storage

        result.setStorageFactory(getFactory());

        // Setando a quantidade de tentativas para acessar a funcao

        result.setTryNumber(new Integer(getConfig().getParam("STORAGE_TRY_NUMBER")).intValue());

        // Setando a espera apos uma falha de comunicacao

        result.setDelayAfterException(new Long(getConfig().getParam("STORAGE_DELAY_AFTER_EXCEPTION")).longValue());

        return result;
    }

      public static void main(String args[]) {

        try {

            ParameterFile config = new ParameterFile (args);

            StorageFactory run = new StorageCreator(config);

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

                else if (command.equals("remove")) {

                    storage.remove(null);

                } //else

                else if (command.equals("select")) {

                    System.out.println("Select " + args[2] + "=" + storage.select(args[2]));

                } //else

                else if (command.equals("selectArray")) {

                    String[] str = new String[args.length-2];

                    System.arraycopy(args,2,str,0,str.length);

                    Object[] obj = storage.selectArray(str);

                    System.out.println("SelectArray:");

                    for (int i = 0; i < obj.length; i++) {

                        System.out.println("Select "+str[i]+"="+obj[i]);

                    }
                } //else

            } //if

        }
        catch(Exception exc) {
            exc.printStackTrace();
        }
    }
}

