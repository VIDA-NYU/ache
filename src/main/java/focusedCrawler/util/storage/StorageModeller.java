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
import focusedCrawler.util.distribution.CommunicationException;



public class StorageModeller extends StorageDefault {



    private Storage modeller;

    private Storage storage;



    public StorageModeller() {

    }

    public StorageModeller(Storage modeller, Storage storage) {

        setModeller(modeller);

        setStorager(storage);

    }

    public Storage getModeller() {

        return this.modeller;

    }

    public void setModeller(Storage newModeller) {

        this.modeller = newModeller;

    }

    public Storage getStorager() {

        return this.storage;

    }

    public void setStorager(Storage newStorage) {

        this.storage = newStorage;

    }

    public Object[] insertArray(Object[] objs) throws StorageException,CommunicationException {

        return modeller.insertArray(objs);

    }

    public Object[] updateArray(Object[] objs) throws StorageException,CommunicationException {

        return modeller.updateArray(objs);

    }

    public Object[] removeArray(Object[] objs) throws StorageException,CommunicationException {

        return modeller.removeArray(objs);

    }

    public Object commit(Object obj) throws StorageException,CommunicationException {

        return storage.commit(modeller.commit(obj));

    }



}